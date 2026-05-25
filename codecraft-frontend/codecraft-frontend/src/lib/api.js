import axios from 'axios'

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8085'

const api = axios.create({
  baseURL: `${BACKEND_URL}/api`,
  timeout: 15000,
})

export const runCode = (code, language, stdin = '') =>
  api.post('/run', { code, language, stdin })

export const getResult = (jobId) =>
  api.get(`/result/${jobId}`)

export const getLanguages = () =>
  api.get('/languages')

export const askAi = (code, language, action, errorMessage = null) =>
  api.post('/ai', { code, language, action, errorMessage })