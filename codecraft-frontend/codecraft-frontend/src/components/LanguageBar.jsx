import { Play, Loader2, Sparkles } from 'lucide-react'

export function LanguageBar({ languages, selected, onSelect, onRun, onAiToggle, running }) {
  return (
    <div
      className="flex items-center gap-3 px-4 h-11 shrink-0 border-b overflow-x-auto"
      style={{ background: 'var(--bg-subtle)', borderColor: 'var(--border)' }}
    >
      {/* Language pills */}
      <div className="flex items-center gap-1.5 flex-1 overflow-x-auto pb-0.5">
        {languages.map(lang => (
          <button
            key={lang.id}
            className={`lang-pill ${selected === lang.id ? 'active' : ''}`}
            onClick={() => onSelect(lang)}
          >
            {lang.displayName.split(' ')[0]}
          </button>
        ))}
      </div>

      {/* Right actions */}
      <div className="flex items-center gap-2 shrink-0">
        <button
          className="btn btn-ghost"
          onClick={onAiToggle}
          style={{ fontSize: '12px' }}
        >
          <Sparkles size={13} style={{ color: 'var(--accent)' }} />
          AI
        </button>

        <button
          className="btn btn-primary"
          onClick={onRun}
          disabled={running}
          style={{ minWidth: '80px' }}
        >
          {running
            ? <><Loader2 size={13} className="animate-spin" /> Running</>
            : <><Play size={13} /> Run</>
          }
        </button>
      </div>
    </div>
  )
}
