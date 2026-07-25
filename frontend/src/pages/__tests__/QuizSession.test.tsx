import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QuizSession } from '../QuizSession';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockQuestions = [
  {
    id: 'q1',
    topicId: 't1',
    type: 'MULTIPLE_CHOICE',
    question: 'What is the time complexity of binary search?',
    options: ['O(1)', 'O(log n)', 'O(n)', 'O(n^2)'],
    correctAnswer: 'O(log n)',
    explanation: 'Binary search halves the search space each step.',
    difficulty: 'EASY',
  },
  {
    id: 'q2',
    topicId: 't1',
    type: 'MULTIPLE_CHOICE',
    question: 'Which data structure uses LIFO?',
    options: ['Queue', 'Stack', 'Array', 'Tree'],
    correctAnswer: 'Stack',
    explanation: 'Stack is Last In First Out.',
    difficulty: 'EASY',
  },
];

function renderQuiz() {
  return render(
    <MemoryRouter initialEntries={['/quiz/test-id']}>
      <Routes>
        <Route path="/quiz/:id" element={<QuizSession />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('QuizSession', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state', () => {
    vi.mocked(client.get).mockReturnValue(new Promise(() => {}));
    renderQuiz();
    expect(screen.getByText('Loading quiz...')).toBeInTheDocument();
  });

  it('renders first question', async () => {
    vi.mocked(client.get).mockResolvedValue(mockQuestions);
    renderQuiz();
    await waitFor(() => {
      expect(screen.getByText('What is the time complexity of binary search?')).toBeInTheDocument();
    });
  });

  it('shows question number', async () => {
    vi.mocked(client.get).mockResolvedValue(mockQuestions);
    renderQuiz();
    await waitFor(() => {
      expect(screen.getByText('Question 1/2')).toBeInTheDocument();
    });
  });

  it('renders all options', async () => {
    vi.mocked(client.get).mockResolvedValue(mockQuestions);
    renderQuiz();
    await waitFor(() => {
      expect(screen.getByText('O(1)')).toBeInTheDocument();
      expect(screen.getByText('O(log n)')).toBeInTheDocument();
      expect(screen.getByText('O(n)')).toBeInTheDocument();
      expect(screen.getByText('O(n^2)')).toBeInTheDocument();
    });
  });

  it('allows selecting an answer and submitting', async () => {
    vi.mocked(client.get).mockResolvedValue(mockQuestions);
    vi.mocked(client.post).mockResolvedValue({});
    renderQuiz();
    await waitFor(() => screen.getByText('O(log n)'));
    fireEvent.click(screen.getByText('O(log n)'));
    fireEvent.click(screen.getByText('Submit Answer'));
    await waitFor(() => {
      expect(screen.getByText('Binary search halves the search space each step.')).toBeInTheDocument();
    });
  });

  it('shows empty state when no questions', async () => {
    vi.mocked(client.get).mockResolvedValue([]);
    renderQuiz();
    await waitFor(() => {
      expect(screen.getByText('No questions found for this quiz.')).toBeInTheDocument();
    });
  });

  it('shows error state', async () => {
    vi.mocked(client.get).mockRejectedValue(new Error('Quiz not found'));
    renderQuiz();
    await waitFor(() => {
      expect(screen.getByText('Quiz not found')).toBeInTheDocument();
    });
  });
});
