package com.htmake.reader.project

interface TextGenerator {
    fun generateSectionText(
        docType: String,
        topicTitle: String,
        host: ProjectHost,
        section: ProjectTemplateSection
    ): String
}

