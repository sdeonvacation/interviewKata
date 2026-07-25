import { useMemo } from 'react';
import { Code2 } from 'lucide-react';

interface CodeEditorProps {
  value: string;
  onChange: (value: string) => void;
  language?: string;
  readOnly?: boolean;
}

export function CodeEditor({ value, onChange, language = 'typescript', readOnly = false }: CodeEditorProps) {
  const lineNumbers = useMemo(() => {
    const count = value.split('\n').length;
    return Array.from({ length: count }, (_, i) => i + 1);
  }, [value]);

  return (
    <div className="bg-[#0d1117] rounded-xl overflow-hidden">
      {/* Top bar */}
      <div className="bg-[#161b22] px-4 py-2 flex justify-between items-center border-b border-white/[0.06]">
        <div className="flex items-center gap-2">
          <Code2 className="w-4 h-4 text-[#484f58]" />
          <span className="text-[#8b949e] text-sm">Code Editor</span>
        </div>
        <span className="bg-amber-500/10 text-amber-400 px-2 py-0.5 rounded text-xs font-medium">
          {language}
        </span>
      </div>

      {/* Editor area */}
      <div className="flex min-h-[300px]">
        {/* Line numbers */}
        <div className="pt-4 pb-4 pl-4 pr-0 select-none">
          <div className="flex flex-col text-[#484f58] text-right pr-4 font-mono text-sm leading-6 border-r border-white/[0.06]">
            {lineNumbers.map((num) => (
              <span key={num}>{num}</span>
            ))}
          </div>
        </div>

        {/* Textarea */}
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          readOnly={readOnly}
          spellCheck={false}
          className="w-full min-h-[300px] p-4 bg-transparent text-[#f0f6fc] resize-y outline-none font-mono text-sm leading-6 placeholder:text-[#484f58]"
          style={{ fontFamily: "'JetBrains Mono', monospace" }}
          placeholder="// Write your solution here..."
        />
      </div>
    </div>
  );
}
