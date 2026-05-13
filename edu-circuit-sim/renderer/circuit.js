;(function () {
  const fmt = (n, d = 2) => (Number.isFinite(n) ? n.toFixed(d) : '—')

  const calc = ({ voltage, mode, resistors }) => {
    const Rs = resistors.map((r) => r.ohm).filter((v) => Number.isFinite(v) && v > 0)
    if (Rs.length === 0) {
      return { Rt: 0, It: 0, detail: [] }
    }

    let Rt = 0
    if (mode === 'parallel') {
      const inv = Rs.reduce((s, r) => s + 1 / r, 0)
      Rt = inv > 0 ? 1 / inv : 0
    } else {
      Rt = Rs.reduce((s, r) => s + r, 0)
    }

    const It = Rt > 0 ? voltage / Rt : 0
    const detail =
      mode === 'parallel'
        ? Rs.map((R, idx) => ({ idx, R, V: voltage, I: voltage / R }))
        : Rs.map((R, idx) => ({ idx, R, V: It * R, I: It }))

    return { Rt, It, detail }
  }

  const createEl = (tag, cls, text) => {
    const el = document.createElement(tag)
    if (cls) el.className = cls
    if (typeof text === 'string') el.textContent = text
    return el
  }

  const renderSim = ({ container, onSave }) => {
    const state = {
      mode: 'series',
      voltage: 6,
      slots: [null, null, null],
    }

    const palette = [
      { label: '电阻', ohm: 10 },
      { label: '电阻', ohm: 20 },
      { label: '电阻', ohm: 50 },
      { label: '电阻', ohm: 100 },
    ]

    const root = createEl('div', 'grid')
    const left = createEl('div', 'card')
    const center = createEl('div', 'bench')
    const right = createEl('div', 'card')
    root.append(left, center, right)
    container.innerHTML = ''
    container.append(root)

    left.append(createEl('div', 'card-title', '元件盒'))
    left.append(
      createEl(
        'div',
        'notice',
        '把“电阻”从元件盒拖拽到实验台插槽中；再选择串联/并联与电源电压，观察电流电压变化。',
      ),
    )
    const paletteGrid = createEl('div', 'palette')
    palette.forEach((p) => {
      const c = createEl('div', 'comp')
      c.setAttribute('draggable', 'true')
      c.addEventListener('dragstart', (e) => {
        e.dataTransfer.setData('application/json', JSON.stringify(p))
        e.dataTransfer.effectAllowed = 'copy'
      })
      c.append(createEl('div', 'lab', '拖拽到插槽'))
      c.append(createEl('strong', '', `${p.label} ${p.ohm}Ω`))
      paletteGrid.append(c)
    })
    left.append(paletteGrid)

    const benchTop = createEl('div', 'bench-top')
    center.append(benchTop)

    const modePill = createEl('div', 'pill')
    const modeLabel = createEl('div', 'label', '连接方式')
    const modeSelect = document.createElement('select')
    ;[
      { v: 'series', t: '串联' },
      { v: 'parallel', t: '并联' },
    ].forEach((m) => {
      const opt = document.createElement('option')
      opt.value = m.v
      opt.textContent = m.t
      modeSelect.append(opt)
    })
    modeSelect.value = state.mode
    modeSelect.addEventListener('change', () => {
      state.mode = modeSelect.value
      renderDerived()
    })
    modePill.append(modeLabel, modeSelect)

    const vPill = createEl('div', 'pill')
    const vLabel = createEl('div', 'label', '电源电压')
    const vRange = document.createElement('input')
    vRange.type = 'range'
    vRange.min = '1'
    vRange.max = '12'
    vRange.value = String(state.voltage)
    vRange.addEventListener('input', () => {
      state.voltage = Number(vRange.value)
      vValue.textContent = `${state.voltage} V`
      renderDerived()
    })
    const vValue = createEl('div', 'value', `${state.voltage} V`)
    vPill.append(vLabel, vRange, vValue)

    const btnClear = createEl('button', 'btn', '清空实验台')
    btnClear.addEventListener('click', () => {
      state.slots = [null, null, null]
      renderSlots()
      renderDerived()
    })

    benchTop.append(modePill, vPill, btnClear)

    const slotsWrap = createEl('div', 'slots')
    center.append(slotsWrap)

    const createSlot = (idx) => {
      const slot = createEl('div', 'slot')
      const leftBox = createEl('div', 'slot-left')
      const name = createEl('div', 'slot-name', `插槽 ${idx + 1}`)
      const hint = createEl('div', 'label', '可放置：电阻')
      leftBox.append(name, hint)
      const rightBox = createEl('div', 'slot-right')
      const chip = createEl('div', 'chip', '空')
      const removeBtn = createEl('button', 'btn', '移除')
      removeBtn.disabled = true
      removeBtn.addEventListener('click', () => {
        state.slots[idx] = null
        renderSlots()
        renderDerived()
      })
      rightBox.append(chip, removeBtn)
      slot.append(leftBox, rightBox)

      const setHover = (on) => {
        if (on) slot.classList.add('is-hover')
        else slot.classList.remove('is-hover')
      }

      slot.addEventListener('dragover', (e) => {
        e.preventDefault()
        e.dataTransfer.dropEffect = 'copy'
        setHover(true)
      })
      slot.addEventListener('dragleave', () => setHover(false))
      slot.addEventListener('drop', (e) => {
        e.preventDefault()
        setHover(false)
        try {
          const raw = e.dataTransfer.getData('application/json')
          const parsed = JSON.parse(raw)
          if (!parsed || parsed.label !== '电阻' || !Number.isFinite(parsed.ohm)) return
          state.slots[idx] = { label: parsed.label, ohm: parsed.ohm }
          renderSlots()
          renderDerived()
        } catch {}
      })

      return { slot, chip, removeBtn, hint }
    }

    const slotEls = [0, 1, 2].map((i) => createSlot(i))
    slotEls.forEach((s) => slotsWrap.append(s.slot))

    const resultWrap = createEl('div', 'card')
    resultWrap.append(createEl('div', 'card-title', '测量与计算'))
    const kpi = createEl('div', 'kpi')
    const kpiRt = createEl('div', 'kpi-item')
    const kpiIt = createEl('div', 'kpi-item')
    kpiRt.append(createEl('div', 'label', '等效电阻 Rt'), createEl('div', 'value', '—'))
    kpiIt.append(createEl('div', 'label', '总电流 It'), createEl('div', 'value', '—'))
    kpi.append(kpiRt, kpiIt)
    resultWrap.append(kpi)

    const table = document.createElement('table')
    table.className = 'table'
    table.innerHTML =
      '<thead><tr><th>元件</th><th>电阻(Ω)</th><th>电压(V)</th><th>电流(A)</th></tr></thead><tbody></tbody>'
    const tbody = table.querySelector('tbody')
    resultWrap.append(table)

    const saveBtn = createEl('button', 'btn primary', '保存本次实验')
    saveBtn.disabled = true
    const saveHint = createEl('div', 'label', '提示：保存后可在“实验记录”查看/导出')
    const saveBox = createEl('div', 'stack')
    saveBox.append(saveBtn, saveHint)

    right.append(createEl('div', 'card-title', '实验操作'))
    right.append(
      createEl(
        'div',
        'notice',
        '建议步骤：①选择连接方式 ②拖入电阻 ③调整电压与电阻组合 ④观察仪表 ⑤保存实验记录。',
      ),
    )
    right.append(resultWrap)
    right.append(saveBox)

    const renderSlots = () => {
      slotEls.forEach((s, idx) => {
        const it = state.slots[idx]
        if (!it) {
          s.chip.textContent = '空'
          s.removeBtn.disabled = true
          s.hint.textContent = '可放置：电阻'
          return
        }
        s.chip.textContent = `${it.label} ${it.ohm}Ω`
        s.removeBtn.disabled = false
        s.hint.textContent = '已放置：电阻'
      })
      saveBtn.disabled = state.slots.every((x) => !x)
    }

    const renderDerived = () => {
      const resistors = state.slots.filter(Boolean)
      const out = calc({ voltage: state.voltage, mode: state.mode, resistors })
      kpiRt.querySelector('.value').textContent = out.Rt > 0 ? `${fmt(out.Rt, 2)} Ω` : '—'
      kpiIt.querySelector('.value').textContent = out.Rt > 0 ? `${fmt(out.It, 3)} A` : '—'
      tbody.innerHTML = ''
      out.detail.forEach((d, i) => {
        const tr = document.createElement('tr')
        const name = `R${i + 1}`
        tr.innerHTML = `<td>${name}</td><td>${fmt(d.R, 2)}</td><td>${fmt(d.V, 3)}</td><td>${fmt(
          d.I,
          3,
        )}</td>`
        tbody.append(tr)
      })
      saveBtn.disabled = resistors.length === 0
    }

    saveBtn.addEventListener('click', () => {
      const resistors = state.slots.filter(Boolean)
      const out = calc({ voltage: state.voltage, mode: state.mode, resistors })
      if (!resistors.length || out.Rt <= 0) return
      const now = new Date()
      const experiment = {
        id: `${now.getTime()}-${Math.random().toString(16).slice(2)}`,
        createdAt: now.toISOString(),
        voltage: state.voltage,
        mode: state.mode,
        resistors: resistors.map((r, idx) => ({ name: `R${idx + 1}`, ohm: r.ohm })),
        result: {
          Rt: out.Rt,
          It: out.It,
          detail: out.detail.map((d, idx) => ({
            name: `R${idx + 1}`,
            ohm: d.R,
            V: d.V,
            I: d.I,
          })),
        },
      }
      onSave(experiment)
    })

    renderSlots()
    renderDerived()
  }

  window.CircuitSim = { renderSim }
})()

