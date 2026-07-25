import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { PenTool, Clock } from 'lucide-react';
import { get } from '@/api/client';
import { DesignExercise, Difficulty, SpringPage } from '@/types';
import { DifficultyBadge } from '@/components/DifficultyBadge';

const DIFFICULTY_OPTIONS: Array<{ label: string; value: Difficulty | null }> = [
  { label: 'All', value: null },
  { label: 'Easy', value: Difficulty.EASY },
  { label: 'Medium', value: Difficulty.MEDIUM },
  { label: 'Hard', value: Difficulty.HARD },
];

export function DesignExerciseList() {
  const [exercises, setExercises] = useState<DesignExercise[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeDifficulty, setActiveDifficulty] = useState<Difficulty | null>(null);

  useEffect(() => {
    get<SpringPage<DesignExercise>>('/exercises?page=0&size=100')
      .then((res) => setExercises(res.content ?? []))
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load exercises'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = exercises.filter((e) => {
    if (activeDifficulty && e.difficulty !== activeDifficulty) return false;
    return true;
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-pulse flex items-center gap-3 text-[#8b949e]">
          <PenTool className="w-5 h-5" />
          <span>Loading exercises...</span>
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
        <PenTool className="w-6 h-6 text-amber-400" />
        <h1 className="text-2xl font-bold text-[#f0f6fc]">Design Exercises</h1>
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
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-16 text-[#8b949e]">
          <PenTool className="w-10 h-10 mx-auto mb-3 text-[#484f58]" />
          <p>No design exercises match your filter.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((exercise) => (
            <Link
              key={exercise.id}
              to={`/exercises/${exercise.id}`}
              className="card hover:border-amber-500/30 transition-colors"
            >
              <div className="flex items-start justify-between mb-3">
                <h3 className="text-[#f0f6fc] font-medium">{exercise.title}</h3>
                <DifficultyBadge difficulty={exercise.difficulty} />
              </div>
              {exercise.estimatedMinutes && (
                <div className="flex items-center gap-1.5 text-[#484f58] text-sm">
                  <Clock className="w-3.5 h-3.5" />
                  <span>{exercise.estimatedMinutes} min</span>
                </div>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

export default DesignExerciseList;
