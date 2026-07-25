import { RotateCcw } from 'lucide-react';

interface FlashCardProps {
  front: string;
  back: string;
  isFlipped: boolean;
  onFlip: () => void;
}

export function FlashCard({ front, back, isFlipped, onFlip }: FlashCardProps) {
  return (
    <div style={{ perspective: '1000px' }} className="w-full">
      <div
        onClick={onFlip}
        className="relative min-h-[280px] cursor-pointer transition-transform duration-500"
        style={{ transformStyle: 'preserve-3d', transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)' }}
      >
        {/* Front */}
        <div
          className="card absolute inset-0 min-h-[280px] flex flex-col justify-center items-center
            hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5 hover:-translate-y-[2px]
            transition-all"
          style={{ backfaceVisibility: 'hidden' }}
        >
          <p className="text-lg font-medium text-[#f0f6fc] text-center whitespace-pre-wrap">
            {front}
          </p>
          <div className="absolute bottom-4 right-4 text-[#484f58]">
            <RotateCcw size={16} />
          </div>
        </div>

        {/* Back */}
        <div
          className="card absolute inset-0 min-h-[280px] flex flex-col justify-center
            hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5 hover:-translate-y-[2px]
            transition-all"
          style={{ backfaceVisibility: 'hidden', transform: 'rotateY(180deg)' }}
        >
          <div className="text-[#8b949e] text-sm whitespace-pre-wrap">
            {back.split(/(```[\s\S]*?```)/g).map((segment, i) => {
              if (segment.startsWith('```') && segment.endsWith('```')) {
                const code = segment.replace(/^```\w*\n?/, '').replace(/\n?```$/, '');
                return (
                  <pre key={i} className="bg-[#0d1117] rounded-lg p-3 font-mono text-sm overflow-x-auto my-2">
                    {code}
                  </pre>
                );
              }
              return <span key={i}>{segment}</span>;
            })}
          </div>
          <div className="absolute bottom-4 right-4 text-[#484f58]">
            <RotateCcw size={16} />
          </div>
        </div>
      </div>
    </div>
  );
}
