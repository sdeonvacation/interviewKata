import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ChallengeList } from '../ChallengeList';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockChallenges = [
  {
    id: '1',
    title: 'Two Sum',
    description: 'Find two numbers that add up to target.',
    type: 'CODING',
    difficulty: 'EASY',
    starterCode: '',
    testCases: '',
    hints: [],
    topicIds: [],
    solved: true,
  },
  {
    id: '2',
    title: 'LRU Cache',
    description: 'Design a data structure for LRU cache.',
    type: 'SYSTEM_DESIGN',
    difficulty: 'HARD',
    starterCode: '',
    testCases: '',
    hints: [],
    topicIds: [],
    solved: false,
  },
];

function renderChallengeList() {
  return render(
    <MemoryRouter>
      <ChallengeList />
    </MemoryRouter>
  );
}

describe('ChallengeList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state', () => {
    vi.mocked(client.get).mockReturnValue(new Promise(() => {}));
    renderChallengeList();
    expect(screen.getByText('Loading challenges...')).toBeInTheDocument();
  });

  it('renders header with Coding Dojo title', async () => {
    vi.mocked(client.get).mockResolvedValue(mockChallenges);
    renderChallengeList();
    await waitFor(() => {
      expect(screen.getByText('Coding Dojo')).toBeInTheDocument();
    });
  });

  it('renders challenge cards', async () => {
    vi.mocked(client.get).mockResolvedValue(mockChallenges);
    renderChallengeList();
    await waitFor(() => {
      expect(screen.getByText('Two Sum')).toBeInTheDocument();
      expect(screen.getByText('LRU Cache')).toBeInTheDocument();
    });
  });

  it('shows filter pills', async () => {
    vi.mocked(client.get).mockResolvedValue(mockChallenges);
    renderChallengeList();
    await waitFor(() => {
      expect(screen.getByText('All')).toBeInTheDocument();
      expect(screen.getByText('Easy')).toBeInTheDocument();
      expect(screen.getByText('Hard')).toBeInTheDocument();
    });
  });

  it('filters by difficulty', async () => {
    vi.mocked(client.get).mockResolvedValue(mockChallenges);
    renderChallengeList();
    await waitFor(() => screen.getByText('Two Sum'));
    fireEvent.click(screen.getByText('Hard'));
    expect(screen.queryByText('Two Sum')).not.toBeInTheDocument();
    expect(screen.getByText('LRU Cache')).toBeInTheDocument();
  });

  it('shows empty state when no challenges match filter', async () => {
    vi.mocked(client.get).mockResolvedValue([]);
    renderChallengeList();
    await waitFor(() => {
      expect(screen.getByText('No challenges match your filters.')).toBeInTheDocument();
    });
  });

  it('shows error state', async () => {
    vi.mocked(client.get).mockRejectedValue(new Error('Server error'));
    renderChallengeList();
    await waitFor(() => {
      expect(screen.getByText('Server error')).toBeInTheDocument();
    });
  });
});
