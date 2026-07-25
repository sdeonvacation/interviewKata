import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Progress } from '../Progress';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockTopicProgress = [
  {
    topicId: 't1',
    topicName: 'Arrays',
    area: 'DATA_STRUCTURES',
    totalCards: 20,
    masteredCards: 15,
    accuracy: 75,
    lastStudiedAt: '2024-01-15',
  },
  {
    topicId: 't2',
    topicName: 'Sorting',
    area: 'ALGORITHMS',
    totalCards: 10,
    masteredCards: 3,
    accuracy: 30,
    lastStudiedAt: '2024-01-14',
  },
];

const mockStreak = { currentStreak: 12, longestStreak: 25 };

function setupMocks() {
  vi.mocked(client.get).mockImplementation((path: string) => {
    if (path === '/progress') return Promise.resolve(mockTopicProgress);
    if (path === '/progress/streak') return Promise.resolve(mockStreak);
    return Promise.reject(new Error('Unknown path'));
  });
}

function renderProgress() {
  return render(
    <MemoryRouter>
      <Progress />
    </MemoryRouter>
  );
}

describe('Progress', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state', () => {
    vi.mocked(client.get).mockReturnValue(new Promise(() => {}));
    renderProgress();
    expect(screen.getByText('Loading progress...')).toBeInTheDocument();
  });

  it('renders stats row', async () => {
    setupMocks();
    renderProgress();
    await waitFor(() => {
      expect(screen.getByText('18')).toBeInTheDocument(); // totalMastered = 15 + 3
      expect(screen.getByText('Cards Mastered')).toBeInTheDocument();
      expect(screen.getByText('Topics Studied')).toBeInTheDocument();
    });
  });

  it('renders streak badge', async () => {
    setupMocks();
    renderProgress();
    await waitFor(() => {
      expect(screen.getByText('12')).toBeInTheDocument();
    });
  });

  it('shows longest streak', async () => {
    setupMocks();
    renderProgress();
    await waitFor(() => {
      expect(screen.getByText(/25 days/)).toBeInTheDocument();
    });
  });

  it('renders topic mastery progress bars', async () => {
    setupMocks();
    renderProgress();
    await waitFor(() => {
      expect(screen.getByText('Topic Mastery')).toBeInTheDocument();
      expect(screen.getByText(/Data Structures/)).toBeInTheDocument();
      expect(screen.getByText(/Algorithms/)).toBeInTheDocument();
    });
  });

  it('highlights weak areas with rose border', async () => {
    setupMocks();
    const { container } = renderProgress();
    await waitFor(() => {
      const weakArea = container.querySelector('.border-rose-500');
      expect(weakArea).toBeInTheDocument();
    });
  });

  it('shows error state', async () => {
    vi.mocked(client.get).mockRejectedValue(new Error('Failed'));
    renderProgress();
    await waitFor(() => {
      expect(screen.getByText('Failed')).toBeInTheDocument();
    });
  });

  it('shows empty state when no progress data', async () => {
    vi.mocked(client.get).mockImplementation((path: string) => {
      if (path === '/progress') return Promise.resolve([]);
      if (path === '/progress/streak') return Promise.resolve({ currentStreak: 0, longestStreak: 0 });
      return Promise.reject(new Error('Unknown path'));
    });
    renderProgress();
    await waitFor(() => {
      expect(screen.getByText(/No topic progress recorded/)).toBeInTheDocument();
    });
  });
});
