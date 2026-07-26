import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get, del } from '@/api/client';
import { StudySessionSummary } from '@/types';
import { BookOpen, Clock, MessageSquare, Loader2, Trash2 } from 'lucide-react';

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

export function StudyHistory() {
  const navigate = useNavigate();
  const [sessions, setSessions] = useState<StudySessionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<string | null>(null);
  const [activeTag, setActiveTag] = useState<string | null>(null);

  useEffect(() => {
    get<StudySessionSummary[]>('/study/sessions')
      .then((data) => setSessions(data))
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load study history'))
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    setDeleting(id);
    try {
      await del(`/study/sessions/${id}`);
      setSessions((prev) => prev.filter((s) => s.id !== id));
    } catch {
      // keep the card; brief noop
    } finally {
      setDeleting(null);
    }
  };

  // Distinct topic tags present in history, sorted.
  const tags = Array.from(new Set(sessions.map((s) => s.topicArea))).sort();
  const visible = activeTag ? sessions.filter((s) => s.topicArea === activeTag) : sessions;

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
      <div className="flex items-center gap-3">
        <BookOpen className="w-6 h-6 text-amber-400" />
        <h1 className="text-2xl font-bold text-[#f0f6fc]">Study History</h1>
      </div>

      {/* Tag filter */}
      {tags.length > 1 && (
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setActiveTag(null)}
            className={`px-3 py-1 text-xs rounded-full border transition-colors ${
              activeTag === null
                ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                : 'bg-[#161b22] text-[#8b949e] border-white/[0.06] hover:border-amber-500/30'
            }`}
          >
            All
          </button>
          {tags.map((tag) => (
            <button
              key={tag}
              onClick={() => setActiveTag(tag)}
              className={`px-3 py-1 text-xs rounded-full border transition-colors ${
                activeTag === tag
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                  : 'bg-[#161b22] text-[#8b949e] border-white/[0.06] hover:border-emerald-500/30'
              }`}
            >
              {tag}
            </button>
          ))}
        </div>
      )}

      {sessions.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <BookOpen className="w-10 h-10 text-[#484f58] mb-3" />
          <p className="text-[#8b949e]">No study sessions yet.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {visible.map((session) => (
            <div
              key={session.id}
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/study/session/${session.id}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/study/session/${session.id}`); }}
              className="block bg-[#161b22] rounded-xl px-5 py-4 border border-white/[0.06] hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5 transition-all cursor-pointer"
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <h3 className="text-[#f0f6fc] font-semibold leading-tight">
                  {session.topicName}
                </h3>
                <div className="flex items-center gap-2 shrink-0">
                  <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    {session.topicArea}
                  </span>
                  <button
                    onClick={(e) => { e.stopPropagation(); handleDelete(session.id); }}
                    disabled={deleting === session.id}
                    title="Delete session"
                    className="p-1 rounded-md text-[#8b949e] hover:text-red-400 hover:bg-red-500/10 transition-colors disabled:opacity-40"
                  >
                    {deleting === session.id ? (
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                      <Trash2 className="w-3.5 h-3.5" />
                    )}
                  </button>
                </div>
              </div>

              {session.preview && (
                <p className="text-sm text-[#8b949e] line-clamp-2 mb-3">
                  {session.preview}
                </p>
              )}

              <div className="flex items-center gap-4 text-xs text-[#8b949e]">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5" />
                  {formatDate(session.lastActivityAt)}
                </span>
                <span className="flex items-center gap-1.5">
                  <MessageSquare className="w-3.5 h-3.5" />
                  {session.messageCount} messages
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default StudyHistory;
