import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CheckCircle2, XCircle, Trophy, ArrowRight, Loader2, AlertTriangle } from 'lucide-react';
import { QuizQuestion } from '@/types';
import { get, post } from '@/api/client';

interface AnswerRecord {
  questionId: string;
  selected: string;
  correct: boolean;
}

interface AnswerResponse {
  correct: boolean;
  correctAnswer: string;
  explanation: string | null;
}

export function QuizSession() {
  const { id } = useParams<{ id: string }>();
  const [questions, setQuestions] = useState<QuizQuestion[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [showResult, setShowResult] = useState(false);
  const [answerResponse, setAnswerResponse] = useState<AnswerResponse | null>(null);
  const [answers, setAnswers] = useState<AnswerRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    get<QuizQuestion[]>(`/quizzes/${id}/questions`)
      .then(setQuestions)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load quiz'))
      .finally(() => setLoading(false));
  }, [id]);

  const currentQuestion = questions[currentIndex];
  const isComplete = currentIndex >= questions.length && questions.length > 0;
  const score = answers.filter((a) => a.correct).length;

  const handleSelect = (option: string) => {
    if (showResult) return;
    setSelectedAnswer(option);
  };

  const handleAnswer = async () => {
    if (!selectedAnswer || !currentQuestion) return;

    try {
      const response = await post<AnswerResponse>(`/quizzes/${id}/answer`, {
        questionId: currentQuestion.id,
        answer: selectedAnswer,
      });
      setAnswerResponse(response);
      setAnswers((prev) => [
        ...prev,
        { questionId: currentQuestion.id, selected: selectedAnswer, correct: response.correct },
      ]);
      setShowResult(true);
    } catch {
      // Fallback: mark as incorrect if server fails
      setAnswers((prev) => [
        ...prev,
        { questionId: currentQuestion.id, selected: selectedAnswer, correct: false },
      ]);
      setShowResult(true);
    }
  };

  const handleNext = () => {
    setCurrentIndex((i) => i + 1);
    setSelectedAnswer(null);
    setShowResult(false);
    setAnswerResponse(null);
  };

  // Loading state
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-3">
        <Loader2 className="w-6 h-6 text-amber-400 animate-spin" />
        <p className="text-[#8b949e] text-sm">Loading quiz...</p>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="card border-rose-500/30">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0" />
            <p className="text-rose-400">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  // Empty state
  if (questions.length === 0) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="card text-center py-12">
          <p className="text-[#8b949e]">No questions found for this quiz.</p>
          <Link to="/quizzes" className="btn-secondary inline-block mt-4">
            Back to Quizzes
          </Link>
        </div>
      </div>
    );
  }

  // Complete state
  if (isComplete) {
    const incorrect = questions.length - score;
    const percentage = Math.round((score / questions.length) * 100);

    return (
      <div className="max-w-lg mx-auto space-y-6">
        <div className="card text-center py-10">
          <Trophy className="w-10 h-10 text-amber-400 mx-auto mb-4" />
          <p className="text-[#8b949e] text-sm uppercase tracking-wide mb-2">Quiz Complete</p>
          <p className="text-5xl font-bold text-amber-400">{percentage}%</p>
          <p className="text-[#8b949e] mt-2">
            {score} of {questions.length} correct
          </p>

          <div className="flex items-center justify-center gap-6 mt-6 pt-6 border-t border-white/[0.06]">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              <span className="text-[#f0f6fc] text-sm font-medium">{score} correct</span>
            </div>
            <div className="flex items-center gap-2">
              <XCircle className="w-4 h-4 text-rose-400" />
              <span className="text-[#f0f6fc] text-sm font-medium">{incorrect} incorrect</span>
            </div>
          </div>
        </div>

        <Link to="/quizzes" className="btn-primary w-full block text-center">
          Done
        </Link>
      </div>
    );
  }

  if (!currentQuestion) return null;

  const progress = ((currentIndex) / questions.length) * 100;

  // Extract options as key-value pairs from Record<string, string>[]
  const optionEntries: [string, string][] = currentQuestion.options
    ? currentQuestion.options.flatMap((rec) => Object.entries(rec))
    : [];

  // Active / Answered state
  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Progress bar */}
      <div className="w-full h-1.5 bg-[#161b22] rounded-full overflow-hidden">
        <div
          className="h-full bg-amber-500 rounded-full transition-all duration-300"
          style={{ width: `${progress}%` }}
        />
      </div>

      {/* Question card */}
      <div className="card">
        <p className="text-[#8b949e] text-sm mb-3">
          Question {currentIndex + 1}/{questions.length}
        </p>
        <p className="text-lg text-[#f0f6fc] font-medium">{currentQuestion.questionText}</p>
      </div>

      {/* Options grid */}
      <div className="grid grid-cols-1 gap-3">
        {optionEntries.map(([key, value]) => {
          let classes =
            'bg-[#161b22] hover:bg-[#1c2128] border border-white/[0.06] rounded-lg p-4 cursor-pointer transition-all text-left w-full';

          if (showResult && answerResponse) {
            if (key === answerResponse.correctAnswer) {
              classes =
                'bg-emerald-500/5 border border-emerald-500/30 rounded-lg p-4 cursor-default transition-all text-left w-full';
            } else if (key === selectedAnswer && key !== answerResponse.correctAnswer) {
              classes =
                'bg-rose-500/5 border border-rose-500/30 rounded-lg p-4 cursor-default transition-all text-left w-full';
            } else {
              classes =
                'bg-[#161b22] border border-white/[0.06] rounded-lg p-4 cursor-default opacity-50 transition-all text-left w-full';
            }
          } else if (key === selectedAnswer) {
            classes =
              'bg-amber-500/5 border border-amber-500/30 rounded-lg p-4 cursor-pointer transition-all text-left w-full';
          }

          return (
            <button
              key={key}
              onClick={() => handleSelect(key)}
              disabled={showResult}
              className={classes}
            >
              <span
                className={
                  showResult && answerResponse && key === answerResponse.correctAnswer
                    ? 'text-emerald-400'
                    : showResult && key === selectedAnswer
                      ? 'text-rose-400'
                      : 'text-[#f0f6fc]'
                }
              >
                {key}: {value}
              </span>
              {showResult && answerResponse && key === answerResponse.correctAnswer && (
                <CheckCircle2 className="inline-block w-4 h-4 text-emerald-400 ml-2" />
              )}
              {showResult && answerResponse && key === selectedAnswer && key !== answerResponse.correctAnswer && (
                <XCircle className="inline-block w-4 h-4 text-rose-400 ml-2" />
              )}
            </button>
          );
        })}
      </div>

      {/* Explanation */}
      {showResult && answerResponse?.explanation && (
        <div className="card">
          <p className="text-[#8b949e] text-sm">{answerResponse.explanation}</p>
        </div>
      )}

      {/* Action button */}
      <div className="flex justify-end">
        {!showResult ? (
          <button
            onClick={handleAnswer}
            disabled={!selectedAnswer}
            className="btn-primary disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Submit Answer
          </button>
        ) : (
          <button onClick={handleNext} className="btn-primary flex items-center gap-2">
            Next
            <ArrowRight className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
}

export default QuizSession;
