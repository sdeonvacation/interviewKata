import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { get, post } from '@/api/client';
import { Difficulty } from '@/types';
import { AiFeedbackPanel } from '@/components/AiFeedbackPanel';
import { MarkdownRenderer } from '@/components/MarkdownRenderer';
import { DifficultyBadge } from '@/components/DifficultyBadge';
import { Send } from 'lucide-react';

interface DesignExerciseDetail {
  id: string;
  topicId: string;
  title: string;
  difficulty: Difficulty;
  estimatedMinutes: number;
  prompt: string;
  evaluationCriteria: string[];
}

export function DesignWorkspace() {
  const { id } = useParams<{ id: string }>();
  const [exercise, setExercise] = useState<DesignExerciseDetail | null>(null);
  const [answer, setAnswer] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    get<DesignExerciseDetail>(`/exercises/${id}`)
      .then(setExercise)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load exercise'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async () => {
    if (!id || !answer.trim()) return;
    setSubmitting(true);
    try {
      const result = await post<{ feedback: string }>(
        `/exercises/${id}/submit`,
        { answer }
      );
      setFeedback(result.feedback);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Evaluation failed');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-[#8b949e] text-sm">Loading exercise...</div>
      </div>
    );
  }

  if (error && !exercise) {
    return (
      <div className="card border-red-500/30">
        <p className="text-red-400">{error}</p>
      </div>
    );
  }

  if (!exercise) {
    return (
      <div className="card">
        <p className="text-[#8b949e]">Exercise not found.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <h1 className="text-xl font-semibold text-[#f0f6fc]">{exercise.title}</h1>
        <DifficultyBadge difficulty={exercise.difficulty} />
      </div>

      <div className="flex gap-4">
        {/* Left panel - prompt & criteria */}
        <div className="w-2/5 space-y-4">
          <div className="card">
            <MarkdownRenderer content={exercise.prompt} />
          </div>

          <div className="card">
            <h3 className="text-xs font-medium uppercase tracking-wider text-[#484f58] mb-3">
              Evaluation Criteria
            </h3>
            <ul className="space-y-2">
              {exercise.evaluationCriteria.map((criterion, i) => (
                <li key={i} className="flex items-start gap-2 text-sm text-[#8b949e]">
                  <span className="text-amber-400 mt-0.5">&#x2022;</span>
                  {criterion}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Right panel - answer textarea */}
        <div className="w-3/5 space-y-4">
          <textarea
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder="Write your design answer here..."
            className="card bg-[#0d1117] font-mono text-sm p-4 min-h-[400px] resize-y text-[#f0f6fc] outline-none border-white/[0.06] focus:border-amber-500/30 rounded-xl w-full"
          />

          <button
            onClick={handleSubmit}
            disabled={submitting || !answer.trim()}
            className="btn-primary flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Send className="w-4 h-4" />
            {submitting ? 'Evaluating...' : 'Submit for AI Evaluation'}
          </button>
        </div>
      </div>

      {error && (
        <div className="card border-red-500/30">
          <p className="text-red-400 text-sm">{error}</p>
        </div>
      )}

      <AiFeedbackPanel feedback={feedback} loading={submitting} />
    </div>
  );
}

export default DesignWorkspace;
