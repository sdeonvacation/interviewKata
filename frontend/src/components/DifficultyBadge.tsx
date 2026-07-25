import { Difficulty } from '@/types';

interface DifficultyBadgeProps {
  difficulty: Difficulty;
}

const difficultyStyles: Record<Difficulty, string> = {
  [Difficulty.EASY]: 'bg-emerald-500/10 text-emerald-400',
  [Difficulty.MEDIUM]: 'bg-amber-500/10 text-amber-400',
  [Difficulty.HARD]: 'bg-rose-500/10 text-rose-400',
};

export function DifficultyBadge({ difficulty }: DifficultyBadgeProps) {
  return (
    <span
      className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium uppercase tracking-wide ${difficultyStyles[difficulty]}`}
    >
      {difficulty}
    </span>
  );
}
