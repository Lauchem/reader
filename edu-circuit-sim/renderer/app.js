const fmt = (n, d = 2) => (Number.isFinite(n) ? n.toFixed(d) : '—')

const byId = (id) => document.getElementById(id)

const switchView = (view) => {
  document.querySelectorAll('.nav-btn').forEach((b) => {
    b.classList.toggle('is-active', b.dataset.view === view)
  })
  document.querySelectorAll('.view').forEach((v) => v.classList.remove('is-active'))
  byId(`view-${view}`).classList.add('is-active')
}

const createEl = (tag, cls, text) => {
  const el = document.createElement(tag)
  if (cls) el.className = cls
  if (typeof text === 'string') el.textContent = text
  return el
}

const renderAbout = (container) => {
  container.innerHTML = ''
  const root = createEl('div', 'stack')
  container.append(root)

  const card = createEl('div', 'card')
  card.append(createEl('div', 'card-title', '使用说明（可直接放入提交材料）'))
  const text = createEl(
    'div',
    'notice',
    [
      '一、运行：双击程序即可进入主界面；点击顶部“仿真实验/实验记录/练习评价”切换模块。',
      '二、仿真实验：选择连接方式（串联/并联）→拖拽电阻到插槽→调节电源电压→观察仪表与表格→保存实验记录。',
      '三、实验记录：查看已保存的实验与练习成绩；可导出 JSON 文件用于备份/上交；也可清除全部本地记录。',
      '四、卸载：绿色便携版直接删除解压后的文件夹；如需删除个人数据，可在“实验记录”先点击清除。',
      '五、注意：本软件用于教学演示与概念理解，采用理想电路模型（忽略导线电阻、内阻等）。',
    ].join('\n'),
  )
  card.append(text)
  root.append(card)
}

const renderRecords = ({ container, state, onExport, onClear }) => {
  container.innerHTML = ''
  const root = createEl('div', 'stack')
  container.append(root)

  const actionCard = createEl('div', 'card')
  actionCard.append(createEl('div', 'card-title', '数据管理'))
  const row = createEl('div', 'row')
  const exportBtn = createEl('button', 'btn primary', '导出全部记录')
  const clearBtn = createEl('button', 'btn danger', '清除全部记录')
  row.append(exportBtn, clearBtn)
  actionCard.append(row)
  actionCard.append(
    createEl(
      'div',
      'label',
      `实验记录：${state.experiments.length} 条；练习成绩：${state.quizAttempts.length} 条`,
    ),
  )
  root.append(actionCard)

  exportBtn.addEventListener('click', onExport)
  clearBtn.addEventListener('click', onClear)

  const expCard = createEl('div', 'card')
  expCard.append(createEl('div', 'card-title', '仿真实验记录'))
  if (!state.experiments.length) {
    expCard.append(createEl('div', 'notice', '暂无记录。请在“仿真实验”中保存一次实验。'))
  } else {
    const table = document.createElement('table')
    table.className = 'table'
    table.innerHTML =
      '<thead><tr><th>时间</th><th>方式</th><th>电压(V)</th><th>电阻(Ω)</th><th>Rt(Ω)</th><th>It(A)</th></tr></thead><tbody></tbody>'
    const tbody = table.querySelector('tbody')
    state.experiments.slice(0, 50).forEach((e) => {
      const tr = document.createElement('tr')
      const dt = new Date(e.createdAt)
      const mode = e.mode === 'parallel' ? '并联' : '串联'
      const rs = (e.resistors || []).map((r) => `${r.ohm}Ω`).join('，')
      tr.innerHTML = `<td>${dt.toLocaleString()}</td><td>${mode}</td><td>${fmt(
        e.voltage,
        0,
      )}</td><td>${rs}</td><td>${fmt(e.result?.Rt, 2)}</td><td>${fmt(e.result?.It, 3)}</td>`
      tbody.append(tr)
    })
    expCard.append(table)
    expCard.append(createEl('div', 'label', '仅显示最近 50 条，可导出获取完整数据。'))
  }
  root.append(expCard)

  const quizCard = createEl('div', 'card')
  quizCard.append(createEl('div', 'card-title', '练习成绩记录'))
  if (!state.quizAttempts.length) {
    quizCard.append(createEl('div', 'notice', '暂无成绩。请在“练习评价”中完成一次练习。'))
  } else {
    const table = document.createElement('table')
    table.className = 'table'
    table.innerHTML =
      '<thead><tr><th>时间</th><th>得分</th><th>总分</th></tr></thead><tbody></tbody>'
    const tbody = table.querySelector('tbody')
    state.quizAttempts.slice(0, 50).forEach((a) => {
      const tr = document.createElement('tr')
      const dt = new Date(a.createdAt)
      tr.innerHTML = `<td>${dt.toLocaleString()}</td><td>${fmt(a.score, 0)}</td><td>${fmt(
        a.total,
        0,
      )}</td>`
      tbody.append(tr)
    })
    quizCard.append(table)
    quizCard.append(createEl('div', 'label', '仅显示最近 50 条，可导出获取完整数据。'))
  }
  root.append(quizCard)
}

