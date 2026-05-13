;(function () {
  const questionBank = [
    {
      id: 'q1',
      stem: '已知电压 U=6V，电阻 R=3Ω，则电流 I 等于多少？',
      options: ['1A', '2A', '3A', '18A'],
      answer: 1,
      explain: '欧姆定律：I=U/R=6/3=2A。',
    },
    {
      id: 'q2',
      stem: '两个电阻 10Ω 和 20Ω 串联，它们的等效电阻是多少？',
      options: ['10Ω', '20Ω', '30Ω', '200Ω'],
      answer: 2,
      explain: '串联等效电阻：Rt=R1+R2=30Ω。',
    },
    {
      id: 'q3',
      stem: '两个电阻 10Ω 和 20Ω 并联，它们的等效电阻最可能是？',
      options: ['大于20Ω', '介于10Ω和20Ω之间', '等于30Ω', '小于10Ω'],
      answer: 3,
      explain: '并联等效电阻一定小于最小的电阻，因此小于10Ω。',
    },
    {
      id: 'q4',
      stem: '在串联电路中，各元件的电流关系正确的是？',
      options: ['各处电流相等', '电流按电阻大小分配', '越靠近电源电流越大', '电流为0'],
      answer: 0,
      explain: '串联电路中电流处处相等。',
    },
    {
      id: 'q5',
      stem: '在并联电路中，各支路的电压关系正确的是？',
      options: ['支路电压相等', '电压按电阻大小分配', '越靠近电源电压越大', '支路电压为0'],
      answer: 0,
      explain: '并联电路中各支路电压相等，都等于电源电压。',
    },
    {
      id: 'q6',
      stem: '测量电流时，电流表应当怎样连接？',
      options: ['与被测部分并联', '与被测部分串联', '随意连接', '接不接都一样'],
      answer: 1,
      explain: '电流表应与被测部分串联。',
    },
    {
      id: 'q7',
      stem: '测量电压时，电压表应当怎样连接？',
      options: ['与被测部分并联', '与被测部分串联', '随意连接', '接不接都一样'],
      answer: 0,
      explain: '电压表应与被测部分并联。',
    },
  ]

  const shuffle = (arr) => {
    const a = [...arr]
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[a[i], a[j]] = [a[j], a[i]]
    }
    return a
  }

  const createEl = (tag, cls, text) => {
    const el = document.createElement(tag)
    if (cls) el.className = cls
    if (typeof text === 'string') el.textContent = text
    return el
  }

  const renderQuiz = ({ container, onSave }) => {
    const root = createEl('div', 'stack')
    container.innerHTML = ''
    container.append(root)

    const header = createEl('div', 'card')
    header.append(createEl('div', 'card-title', '练习评价（自动判分）'))
    header.append(
      createEl(
        'div',
        'notice',
        '点击“开始练习”生成一套题；完成后提交自动判分，并保存到本地成绩记录。',
      ),
    )
    root.append(header)

    const actions = createEl('div', 'card')
    const startBtn = createEl('button', 'btn primary', '开始练习')
    const submitBtn = createEl('button', 'btn', '提交判分')
    submitBtn.disabled = true
    const resetBtn = createEl('button', 'btn', '重做一套')
    resetBtn.disabled = true
    const row = createEl('div', 'row')
    row.append(startBtn, submitBtn, resetBtn)
    actions.append(createEl('div', 'card-title', '操作'))
    actions.append(row)
    root.append(actions)

    const panel = createEl('div', 'card')
    panel.append(createEl('div', 'card-title', '题目'))
    const qWrap = createEl('div', 'stack')
    panel.append(qWrap)
    root.append(panel)

    const result = createEl('div', 'card')
    result.append(createEl('div', 'card-title', '结果'))
    const resultBox = createEl('div', 'notice')
    result.append(resultBox)
    root.append(result)

    let current = []
    let startedAt = null

    const buildQuiz = () => {
      current = shuffle(questionBank).slice(0, 5)
      startedAt = new Date()
      qWrap.innerHTML = ''
      current.forEach((q, idx) => {
        const box = createEl('div', 'quiz-q')
        const title = createEl('h3', '', `${idx + 1}. ${q.stem}`)
        box.append(title)
        q.options.forEach((opt, oi) => {
          const label = createEl('label', 'quiz-opt')
          const input = document.createElement('input')
          input.type = 'radio'
          input.name = `q-${q.id}`
          input.value = String(oi)
          label.append(input, document.createTextNode(opt))
          box.append(label)
        })
        qWrap.append(box)
      })
      resultBox.className = 'notice'
      resultBox.textContent = '已生成题目，请作答后提交。'
      submitBtn.disabled = false
      resetBtn.disabled = false
    }

    const grade = () => {
      if (!current.length) return
      let score = 0
      const answers = current.map((q) => {
        const picked = document.querySelector(`input[name="q-${q.id}"]:checked`)
        const pickedIdx = picked ? Number(picked.value) : null
        const correct = pickedIdx === q.answer
        if (correct) score += 20
        return { id: q.id, picked: pickedIdx, correctAnswer: q.answer, correct }
      })
      const now = new Date()
      const attempt = {
        id: `${now.getTime()}-${Math.random().toString(16).slice(2)}`,
        createdAt: now.toISOString(),
        startedAt: startedAt ? startedAt.toISOString() : now.toISOString(),
        total: 100,
        score,
        answers,
      }
      onSave(attempt, current)
    }

    startBtn.addEventListener('click', buildQuiz)
    resetBtn.addEventListener('click', buildQuiz)
    submitBtn.addEventListener('click', grade)

    const showResult = ({ score, detailText }) => {
      resultBox.className = score >= 80 ? 'notice success' : score >= 60 ? 'notice' : 'notice dangerBox'
      resultBox.textContent = detailText
    }

    return { showResult }
  }

  window.Quiz = { renderQuiz, questionBank }
})()

