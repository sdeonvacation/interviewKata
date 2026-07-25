import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '@/api/client';
import { Topic, TopicArea } from '@/types';
import { TopicTree } from '@/components/TopicTree';
import {
  BookOpen,
  Code2,
  Layers,
  Network,
  Binary,
  Database,
  Building2,
  Users,
  Loader2,
  AlertCircle,
} from 'lucide-react';

const AREA_CONFIG: Record<TopicArea, { icon: typeof Layers; label: string }> = {
  [TopicArea.JAVA_CORE]: { icon: Code2, label: 'Java Core' },
  [TopicArea.SPRING_BOOT]: { icon: Layers, label: 'Spring Boot' },
  [TopicArea.SYSTEM_DESIGN]: { icon: Network, label: 'System Design' },
  [TopicArea.DSA]: { icon: Binary, label: 'DSA' },
  [TopicArea.DATABASE]: { icon: Database, label: 'Database' },
  [TopicArea.ARCHITECTURE]: { icon: Building2, label: 'Architecture' },
  [TopicArea.BEHAVIORAL]: { icon: Users, label: 'Behavioral' },
};

function groupByArea(topics: Topic[]): Record<string, Topic[]> {
  const groups: Record<string, Topic[]> = {};
  for (const topic of topics) {
    if (!groups[topic.area]) groups[topic.area] = [];
    groups[topic.area].push(topic);
  }
  return groups;
}

export function TopicBrowser() {
  const navigate = useNavigate();
  const [topics, setTopics] = useState<Topic[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedArea, setSelectedArea] = useState<TopicArea | null>(null);
  const [children, setChildren] = useState<Topic[]>([]);
  const [childrenLoading, setChildrenLoading] = useState(false);

  useEffect(() => {
    get<Topic[]>('/topics')
      .then(setTopics)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load topics'))
      .finally(() => setLoading(false));
  }, []);

  const handleAreaClick = (area: TopicArea) => {
    if (selectedArea === area) {
      setSelectedArea(null);
      setChildren([]);
      return;
    }
    setSelectedArea(area);
    setChildren([]);

    // Find root topic for this area to fetch its children
    const rootTopic = topics.find((t) => t.area === area);
    if (!rootTopic) return;

    setChildrenLoading(true);
    get<Topic[]>(`/topics/${rootTopic.id}/children`)
      .then(setChildren)
      .catch(() => setChildren([]))
      .finally(() => setChildrenLoading(false));
  };

  const handleTopicSelect = (topic: Topic) => {
    navigate(`/review?topicId=${topic.id}`);
  };

  const handleCardsGenerated = (topicId: string, count: number) => {
    setChildren((prev) =>
      prev.map((t) =>
        t.id === topicId ? { ...t, cardCount: t.cardCount + count } : t
      )
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-6 h-6 text-amber-400 animate-spin" />
        <span className="ml-3 text-[#8b949e]">Loading topics...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card border-red-500/30">
        <div className="flex items-center gap-3">
          <AlertCircle className="w-5 h-5 text-red-400 shrink-0" />
          <p className="text-red-400">{error}</p>
        </div>
      </div>
    );
  }

  if (topics.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-[#484f58]">
        <BookOpen className="w-10 h-10 mb-3" />
        <p>No topics available yet.</p>
      </div>
    );
  }

  const grouped = groupByArea(topics);

  return (
    <div className="space-y-8">
      <div className="flex items-center gap-3">
        <BookOpen className="w-7 h-7 text-amber-400" />
        <h1 className="text-2xl font-bold text-[#f0f6fc]">Knowledge Tree</h1>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
        {(Object.keys(AREA_CONFIG) as TopicArea[]).map((area) => {
          const config = AREA_CONFIG[area];
          const Icon = config.icon;
          const areaTopics = grouped[area] || [];
          const totalCards = areaTopics.reduce((sum, t) => sum + t.cardCount, 0);
          const isSelected = selectedArea === area;

          return (
            <button
              key={area}
              onClick={() => handleAreaClick(area)}
              className={`card text-left transition-all duration-200 cursor-pointer ${
                isSelected
                  ? 'border-amber-500/30 ring-1 ring-amber-500/30'
                  : 'hover:border-amber-500/30'
              }`}
            >
              <div className="flex items-center gap-2 mb-3">
                <Icon className="w-5 h-5 text-amber-400" />
                <span className="text-sm font-medium text-[#f0f6fc]">{config.label}</span>
              </div>

              <div className="w-full h-1.5 rounded-full bg-white/[0.06] mb-2">
                <div
                  className="h-full rounded-full bg-emerald-500 transition-all duration-300"
                  style={{ width: '0%' }}
                />
              </div>

              <div className="flex items-center justify-between">
                <span className="text-xs text-[#8b949e]">
                  {totalCards} cards
                </span>
                <span className="text-xs text-[#484f58]">
                  {areaTopics.length} topics
                </span>
              </div>
            </button>
          );
        })}
      </div>

      {selectedArea && (
        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            {(() => {
              const Icon = AREA_CONFIG[selectedArea].icon;
              return <Icon className="w-5 h-5 text-amber-400" />;
            })()}
            <h2 className="text-lg font-semibold text-[#f0f6fc]">
              {AREA_CONFIG[selectedArea].label}
            </h2>
          </div>
          {childrenLoading ? (
            <div className="flex items-center gap-2 py-4">
              <Loader2 className="w-4 h-4 text-amber-400 animate-spin" />
              <span className="text-sm text-[#8b949e]">Loading subtopics...</span>
            </div>
          ) : (
            <TopicTree topics={children} onSelect={handleTopicSelect} onCardsGenerated={handleCardsGenerated} />
          )}
        </div>
      )}
    </div>
  );
}

export default TopicBrowser;
