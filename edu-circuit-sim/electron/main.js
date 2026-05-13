const { app, BrowserWindow, dialog, ipcMain } = require('electron')
const path = require('path')
const fs = require('fs')

const getDataFilePath = () => path.join(app.getPath('userData'), 'data.json')

const ensureDataFile = () => {
  const filePath = getDataFilePath()
  const dir = path.dirname(filePath)
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true })
  if (!fs.existsSync(filePath)) {
    fs.writeFileSync(
      filePath,
      JSON.stringify({ experiments: [], quizAttempts: [] }, null, 2),
      'utf-8',
    )
  }
  return filePath
}

const readData = () => {
  const filePath = ensureDataFile()
  try {
    const raw = fs.readFileSync(filePath, 'utf-8')
    const parsed = JSON.parse(raw)
    return {
      experiments: Array.isArray(parsed.experiments) ? parsed.experiments : [],
      quizAttempts: Array.isArray(parsed.quizAttempts) ? parsed.quizAttempts : [],
    }
  } catch {
    return { experiments: [], quizAttempts: [] }
  }
}

const writeData = (data) => {
  const filePath = ensureDataFile()
  fs.writeFileSync(
    filePath,
    JSON.stringify(
      {
        experiments: Array.isArray(data.experiments) ? data.experiments : [],
        quizAttempts: Array.isArray(data.quizAttempts) ? data.quizAttempts : [],
      },
      null,
      2,
    ),
    'utf-8',
  )
}

const createWindow = () => {
  const win = new BrowserWindow({
    width: 1100,
    height: 720,
    backgroundColor: '#0b1020',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  })

  win.removeMenu()
  win.loadFile(path.join(__dirname, '..', 'renderer', 'index.html'))
}

app.whenReady().then(() => {
  ipcMain.handle('data:get', () => readData())
  ipcMain.handle('data:clear', () => {
    writeData({ experiments: [], quizAttempts: [] })
    return true
  })
  ipcMain.handle('data:addExperiment', (_, experiment) => {
    const data = readData()
    data.experiments.unshift(experiment)
    writeData(data)
    return data.experiments
  })
  ipcMain.handle('data:addQuizAttempt', (_, attempt) => {
    const data = readData()
    data.quizAttempts.unshift(attempt)
    writeData(data)
    return data.quizAttempts
  })
  ipcMain.handle('data:export', async (_, payload) => {
    const { canceled, filePath } = await dialog.showSaveDialog({
      title: '导出记录',
      defaultPath: 'records.json',
      filters: [{ name: 'JSON', extensions: ['json'] }],
    })
    if (canceled || !filePath) return { ok: false, reason: 'canceled' }
    fs.writeFileSync(filePath, JSON.stringify(payload, null, 2), 'utf-8')
    return { ok: true, filePath }
  })

  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
