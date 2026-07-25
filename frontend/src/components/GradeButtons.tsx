import { RotateCcw, AlertTriangle, ThumbsUp, Smile, Star } from 'lucide-react';

interface GradeButtonsProps {
  onGrade: (grade: number) => void;
  disabled?: boolean;
}

const grades = [
  { grade: 1, label: 'Again', icon: RotateCcw, classes: 'bg-rose-500/10 text-rose-500 border-rose-500/20 hover:bg-rose-500/20' },
  { grade: 2, label: 'Hard', icon: AlertTriangle, classes: 'bg-orange-500/10 text-orange-500 border-orange-500/20 hover:bg-orange-500/20' },
  { grade: 3, label: 'Good', icon: ThumbsUp, classes: 'bg-amber-500/10 text-amber-500 border-amber-500/20 hover:bg-amber-500/20' },
  { grade: 4, label: 'Easy', icon: Smile, classes: 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20 hover:bg-emerald-500/20' },
  { grade: 5, label: 'Perfect', icon: Star, classes: 'bg-sky-500/10 text-sky-500 border-sky-500/20 hover:bg-sky-500/20' },
] as const;

export function GradeButtons({ onGrade, disabled }: GradeButtonsProps) {
  return (
    <div className="flex flex-row gap-2 flex-wrap justify-center">
      {grades.map(({ grade, label, icon: Icon, classes }) => (
        <button
          key={grade}
          onClick={() => onGrade(grade)}
          disabled={disabled}
          className={`rounded-full px-4 py-2 flex items-center gap-1.5 border transition-all ${classes} ${
            disabled ? 'opacity-50 cursor-not-allowed' : ''
          }`}
        >
          <Icon className="w-4 h-4" />
          <span className="text-sm">{label}</span>
        </button>
      ))}
    </div>
  );
}
