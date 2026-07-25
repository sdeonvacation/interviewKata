import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ChallengeWorkspace } from '../ChallengeWorkspace';
import { SubmissionStatus } from '@/types';

const mockGet = vi.fn();
const mockPost = vi.fn();

vi.mock('@/api/client', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
}));

vi.mock('react-router-dom', () => ({
  useParams: () => ({ id: 'challenge-1' }),
}));

vi.mock('@/components/CodeEditor', () => ({
  CodeEditor: ({ value, onChange }: { value: string; onChange: (v: string) => void }) => (
    <textarea data-testid="code-editor" value={value} onChange={(e) => onChange(e.target.value)} />
  ),
}));

vi.mock('@/components/TestResultPanel', () => ({
  TestResultPanel: ({ results }: { results: any[] }) => (
    <div data-testid="test-results">{results.length} results</div>
  ),
}));

vi.mock('@/components/AiFeedbackPanel', () => ({
  AiFeedbackPanel: ({ feedback }: { feedback: string | null }) => (
    <div data-testid="ai-feedback">{feedback}</div>
  ),
}));

vi.mock('@/components/DifficultyBadge', () => ({
  DifficultyBadge: ({ difficulty }: { difficulty: string }) => <span>{difficulty}</span>,
}));

vi.mock('@/components/Timer', () => ({
  Timer: () => <span data-testid="timer">Timer</span>,
}));

vi.mock('@/hooks/useTimer', () => ({
  useTimer: () => ({ timeLeft: 2700, isRunning: true, start: vi.fn(), stop: vi.fn(), reset: vi.fn() }),
}));

const challengeNoSolution = {
  id: 'challenge-1',
  title: 'Two Sum',
  problemStatement: 'Find two numbers...',
  difficulty: 'EASY',
  challengeType: 'DSA',
  starterCode: 'public class Solution {}',
  hints: ['Use a HashMap'],
  timeLimitSeconds: 300,
  submissions: [],
  referenceSolution: null,
};

const challengeWithSolution = {
  ...challengeNoSolution,
  referenceSolution: '// HashMap approach O(n)\npublic int[] twoSum(int[] nums, int target) { ... }',
};

describe('ChallengeWorkspace - Reference Solution', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('does NOT show solution button when challenge has no reference solution', async () => {
    mockGet.mockResolvedValue(challengeNoSolution);
    render(<ChallengeWorkspace />);

    await waitFor(() => {
      expect(screen.getByText('Two Sum')).toBeInTheDocument();
    });

    expect(screen.queryByText('Show Optimal Solution')).not.toBeInTheDocument();
  });

  it('shows "Show Optimal Solution" button when challenge has reference solution', async () => {
    mockGet.mockResolvedValue(challengeWithSolution);
    render(<ChallengeWorkspace />);

    await waitFor(() => {
      expect(screen.getByText('Two Sum')).toBeInTheDocument();
    });

    expect(screen.getByText('Show Optimal Solution')).toBeInTheDocument();
  });

  it('reveals solution content after clicking show button', async () => {
    mockGet.mockResolvedValue(challengeWithSolution);
    render(<ChallengeWorkspace />);

    await waitFor(() => {
      expect(screen.getByText('Show Optimal Solution')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Show Optimal Solution'));

    expect(screen.getByText('Optimal Solution')).toBeInTheDocument();
    expect(screen.getByText(/HashMap approach/)).toBeInTheDocument();
  });

  it('shows reference solution after successful submission', async () => {
    mockGet
      .mockResolvedValueOnce(challengeNoSolution) // initial load
      .mockResolvedValueOnce(challengeWithSolution); // re-fetch after PASSED

    mockPost.mockResolvedValue({
      id: 'sub-1',
      status: SubmissionStatus.PASSED,
      testResults: [{ passed: true }],
      aiReview: 'Good job',
      executionTimeMs: 50,
    });

    render(<ChallengeWorkspace />);

    await waitFor(() => {
      expect(screen.getByText('Two Sum')).toBeInTheDocument();
    });

    // No solution button initially
    expect(screen.queryByText('Show Optimal Solution')).not.toBeInTheDocument();

    // Submit solution
    fireEvent.click(screen.getByText('Run Tests'));

    await waitFor(() => {
      expect(screen.getByText('Show Optimal Solution')).toBeInTheDocument();
    });
  });
});
