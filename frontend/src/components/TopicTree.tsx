import { useState } from 'react';
import { Topic, Card } from '@/types';
import { post } from '@/api/client';
import { ChevronRight, Sparkles, Loader2, AlertCircle } from 'lucide-react';

interface TopicTreeProps {
  topics: Topic[];
  onSelect?: (topic: Topic) => void;
  onCardsGenerated?: (topicId: string, count: number) => void;
}

function TopicNode({
  topic,
  onSelect,
  onCardsGenerated,
}: {
  topic: Topic;
  onSelect?: (topic: Topic) => void;
  onCardsGenerated?: (topicId: string, count: number) => void;
}) {
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async (e: React.MouseEvent) => {
    e.stopPropagation();
    setGenerating(true);
    setError(null);
    try {
      const cards = await post<Card[]>(`/topics/${topic.id}/generate-cards`);
      onCardsGenerated?.(topic.id, cards.length);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Generation failed');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div>
      <div
        role="button"
        tabIndex={0}
        className="w-full flex justify-between items-center py-2.5 px-3 rounded-lg hover:bg-[#161b22] cursor-pointer transition-colors text-left"
        onClick={() => onSelect?.(topic)}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') onSelect?.(topic); }}
      >
        <div className="flex items-center gap-2">
          <span className="text-[#f0f6fc]">{topic.name}</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-[#8b949e] text-sm">
            {topic.cardCount} cards
          </span>
          {topic.cardCount === 0 && (
            <button
              onClick={handleGenerate}
              disabled={generating}
              className="flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              title="Generate cards with AI"
            >
              {generating ? (
                <Loader2 className="w-3 h-3 animate-spin" />
              ) : (
                <Sparkles className="w-3 h-3" />
              )}
              {generating ? 'Generating...' : 'Generate'}
            </button>
          )}
          <ChevronRight className="w-4 h-4 text-[#484f58]" />
        </div>
      </div>
      {error && (
        <div className="flex items-center gap-1.5 px-3 py-1 text-xs text-red-400">
          <AlertCircle className="w-3 h-3 shrink-0" />
          <span>{error}</span>
        </div>
      )}
    </div>
  );
}

export function TopicTree({ topics, onSelect, onCardsGenerated }: TopicTreeProps) {
  if (topics.length === 0) {
    return (
      <p className="text-sm text-[#484f58] py-2">No subtopics found.</p>
    );
  }

  return (
    <div className="space-y-0.5">
      {topics.map((topic) => (
        <TopicNode
          key={topic.id}
          topic={topic}
          onSelect={onSelect}
          onCardsGenerated={onCardsGenerated}
        />
      ))}
    </div>
  );
}
