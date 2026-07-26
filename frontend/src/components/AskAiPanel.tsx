import { useState } from 'react';
import { MessageCircleQuestion, Send, X } from 'lucide-react';
import { MarkdownRenderer } from './MarkdownRenderer';
import { post } from '@/api/client';

interface AskAiPanelProps {
  /** Context string passed to AI (card content, challenge description, etc.) */
  context: string;
  /** Optional placeholder for the input */
  placeholder?: string;
}

export function AskAiPanel({ context, placeholder = 'Ask a question about this...' }: AskAiPanelProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleAsk = async () => {
    if (!question.trim() || loading) return;
    setLoading(true);
    setAnswer(null);
    try {
      const res = await post<{ answer: string }>('/ai/ask', { question: question.trim(), context });
      setAnswer(res.answer);
    } catch {
      setAnswer('Failed to get a response. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) {
    return (
      <button
        onClick={() => setIsOpen(true)}
        className="btn-secondary flex items-center gap-2 text-xs"
      >
        <MessageCircleQuestion className="w-3.5 h-3.5" />
        Ask AI
      </button>
    );
  }

  return (
    <div className="card border-amber-500/20 space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <MessageCircleQuestion className="w-4 h-4 text-amber-400" />
          <span className="text-sm font-medium text-[#f0f6fc]">Ask AI</span>
        </div>
        <button
          onClick={() => { setIsOpen(false); setAnswer(null); setQuestion(''); }}
          className="text-[#484f58] hover:text-[#8b949e] transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      <div className="flex gap-2">
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') handleAsk(); }}
          placeholder={placeholder}
          className="flex-1 bg-[#0d1117] rounded-lg px-3 py-2 text-sm text-[#f0f6fc] border border-white/[0.06] focus:border-amber-500/30 outline-none"
          disabled={loading}
        />
        <button
          onClick={handleAsk}
          disabled={!question.trim() || loading}
          className="p-2 bg-amber-500 rounded-lg hover:bg-amber-400 text-[#06090f] transition-colors disabled:opacity-50"
        >
          <Send className="w-4 h-4" />
        </button>
      </div>

      {loading && (
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 border-2 border-amber-500/30 border-t-amber-400 rounded-full animate-spin" />
          <span className="text-xs text-[#8b949e]">Thinking...</span>
        </div>
      )}

      {answer && (
        <div className="border-t border-white/[0.06] pt-3">
          <MarkdownRenderer content={answer} />
        </div>
      )}
    </div>
  );
}
