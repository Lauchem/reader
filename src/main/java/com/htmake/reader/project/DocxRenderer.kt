package com.htmake.reader.project

import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

object DocxRenderer {
    fun render(
        request: ProjectGenerateRequest,
        template: ProjectTemplate,
        sectionTexts: List<Pair<ProjectTemplateSection, String>>,
        outputFile: File
    ) {
        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }
        XWPFDocument().use { doc ->
            val titleP = doc.createParagraph()
            titleP.alignment = ParagraphAlignment.CENTER
            val titleRun = titleP.createRun()
            titleRun.isBold = true
            titleRun.fontFamily = "黑体"
            titleRun.fontSize = 22
            titleRun.setText("${template.docType}：${request.topicTitle}")

            val infoP = doc.createParagraph()
            infoP.alignment = ParagraphAlignment.CENTER
            val infoRun = infoP.createRun()
            infoRun.fontFamily = "仿宋"
            infoRun.fontSize = 12
            val host = request.host
            val hostLine = listOf(host.unit, host.title, host.name).filter { it.isNotEmpty() }.joinToString("")
            if (hostLine.isNotEmpty()) {
                infoRun.setText("主持人：$hostLine")
            }

            val globalGuidelines = template.globalGuidelines ?: ""
            if (globalGuidelines.isNotEmpty()) {
                val h = doc.createParagraph()
                h.alignment = ParagraphAlignment.LEFT
                val hr = h.createRun()
                hr.isBold = true
                hr.fontFamily = "黑体"
                hr.fontSize = 14
                hr.setText("格式与提交要求")
                globalGuidelines.split("\n").filter { it.isNotEmpty() }.forEach { line ->
                    val p = doc.createParagraph()
                    p.alignment = ParagraphAlignment.BOTH
                    val r = p.createRun()
                    r.fontFamily = "仿宋"
                    r.fontSize = 12
                    r.setText(line)
                }
            }

            val requiredFields = template.requiredFields ?: listOf()
            if (requiredFields.isNotEmpty()) {
                val h = doc.createParagraph()
                h.alignment = ParagraphAlignment.LEFT
                val hr = h.createRun()
                hr.isBold = true
                hr.fontFamily = "黑体"
                hr.fontSize = 14
                hr.setText("课题基本信息")

                val table = doc.createTable(1, 2)
                fillRow(table.getRow(0).getCell(0).paragraphs[0], "字段", true)
                fillRow(table.getRow(0).getCell(1).paragraphs[0], "内容", true)
                requiredFields.forEach { f ->
                    val row = table.createRow()
                    fillRow(row.getCell(0).paragraphs[0], f, false)
                    fillRow(row.getCell(1).paragraphs[0], fieldValue(request, f), false)
                }
            }

            sectionTexts.forEach { (section, text) ->
                val h = doc.createParagraph()
                h.alignment = ParagraphAlignment.LEFT
                val hr = h.createRun()
                hr.isBold = true
                hr.fontFamily = "黑体"
                hr.fontSize = 14
                hr.setText(section.title)

                if (!section.guidelines.isNullOrEmpty()) {
                    val g = doc.createParagraph()
                    g.alignment = ParagraphAlignment.BOTH
                    val gr = g.createRun()
                    gr.fontFamily = "仿宋"
                    gr.fontSize = 12
                    gr.setText(section.guidelines)
                }

                text.split("\n").forEach { line ->
                    val p = doc.createParagraph()
                    p.alignment = ParagraphAlignment.BOTH
                    val r = p.createRun()
                    r.fontFamily = "仿宋"
                    r.fontSize = 12
                    r.setText(line)
                }
            }

            FileOutputStream(outputFile).use { out ->
                doc.write(out)
            }
        }
    }

    private fun fillRow(paragraph: org.apache.poi.xwpf.usermodel.XWPFParagraph, text: String, header: Boolean) {
        while (paragraph.runs.isNotEmpty()) {
            paragraph.removeRun(0)
        }
        val run = paragraph.createRun()
        run.fontFamily = if (header) "黑体" else "仿宋"
        run.isBold = header
        run.fontSize = 12
        run.setText(text)
    }

    private fun fieldValue(request: ProjectGenerateRequest, fieldName: String): String {
        val f = fieldName.replace(" ", "")
        return when {
            f.contains("课题名称") -> request.topicTitle
            f.contains("课题主持人") || f == "主持人" || f.contains("负责人") -> request.host.name
            f.contains("工作单位") || f.contains("单位") || f.contains("学校") -> request.host.unit
            f.contains("职称") || f.contains("职务") -> request.host.title
            else -> ""
        }
    }
}
