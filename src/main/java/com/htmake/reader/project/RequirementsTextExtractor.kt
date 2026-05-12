package com.htmake.reader.project

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

object RequirementsTextExtractor {
    fun extract(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".docx") -> extractDocx(file)
            name.endsWith(".doc") -> extractDoc(file)
            name.endsWith(".pdf") -> extractPdf(file)
            name.endsWith(".xlsx") -> extractXlsx(file)
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

    private fun extractDoc(file: File): String {
        return FileInputStream(file).use { fis ->
            HWPFDocument(fis).use { doc ->
                WordExtractor(doc).use { extractor ->
                    extractor.paragraphText?.joinToString("\n") ?: ""
                }
            }
        }
    }

    private fun extractXlsx(file: File): String {
        return FileInputStream(file).use { fis ->
            XSSFWorkbook(fis).use { wb ->
                val sb = StringBuilder()
                for (i in 0 until wb.numberOfSheets) {
                    val sheet = wb.getSheetAt(i)
                    sheet.forEach { row ->
                        row.forEach { cell ->
                            val v = when (cell.cellType) {
                                CellType.STRING -> cell.stringCellValue
                                CellType.NUMERIC -> cell.numericCellValue.toString()
                                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                CellType.FORMULA -> runCatching { cell.stringCellValue }.getOrElse {
                                    runCatching { cell.numericCellValue.toString() }.getOrDefault("")
                                }
                                else -> ""
                            }
                            if (v.isNotEmpty()) {
                                sb.append(v).append('\n')
                            }
                        }
                    }
                }
                sb.toString()
            }
        }
    }

    private fun extractPlain(file: File): String {
        return runCatching { file.readText(Charsets.UTF_8) }
            .getOrElse { runCatching { file.readText() }.getOrDefault("") }
    }
}
