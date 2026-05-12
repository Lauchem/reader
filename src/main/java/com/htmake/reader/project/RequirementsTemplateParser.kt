package com.htmake.reader.project

object RequirementsTemplateParser {
    private val headingPattern = Regex(
        "^((\\d+|[一二三四五六七八九十]+)[、.]|（(\\d+|[一二三四五六七八九十]+)）)\\s*([^\\s].{0,40})$"
    )

    fun parse(docType: String, rawText: String): List<ProjectTemplateSection> {
        val lines = rawText
            .split("\n", "\r\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val sectionTitles = lines.mapNotNull { line ->
            val m = headingPattern.find(line) ?: return@mapNotNull null
            val title = m.groupValues.getOrNull(5)?.trim() ?: ""
            if (title.isEmpty()) null else title
        }.distinct()

        if (sectionTitles.isEmpty()) {
            return DefaultProjectTemplates.forDocType(docType)
        }

        return sectionTitles.map { ProjectTemplateSection(title = it) }
    }
}

