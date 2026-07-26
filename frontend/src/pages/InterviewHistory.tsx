import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get, del } from '@/api/client';
import { InterviewSummary } from '@/types';
import { Mic, Clock, MessageSquare, Loader2, Trash2, Award, Plus } from 'lucide-react';

const TOPIC_LABELS: Record<string, string> = {
  JAVA_CORE: 'Java Core',
  SPRING_BOOT: 'Spring Boot',
  SYSTEM_DESIGN: 'System Design',
  DSA: 'DSA',
  DATABASE: 'Database',
  ARCHITECTURE: 'Architecture',
  BEHAVIORAL: 'Behavioral',
};

const STATE_STYLES: Record<string, string> = {
  COMPLETE: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  ASKING: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  EVALUATING: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
};

function formatDate(iso: string): string {
  const date = new Date(iso);
  if (isNaN(date.getTime())) return iso;
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function InterviewHistory() {
  const navigate = useNavigate();
  const [interviews, setInterviews] = useState<InterviewSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<string | null>(null);

  useEffect(() => {
    get<InterviewSummary[]>('/interviews')
      .then((data) => setInterviews(data))
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load interview history'))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    setDeleting(id);
    try {
      await del(`/interviews/${id}`);
      setInterviews((prev) => prev.filter((i) => i.id !== id));
    } catch {
      // keep the card
    } finally {
      setDeleting(null);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-6 h-6 text-amber-400 animate-spin" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="card border-red-500/30">
        <p className="text-red-400">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Mic className="w-6 h-6 text-amber-400" />
          <h1 className="text-2xl font-bold text-[#f0f6fc]">Interview History</h1>
        </div>
        <button
          onClick={() => navigate('/interviews/new')}
          className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 transition-colors"
        >
          <Plus className="w-4 h-4" />
          New Interview
        </button>
      </div>

      {interviews.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <Mic className="w-10 h-10 text-[#484f58] mb-3" />
          <p className="text-[#8b949e]">No interviews yet. Start one to see it here.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {interviews.map((interview) => (
            <div
              key={interview.id}
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/interviews/${interview.id}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/interviews/${interview.id}`); }}
              className="block bg-[#161b22] rounded-xl px-5 py-4 border border-white/[0.06] hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5 transition-all cursor-pointer"
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <h3 className="text-[#f0f6fc] font-semibold leading-tight">
                  {TOPIC_LABELS[interview.topicArea] ?? interview.topicArea}
                  <span className="text-[#8b949e] font-normal text-sm ml-2">
                    {interview.difficulty}
                  </span>
                </h3>
                <div className="flex items-center gap-2 shrink-0">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium border ${STATE_STYLES[interview.state] ?? 'bg-white/5 text-[#8b949e] border-white/10'}`}>
                    {interview.state}
                  </span>
                  <button
                    onClick={(e) => { e.stopPropagation(); handleDelete(interview.id); }}
                    disabled={deleting === interview.id}
                    title="Delete interview"
                    className="p-1 rounded-md text-[#8b949e] hover:text-red-400 hover:bg-red-500/10 transition-colors disabled:opacity-40"
                  >
                    {deleting === interview.id ? (
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                      <Trash2 className="w-3.5 h-3.5" />
                    )}
                  </button>
                </div>
              </div>

              <div className="flex items-center gap-4 text-xs text-[#8b949e]">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5" />
                  {formatDate(interview.startedAt)}
                </span>
                <span className="flex items-center gap-1.5">
                  <MessageSquare className="w-3.5 h-3.5" />
                  {interview.turnCount} turns
                </span>
                {interview.overallScore != null && (
                  <span className="flex items-center gap-1.5">
                    <Award className="w-3.5 h-3.5" />
                    {interview.overallScore}/10
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default InterviewHistory;
