package com.htmake.reader.api.controller

import com.htmake.reader.api.ReturnData
import com.htmake.reader.project.DefaultProjectTemplates
import com.htmake.reader.project.DocxRenderer
import com.htmake.reader.project.MockTextGenerator
import com.htmake.reader.project.ProjectGenerateRequest
import com.htmake.reader.project.ProjectTemplate
import com.htmake.reader.project.ProjectTemplateSection
import com.htmake.reader.project.RequirementsTemplateParser
import com.htmake.reader.project.RequirementsTextExtractor
import com.htmake.reader.project.ZipRequirementExpander
import com.htmake.reader.utils.asJsonArray
import com.htmake.reader.utils.convert
import com.htmake.reader.utils.getWorkDir
import com.htmake.reader.utils.toDataClass
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class ProjectController(override val coroutineContext: CoroutineContext) : BaseController(coroutineContext) {
    private val generator = MockTextGenerator()

    suspend fun importTemplate(context: RoutingContext): ReturnData {
        val returnData = ReturnData()
        if (!checkAuth(context)) {
            return returnData.setData("NEED_LOGIN").setErrorMsg("请登录后使用")
        }
        if (context.fileUploads() == null || context.fileUploads().isEmpty()) {
            return returnData.setErrorMsg("请上传要求文件")
        }

        val docType = (context.request().getParam("docType") ?: "立项申报书").trim().ifEmpty { "立项申报书" }
        val templateName = (context.request().getParam("name") ?: "导入模板").trim().ifEmpty { "导入模板" }
        val templateId = UUID.randomUUID().toString().replace("-", "")
        val userNameSpace = getUserNameSpace(context)

        val requirementsDir = File(getWorkDir("storage", "data", userNameSpace, "projectRequirements", templateId))
        if (!requirementsDir.exists()) {
            requirementsDir.mkdirs()
        }

        val rawText = StringBuilder()
        context.fileUploads().forEach { upload ->
            val tmpFile = File(upload.uploadedFileName())
            if (tmpFile.exists()) {
                val safeName = File(upload.fileName()).name
                val dest = File(requirementsDir, safeName)
                if (dest.exists()) {
                    dest.delete()
                }
                tmpFile.copyRecursively(dest, true)
                if (safeName.lowercase().endsWith(".zip")) {
                    val expandedDir = File(requirementsDir, "expanded")
                    val files = ZipRequirementExpander.expand(dest, expandedDir)
                    files.forEach { f ->
                        rawText.append("\n").append(RequirementsTextExtractor.extract(f))
                    }
                } else {
                    rawText.append("\n").append(RequirementsTextExtractor.extract(dest))
                }
                tmpFile.deleteRecursively()
            }
        }

        val parsed = RequirementsTemplateParser.parseAll(docType, rawText.toString())
        val template = ProjectTemplate(
            id = templateId,
            name = templateName,
            docType = docType,
            globalGuidelines = parsed.globalGuidelines,
            requiredFields = parsed.requiredFields,
            sections = parsed.sections,
            createdAt = System.currentTimeMillis()
        )

        val templates = loadTemplates(userNameSpace).toMutableList()
        templates.add(0, template)
        saveUserStorage(userNameSpace, "projectTemplates", templates)

        return returnData.setData(template)
    }

    suspend fun listTemplates(context: RoutingContext): ReturnData {
        val returnData = ReturnData()
        if (!checkAuth(context)) {
            return returnData.setData("NEED_LOGIN").setErrorMsg("请登录后使用")
        }
        val userNameSpace = getUserNameSpace(context)
        val templates = loadTemplates(userNameSpace)
        return returnData.setData(templates)
    }

    suspend fun generateDoc(context: RoutingContext): ReturnData {
        val returnData = ReturnData()
        if (!checkAuth(context)) {
            return returnData.setData("NEED_LOGIN").setErrorMsg("请登录后使用")
        }
        val body = context.bodyAsJson
        if (body == null) {
            return returnData.setErrorMsg("参数错误")
        }
        val request = body.map.toDataClass<ProjectGenerateRequest>()
        if (request.topicTitle.isEmpty()) {
            return returnData.setErrorMsg("请输入课题题目")
        }

        val docType = request.docType.trim().ifEmpty { "立项申报书" }
        val userNameSpace = getUserNameSpace(context)
        val template = resolveTemplate(userNameSpace, docType, request.templateId)

        val templateSections = template.sections ?: DefaultProjectTemplates.forDocType(docType)
        val sectionTexts = templateSections.map { section ->
            section to generator.generateSectionText(docType, request.topicTitle, request.host, section)
        }

        val outputDir = File(getWorkDir("storage", "assets", userNameSpace, "projects"))
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val safeTitle = sanitizeFileName(request.topicTitle, 40)
        val safeDocType = sanitizeFileName(docType, 20)
        val outFile = File(outputDir, "${ts}_${safeDocType}_${safeTitle}.docx")

        DocxRenderer.render(
            request = request,
            template = template,
            sectionTexts = sectionTexts,
            outputFile = outFile
        )

        val url = "/assets/$userNameSpace/projects/${outFile.name}"
        return returnData.setData(mapOf("url" to url, "fileName" to outFile.name, "templateId" to template.id))
    }

    private fun resolveTemplate(userNameSpace: String, docType: String, templateId: String?): ProjectTemplate {
        if (!templateId.isNullOrEmpty()) {
            val t = loadTemplates(userNameSpace).firstOrNull { it.id == templateId }
            if (t != null) {
                return t
            }
        }
        return ProjectTemplate(
            id = "default_$docType",
            name = "默认模板",
            docType = docType,
            globalGuidelines = "",
            requiredFields = listOf(),
            sections = DefaultProjectTemplates.forDocType(docType),
            createdAt = 0L
        )
    }

    private fun loadTemplates(userNameSpace: String): List<ProjectTemplate> {
        val json = asJsonArray(getUserStorage(userNameSpace, "projectTemplates")) ?: return listOf()
        val list = json.getList() ?: return listOf()
        return runCatching { (list as List<Any>).convert<List<ProjectTemplate>>() }.getOrDefault(listOf())
    }

    private fun sanitizeFileName(value: String, maxLen: Int): String {
        val cleaned = value.trim().ifEmpty { "untitled" }.map { ch ->
            when {
                ch.isLetterOrDigit() -> ch
                ch in listOf(' ', '-', '_') -> '_'
                ch.code in 0x4E00..0x9FFF -> ch
                else -> '_'
            }
        }.joinToString("")
        val compact = cleaned.replace(Regex("_+"), "_").trim('_')
        return if (compact.length > maxLen) compact.substring(0, maxLen) else compact
    }
}
