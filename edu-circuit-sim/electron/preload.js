const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('eduApi', {
  getData: () => ipcRenderer.invoke('data:get'),
  clearAll: () => ipcRenderer.invoke('data:clear'),
  addExperiment: (experiment) => ipcRenderer.invoke('data:addExperiment', experiment),
  addQuizAttempt: (attempt) => ipcRenderer.invoke('data:addQuizAttempt', attempt),
  exportJson: (payload) => ipcRenderer.invoke('data:export', payload),
})

