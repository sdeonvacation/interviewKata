import { useState, useCallback } from 'react';
import { Card, ReviewSessionData } from '@/types';
import { post } from '@/api/client';

type ReviewState = 'idle' | 'loading' | 'reviewing' | 'grading' | 'complete';

interface UseReviewSessionReturn {
  state: ReviewState;
  session: ReviewSessionData | null;
  currentCard: Card | null;
  currentIndex: number;
  completedCards: number;
  error: string | null;
  startSession: () => void;
  showAnswer: () => void;
  gradeCard: (grade: number) => void;
}

interface GradeResponse {
  nextReviewDate: string;
  newInterval: number;
  cardsRemaining: number;
}

function useReviewSession(topicId?: string): UseReviewSessionReturn {
  const [state, setState] = useState<ReviewState>('idle');
  const [session, setSession] = useState<ReviewSessionData | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const currentCard = session
    ? session.cards[currentIndex] ?? null
    : null;

  const startSession = useCallback(async () => {
    setState('loading');
    setError(null);
    try {
      const body: Record<string, unknown> = { limit: 20 };
      if (topicId) body.topicId = topicId;
      const data = await post<ReviewSessionData>('/reviews/start', body);
      setSession(data);
      setCurrentIndex(0);
      setState(data.cards.length > 0 ? 'reviewing' : 'complete');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to start session');
      setState('idle');
    }
  }, [topicId]);

  const showAnswer = useCallback(() => {
    if (state === 'reviewing') {
      setState('grading');
    }
  }, [state]);

  const gradeCard = useCallback(
    async (grade: number) => {
      if (!session || !currentCard) return;

      try {
        await post<GradeResponse>(`/reviews/${session.sessionId}/grade`, {
          cardId: currentCard.id,
          grade,
        });

        const nextIdx = currentIndex + 1;
        if (nextIdx >= session.cards.length) {
          setState('complete');
        } else {
          setCurrentIndex(nextIdx);
          setState('reviewing');
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to submit grade');
      }
    },
    [session, currentCard, currentIndex]
  );

  return {
    state,
    session,
    currentCard,
    currentIndex,
    completedCards: currentIndex,
    error,
    startSession,
    showAnswer,
    gradeCard,
  };
}

export { useReviewSession };
export type { ReviewState };
