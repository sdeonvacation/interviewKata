import { useState, useEffect } from 'react';
import { get } from '@/api/client';
import { UserProgress, TopicArea } from '@/types';
import { StreakBadge } from '@/components/StreakBadge';
import { Award, TrendingUp, Target } from 'lucide-react';

interface StreakData {
  currentStreak: number;
  longestStreak: number;
}

const AREA_LABELS: Record<TopicArea, string> = {
  [TopicArea.JAVA_CORE]: 'Java Core',
  [TopicArea.SPRING_BOOT]: 'Spring Boot',
  [TopicArea.SYSTEM_DESIGN]: 'System Design',
  [TopicArea.DSA]: 'DSA',
  [TopicArea.DATABASE]: 'Database',
  [TopicArea.ARCHITECTURE]: 'Architecture',
  [TopicArea.BEHAVIORAL]: 'Behavioral',
};

const WEAK_THRESHOLD = 50;

export function Progress() {
  const [topicProgress, setTopicProgress] = useState<UserProgress[]>([]);
  const [streakData, setStreakData] = useState<StreakData>({ currentStreak: 0, longestStreak: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      get<UserProgress[]>('/progress').catch(() => [] as UserProgress[]),
      get<StreakData>('/progress/streak').catch(() => ({ currentStreak: 0, longestStreak: 0 })),
    ])
      .then(([progress, streak]) => {
        setTopicProgress(progress ?? []);
        // Handle case where streak might be a number or an object
        if (typeof streak === 'number') {
          setStreakData({ currentStreak: streak, longestStreak: streak });
        } else {
          setStreakData(streak ?? { currentStreak: 0, longestStreak: 0 });
        }
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load progress'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-amber-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-[#8b949e] text-sm">Loading progress...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card border-rose-500/30">
        <p className="text-rose-400 text-sm">{error}</p>
      </div>
    );
  }

  // Compute derived values
  const totalMastered = topicProgress.reduce((s, t) => s + (t.cardsMastered ?? 0), 0);

  // Group topics by topicId (flat list, no area grouping since UserProgress doesn't have area)
  const areaGroups: Record<string, UserProgress[]> = { ALL: topicProgress };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-[#f0f6fc]">Progress</h1>
        <div className="flex items-center gap-2 text-[#8b949e] text-sm">
          <TrendingUp className="w-4 h-4" />
          <span>Longest streak: {streakData.longestStreak} days</span>
        </div>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="card flex items-center gap-4">
          <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center">
            <Award className="w-5 h-5 text-amber-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-amber-400">{totalMastered}</p>
            <p className="text-xs text-[#8b949e]">Cards Mastered</p>
          </div>
        </div>

        <div className="card flex items-center gap-4">
          <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center">
            <Target className="w-5 h-5 text-emerald-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-emerald-400">{topicProgress.length}</p>
            <p className="text-xs text-[#8b949e]">Topics Studied</p>
          </div>
        </div>

        <div className="card flex items-center justify-center">
          <StreakBadge streak={streakData.currentStreak} />
        </div>
      </div>

      {/* Per-topic progress by area */}
      <div className="card">
        <h2 className="text-lg font-semibold text-[#f0f6fc] mb-5">Topic Mastery</h2>
        <div className="space-y-6">
          {Object.entries(areaGroups).map(([area, topics]) => {
            const totalCards = topics.reduce((s, t) => s + (t.cardsTotal ?? 0), 0);
            const masteredCards = topics.reduce((s, t) => s + (t.cardsMastered ?? 0), 0);
            const pct = totalCards > 0 ? (masteredCards / totalCards) * 100 : 0;
            const isWeak = pct < WEAK_THRESHOLD;

            return (
              <div
                key={area}
                className={`space-y-2 ${isWeak ? 'border-l-2 border-rose-500 pl-3' : ''}`}
              >
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-[#f0f6fc]">
                    {AREA_LABELS[area as TopicArea] ?? area}
                  </span>
                  <span className="text-xs text-[#8b949e]">
                    {masteredCards}/{totalCards} ({Math.round(pct)}%)
                  </span>
                </div>
                <div className="h-2 rounded-full bg-[#161b22]">
                  <div
                    className="h-2 rounded-full bg-amber-500 transition-all duration-500"
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>

        {Object.keys(areaGroups).length === 0 && (
          <p className="text-[#484f58] text-sm">No topic progress recorded yet.</p>
        )}
      </div>
    </div>
  );
}

export default Progress;
