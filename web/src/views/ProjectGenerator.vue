<template>
  <div class="project-generator">
    <div class="header">
      <div class="title">课题自动生成</div>
      <el-button size="mini" @click="$router.push('/')">返回</el-button>
    </div>

    <el-card shadow="never">
      <el-form label-width="100px" :model="form">
        <el-form-item label="文档类型">
          <el-select v-model="form.docType" placeholder="请选择">
            <el-option label="立项申报书" value="立项申报书"></el-option>
            <el-option label="开题报告" value="开题报告"></el-option>
            <el-option label="结题报告" value="结题报告"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="课题题目">
          <el-input v-model="form.topicTitle" placeholder="例如：基于项目化学习的小学语文阅读教学策略研究"></el-input>
        </el-form-item>

        <el-form-item label="主持人姓名">
          <el-input v-model="form.host.name" placeholder="请输入"></el-input>
        </el-form-item>

        <el-form-item label="所在单位">
          <el-input v-model="form.host.unit" placeholder="请输入"></el-input>
        </el-form-item>

        <el-form-item label="职称/职务">
          <el-input v-model="form.host.title" placeholder="请输入"></el-input>
        </el-form-item>

        <el-form-item label="模板选择">
          <el-select v-model="form.templateId" clearable placeholder="默认模板">
            <el-option
              v-for="t in templates"
              :key="t.id"
              :label="t.name + '（' + t.docType + '）'"
              :value="t.id"
            ></el-option>
          </el-select>
          <el-button size="mini" style="margin-left: 8px" @click="refreshTemplates"
            >刷新</el-button
          >
        </el-form-item>

        <el-form-item label="导入要求">
          <el-upload
            drag
            multiple
            :auto-upload="false"
            :file-list="uploadList"
            :on-change="onUploadChange"
            :on-remove="onUploadRemove"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              将要求文件拖到此处，或<em>点击选择</em>
            </div>
            <div class="el-upload__tip" slot="tip">
              支持 .docx / .doc / .pdf / .txt / .xlsx / .zip；导入后会生成一个“章节模板”供选择
            </div>
          </el-upload>
          <div style="margin-top: 10px">
            <el-input
              size="mini"
              v-model="importName"
              placeholder="模板名称（可选）"
              style="width: 240px"
            ></el-input>
            <el-button
              size="mini"
              type="primary"
              style="margin-left: 8px"
              :loading="importing"
              @click="importTemplate"
              >导入为模板</el-button
            >
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="generating" @click="generate"
            >生成Word</el-button
          >
        </el-form-item>

        <el-form-item label="生成结果" v-if="resultUrl">
          <el-link :href="apiRoot + resultUrl" target="_blank">{{
            apiRoot + resultUrl
          }}</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import Axios from "../plugins/axios";

export default {
  name: "ProjectGenerator",
  data() {
    return {
      templates: [],
      uploadList: [],
      importing: false,
      generating: false,
      importName: "",
      resultUrl: "",
      form: {
        docType: "立项申报书",
        topicTitle: "",
        templateId: "",
        host: {
          name: "",
          unit: "",
          title: ""
        }
      }
    };
  },
  computed: {
    api() {
      return this.$store.getters.api;
    },
    apiRoot() {
      return this.$store.getters.apiRoot;
    }
  },
  created() {
    this.refreshTemplates();
  },
  methods: {
    onUploadChange(file, fileList) {
      this.uploadList = fileList || [];
    },
    onUploadRemove(file, fileList) {
      this.uploadList = fileList || [];
    },
    async refreshTemplates() {
      const res = await Axios.get(this.api + "/projectTemplate/list", {
        params: {}
      });
      if (res && res.data && res.data.isSuccess) {
        this.templates = res.data.data || [];
      }
    },
    async importTemplate() {
      if (!this.uploadList.length) {
        this.$message.error("请先选择要求文件");
        return;
      }
      this.importing = true;
      try {
        const formData = new FormData();
        this.uploadList.forEach(f => {
          if (f && f.raw) {
            formData.append("file", f.raw);
          }
        });
        const url =
          this.api +
          "/projectTemplate/import?docType=" +
          encodeURIComponent(this.form.docType) +
          (this.importName
            ? "&name=" + encodeURIComponent(this.importName)
            : "");
        const res = await Axios({
          url,
          method: "post",
          data: formData,
          headers: {
            "Content-Type": "multipart/form-data"
          },
          alert: false
        });
        if (res && res.data && res.data.isSuccess) {
          const t = res.data.data;
          this.form.templateId = t.id;
          this.uploadList = [];
          this.importName = "";
          await this.refreshTemplates();
          this.$message.success("模板导入成功");
        }
      } finally {
        this.importing = false;
      }
    },
    async generate() {
      if (!this.form.topicTitle) {
        this.$message.error("请输入课题题目");
        return;
      }
      this.generating = true;
      this.resultUrl = "";
      try {
        const payload = {
          docType: this.form.docType,
          topicTitle: this.form.topicTitle,
          host: this.form.host,
          templateId: this.form.templateId || null
        };
        const res = await Axios.post(this.api + "/project/generate", payload, {
          silent: true
        });
        if (res && res.data && res.data.isSuccess) {
          this.resultUrl = (res.data.data || {}).url || "";
          if (!this.resultUrl) {
            this.$message.error("生成失败：未返回文件链接");
          } else {
            this.$message.success("生成成功");
          }
        }
      } finally {
        this.generating = false;
      }
    }
  }
};
</script>

<style scoped>
.project-generator {
  padding: 12px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
</style>
