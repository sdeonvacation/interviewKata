import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '@/api/client';
import { Topic } from '@/types';
import {
  GraduationCap,
  ChevronRight,
  ChevronDown,
  History,
  Loader2,
  BookOpen,
} from 'lucide-react';

const AREA_LABELS: Record<string, string> = {
  JAVA_CORE: 'Java Core',
  SPRING_BOOT: 'Spring Boot',
  SYSTEM_DESIGN: 'System Design',
  DSA: 'Data Structures & Algorithms',
  DATABASE: 'Database',
  ARCHITECTURE: 'Architecture',
  BEHAVIORAL: 'Behavioral',
};

function TopicRow({
  topic,
  depth,
  onStudy,
}: {
  topic: Topic;
  depth: number;
  onStudy: (id: string) => void;
}) {
  const [expanded, setExpanded] = useState(false);
  const [children, setChildren] = useState<Topic[] | null>(null);
  const [loading, setLoading] = useState(false);

  const hasChildren = topic.childCount > 0;

  const toggle = async () => {
    if (!hasChildren) return;
    if (!expanded && children === null) {
      setLoading(true);
      try {
        const data = await get<Topic[]>(`/topics/${topic.id}/children`);
        setChildren(data);
      } catch {
        setChildren([]);
      } finally {
        setLoading(false);
      }
    }
    setExpanded((v) => !v);
  };

  return (
    <div>
      <div
        className="flex items-center justify-between py-2.5 px-3 rounded-lg hover:bg-[#161b22] transition-colors"
        style={{ paddingLeft: `${12 + depth * 20}px` }}
      >
        <button
          onClick={toggle}
          className={`flex items-center gap-2 flex-1 text-left ${hasChildren ? 'cursor-pointer' : 'cursor-default'}`}
        >
          {hasChildren ? (
            loading ? (
              <Loader2 className="w-4 h-4 text-[#484f58] animate-spin shrink-0" />
            ) : expanded ? (
              <ChevronDown className="w-4 h-4 text-[#484f58] shrink-0" />
            ) : (
              <ChevronRight className="w-4 h-4 text-[#484f58] shrink-0" />
            )
          ) : (
            <span className="w-4 shrink-0" />
          )}
          <span className="text-[#f0f6fc] text-sm">{topic.name}</span>
          {topic.cardCount > 0 && (
            <span className="text-[#484f58] text-xs">{topic.cardCount} cards</span>
          )}
        </button>
        <button
          onClick={() => onStudy(topic.id)}
          className="flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-medium bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 transition-colors shrink-0"
          title="Study this topic with the AI tutor"
        >
          <GraduationCap className="w-3.5 h-3.5" />
          Study
        </button>
      </div>
      {expanded && children && children.length > 0 && (
        <div>
          {children.map((child) => (
            <TopicRow key={child.id} topic={child} depth={depth + 1} onStudy={onStudy} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function StudyLanding() {
  const navigate = useNavigate();
  const [topics, setTopics] = useState<Topic[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    get<Topic[]>('/topics')
      .then(setTopics)
      .finally(() => setLoading(false));
  }, []);

  const grouped = topics.reduce<Record<string, Topic[]>>((acc, t) => {
    (acc[t.area] ??= []).push(t);
    return acc;
  }, {});

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-emerald-500/10 flex items-center justify-center">
            <GraduationCap className="w-6 h-6 text-emerald-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-[#f0f6fc]">Study & Learn</h1>
            <p className="text-sm text-[#8b949e] mt-0.5">
              Pick any topic and learn interactively with an AI tutor.
            </p>
          </div>
        </div>
        <button
          onClick={() => navigate('/study/history')}
          className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-[#8b949e] hover:text-[#f0f6fc] hover:bg-[#161b22] transition-colors"
        >
          <History className="w-4 h-4" />
          History
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 className="w-6 h-6 text-emerald-400 animate-spin" />
        </div>
      ) : topics.length === 0 ? (
        <div className="card flex flex-col items-center py-16 text-center">
          <BookOpen className="w-8 h-8 text-[#484f58] mb-3" />
          <p className="text-[#8b949e] text-sm">No topics available yet.</p>
        </div>
      ) : (
        <div className="space-y-5">
          {Object.entries(grouped).map(([area, areaTopics]) => (
            <div key={area} className="card">
              <h2 className="text-xs font-semibold uppercase tracking-wide text-[#8b949e] mb-3 px-3">
                {AREA_LABELS[area] ?? area}
              </h2>
              <div className="space-y-0.5">
                {areaTopics.map((topic) => (
                  <TopicRow
                    key={topic.id}
                    topic={topic}
                    depth={0}
                    onStudy={(id) => navigate(`/study/${id}`)}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
