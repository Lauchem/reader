package com.htmake.reader.project

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

object RequirementsTextExtractor {
    fun extract(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".docx") -> extractDocx(file)
            name.endsWith(".pdf") -> extractPdf(file)
            else -> extractPlain(file)
        }
    }

    private fun extractDocx(file: File): String {
        return FileInputStream(file).use { fis ->
            XWPFDocument(fis).use { doc ->
                doc.paragraphs.joinToString("\n") { it.text ?: "" }
            }
        }
    }

    private fun extractPdf(file: File): String {
        return PDDocument.load(file).use { doc ->
            PDFTextStripper().getText(doc) ?: ""
        }
    }

    private fun extractPlain(file: File): String {
        return runCatching { file.readText(Charsets.UTF_8) }
            .getOrElse { runCatching { file.readText() }.getOrDefault("") }
    }
}

