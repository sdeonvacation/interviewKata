import { Bot, User } from 'lucide-react';

interface ChatBubbleProps {
  role: 'AI' | 'USER';
  content: string;
  timestamp?: string;
}

export function ChatBubble({ role, content, timestamp }: ChatBubbleProps) {
  const isAI = role === 'AI';

  return (
    <div className={`flex items-end gap-2 ${isAI ? 'justify-start' : 'justify-end'}`}>
      {isAI && (
        <div className="w-7 h-7 rounded-full flex items-center justify-center bg-amber-500/10 shrink-0">
          <Bot className="w-4 h-4 text-amber-400" />
        </div>
      )}

      <div className="max-w-[80%] flex flex-col gap-1">
        <div
          className={`px-4 py-2.5 text-sm text-[#f0f6fc] ${
            isAI
              ? 'bg-[#161b22] rounded-2xl rounded-bl-sm'
              : 'bg-amber-500/10 rounded-2xl rounded-br-sm'
          }`}
        >
          {content}
        </div>
        {timestamp && (
          <span className={`text-xs text-[#484f58] ${isAI ? 'text-left' : 'text-right'}`}>
            {timestamp}
          </span>
        )}
      </div>

      {!isAI && (
        <div className="w-7 h-7 rounded-full flex items-center justify-center bg-[#161b22] shrink-0">
          <User className="w-4 h-4 text-[#8b949e]" />
        </div>
      )}
    </div>
  );
}
