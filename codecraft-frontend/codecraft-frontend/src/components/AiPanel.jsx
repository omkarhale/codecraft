import { useState } from 'react'
import { Sparkles, X, Loader2, Lightbulb, Bug, BarChart2, Code2 } from 'lucide-react'
import { askAi } from '../lib/api'

const ACTIONS = [
  { id: 'EXPLAIN',    label: 'Explain Code',       icon: Lightbulb,  desc: 'Understand what this code does' },
  { id: 'FIX',        label: 'Fix Bugs',            icon: Bug,        desc: 'Find and fix errors' },
  { id: 'COMPLEXITY', label: 'Complexity Analysis', icon: BarChart2,  desc: 'Time & space complexity' },
  { id: 'GENERATE',   label: 'Generate Template',   icon: Code2,      desc: 'Create starter boilerplate' },
]

export function AiPanel({ open, onClose, code, language, errorMessage }) {
  const [loading, setLoading]   = useState(false)
  const [result, setResult]     = useState(null)
  const [activeAction, setActive] = useState(null)

  const handleAction = async (action) => {
    if (!code.trim()) return
    setLoading(true)
    setActive(action.id)
    setResult(null)
    try {
      const { data } = await askAi(code, language, action.id, errorMessage)
      setResult(data.result)
    } catch (e) {
      setResult('AI request failed. Please check your API key and try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {/* Backdrop on mobile */}
      {open && (
        <div
          className="fixed inset-0 z-10 md:hidden"
          style={{ background: 'rgba(0,0,0,0.5)' }}
          onClick={onClose}
        />
      )}

      {/* Slide-in panel */}
      <div
        className="fixed right-0 top-0 h-full z-20 flex flex-col shadow-2xl"
        style={{
          width: '340px',
          background: 'var(--bg-card)',
          borderLeft: '1px solid var(--border)',
          transform: open ? 'translateX(0)' : 'translateX(100%)',
          transition: 'transform 0.25s cubic-bezier(0.16,1,0.3,1)',
        }}
      >
        {/* Panel header */}
        <div
          className="flex items-center justify-between px-4 h-12 shrink-0 border-b"
          style={{ borderColor: 'var(--border)' }}
        >
          <div className="flex items-center gap-2">
            <Sparkles size={14} style={{ color: 'var(--accent)' }} />
            <span className="text-sm font-semibold" style={{ color: 'var(--text)' }}>AI Assist</span>
            <span
              className="text-xs px-1.5 py-0.5 rounded-full"
              style={{ background: 'var(--accent)', color: 'white', fontSize: '10px' }}
            >
              Gemini
            </span>
          </div>
          <button
            className="btn btn-ghost"
            onClick={onClose}
            style={{ padding: '4px 6px' }}
          >
            <X size={14} />
          </button>
        </div>

        {/* Action buttons */}
        <div className="p-3 shrink-0 border-b" style={{ borderColor: 'var(--border)' }}>
          <p className="text-xs mb-2.5" style={{ color: 'var(--text-muted)' }}>
            What would you like to do with your code?
          </p>
          <div className="grid grid-cols-2 gap-2">
            {ACTIONS.map(action => {
              const Icon = action.icon
              const isActive = activeAction === action.id && loading
              return (
                <button
                  key={action.id}
                  onClick={() => handleAction(action)}
                  disabled={loading}
                  className="flex flex-col items-start gap-1 p-2.5 rounded-lg border text-left transition-all"
                  style={{
                    background: activeAction === action.id ? 'var(--accent)' : 'var(--bg-subtle)',
                    borderColor: activeAction === action.id ? 'var(--accent)' : 'var(--border)',
                    color: activeAction === action.id ? 'white' : 'var(--text)',
                    opacity: loading && activeAction !== action.id ? 0.5 : 1,
                  }}
                >
                  {isActive
                    ? <Loader2 size={13} className="animate-spin" />
                    : <Icon size={13} />
                  }
                  <span className="text-xs font-medium leading-tight">{action.label}</span>
                </button>
              )
            })}
          </div>
        </div>

        {/* Result area */}
        <div className="flex-1 overflow-auto p-4">
          {loading && (
            <div className="flex items-center gap-2 fade-in" style={{ color: 'var(--text-muted)' }}>
              <Loader2 size={14} className="animate-spin" style={{ color: 'var(--accent)' }} />
              <span className="text-xs">Thinking...</span>
            </div>
          )}

          {result && !loading && (
            <div
              className="text-xs leading-relaxed fade-in whitespace-pre-wrap"
              style={{ color: 'var(--text)', fontFamily: 'DM Sans, sans-serif' }}
            >
              {result}
            </div>
          )}

          {!result && !loading && (
            <div className="flex flex-col items-center justify-center h-32 gap-2" style={{ color: 'var(--text-muted)' }}>
              <Sparkles size={20} opacity={0.3} />
              <span className="text-xs text-center">
                Select an action above to get AI insights about your code
              </span>
            </div>
          )}
        </div>

        {/* Footer note */}
        <div
          className="px-4 py-2 shrink-0 border-t text-center"
          style={{ borderColor: 'var(--border)' }}
        >
          <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
            Powered by Google Gemini · Free tier
          </span>
        </div>
      </div>
    </>
  )
}
