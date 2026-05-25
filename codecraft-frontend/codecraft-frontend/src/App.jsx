import { useState, useEffect, useRef } from 'react'
import Editor from '@monaco-editor/react'
import { TopBar } from './components/TopBar'
import { LanguageBar } from './components/LanguageBar'
import { OutputPanel } from './components/OutputPanel'
import { AiPanel } from './components/AiPanel'
import { useCodeRunner } from './hooks/useCodeRunner'
import { useTheme } from './hooks/useTheme'
import { getLanguages } from './lib/api'

export default function App() {
  const { dark, toggle: toggleTheme } = useTheme()
  const { status, output, run, reset } = useCodeRunner()

  const [languages, setLanguages]     = useState([])
  const [selected, setSelected]       = useState(null)
  const [code, setCode]               = useState('')
  const [stdin, setStdin]             = useState('')
  const [aiOpen, setAiOpen]           = useState(false)
  const editorRef                     = useRef(null)

  // Load languages on mount
  useEffect(() => {
    getLanguages().then(({ data }) => {
      setLanguages(data)
      const python = data.find(l => l.id === 'PYTHON') || data[0]
      setSelected(python)
      setCode(python.starterCode)
    }).catch(console.error)
  }, [])

  const handleLangSelect = (lang) => {
    setSelected(lang)
    setCode(lang.starterCode)
    reset()
  }

  const handleRun = () => {
    if (!selected || !code.trim()) return
    run(code, selected.id, stdin)
  }

  // Ctrl+Enter to run
  useEffect(() => {
    const handler = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') handleRun()
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [code, selected, stdin])

  const handleShare = () => {
    const url = `${window.location.origin}?lang=${selected?.id}&code=${encodeURIComponent(code)}`
    navigator.clipboard.writeText(url)
  }

  return (
    <div className="flex flex-col h-screen overflow-hidden">
      <TopBar
        dark={dark}
        onToggleTheme={toggleTheme}
        onShare={handleShare}
        code={code}
      />

      <LanguageBar
        languages={languages}
        selected={selected?.id}
        onSelect={handleLangSelect}
        onRun={handleRun}
        onAiToggle={() => setAiOpen(o => !o)}
        running={status === 'running'}
      />

      {/* Main split layout */}
      <div className="flex flex-1 overflow-hidden">

        {/* Editor pane */}
        <div className="flex-1 overflow-hidden" style={{ minWidth: 0 }}>
          <Editor
            height="100%"
            language={selected?.monacoId || 'python'}
            value={code}
            onChange={val => setCode(val || '')}
            onMount={editor => { editorRef.current = editor }}
            theme={dark ? 'vs-dark' : 'light'}
            options={{
              fontSize: 13.5,
              fontFamily: '"JetBrains Mono", monospace',
              fontLigatures: true,
              lineHeight: 1.7,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              renderLineHighlight: 'gutter',
              padding: { top: 16, bottom: 16 },
              smoothScrolling: true,
              cursorSmoothCaretAnimation: 'on',
              cursorBlinking: 'smooth',
              bracketPairColorization: { enabled: true },
              tabSize: 2,
              wordWrap: 'on',
              renderWhitespace: 'none',
              overviewRulerLanes: 0,
              hideCursorInOverviewRuler: true,
              scrollbar: {
                verticalScrollbarSize: 6,
                horizontalScrollbarSize: 6,
              }
            }}
          />
        </div>

        {/* Output pane */}
        <div
          className="shrink-0 overflow-hidden flex flex-col"
          style={{
            width: aiOpen ? 'calc(40% - 170px)' : '40%',
            minWidth: aiOpen ? '200px' : '280px',
            transition: 'width 0.25s cubic-bezier(0.16,1,0.3,1)',
          }}
        >
          <OutputPanel
            status={status}
            output={output}
            stdin={stdin}
            onStdinChange={setStdin}
          />
        </div>
      </div>

      {/* AI slide-in panel */}
      <AiPanel
        open={aiOpen}
        onClose={() => setAiOpen(false)}
        code={code}
        language={selected?.id}
        errorMessage={output?.stderr || output?.compileOutput}
      />
    </div>
  )
}
