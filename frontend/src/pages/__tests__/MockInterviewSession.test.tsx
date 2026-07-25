import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MockInterviewSession } from '../MockInterviewSession';

const mockHook: {
  state: string;
  interview: any;
  turns: any[];
  error: string | null;
  startInterview: ReturnType<typeof vi.fn>;
  sendMessage: ReturnType<typeof vi.fn>;
  endInterview: ReturnType<typeof vi.fn>;
} = {
  state: 'idle',
  interview: null,
  turns: [],
  error: null,
  startInterview: vi.fn(),
  sendMessage: vi.fn(),
  endInterview: vi.fn(),
};

vi.mock('@/hooks/useInterviewSession', () => ({
  useInterviewSession: () => mockHook,
}));

vi.mock('@/hooks/useTimer', () => ({
  useTimer: () => ({
    timeLeft: 1800,
    isRunning: false,
    start: vi.fn(),
    stop: vi.fn(),
    reset: vi.fn(),
  }),
}));

describe('MockInterviewSession', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockHook.state = 'idle';
    mockHook.interview = null;
    mockHook.turns = [];
    mockHook.error = null;
  });

  it('shows setup card in idle state', () => {
    render(<MockInterviewSession />);
    expect(screen.getByText('Mock Interview')).toBeInTheDocument();
    expect(screen.getByText('Start Interview')).toBeInTheDocument();
  });

  it('shows topic area selection', () => {
    render(<MockInterviewSession />);
    expect(screen.getByText('Topic Area')).toBeInTheDocument();
    expect(screen.getByText('Algorithms')).toBeInTheDocument();
    expect(screen.getByText('System Design')).toBeInTheDocument();
  });

  it('shows difficulty selection', () => {
    render(<MockInterviewSession />);
    expect(screen.getByText('Difficulty')).toBeInTheDocument();
    expect(screen.getByText('EASY')).toBeInTheDocument();
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
    expect(screen.getByText('HARD')).toBeInTheDocument();
  });

  it('calls startInterview on button click', () => {
    render(<MockInterviewSession />);
    fireEvent.click(screen.getByText('Start Interview'));
    expect(mockHook.startInterview).toHaveBeenCalledWith('ALGORITHMS', 'MEDIUM');
  });

  it('shows chat messages in active state', () => {
    mockHook.state = 'active';
    mockHook.interview = {
      id: '1', topicArea: 'ALGORITHMS', difficulty: 'MEDIUM',
      durationMinutes: 30, turns: [], status: 'IN_PROGRESS',
      feedback: null, startedAt: '', completedAt: null,
    };
    mockHook.turns = [
      { id: 't1', role: 'AI', content: 'Tell me about arrays.', timestamp: '2024-01-15T10:00:00Z' },
    ];
    render(<MockInterviewSession />);
    expect(screen.getByText('Tell me about arrays.')).toBeInTheDocument();
  });

  it('shows scorecard in complete state', () => {
    mockHook.state = 'complete';
    mockHook.interview = {
      id: '1', topicArea: 'ALGORITHMS', difficulty: 'MEDIUM',
      durationMinutes: 30, turns: [], status: 'COMPLETED',
      feedback: 'Good performance overall.', startedAt: '', completedAt: '',
    };
    mockHook.turns = [
      { id: 't1', role: 'AI', content: 'Q', timestamp: '' },
      { id: 't2', role: 'USER', content: 'A', timestamp: '' },
    ];
    render(<MockInterviewSession />);
    expect(screen.getByText('Interview Complete')).toBeInTheDocument();
    expect(screen.getByText('Good performance overall.')).toBeInTheDocument();
  });

  it('shows error in idle state', () => {
    mockHook.error = 'Connection failed';
    render(<MockInterviewSession />);
    expect(screen.getByText('Connection failed')).toBeInTheDocument();
  });

  it('shows loading text during loading state', () => {
    mockHook.state = 'loading';
    render(<MockInterviewSession />);
    expect(screen.getByText('Starting...')).toBeInTheDocument();
  });
});
