import { useState, useRef, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { get, post } from '@/api/client';
import { StudySessionDto, StudyMessageDto, Topic } from '@/types';
import { ChatBubble } from '@/components/ChatBubble';
import { MarkdownRenderer } from '@/components/MarkdownRenderer';
import { BookOpen, Send, Loader2, History } from 'lucide-react';

interface Message {
  role: 'user' | 'ai';
  content: string;
}

interface TopicInfo {
  name: string;
  area: string;
}

export function StudySession() {
  // Route is either /study/:topicId (new session) or /study/session/:sessionId (existing).
  const { topicId, sessionId: routeSessionId } = useParams<{ topicId?: string; sessionId?: string }>();
  const [topic, setTopic] = useState<TopicInfo | null>(null);
  const [topicIdState, setTopicIdState] = useState<string | null>(topicId ?? null);
  const [sessionId, setSessionId] = useState<string | null>(routeSessionId ?? null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [topicLoading, setTopicLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const loaded = useRef(false);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const postMessage = useCallback(async (userMessage: string, activeSessionId: string) => {
    setMessages((prev) => [...prev, { role: 'user', content: userMessage }]);
    setLoading(true);
    try {
      const data = await post<StudyMessageDto>(
        `/study/sessions/${activeSessionId}/messages`,
        { message: userMessage }
      );
      setMessages((prev) => [...prev, { role: 'ai', content: data.content }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'ai', content: "I'm having trouble connecting right now. Please try again." },
      ]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load: existing session (by sessionId) or prepare a new one (by topicId).
  useEffect(() => {
    if (loaded.current) return;
    loaded.current = true;

    if (routeSessionId) {
      // Existing session — load persisted messages.
      get<StudySessionDto>(`/study/sessions/${routeSessionId}`)
        .then((session) => {
          setSessionId(session.id);
          setTopicIdState(session.topicId);
          setTopic({ name: session.topicName, area: session.topicArea });
          setMessages(
            (session.messages ?? []).map((m) => ({
              role: m.role === 'USER' ? 'user' : 'ai',
              content: m.content,
            }))
          );
        })
        .catch(() => setNotFound(true))
        .finally(() => setTopicLoading(false));
    } else if (topicId) {
      // New session — resolve topic, prefill a suggestion in the input (do NOT send).
      get<Topic>(`/topics/${topicId}`)
        .then((t) => {
          setTopic({ name: t.name, area: t.area });
          setInput(`I want to learn about ${t.name}. Where should we start?`);
        })
        .catch(() => setNotFound(true))
        .finally(() => setTopicLoading(false));
    } else {
      setNotFound(true);
      setTopicLoading(false);
    }
  }, [routeSessionId, topicId]);

  const handleSend = async () => {
    const message = input.trim();
    if (!message || loading) return;
    setInput('');

    // Lazily create the session on first message so empty sessions aren't persisted.
    let activeSessionId = sessionId;
    if (!activeSessionId) {
      if (!topicIdState) return;
      try {
        const session = await post<StudySessionDto>('/study/sessions', { topicId: topicIdState });
        activeSessionId = session.id;
        setSessionId(session.id);
      } catch {
        setMessages((prev) => [
          ...prev,
          { role: 'ai', content: 'Could not start the session. Please try again.' },
        ]);
        return;
      }
    }
    postMessage(message, activeSessionId);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  if (topicLoading) {
    return (
      <div className="h-screen bg-[#06090f] flex items-center justify-center">
        <Loader2 className="w-6 h-6 text-amber-400 animate-spin" />
      </div>
    );
  }

  if (notFound || !topic) {
    return (
      <div className="h-screen bg-[#06090f] flex items-center justify-center">
        <p className="text-red-400">Study session not found.</p>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-3rem)] bg-[#06090f] flex flex-col">
      {/* Header */}
      <div className="flex items-center gap-3 px-6 py-3 border-b border-white/[0.06]">
        <BookOpen className="w-5 h-5 text-amber-400" />
        <h1 className="text-lg font-semibold text-[#f0f6fc]">{topic.name}</h1>
        <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
          Study Mode
        </span>
        <Link
          to="/study/history"
          className="ml-auto flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-[#8b949e] hover:text-amber-400 hover:bg-amber-500/10 transition-colors"
        >
          <History className="w-3.5 h-3.5" />
          History
        </Link>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((msg, idx) =>
          msg.role === 'ai' ? (
            <div key={idx} className="max-w-3xl bg-[#161b22] rounded-xl px-5 py-4 border border-white/[0.06]">
              <MarkdownRenderer content={msg.content} />
            </div>
          ) : (
            <ChatBubble key={idx} role="USER" content={msg.content} />
          )
        )}
        {loading && (
          <div className="flex items-center gap-2 text-[#8b949e]">
            <Loader2 className="w-4 h-4 animate-spin" />
            <span className="text-sm">Thinking...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input area */}
      <div className="border-t border-white/[0.06] px-4 py-3">
        <div className="max-w-3xl mx-auto flex items-end gap-3">
          <textarea
            ref={textareaRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask a question or respond..."
            disabled={loading}
            rows={1}
            className="flex-1 resize-none rounded-xl border border-white/[0.06] bg-[#161b22] px-4 py-2.5 text-sm text-[#f0f6fc] placeholder-[#484f58] focus:outline-none focus:border-amber-500/30 disabled:opacity-50"
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || loading}
            className="p-2.5 rounded-xl bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}

export default StudySession;