const main = async () => {
  if (!window.eduApi) {
    byId('view-sim').textContent = '运行环境异常：未加载到桌面API。'
    return
  }

  const state = await window.eduApi.getData()

  const viewSim = byId('view-sim')
  const viewRecords = byId('view-records')
  const viewQuiz = byId('view-quiz')
  const viewAbout = byId('view-about')

  const simWrap = createEl('div', 'stack')
  const simMsg = createEl('div', 'notice', '准备就绪。完成实验后点击“保存本次实验”。')
  const simInner = createEl('div')
  simWrap.append(simMsg, simInner)
  viewSim.append(simWrap)

  window.CircuitSim.renderSim({
    container: simInner,
    onSave: async (experiment) => {
      const experiments = await window.eduApi.addExperiment(experiment)
      state.experiments = experiments
      simMsg.className = 'notice success'
      simMsg.textContent = '已保存实验记录。'
      if (viewRecords.classList.contains('is-active')) {
        renderRecords({
          container: viewRecords,
          state,
          onExport,
          onClear,
        })
      }
    },
  })

  const quizWrap = createEl('div')
  viewQuiz.append(quizWrap)
  const quizController = window.Quiz.renderQuiz({
    container: quizWrap,
    onSave: async (attempt, questions) => {
      const attempts = await window.eduApi.addQuizAttempt(attempt)
      state.quizAttempts = attempts
      const lines = []
      lines.push(`得分：${attempt.score}/${attempt.total}`)
      lines.push('解析：')
      attempt.answers.forEach((a, idx) => {
        const q = questions[idx]
        const pickedText = a.picked == null ? '未作答' : q.options[a.picked]
        const correctText = q.options[a.correctAnswer]
        lines.push(
          `${idx + 1}. ${a.correct ? '正确' : '错误'}（你的答案：${pickedText}；正确答案：${correctText}）${q.explain}`,
        )
      })
      quizController.showResult({ score: attempt.score, detailText: lines.join('\n') })
      if (viewRecords.classList.contains('is-active')) {
        renderRecords({
          container: viewRecords,
          state,
          onExport,
          onClear,
        })
      }
    },
  })

  const onExport = async () => {
    const res = await window.eduApi.exportJson(state)
    const msg = res.ok ? `已导出：${res.filePath}` : '已取消导出。'
    const box = createEl('div', res.ok ? 'notice success' : 'notice', msg)
    viewRecords.prepend(box)
    setTimeout(() => box.remove(), 2500)
  }

  const onClear = async () => {
    const ok = window.confirm('确定要清除全部本地记录吗？此操作不可撤销。')
    if (!ok) return
    await window.eduApi.clearAll()
    state.experiments = []
    state.quizAttempts = []
    renderRecords({ container: viewRecords, state, onExport, onClear })
  }

  renderRecords({ container: viewRecords, state, onExport, onClear })
  renderAbout(viewAbout)

  byId('nav').addEventListener('click', (e) => {
    const btn = e.target.closest('.nav-btn')
    if (!btn) return
    switchView(btn.dataset.view)
  })
}

window.addEventListener('DOMContentLoaded', main)

