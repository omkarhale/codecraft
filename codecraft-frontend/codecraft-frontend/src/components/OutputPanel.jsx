import { Terminal, Clock, MemoryStick, CheckCircle2, XCircle, Loader2 } from 'lucide-react'

const STATUS_MAP = {
  SUCCESS:       { label: 'Success',       color: 'var(--success)', icon: CheckCircle2 },
  COMPILE_ERROR: { label: 'Compile Error', color: 'var(--error)',   icon: XCircle },
  RUNTIME_ERROR: { label: 'Runtime Error', color: 'var(--error)',   icon: XCircle },
  TIME_LIMIT:    { label: 'Time Limit',    color: 'var(--warning)', icon: XCircle },
  MEMORY_LIMIT:  { label: 'Memory Limit',  color: 'var(--warning)', icon: XCircle },
  UNKNOWN_ERROR: { label: 'Error',         color: 'var(--error)',   icon: XCircle },
}

export function OutputPanel({ status, output, stdin, onStdinChange }) {
  const isRunning = status === 'running'
  const meta = output ? STATUS_MAP[output.status] : null

  const displayText = () => {
    if (!output) return null
    if (output.stdout) return { text: output.stdout, type: 'success' }
    if (output.compileOutput) return { text: output.compileOutput, type: 'error' }
    if (output.stderr) return { text: output.stderr, type: 'error' }
    return { text: 'No output', type: 'muted' }
  }

  const display = displayText()

  return (
    <div
      className="flex flex-col h-full"
      style={{ background: 'var(--bg-subtle)', borderLeft: '1px solid var(--border)' }}
    >
      {/* Header */}
      <div
        className="flex items-center justify-between px-4 h-9 shrink-0 border-b"
        style={{ borderColor: 'var(--border)' }}
      >
        <div className="flex items-center gap-2">
          <Terminal size={13} style={{ color: 'var(--text-muted)' }} />
          <span className="text-xs font-medium uppercase tracking-wide" style={{ color: 'var(--text-muted)' }}>
            Output
          </span>
        </div>

        {/* Status badge */}
        {meta && (
          <div className="flex items-center gap-1.5 fade-in">
            <meta.icon size={12} style={{ color: meta.color }} />
            <span className="text-xs font-medium" style={{ color: meta.color }}>
              {meta.label}
            </span>
            {output.executionTime && (
              <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                · {(output.executionTime * 1000).toFixed(0)}ms
              </span>
            )}
          </div>
        )}

        {isRunning && (
          <div className="flex items-center gap-1.5">
            <Loader2 size={12} className="animate-spin" style={{ color: 'var(--accent)' }} />
            <span className="text-xs" style={{ color: 'var(--accent)' }}>Running...</span>
          </div>
        )}
      </div>

      {/* Output content */}
      <div className="flex-1 overflow-auto p-4">
        {isRunning && (
          <div className="flex items-center gap-2 fade-in" style={{ color: 'var(--text-muted)' }}>
            <span className="pulse text-xs font-mono">▋</span>
            <span className="text-xs">Executing...</span>
          </div>
        )}

        {display && !isRunning && (
          <pre
            className="output-area fade-in"
            style={{
              color: display.type === 'error' ? 'var(--error)'
                   : display.type === 'muted' ? 'var(--text-muted)'
                   : 'var(--text)',
              margin: 0
            }}
          >
            {display.text}
          </pre>
        )}

        {!display && !isRunning && (
          <div className="flex flex-col items-center justify-center h-full gap-2" style={{ color: 'var(--text-muted)' }}>
            <Terminal size={24} opacity={0.3} />
            <span className="text-xs">Run your code to see output here</span>
          </div>
        )}
      </div>

      {/* Stdin input */}
      <div
        className="shrink-0 border-t px-4 py-2 flex items-center gap-3"
        style={{ borderColor: 'var(--border)' }}
      >
        <span className="text-xs font-mono shrink-0" style={{ color: 'var(--text-muted)' }}>
          stdin
        </span>
        <input
          value={stdin}
          onChange={e => onStdinChange(e.target.value)}
          placeholder="optional input for your program..."
          className="flex-1 bg-transparent text-xs font-mono outline-none"
          style={{ color: 'var(--text)', caretColor: 'var(--accent)' }}
        />
      </div>
    </div>
  )
}
