import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get, post } from '@/api/client';
import { Topic, TopicArea, Difficulty, MockInterview } from '@/types';
import {
  Users,
  BookOpen,
  Play,
  Award,
  Target,
  MessageSquare,
  Lightbulb,
  Shield,
  Heart,
  Brain,
  RefreshCw,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

interface BehavioralCategory {
  name: string;
  icon: LucideIcon;
  description: string;
  topicId: string | null;
  cardCount: number;
}

const CATEGORY_ICONS: Record<string, LucideIcon> = {
  Leadership: Award,
  'Conflict Resolution': Shield,
  Teamwork: Heart,
  'Problem Solving': Brain,
  Communication: MessageSquare,
  Adaptability: RefreshCw,
};

const CATEGORY_DESCRIPTIONS: Record<string, string> = {
  Leadership: 'Taking initiative, mentoring, driving decisions',
  'Conflict Resolution': 'Disagreements, code review conflicts, feedback',
  Teamwork: 'Collaboration, cross-team projects, remote work',
  'Problem Solving': 'Debugging, tight deadlines, ambiguous requirements',
  Communication: 'Explaining tech, giving feedback, stakeholder management',
  Adaptability: 'Learning new tech, handling failure, changing priorities',
};

export default function BehavioralPage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<BehavioralCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [startingInterview, setStartingInterview] = useState(false);

  useEffect(() => {
    loadCategories();
  }, []);

  async function loadCategories() {
    try {
      const topics = await get<Topic[]>('/topics?area=BEHAVIORAL');
      const cats: BehavioralCategory[] = topics
        .filter((t) => t.parentId !== null)
        .map((t) => ({
          name: t.name,
          icon: CATEGORY_ICONS[t.name] || Target,
          description: CATEGORY_DESCRIPTIONS[t.name] || '',
          topicId: t.id,
          cardCount: t.cardCount,
        }));
      setCategories(cats);
    } catch {
      // Fallback with static categories if API fails
      setCategories(
        Object.entries(CATEGORY_DESCRIPTIONS).map(([name, description]) => ({
          name,
          icon: CATEGORY_ICONS[name] || Target,
          description,
          topicId: null,
          cardCount: 0,
        }))
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleStartInterview() {
    setStartingInterview(true);
    try {
      const interview = await post<MockInterview>('/interviews/start', {
        topicArea: TopicArea.BEHAVIORAL,
        difficulty: Difficulty.MEDIUM,
      });
      navigate(`/interviews/${interview.id}`);
    } catch {
      setStartingInterview(false);
    }
  }

  function handleReviewCards(topicId: string | null) {
    if (topicId) {
      navigate(`/review?topicId=${topicId}`);
    } else {
      navigate('/review');
    }
  }

  const totalCards = categories.reduce((sum, c) => sum + c.cardCount, 0);

  return (
    <div className="min-h-screen p-6 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-violet-500/10">
            <Users className="w-6 h-6 text-violet-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-[#f0f6fc]">
              Behavioral Interview Prep
            </h1>
            <p className="text-sm text-[#8b949e]">
              Master the STAR method for behavioral interviews
            </p>
          </div>
        </div>
      </div>

      {/* STAR Method Overview */}
      <div className="card p-5 border border-violet-500/20 bg-violet-500/5">
        <div className="flex items-start gap-3">
          <Lightbulb className="w-5 h-5 text-violet-400 mt-0.5 shrink-0" />
          <div>
            <h3 className="text-sm font-semibold text-violet-300 mb-1">
              STAR Method
            </h3>
            <p className="text-xs text-[#8b949e] leading-relaxed">
              <strong className="text-[#c9d1d9]">Situation</strong> — Set the
              context.{' '}
              <strong className="text-[#c9d1d9]">Task</strong> — Your specific
              responsibility.{' '}
              <strong className="text-[#c9d1d9]">Action</strong> — What YOU did
              (not the team).{' '}
              <strong className="text-[#c9d1d9]">Result</strong> — Measurable
              outcome.
            </p>
          </div>
        </div>
      </div>

      {/* Action Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Review STAR Cards */}
        <div className="card p-5 space-y-3">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-emerald-500/10">
              <BookOpen className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-[#f0f6fc]">
                STAR Method Cards
              </h2>
              <p className="text-xs text-[#8b949e]">
                {totalCards} flashcards across {categories.length} categories
              </p>
            </div>
          </div>
          <p className="text-sm text-[#8b949e]">
            Review common behavioral questions with structured STAR guidance and
            German tech interview cultural notes.
          </p>
          <button
            onClick={() => handleReviewCards(null)}
            className="w-full px-4 py-2 rounded-lg bg-emerald-500/10 text-emerald-400 text-sm font-medium
              hover:bg-emerald-500/20 transition-colors border border-emerald-500/20"
          >
            Review All Cards
          </button>
        </div>

        {/* Practice Interview */}
        <div className="card p-5 space-y-3">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-amber-500/10">
              <Play className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-[#f0f6fc]">
                Practice Behavioral Interview
              </h2>
              <p className="text-xs text-[#8b949e]">
                AI mock interview with STAR probing
              </p>
            </div>
          </div>
          <p className="text-sm text-[#8b949e]">
            Practice answering behavioral questions. The AI interviewer will
            probe for specific details using the STAR method.
          </p>
          <button
            onClick={handleStartInterview}
            disabled={startingInterview}
            className="w-full px-4 py-2 rounded-lg bg-amber-500/10 text-amber-400 text-sm font-medium
              hover:bg-amber-500/20 transition-colors border border-amber-500/20
              disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {startingInterview ? 'Starting...' : 'Start Mock Interview'}
          </button>
        </div>
      </div>

      {/* Category Grid */}
      <div>
        <h2 className="text-lg font-semibold text-[#f0f6fc] mb-4">
          Categories
        </h2>
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <div
                key={i}
                className="card p-4 animate-pulse h-28 bg-surface-2/50"
              />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {categories.map((category) => {
              const Icon = category.icon;
              return (
                <div
                  key={category.name}
                  className="card p-4 space-y-2 hover:border-violet-500/30 transition-colors cursor-pointer"
                  onClick={() => handleReviewCards(category.topicId)}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Icon className="w-4 h-4 text-violet-400" />
                      <span className="text-sm font-medium text-[#f0f6fc]">
                        {category.name}
                      </span>
                    </div>
                    <span className="text-xs text-[#8b949e] bg-surface-2 px-2 py-0.5 rounded">
                      {category.cardCount} cards
                    </span>
                  </div>
                  <p className="text-xs text-[#8b949e]">
                    {category.description}
                  </p>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleReviewCards(category.topicId);
                    }}
                    className="text-xs text-violet-400 hover:text-violet-300 transition-colors"
                  >
                    Practice →
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
