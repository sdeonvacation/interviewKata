import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ReviewSession } from '../ReviewSession';

const mockHook: {
  state: string;
  session: any;
  currentCard: any;
  error: string | null;
  startSession: ReturnType<typeof vi.fn>;
  showAnswer: ReturnType<typeof vi.fn>;
  gradeCard: ReturnType<typeof vi.fn>;
} = {
  state: 'idle',
  session: null,
  currentCard: null,
  error: null,
  startSession: vi.fn(),
  showAnswer: vi.fn(),
  gradeCard: vi.fn(),
};

vi.mock('@/hooks/useReviewSession', () => ({
  useReviewSession: () => mockHook,
}));

describe('ReviewSession', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockHook.state = 'idle';
    mockHook.session = null;
    mockHook.currentCard = null;
    mockHook.error = null;
  });

  it('shows idle state with start button', () => {
    render(<ReviewSession />);
    expect(screen.getByText('Ready to Review')).toBeInTheDocument();
    expect(screen.getByText('Start Review')).toBeInTheDocument();
  });

  it('calls startSession on button click', () => {
    render(<ReviewSession />);
    fireEvent.click(screen.getByText('Start Review'));
    expect(mockHook.startSession).toHaveBeenCalled();
  });

  it('shows loading state', () => {
    mockHook.state = 'loading';
    render(<ReviewSession />);
    expect(screen.getByText('Loading cards...')).toBeInTheDocument();
  });

  it('shows card during reviewing state', () => {
    mockHook.state = 'reviewing';
    mockHook.session = { id: '1', cards: [], currentIndex: 0, totalCards: 5, completedCards: 0 } as any;
    mockHook.currentCard = {
      id: 'c1', topicId: 't1', front: 'What is O(n)?',
      back: 'Linear time complexity', status: 'REVIEW',
      difficulty: 'MEDIUM', nextReviewAt: '', easeFactor: 2.5, intervalDays: 1,
    } as any;
    render(<ReviewSession />);
    expect(screen.getByText('What is O(n)?')).toBeInTheDocument();
    expect(screen.getByText('Show Answer')).toBeInTheDocument();
  });

  it('shows grade buttons in grading state', () => {
    mockHook.state = 'grading';
    mockHook.session = { id: '1', cards: [], currentIndex: 0, totalCards: 5, completedCards: 0 } as any;
    mockHook.currentCard = {
      id: 'c1', topicId: 't1', front: 'Q', back: 'A',
      status: 'REVIEW', difficulty: 'MEDIUM',
      nextReviewAt: '', easeFactor: 2.5, intervalDays: 1,
    } as any;
    render(<ReviewSession />);
    expect(screen.getByText('Again')).toBeInTheDocument();
    expect(screen.getByText('Good')).toBeInTheDocument();
    expect(screen.getByText('Perfect')).toBeInTheDocument();
  });

  it('shows completion state with trophy', () => {
    mockHook.state = 'complete';
    mockHook.session = { id: '1', cards: [], currentIndex: 5, totalCards: 5, completedCards: 5 } as any;
    render(<ReviewSession />);
    expect(screen.getByText('Well done!')).toBeInTheDocument();
    expect(screen.getByText('Review Again')).toBeInTheDocument();
  });

  it('shows error message', () => {
    mockHook.error = 'Failed to load';
    render(<ReviewSession />);
    expect(screen.getByText('Failed to load')).toBeInTheDocument();
  });

  it('shows progress bar during review', () => {
    mockHook.state = 'reviewing';
    mockHook.session = { id: '1', cards: [], currentIndex: 2, totalCards: 10, completedCards: 2 } as any;
    mockHook.currentCard = {
      id: 'c1', topicId: 't1', front: 'Q', back: 'A',
      status: 'REVIEW', difficulty: 'EASY',
      nextReviewAt: '', easeFactor: 2.5, intervalDays: 1,
    } as any;
    render(<ReviewSession />);
    expect(screen.getByText('Card 3 of 10')).toBeInTheDocument();
  });
});
