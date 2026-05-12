package com.htmake.reader.project

object DefaultProjectTemplates {
    fun forDocType(docType: String): List<ProjectTemplateSection> {
        return when (docType) {
            "立项申报书" -> listOf(
                ProjectTemplateSection("选题依据"),
                ProjectTemplateSection("研究背景与意义"),
                ProjectTemplateSection("研究目标"),
                ProjectTemplateSection("研究内容与创新点"),
                ProjectTemplateSection("研究方法与技术路线"),
                ProjectTemplateSection("研究计划与进度安排"),
                ProjectTemplateSection("预期成果与推广应用"),
                ProjectTemplateSection("参考文献")
            )
            "开题报告" -> listOf(
                ProjectTemplateSection("研究背景与意义"),
                ProjectTemplateSection("相关研究现状"),
                ProjectTemplateSection("研究目标"),
                ProjectTemplateSection("研究内容"),
                ProjectTemplateSection("研究方法与资料来源"),
                ProjectTemplateSection("实施方案与进度安排"),
                ProjectTemplateSection("可行性分析与风险应对"),
                ProjectTemplateSection("预期成果")
            )
            "结题报告" -> listOf(
                ProjectTemplateSection("研究背景与目标"),
                ProjectTemplateSection("研究过程与实施"),
                ProjectTemplateSection("研究成果与证据材料"),
                ProjectTemplateSection("成效与推广应用"),
                ProjectTemplateSection("存在问题与反思"),
                ProjectTemplateSection("结论与建议"),
                ProjectTemplateSection("成果清单")
            )
            else -> listOf(
                ProjectTemplateSection("研究背景与意义"),
                ProjectTemplateSection("研究目标"),
                ProjectTemplateSection("研究内容"),
                ProjectTemplateSection("研究方法"),
                ProjectTemplateSection("进度安排"),
                ProjectTemplateSection("预期成果")
            )
        }
    }
}

