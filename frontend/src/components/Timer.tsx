import { ProgressRing } from './ProgressRing';

interface TimerProps {
  timeLeft: number;
  totalTime: number;
  isRunning: boolean;
}

export function Timer({ timeLeft, totalTime }: TimerProps) {
  const progress = ((totalTime - timeLeft) / totalTime) * 100;
  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  const timeDisplay = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  const urgent = timeLeft < 60;

  return (
    <div className="relative inline-flex items-center justify-center">
      <ProgressRing progress={progress} />
      <span
        className={`absolute font-mono text-2xl font-bold ${
          urgent ? 'text-rose-400 animate-pulse' : 'text-amber-400'
        }`}
      >
        {timeDisplay}
      </span>
    </div>
  );
}
