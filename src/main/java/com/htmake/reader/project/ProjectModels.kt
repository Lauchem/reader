package com.htmake.reader.project

data class ProjectHost(
    val name: String = "",
    val unit: String = "",
    val title: String = ""
)

data class ProjectGenerateRequest(
    val docType: String = "",
    val topicTitle: String = "",
    val host: ProjectHost = ProjectHost(),
    val templateId: String? = null
)

data class ProjectTemplateSection(
    val title: String = "",
    val guidelines: String? = ""
)

data class ProjectTemplate(
    val id: String = "",
    val name: String = "",
    val docType: String = "",
    val globalGuidelines: String? = "",
    val requiredFields: List<String>? = listOf(),
    val sections: List<ProjectTemplateSection>? = listOf(),
    val createdAt: Long = 0L
)
