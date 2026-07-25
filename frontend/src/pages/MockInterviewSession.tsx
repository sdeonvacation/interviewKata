import { useState, useRef, useEffect } from 'react';
import { useInterviewSession } from '@/hooks/useInterviewSession';
import { ChatBubble } from '@/components/ChatBubble';
import { Timer } from '@/components/Timer';
import { useTimer } from '@/hooks/useTimer';
import { TopicArea, Difficulty } from '@/types';
import { Send, Play, Square, Award, MessageSquare } from 'lucide-react';

const TOPIC_AREAS = Object.values(TopicArea);
const DIFFICULTIES = Object.values(Difficulty);

const TOPIC_LABELS: Record<TopicArea, string> = {
  [TopicArea.JAVA_CORE]: 'Java Core',
  [TopicArea.SPRING_BOOT]: 'Spring Boot',
  [TopicArea.SYSTEM_DESIGN]: 'System Design',
  [TopicArea.DSA]: 'DSA',
  [TopicArea.DATABASE]: 'Database',
  [TopicArea.ARCHITECTURE]: 'Architecture',
};

export function MockInterviewSession() {
  const { state, interview, turns, error, startInterview, sendMessage, endInterview } =
    useInterviewSession();
  const [input, setInput] = useState('');
  const [selectedTopic, setSelectedTopic] = useState<TopicArea>(TopicArea.DSA);
  const [selectedDifficulty, setSelectedDifficulty] = useState<Difficulty>(Difficulty.MEDIUM);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const DURATION_SECONDS = 30 * 60;
  const { timeLeft, isRunning, start: startTimer, stop: stopTimer } = useTimer(DURATION_SECONDS);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [turns]);

  useEffect(() => {
    if (state === 'active' && !isRunning) {
      startTimer();
    }
    if (state === 'complete' && isRunning) {
      stopTimer();
    }
  }, [state, isRunning, startTimer, stopTimer]);

  const handleSend = () => {
    if (!input.trim()) return;
    sendMessage(input.trim());
    setInput('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleStart = () => {
    startInterview(selectedTopic, selectedDifficulty);
  };

  // Idle state - setup card
  if (state === 'idle' || state === 'loading') {
    return (
      <div className="min-h-screen bg-[#06090f] flex items-center justify-center p-6">
        <div className="card max-w-lg w-full space-y-6">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-lg bg-amber-500/10">
              <MessageSquare className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-[#f0f6fc]">Mock Interview</h1>
              <p className="text-sm text-[#8b949e]">Practice with an AI interviewer</p>
            </div>
          </div>

          {/* Topic Area Selection */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-[#8b949e]">Topic Area</label>
            <div className="flex flex-wrap gap-2">
              {TOPIC_AREAS.map((topic) => (
                <button
                  key={topic}
                  onClick={() => setSelectedTopic(topic)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                    selectedTopic === topic
                      ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                      : 'bg-[#161b22] text-[#8b949e] border border-white/[0.06] hover:border-amber-500/30 hover:text-[#f0f6fc]'
                  }`}
                >
                  {TOPIC_LABELS[topic]}
                </button>
              ))}
            </div>
          </div>

          {/* Difficulty Selection */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-[#8b949e]">Difficulty</label>
            <div className="flex gap-2">
              {DIFFICULTIES.map((diff) => (
                <button
                  key={diff}
                  onClick={() => setSelectedDifficulty(diff)}
                  className={`px-4 py-1.5 rounded-lg text-xs font-medium transition-all ${
                    selectedDifficulty === diff
                      ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                      : 'bg-[#161b22] text-[#8b949e] border border-white/[0.06] hover:border-amber-500/30 hover:text-[#f0f6fc]'
                  }`}
                >
                  {diff}
                </button>
              ))}
            </div>
          </div>

          {error && (
            <p className="text-sm text-red-400 bg-red-500/10 rounded-lg px-3 py-2">{error}</p>
          )}

          <button
            onClick={handleStart}
            disabled={state === 'loading'}
            className="btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-50"
          >
            <Play className="w-4 h-4" />
            {state === 'loading' ? 'Starting...' : 'Start Interview'}
          </button>
        </div>
      </div>
    );
  }

  // Complete state - scorecard
  if (state === 'complete') {
    return (
      <div className="min-h-screen bg-[#06090f] flex items-center justify-center p-6">
        <div className="card max-w-2xl w-full space-y-6">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-lg bg-amber-500/10">
              <Award className="w-6 h-6 text-amber-400" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-[#f0f6fc]">Interview Complete</h1>
              <p className="text-sm text-[#8b949e]">
                {TOPIC_LABELS[interview?.topicArea ?? TopicArea.DSA]} - {interview?.difficulty}
              </p>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-4">
            <div className="bg-[#161b22] rounded-lg p-4 border border-white/[0.06] text-center">
              <p className="text-2xl font-bold text-[#f0f6fc]">{turns.length}</p>
              <p className="text-xs text-[#8b949e]">Messages</p>
            </div>
            <div className="bg-[#161b22] rounded-lg p-4 border border-white/[0.06] text-center">
              <p className="text-2xl font-bold text-[#f0f6fc]">{interview?.overallScore ?? '—'}</p>
              <p className="text-xs text-[#8b949e]">Score</p>
            </div>
            <div className="bg-[#161b22] rounded-lg p-4 border border-white/[0.06] text-center">
              <p className="text-2xl font-bold text-amber-400">
                {turns.filter((t) => t.isComplete).length}
              </p>
              <p className="text-xs text-[#8b949e]">Completed Turns</p>
            </div>
          </div>

          {/* Category Scores */}
          {interview?.categoryScores && (
            <div className="bg-[#161b22] rounded-xl p-5 border border-white/[0.06] space-y-2">
              <h3 className="text-sm font-medium text-amber-400">Category Scores</h3>
              <div className="space-y-1">
                {Object.entries(interview.categoryScores).map(([category, score]) => (
                  <div key={category} className="flex justify-between text-sm">
                    <span className="text-[#8b949e]">{category}</span>
                    <span className="text-[#f0f6fc]">{score}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  // Active state - interview in progress
  return (
    <div className="h-screen bg-[#06090f] flex flex-col">
      {/* Top bar */}
      <div className="flex items-center justify-between px-6 py-3 border-b border-white/[0.06] bg-[#0d1117]">
        <div className="flex items-center gap-3">
          <MessageSquare className="w-4 h-4 text-amber-400" />
          <span className="text-sm font-medium text-[#f0f6fc]">
            {TOPIC_LABELS[interview?.topicArea ?? TopicArea.DSA]}
          </span>
          <span className="px-2 py-0.5 rounded text-xs font-medium bg-amber-500/10 text-amber-400 border border-amber-500/30">
            {interview?.difficulty}
          </span>
        </div>
        <div className="flex items-center gap-4">
          <Timer timeLeft={timeLeft} totalTime={DURATION_SECONDS} isRunning={isRunning} />
          <button
            onClick={endInterview}
            className="btn-secondary flex items-center gap-2 text-sm"
          >
            <Square className="w-3.5 h-3.5" />
            End
          </button>
        </div>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto space-y-4 p-4">
        {turns.length === 0 && (
          <div className="flex items-center justify-center h-full">
            <p className="text-[#484f58] text-sm">Waiting for interviewer...</p>
          </div>
        )}
        {turns.map((turn) => (
          <ChatBubble
            key={turn.turnNumber}
            role={turn.phase === 'QUESTION' ? 'AI' : 'USER'}
            content={turn.aiQuestion}
            timestamp={undefined}
          />
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Input area */}
      <div className="px-4 py-3 border-t border-white/[0.06] bg-[#0d1117]">
        <div className="flex items-end gap-3">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type your response..."
            rows={2}
            className="bg-[#0d1117] rounded-xl p-3 flex-1 text-[#f0f6fc] text-sm resize-none outline-none border border-white/[0.06] focus:border-amber-500/30"
          />
          <button
            onClick={handleSend}
            disabled={!input.trim()}
            className="p-3 bg-amber-500 rounded-xl hover:bg-amber-400 text-[#06090f] transition-colors disabled:opacity-50 disabled:hover:bg-amber-500"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}

export default MockInterviewSession;
