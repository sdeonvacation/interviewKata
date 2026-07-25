import { Flame } from 'lucide-react';

interface StreakBadgeProps {
  streak: number;
}

export function StreakBadge({ streak }: StreakBadgeProps) {
  return (
    <div
      className={`inline-flex items-center gap-2 ${
        streak > 0 ? 'shadow-lg shadow-amber-500/20' : ''
      } ${streak >= 7 ? 'animate-pulse' : ''}`}
    >
      <Flame className="h-5 w-5 text-amber-400" />
      <span className="text-[#f0f6fc] font-bold text-lg">{streak}</span>
    </div>
  );
}
