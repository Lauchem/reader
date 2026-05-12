package com.htmake.reader.project

class MockTextGenerator : TextGenerator {
    override fun generateSectionText(
        docType: String,
        topicTitle: String,
        host: ProjectHost,
        section: ProjectTemplateSection
    ): String {
        val base = "本课题围绕《$topicTitle》开展，主持人为${host.unit}${host.title}${host.name}。"
        val text = when (section.title) {
            "选题依据" -> base + "选题聚焦中小学教育教学中的真实问题，强调可操作、可验证、可推广。"
            "研究背景与意义", "研究背景与目标" -> base + "从政策导向、学校实际与学情需求出发，阐明研究的必要性与价值。"
            "相关研究现状" -> base + "梳理国内外相关研究与典型实践，提炼已有成果与不足，明确本课题切入点。"
            "研究目标" -> base + "提出总体目标与可量化的分目标，确保目标与研究内容、方法及成果一致。"
            "研究内容", "研究内容与创新点" -> base + "分解研究任务与关键问题，明确创新点与预期改进路径。"
            "研究方法", "研究方法与资料来源", "研究方法与技术路线" -> base + "采用行动研究、案例研究、问卷与访谈等方法，形成“设计—实施—反思—优化”的循环改进。"
            "研究计划与进度安排", "实施方案与进度安排", "进度安排" -> base + "按学期或月份制定阶段任务，包含准备、实施、总结与推广四个阶段，并设置过程性评价节点。"
            "预期成果", "预期成果与推广应用", "成效与推广应用" -> base + "形成可复制的课堂/活动范式、过程资料、论文或案例集，并通过校本教研与区域交流进行推广。"
            "研究过程与实施" -> base + "记录实施路径、关键活动与迭代过程，体现数据与证据的连续积累。"
            "研究成果与证据材料" -> base + "汇总量化与质性证据，包括学生发展数据、课堂观察、作业作品、教师反思与家校反馈等。"
            "存在问题与反思" -> base + "复盘实施中的限制因素与改进空间，提出下一步优化建议。"
            "结论与建议" -> base + "归纳主要结论，提出对学校教学改进与区域推广的建议。"
            "成果清单" -> "成果包括：研究报告、课堂案例、教学设计、课件资源、论文/获奖/公开课记录、过程性材料汇编等。"
            "参考文献" -> "参考文献可包含教育政策文件、课程标准、核心期刊论文与相关专著等，按学校/地区要求格式整理。"
            else -> base + "围绕“问题—目标—策略—证据”主线展开撰写，确保逻辑完整、表述规范。"
        }
        return applyWordLimit(text, section.guidelines ?: "")
    }

    private fun applyWordLimit(text: String, guidelines: String): String {
        val m = Regex("(\\d{2,5})\\s*字").find(guidelines) ?: return text
        val limit = m.groupValues.getOrNull(1)?.toIntOrNull() ?: return text
        if (limit <= 0) return text
        val sb = StringBuilder(text)
        val padding = "本段以学校真实情境为依据，围绕问题提出、证据收集与改进策略进行阐述，确保表述具体可执行。"
        while (sb.length < limit) {
            if (!sb.endsWith("。")) {
                sb.append("。")
            }
            sb.append(padding)
        }
        val max = (limit * 12) / 10
        if (sb.length > max) {
            return sb.substring(0, max).trimEnd('。') + "。"
        }
        return sb.toString()
    }
}
