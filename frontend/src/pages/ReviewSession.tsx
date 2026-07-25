import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { RotateCcw, Trophy } from 'lucide-react';
import { FlashCard } from '@/components/FlashCard';
import { GradeButtons } from '@/components/GradeButtons';
import { useReviewSession } from '@/hooks/useReviewSession';

export function ReviewSession() {
  const [searchParams] = useSearchParams();
  const topicId = searchParams.get('topicId') || undefined;
  const { state, session, currentCard, currentIndex, completedCards, error, startSession, showAnswer, gradeCard } =
    useReviewSession(topicId);

  const [isFlipped, setIsFlipped] = useState(false);

  // Auto-start session on mount (skip idle screen)
  useEffect(() => {
    startSession();
  }, [startSession]);

  // Reset flip state when card advances
  useEffect(() => {
    setIsFlipped(false);
  }, [currentCard?.id]);

  // Keyboard shortcuts: Space to flip, 1-5 to grade
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      // Don't intercept if user is typing in an input
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;

      if (state === 'reviewing' && e.code === 'Space') {
        e.preventDefault();
        setIsFlipped(true);
        showAnswer();
        return;
      }

      if (state === 'grading') {
        const key = parseInt(e.key, 10);
        if (key >= 1 && key <= 5) {
          e.preventDefault();
          // Trigger flash on the button via DOM attribute
          const btn = document.querySelector(`[data-grade="${key}"]`) as HTMLElement | null;
          if (btn) {
            btn.classList.add('ring-2', 'ring-white/40', 'scale-95');
            setTimeout(() => btn.classList.remove('ring-2', 'ring-white/40', 'scale-95'), 150);
          }
          gradeCard(key);
        }
      }
    },
    [state, showAnswer, gradeCard]
  );

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);

  const progress = session
    ? ((currentIndex + 1) / session.totalCards) * 100
    : 0;

  const handleShowAnswer = () => {
    setIsFlipped(true);
    showAnswer();
  };

  return (
    <div className="min-h-screen bg-[#06090f] px-4 py-10">
      <div className="max-w-2xl mx-auto space-y-8">
        <h1 className="text-2xl font-bold text-[#f0f6fc] tracking-tight">
          Review Session
        </h1>

        {/* Error state */}
        {error && (
          <div className="card border-red-500/30">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        {/* Idle state — only shown if auto-start errors out */}
        {state === 'idle' && !error && (
          <div className="card flex flex-col items-center justify-center py-16">
            <div className="w-8 h-8 border-2 border-amber-500/30 border-t-amber-400 rounded-full animate-spin mb-4" />
            <p className="text-[#8b949e] text-sm">Starting review...</p>
          </div>
        )}

        {/* Loading state */}
        {state === 'loading' && (
          <div className="card flex flex-col items-center justify-center py-16">
            <div className="w-8 h-8 border-2 border-amber-500/30 border-t-amber-400 rounded-full animate-spin mb-4" />
            <p className="text-[#8b949e] text-sm">Loading cards...</p>
          </div>
        )}

        {/* Reviewing / Grading states */}
        {(state === 'reviewing' || state === 'grading') && currentCard && (
          <div className="space-y-6">
            {/* Progress bar */}
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs text-[#8b949e]">
                <span>
                  Card {currentIndex + 1} of {session!.totalCards}
                </span>
                <span>{completedCards} completed</span>
              </div>
              <div className="h-1 bg-[#161b22] rounded-full overflow-hidden">
                <div
                  className="h-full bg-amber-500 rounded-full transition-all duration-300 ease-out"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>

            {/* Flash card */}
            <div className="flex justify-center">
              <FlashCard
                front={currentCard.front}
                back={currentCard.back}
                isFlipped={isFlipped}
                onFlip={() => {
                  if (state === 'reviewing') {
                    handleShowAnswer();
                  }
                }}
              />
            </div>

            {/* Action area */}
            {state === 'reviewing' && (
              <div className="flex flex-col items-center gap-3">
                <button onClick={handleShowAnswer} className="btn-primary">
                  Show Answer
                </button>
                <p className="text-[#484f58] text-xs">
                  or click the card / press <kbd className="px-1 bg-white/10 rounded text-[10px]">Space</kbd> to flip
                </p>
              </div>
            )}

            {state === 'grading' && (
              <div className="space-y-3">
                <p className="text-center text-xs text-[#8b949e]">
                  How well did you know this? <span className="text-[#484f58]">(press 1-5)</span>
                </p>
                <GradeButtons onGrade={gradeCard} disabled={false} />
              </div>
            )}
          </div>
        )}

        {/* Complete state */}
        {state === 'complete' && completedCards > 0 && (
          <div className="card flex flex-col items-center justify-center py-16 text-center">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center mb-5">
              <Trophy className="w-8 h-8 text-emerald-400" />
            </div>
            <h2 className="text-xl font-semibold text-[#f0f6fc] mb-2">
              Well done!
            </h2>
            <p className="text-[#8b949e] text-sm">
              You reviewed{' '}
              <span className="text-amber-400 font-medium">
                {completedCards}
              </span>{' '}
              cards this session.
            </p>
            <button onClick={startSession} className="btn-secondary mt-8">
              <RotateCcw className="w-4 h-4 mr-2" />
              Review Again
            </button>
          </div>
        )}

        {/* Empty state (no cards due) */}
        {state === 'complete' && completedCards === 0 && (
          <div className="card text-center py-10">
            <p className="text-[#8b949e] text-sm">
              No cards due for review right now. Check back later.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

export default ReviewSession;
