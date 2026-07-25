import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Dashboard } from '../Dashboard';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockDashboard = {
  currentStreak: 5,
  longestStreak: 10,
  dueCardCount: 12,
  todayActivity: {
    cardsReviewed: 8,
    challengesSolved: 1,
    quizzesCompleted: 0,
    interviewsDone: 0,
    studyMinutes: 45,
  },
  weakAreas: ['Sorting'],
  recentSessions: [],
};

function renderDashboard() {
  return render(
    <MemoryRouter>
      <Dashboard />
    </MemoryRouter>
  );
}

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading skeleton initially', () => {
    vi.mocked(client.get).mockReturnValue(new Promise(() => {}));
    const { container } = renderDashboard();
    const pulseElements = container.querySelectorAll('.animate-pulse');
    expect(pulseElements.length).toBeGreaterThan(0);
  });

  it('renders greeting and streak after loading', async () => {
    vi.mocked(client.get).mockResolvedValue(mockDashboard);
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText('5')).toBeInTheDocument();
    });
    expect(screen.getByText(/Good (morning|afternoon|evening)/)).toBeInTheDocument();
  });

  it('renders stat cards with correct values', async () => {
    vi.mocked(client.get).mockResolvedValue(mockDashboard);
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText('12')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('45')).toBeInTheDocument();
    });
  });

  it('shows Today Focus section when due cards > 0', async () => {
    vi.mocked(client.get).mockResolvedValue(mockDashboard);
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText("Today's Focus")).toBeInTheDocument();
      expect(screen.getByText('Review Flashcards')).toBeInTheDocument();
    });
  });

  it('shows weak areas section', async () => {
    vi.mocked(client.get).mockResolvedValue(mockDashboard);
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText('Weak Areas')).toBeInTheDocument();
      expect(screen.getByText('Sorting')).toBeInTheDocument();
    });
  });

  it('shows "all caught up" state when no due cards and no weak topics', async () => {
    vi.mocked(client.get).mockResolvedValue({
      ...mockDashboard,
      dueCardCount: 0,
      weakAreas: [],
    });
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText('All caught up!')).toBeInTheDocument();
      expect(screen.getByText('Your discipline is paying off.')).toBeInTheDocument();
    });
  });

  it('shows error state with retry button on failure', async () => {
    vi.mocked(client.get).mockRejectedValue(new Error('Network error'));
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText('Network error')).toBeInTheDocument();
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });
  });
});
