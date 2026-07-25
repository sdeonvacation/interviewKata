import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Zap, Brain, Clock, CheckCircle2 } from 'lucide-react';
import { get } from '@/api/client';
import { DashboardData } from '@/types';
import { StreakBadge } from '@/components/StreakBadge';

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

function SkeletonCard() {
  return (
    <div className="card animate-pulse">
      <div className="h-4 w-24 bg-white/[0.06] rounded mb-3" />
      <div className="h-8 w-16 bg-white/[0.06] rounded mb-2" />
      <div className="h-3 w-32 bg-white/[0.06] rounded" />
    </div>
  );
}

export function Dashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = () => {
    setLoading(true);
    setError(null);
    get<DashboardData>('/dashboard')
      .then(setData)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load dashboard'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 bg-white/[0.06] rounded animate-pulse" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <SkeletonCard />
          <SkeletonCard />
          <SkeletonCard />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <SkeletonCard />
          <SkeletonCard />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-rose-400 text-sm">{error}</p>
        <button onClick={fetchDashboard} className="btn-secondary">
          Retry
        </button>
      </div>
    );
  }

  if (!data) return null;

  const allCaughtUp = data.dueCardCount === 0 && (!data.weakAreas || data.weakAreas.length === 0);

  return (
    <div className="space-y-8">
      {/* Welcome Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-[#f0f6fc]">
          {getGreeting()}, warrior
        </h1>
        <StreakBadge streak={data.currentStreak} />
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Link to="/review" className="card group hover:border-amber-500/30 transition-colors">
          <div className="flex items-center gap-2 mb-2">
            <BookOpen className="w-4 h-4 text-[#8b949e]" />
            <h3 className="text-sm text-[#8b949e]">Due Cards</h3>
          </div>
          <p className="text-3xl font-bold text-amber-400">{data.dueCardCount}</p>
          <p className="text-xs text-[#484f58] mt-1">Tap to start review</p>
        </Link>

        <div className="card">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle2 className="w-4 h-4 text-[#8b949e]" />
            <h3 className="text-sm text-[#8b949e]">Cards Today</h3>
          </div>
          <p className="text-3xl font-bold text-emerald-400">
            {data.todayActivity?.cardsReviewed ?? 0}
          </p>
          <p className="text-xs text-[#484f58] mt-1">reviewed today</p>
        </div>

        <div className="card">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="w-4 h-4 text-[#8b949e]" />
            <h3 className="text-sm text-[#8b949e]">Study Minutes</h3>
          </div>
          <p className="text-3xl font-bold text-sky-400">
            {data.todayActivity?.studyMinutes ?? 0}
          </p>
          <p className="text-xs text-[#484f58] mt-1">minutes today</p>
        </div>
      </div>

      {/* All Caught Up State */}
      {allCaughtUp && (
        <div className="card flex flex-col items-center justify-center py-10 gap-3">
          <CheckCircle2 className="w-10 h-10 text-emerald-400" />
          <p className="text-[#f0f6fc] font-medium">All caught up!</p>
          <p className="text-[#8b949e] text-sm">Your discipline is paying off.</p>
        </div>
      )}

      {/* Today's Focus */}
      {!allCaughtUp && (
        <section>
          <h2 className="text-lg font-semibold text-[#f0f6fc] mb-4">Today's Focus</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {data.dueCardCount > 0 && (
              <Link to="/review" className="card group hover:border-amber-500/30 transition-colors flex items-start gap-4">
                <div className="p-2 rounded-lg bg-amber-500/10">
                  <BookOpen className="w-5 h-5 text-amber-400" />
                </div>
                <div>
                  <h3 className="text-[#f0f6fc] font-medium">Review Flashcards</h3>
                  <p className="text-sm text-[#8b949e] mt-1">
                    {data.dueCardCount} card{data.dueCardCount !== 1 ? 's' : ''} due for review
                  </p>
                </div>
              </Link>
            )}

            {(data.todayActivity?.challengesSolved ?? 0) < 3 && (
              <Link to="/challenges" className="card group hover:border-amber-500/30 transition-colors flex items-start gap-4">
                <div className="p-2 rounded-lg bg-sky-500/10">
                  <Zap className="w-5 h-5 text-sky-400" />
                </div>
                <div>
                  <h3 className="text-[#f0f6fc] font-medium">Solve a Challenge</h3>
                  <p className="text-sm text-[#8b949e] mt-1">
                    {data.todayActivity?.challengesSolved ?? 0}/3 completed today
                  </p>
                </div>
              </Link>
            )}

            {data.weakAreas && data.weakAreas.length > 0 && (
              <Link to="/topics" className="card group hover:border-amber-500/30 transition-colors flex items-start gap-4">
                <div className="p-2 rounded-lg bg-purple-500/10">
                  <Brain className="w-5 h-5 text-purple-400" />
                </div>
                <div>
                  <h3 className="text-[#f0f6fc] font-medium">Strengthen Weak Areas</h3>
                  <p className="text-sm text-[#8b949e] mt-1">
                    {data.weakAreas.length} topic{data.weakAreas.length !== 1 ? 's' : ''} need attention
                  </p>
                </div>
              </Link>
            )}
          </div>
        </section>
      )}

      {/* Weak Areas */}
      {data.weakAreas && data.weakAreas.length > 0 && (
        <section>
          <h2 className="text-lg font-semibold text-[#f0f6fc] mb-4">Weak Areas</h2>
          <div className="flex flex-wrap gap-2">
            {data.weakAreas.map((area) => (
              <span
                key={area}
                className="px-3 py-1.5 text-sm rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20"
              >
                {area}
              </span>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

export default Dashboard;
