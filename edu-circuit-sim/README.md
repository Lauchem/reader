# 电路仿真实验课件（Windows桌面绿色版）

## 运行与开发

```bash
cd edu-circuit-sim
npm install
npm run start
```

## 打包（生成 Windows 绿色目录）

```bash
cd edu-circuit-sim
npm run pack:win
```

打包产物在 `edu-circuit-sim/dist/电路仿真实验课件-win32-x64/`，可直接压缩为 ZIP 提交（解压即用）。

## 使用方式（课堂/演示）

- 仿真实验：选择串联/并联 → 拖入电阻到插槽 → 调节电源电压 → 观察仪表与表格 → 保存实验记录
- 实验记录：查看实验与练习成绩，可导出 JSON 或清除本地记录
- 练习评价：开始练习 → 作答 → 提交判分并保存成绩

## 卸载

- 绿色版：删除解压后的文件夹即可
- 如需同时删除个人数据：先在“实验记录”中点击“清除全部记录”

