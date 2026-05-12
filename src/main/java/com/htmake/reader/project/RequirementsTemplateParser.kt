package com.htmake.reader.project

data class RequirementsParseResult(
    val globalGuidelines: String = "",
    val requiredFields: List<String> = listOf(),
    val sections: List<ProjectTemplateSection> = listOf()
)

object RequirementsTemplateParser {
    private val numberedHeadingPattern = Regex(
        "^((\\d+|[一二三四五六七八九十]+)[、.]|（(\\d+|[一二三四五六七八九十]+)）)\\s*([^\\s].{0,40})$"
    )

    fun parse(docType: String, rawText: String): List<ProjectTemplateSection> {
        return parseAll(docType, rawText).sections
    }

    fun parseAll(docType: String, rawText: String): RequirementsParseResult {
        val lines = rawText
            .split("\n", "\r\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val globalGuidelines = extractGlobalGuidelines(lines)
        val requiredFields = extractRequiredFields(lines)
        val sections = extractSections(docType, lines)

        return RequirementsParseResult(
            globalGuidelines = globalGuidelines,
            requiredFields = requiredFields,
            sections = sections
        )
    }

    private fun extractGlobalGuidelines(lines: List<String>): String {
        val keys = listOf(
            "版面要求",
            "标题格式",
            "正文",
            "图表",
            "目录页",
            "A4",
            "装订",
            "双面",
            "行距",
            "字体",
            "字号",
            "相似度",
            "学术不端"
        )
        val picked = lines.filter { line ->
            keys.any { k -> line.contains(k) }
        }.distinct()
        return picked.joinToString("\n")
    }

    private fun extractRequiredFields(lines: List<String>): List<String> {
        val fieldKeys = listOf(
            "课题名称",
            "课题编号",
            "课题主持人",
            "主持人",
            "负责人",
            "课题组成员",
            "成员",
            "工作单位",
            "学校",
            "学区",
            "学科",
            "职称",
            "专业",
            "研究周期",
            "起止时间",
            "联系电话",
            "手机",
            "电子邮箱",
            "微信",
            "申报日期"
        )

        val candidates = lines.filter { line ->
            val compact = line.replace(Regex("\\s+"), "")
            line.length in 2..20 &&
                fieldKeys.any { k -> compact.contains(k) } &&
                !line.contains("：") &&
                !line.contains(":") &&
                !line.contains("（") &&
                !line.contains(")") &&
                !line.contains("。") &&
                !compact.contains("领导") &&
                !compact.contains("意见") &&
                !compact.startsWith("XX") &&
                !compact.startsWith("xxx", true)
        }.map { it.replace(Regex("\\s+"), "") }

        return candidates.distinct()
    }

    private fun extractSections(docType: String, lines: List<String>): List<ProjectTemplateSection> {
        val sections = arrayListOf<ProjectTemplateSection>()
        val sectionTitles = hashSetOf<String>()

        fun addSection(title: String, guidelines: String = "") {
            val t = title.trim()
            if (t.isEmpty()) return
            if (t.length > 60) return
            if (sectionTitles.contains(t)) return
            sectionTitles.add(t)
            sections.add(ProjectTemplateSection(title = t, guidelines = guidelines))
        }

        for (i in lines.indices) {
            val line = lines[i]
            val m = numberedHeadingPattern.find(line)
            if (m != null) {
                val title = m.groupValues.getOrNull(5)?.trim().orEmpty()
                if (title.isNotEmpty()) {
                    addSection(title)
                }
                continue
            }

            val next = lines.getOrNull(i + 1) ?: ""
            if (next.startsWith("（") && (next.contains("字") || next.contains("篇幅") || next.contains("格式") || next.contains("要求"))) {
                addSection(line, guidelines = next)
            }

            if (line.contains("报告") && line.length <= 30 && !line.contains("检测")) {
                addSection(line)
            }
        }

        if (sections.isEmpty()) {
            return DefaultProjectTemplates.forDocType(docType)
        }

        return sections
    }
}
