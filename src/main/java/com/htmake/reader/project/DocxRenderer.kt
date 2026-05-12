package com.htmake.reader.project

import org.apache.poi.xwpf.usermodel.ParagraphAlignment
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
            titleRun.fontSize = 20
            titleRun.setText("${template.docType}：${request.topicTitle}")

            val infoP = doc.createParagraph()
            infoP.alignment = ParagraphAlignment.CENTER
            val infoRun = infoP.createRun()
            infoRun.fontSize = 12
            val host = request.host
            val hostLine = listOf(host.unit, host.title, host.name).filter { it.isNotEmpty() }.joinToString("")
            if (hostLine.isNotEmpty()) {
                infoRun.setText("主持人：$hostLine")
            }

            sectionTexts.forEach { (section, text) ->
                val h = doc.createParagraph()
                h.alignment = ParagraphAlignment.LEFT
                val hr = h.createRun()
                hr.isBold = true
                hr.fontSize = 16
                hr.setText(section.title)

                text.split("\n").forEach { line ->
                    val p = doc.createParagraph()
                    p.alignment = ParagraphAlignment.BOTH
                    val r = p.createRun()
                    r.fontSize = 12
                    r.setText(line)
                }
            }

            FileOutputStream(outputFile).use { out ->
                doc.write(out)
            }
        }
    }
}

