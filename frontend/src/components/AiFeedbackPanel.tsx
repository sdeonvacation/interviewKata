import { Sparkles } from 'lucide-react';

interface AiFeedbackPanelProps {
  feedback: string | null;
  loading?: boolean;
}

export function AiFeedbackPanel({ feedback, loading }: AiFeedbackPanelProps) {
  return (
    <div className="rounded-xl overflow-hidden">
      <div className="h-1 bg-gradient-to-r from-amber-500 to-emerald-500 rounded-t-xl" />
      <div className="card rounded-t-none">
        <div className="flex items-center gap-2 mb-4">
          <Sparkles className="w-5 h-5 text-amber-400" />
          <span className="text-[#f0f6fc] font-semibold">AI Feedback</span>
        </div>

        {loading ? (
          <div className="space-y-3">
            <div className="h-4 bg-[#161b22] rounded animate-pulse" />
            <div className="h-4 bg-[#161b22] rounded animate-pulse w-5/6" />
            <div className="h-4 bg-[#161b22] rounded animate-pulse w-4/6" />
          </div>
        ) : feedback ? (
          <p className="text-[#8b949e] leading-relaxed whitespace-pre-wrap">
            {feedback}
          </p>
        ) : (
          <p className="text-[#484f58]">
            Submit your solution to receive AI feedback
          </p>
        )}
      </div>
    </div>
  );
}
