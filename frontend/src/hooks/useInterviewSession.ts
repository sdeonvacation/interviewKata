import { useState, useCallback } from 'react';
import { InterviewTurn, MockInterview } from '@/types';
import { get, post } from '@/api/client';

type InterviewState = 'idle' | 'loading' | 'active' | 'complete';

interface UseInterviewSessionReturn {
  state: InterviewState;
  interview: MockInterview | null;
  turns: InterviewTurn[];
  error: string | null;
  startInterview: (topicArea: string, difficulty: string) => void;
  sendMessage: (content: string) => void;
  endInterview: () => void;
}

function useInterviewSession(): UseInterviewSessionReturn {
  const [state, setState] = useState<InterviewState>('idle');
  const [interview, setInterview] = useState<MockInterview | null>(null);
  const [turns, setTurns] = useState<InterviewTurn[]>([]);
  const [error, setError] = useState<string | null>(null);

  const startInterview = useCallback(
    async (topicArea: string, difficulty: string) => {
      setState('loading');
      setError(null);
      try {
        const data = await post<MockInterview>('/interviews/start', {
          topicArea,
          difficulty,
        });
        setInterview(data);
        // Fetch the initial turn (first AI question)
        const initialTurns = await get<InterviewTurn[]>(`/interviews/${data.id}/turns`);
        setTurns(initialTurns);
        setState('active');
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to start interview');
        setState('idle');
      }
    },
    []
  );

  const sendMessage = useCallback(
    async (content: string) => {
      if (!interview) return;

      try {
        const response = await post<InterviewTurn>(
          `/interviews/${interview.id}/answer`,
          { answer: content }
        );
        setTurns((prev) => [...prev, response]);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to send message');
      }
    },
    [interview]
  );

  const endInterview = useCallback(async () => {
    if (!interview) return;

    try {
      const result = await post<MockInterview>(
        `/interviews/${interview.id}/end`
      );
      setInterview(result);
      setState('complete');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to end interview');
    }
  }, [interview]);

  return {
    state,
    interview,
    turns,
    error,
    startInterview,
    sendMessage,
    endInterview,
  };
}

export { useInterviewSession };
export type { InterviewState };
