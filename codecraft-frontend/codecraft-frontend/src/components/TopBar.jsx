import { Sun, Moon, Share2, Copy, Check } from 'lucide-react'
import { useState } from 'react'

export function TopBar({ dark, onToggleTheme, onShare, code }) {
  const [copied, setCopied] = useState(false)

  const handleCopy = () => {
    navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <header
      className="flex items-center justify-between px-4 h-12 shrink-0 border-b"
      style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}
    >
      {/* Logo */}
      <div className="flex items-center gap-2">
        <div
          className="w-6 h-6 rounded-md flex items-center justify-center text-white text-xs font-bold"
          style={{ background: 'var(--accent)' }}
        >
          C
        </div>
        <span className="font-semibold text-sm tracking-tight" style={{ color: 'var(--text)' }}>
          code<span style={{ color: 'var(--accent)' }}>craft</span>
        </span>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2">
        <button className="btn btn-ghost" onClick={handleCopy} title="Copy code">
          {copied
            ? <><Check size={14} style={{ color: 'var(--success)' }} /> Copied</>
            : <><Copy size={14} /> Copy</>
          }
        </button>

        <button className="btn btn-ghost" onClick={onShare} title="Share snippet">
          <Share2 size={14} /> Share
        </button>

        <button
          className="btn btn-ghost"
          onClick={onToggleTheme}
          title="Toggle theme"
          style={{ padding: '6px 8px' }}
        >
          {dark ? <Sun size={15} /> : <Moon size={15} />}
        </button>
      </div>
    </header>
  )
}
