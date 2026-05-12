package com.htmake.reader.project

import java.io.File
import java.util.zip.ZipFile

object ZipRequirementExpander {
    private val allowedExt = setOf("doc", "docx", "pdf", "txt", "xlsx")

    fun expand(zipFile: File, destDir: File): List<File> {
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        val files = arrayListOf<File>()
        ZipFile(zipFile).use { z ->
            z.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) return@forEach
                val name = entry.name.replace("\\", "/")
                val ext = name.substringAfterLast('.', "").lowercase()
                if (!allowedExt.contains(ext)) return@forEach
                val safeName = File(name).name
                val out = File(destDir, safeName)
                z.getInputStream(entry).use { input ->
                    out.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                files.add(out)
            }
        }
        return files
    }
}

