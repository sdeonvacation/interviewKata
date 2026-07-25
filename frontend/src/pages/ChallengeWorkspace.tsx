import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { ChallengeDetail, Submission, SubmissionStatus } from '@/types';
import { get, post } from '@/api/client';
import { CodeEditor } from '@/components/CodeEditor';
import { TestResultPanel, TestResult } from '@/components/TestResultPanel';
import { AiFeedbackPanel } from '@/components/AiFeedbackPanel';
import { DifficultyBadge } from '@/components/DifficultyBadge';
import { MarkdownRenderer } from '@/components/MarkdownRenderer';
import { Timer } from '@/components/Timer';
import { useTimer } from '@/hooks/useTimer';
import { Play, ChevronDown, ChevronRight, Lightbulb, CheckCircle, Eye, Sparkles } from 'lucide-react';

export function ChallengeWorkspace() {
  const { id } = useParams<{ id: string }>();
  const [challenge, setChallenge] = useState<ChallengeDetail | null>(null);
  const [code, setCode] = useState('');
  const [submission, setSubmission] = useState<Submission | null>(null);
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [expandedHints, setExpandedHints] = useState<Set<number>>(new Set());
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [bottomExpanded, setBottomExpanded] = useState(false);
  const [showSolution, setShowSolution] = useState(false);

  const timer = useTimer(45 * 60);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    get<ChallengeDetail>(`/challenges/${id}`)
      .then((c) => {
        setChallenge(c);
        setCode(c.starterCode ?? '');
        timer.start();
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load challenge'))
      .finally(() => setLoading(false));
  }, [id]);

  const toggleHint = (index: number) => {
    setExpandedHints((prev) => {
      const next = new Set(prev);
      if (next.has(index)) {
        next.delete(index);
      } else {
        next.add(index);
      }
      return next;
    });
  };

  const handleRunTests = async () => {
    if (!id) return;
    setSubmitting(true);
    setError(null);
    try {
      const result = await post<Submission>(`/challenges/${id}/run-tests`, { code });
      setSubmission(result);
      const results: TestResult[] = (result.testResults ?? []).map((tr, i) => ({
        name: `Test ${i + 1}`,
        passed: (tr as Record<string, unknown>).passed === true,
      }));
      setTestResults(results);
      setBottomExpanded(true);

      if (result.status === SubmissionStatus.PASSED) {
        const updated = await get<ChallengeDetail>(`/challenges/${id}`);
        setChallenge(updated);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Submission failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmitForReview = async () => {
    if (!id) return;
    setSubmitting(true);
    setError(null);
    try {
      const result = await post<Submission>(`/challenges/${id}/submit`, { code });
      setSubmission(result);
      const results: TestResult[] = (result.testResults ?? []).map((tr, i) => ({
        name: `Test ${i + 1}`,
        passed: (tr as Record<string, unknown>).passed === true,
      }));
      setTestResults(results);
      setBottomExpanded(true);

      if (result.status === SubmissionStatus.PASSED) {
        const updated = await get<ChallengeDetail>(`/challenges/${id}`);
        setChallenge(updated);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Submit failed');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64 bg-[#06090f]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-amber-500/30 border-t-amber-500 rounded-full animate-spin" />
          <p className="text-[#8b949e] text-sm">Loading challenge...</p>
        </div>
      </div>
    );
  }

  if (error && !challenge) {
    return (
      <div className="flex items-center justify-center h-64 bg-[#06090f]">
        <div className="card border-red-500/30">
          <p className="text-red-400">{error}</p>
        </div>
      </div>
    );
  }

  if (!challenge) {
    return (
      <div className="flex items-center justify-center h-64 bg-[#06090f]">
        <div className="card">
          <p className="text-[#8b949e]">Challenge not found</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col gap-4 bg-[#06090f] p-4">
      {/* Top bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-bold text-[#f0f6fc]">{challenge.title}</h1>
          <DifficultyBadge difficulty={challenge.difficulty} />
        </div>
        <Timer
          timeLeft={timer.timeLeft}
          totalTime={45 * 60}
          isRunning={timer.isRunning}
        />
      </div>

      {/* Split layout */}
      <div className="flex-1 flex gap-4 min-h-0">
        {/* Left panel - problem description */}
        <div className="w-2/5 card overflow-y-auto">
          <h2 className="text-sm font-semibold text-[#f0f6fc] mb-3">Problem Description</h2>
          <div className="text-sm">
            <MarkdownRenderer content={challenge.problemStatement} />
          </div>

          {/* Hints accordion */}
          {challenge.hints.length > 0 && (
            <div className="mt-6 border-t border-white/[0.06] pt-4">
              <div className="flex items-center gap-2 mb-3">
                <Lightbulb className="w-4 h-4 text-amber-400" />
                <h3 className="text-sm font-semibold text-amber-400">Hints</h3>
              </div>
              <div className="space-y-2">
                {challenge.hints.map((hint, i) => (
                  <div
                    key={i}
                    className="rounded-lg bg-[#161b22] border border-white/[0.06] overflow-hidden"
                  >
                    <button
                      onClick={() => toggleHint(i)}
                      className="w-full flex items-center gap-2 px-3 py-2 text-left hover:border-amber-500/30 transition-colors"
                    >
                      {expandedHints.has(i) ? (
                        <ChevronDown className="w-4 h-4 text-amber-400 shrink-0" />
                      ) : (
                        <ChevronRight className="w-4 h-4 text-amber-400 shrink-0" />
                      )}
                      <span className="text-sm text-amber-400 font-medium">
                        Hint {i + 1}
                      </span>
                    </button>
                    {expandedHints.has(i) && (
                      <div className="px-3 pb-3 pt-0">
                        <p className="text-sm text-[#8b949e] pl-6">{hint}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right panel - code editor */}
        <div className="w-3/5 flex flex-col gap-3 min-h-0">
          <div className="flex-1 min-h-0 card !p-0 overflow-hidden">
            <CodeEditor
              value={code}
              onChange={setCode}
              language={challenge.challengeType === 'SQL' ? 'sql' : 'java'}
            />
          </div>
          <div className="flex gap-2 self-end">
            <button
              onClick={handleRunTests}
              disabled={submitting}
              className="btn-secondary flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Play className="w-4 h-4" />
              {submitting ? 'Running...' : 'Run Tests'}
            </button>
            <button
              onClick={handleSubmitForReview}
              disabled={submitting}
              className="btn-primary flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Sparkles className="w-4 h-4" />
              Submit for AI Review
            </button>
          </div>
          {error && (
            <p className="text-sm text-red-400">{error}</p>
          )}
        </div>
      </div>

      {/* Bottom collapsible section */}
      {(testResults.length > 0 || submission?.aiReview) && (
        <div className="border-t border-white/[0.06]">
          <button
            onClick={() => setBottomExpanded(!bottomExpanded)}
            className="flex items-center gap-2 py-2 text-sm text-[#8b949e] hover:text-[#f0f6fc] transition-colors"
          >
            {bottomExpanded ? (
              <ChevronDown className="w-4 h-4" />
            ) : (
              <ChevronRight className="w-4 h-4" />
            )}
            Results & Feedback
          </button>
          {bottomExpanded && (
            <div className="flex gap-4 pb-4">
              <div className="flex-1">
                <TestResultPanel results={testResults} />
              </div>
              <div className="flex-1">
                <AiFeedbackPanel
                  feedback={submission?.aiReview ?? null}
                  loading={submitting}
                />
              </div>
            </div>
          )}
        </div>
      )}

      {/* Reference Solution — only visible after solving */}
      {challenge.referenceSolution && (
        <div className="border-t border-emerald-500/20">
          {!showSolution ? (
            <button
              onClick={() => setShowSolution(true)}
              className="flex items-center gap-2 py-3 text-sm text-emerald-400 hover:text-emerald-300 transition-colors"
            >
              <Eye className="w-4 h-4" />
              Show Optimal Solution
            </button>
          ) : (
            <div className="py-3 space-y-2">
              <div className="flex items-center gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-400" />
                <h3 className="text-sm font-semibold text-emerald-400">Optimal Solution</h3>
              </div>
              <div className="rounded-lg border border-emerald-500/20 bg-emerald-500/5 p-4 overflow-x-auto">
                <pre className="text-sm text-[#e6edf3] whitespace-pre-wrap font-mono leading-relaxed">
                  {challenge.referenceSolution}
                </pre>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ChallengeWorkspace;
