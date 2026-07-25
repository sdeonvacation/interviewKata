import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Challenge, ChallengeType, Difficulty, DailyRecommendation, SpringPage } from '@/types';
import { get } from '@/api/client';
import { DifficultyBadge } from '@/components/DifficultyBadge';
import { Zap, Check, RotateCcw } from 'lucide-react';

type StatusFilter = 'all' | 'due-review';

const TYPE_OPTIONS: Array<{ label: string; value: ChallengeType | null }> = [
  { label: 'All', value: null },
  { label: 'DSA', value: ChallengeType.DSA },
  { label: 'Java', value: ChallengeType.JAVA },
  { label: 'SQL', value: ChallengeType.SQL },
];

const DIFFICULTY_OPTIONS: Array<{ label: string; value: Difficulty | null }> = [
  { label: 'All', value: null },
  { label: 'Easy', value: Difficulty.EASY },
  { label: 'Medium', value: Difficulty.MEDIUM },
  { label: 'Hard', value: Difficulty.HARD },
];

export function ChallengeList() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [revisionIds, setRevisionIds] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeType, setActiveType] = useState<ChallengeType | null>(null);
  const [activeDifficulty, setActiveDifficulty] = useState<Difficulty | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');

  useEffect(() => {
    Promise.all([
      get<SpringPage<Challenge>>('/challenges?page=0&size=200'),
      get<DailyRecommendation>('/dashboard/recommendations'),
    ])
      .then(([challengeRes, recRes]) => {
        setChallenges(challengeRes.content ?? []);
        const ids = new Set((recRes.revisionChallenges ?? []).map((c) => c.id));
        setRevisionIds(ids);
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load challenges'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = challenges.filter((c) => {
    if (activeType && c.challengeType !== activeType) return false;
    if (activeDifficulty && c.difficulty !== activeDifficulty) return false;
    if (statusFilter === 'due-review' && !revisionIds.has(c.id)) return false;
    return true;
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="flex flex-col items-center gap-3">
          <Zap className="w-8 h-8 text-amber-400 animate-pulse" />
          <p className="text-[#8b949e]">Loading challenges...</p>
        </div>
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
        <Zap className="w-6 h-6 text-amber-400" />
        <h1 className="text-2xl font-bold text-[#f0f6fc]">Coding Dojo</h1>
      </div>

      <div className="space-y-3">
        <div className="flex flex-wrap gap-2">
          {TYPE_OPTIONS.map((opt) => (
            <button
              key={opt.label}
              onClick={() => setActiveType(opt.value)}
              className={`px-3 py-1.5 text-sm rounded-full border transition-colors ${
                activeType === opt.value
                  ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                  : 'bg-[#161b22] text-[#8b949e] border-white/[0.06] hover:border-amber-500/30'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>

        <div className="flex flex-wrap gap-2">
          {DIFFICULTY_OPTIONS.map((opt) => (
            <button
              key={opt.label}
              onClick={() => setActiveDifficulty(opt.value)}
              className={`px-3 py-1.5 text-sm rounded-full border transition-colors ${
                activeDifficulty === opt.value
                  ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                  : 'bg-[#161b22] text-[#8b949e] border-white/[0.06] hover:border-amber-500/30'
              }`}
            >
              {opt.label}
            </button>
          ))}

          <span className="mx-2 border-l border-white/[0.06]" />

          <button
            onClick={() => setStatusFilter(statusFilter === 'due-review' ? 'all' : 'due-review')}
            className={`px-3 py-1.5 text-sm rounded-full border transition-colors flex items-center gap-1.5 ${
              statusFilter === 'due-review'
                ? 'bg-orange-500/10 text-orange-400 border-orange-500/30'
                : 'bg-[#161b22] text-[#8b949e] border-white/[0.06] hover:border-orange-500/30'
            }`}
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Due for Review
          </button>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <Zap className="w-10 h-10 text-[#484f58] mb-3" />
          <p className="text-[#8b949e]">No challenges match your filters.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((challenge) => (
            <Link
              key={challenge.id}
              to={`/challenges/${challenge.id}`}
              className="card hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5 hover:-translate-y-0.5 transition-all"
            >
              <div className="flex items-start justify-between gap-2 mb-2">
                <h3 className="text-[#f0f6fc] font-medium leading-tight">
                  {challenge.title}
                </h3>
                <div className="flex items-center gap-2 shrink-0">
                  {challenge.solved && (
                    <Check className="w-4 h-4 text-emerald-400" />
                  )}
                  <DifficultyBadge difficulty={challenge.difficulty} />
                </div>
              </div>
              <div className="flex items-center gap-2 mt-3">
                <span className="text-xs bg-[#161b22] px-2 py-0.5 rounded-full text-[#8b949e]">
                  {challenge.challengeType}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

export default ChallengeList;
