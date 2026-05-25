import { useState, useCallback, useRef } from 'react'
import { runCode, getResult } from '../lib/api'

export function useCodeRunner() {
  const [status, setStatus]   = useState('idle') // idle | running | success | error
  const [output, setOutput]   = useState(null)
  const pollRef               = useRef(null)

  const run = useCallback(async (code, language, stdin) => {
    setStatus('running')
    setOutput(null)

    try {
      const { data } = await runCode(code, language, stdin)
      const jobId = data.jobId

      // Poll every 600ms until result is ready
      const poll = async () => {
        try {
          const { data: result, status: httpStatus } = await getResult(jobId)

          if (httpStatus === 202) {
            // still running
            pollRef.current = setTimeout(poll, 600)
            return
          }

          setOutput(result)
          setStatus(result.status === 'SUCCESS' ? 'success' : 'error')
        } catch (e) {
          setStatus('error')
          setOutput({ stderr: 'Failed to get result. Please try again.' })
        }
      }

      pollRef.current = setTimeout(poll, 800)
    } catch (e) {
      setStatus('error')
      setOutput({ stderr: e.response?.data?.error || 'Failed to run code.' })
    }
  }, [])

  const reset = useCallback(() => {
    clearTimeout(pollRef.current)
    setStatus('idle')
    setOutput(null)
  }, [])

  return { status, output, run, reset }
}
