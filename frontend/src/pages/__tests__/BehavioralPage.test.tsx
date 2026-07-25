import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import BehavioralPage from '../BehavioralPage';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('@/api/client', () => ({
  get: vi.fn(),
  post: vi.fn(),
}));

import { get, post } from '@/api/client';

describe('BehavioralPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (get as ReturnType<typeof vi.fn>).mockResolvedValue([
      { id: '1', name: 'Leadership', parentId: 'root', area: 'BEHAVIORAL', cardCount: 5, childCount: 0, description: null, sortOrder: 1 },
      { id: '2', name: 'Conflict Resolution', parentId: 'root', area: 'BEHAVIORAL', cardCount: 5, childCount: 0, description: null, sortOrder: 2 },
      { id: '3', name: 'Teamwork', parentId: 'root', area: 'BEHAVIORAL', cardCount: 5, childCount: 0, description: null, sortOrder: 3 },
      { id: '4', name: 'Problem Solving', parentId: 'root', area: 'BEHAVIORAL', cardCount: 5, childCount: 0, description: null, sortOrder: 4 },
      { id: '5', name: 'Communication', parentId: 'root', area: 'BEHAVIORAL', cardCount: 5, childCount: 0, description: null, sortOrder: 5 },
      { id: '6', name: 'Adaptability', parentId: 'root', area: 'BEHAVIORAL', cardCount: 5, childCount: 0, description: null, sortOrder: 6 },
    ]);
  });

  function renderPage() {
    return render(
      <MemoryRouter>
        <BehavioralPage />
      </MemoryRouter>
    );
  }

  it('renders page header', async () => {
    renderPage();
    expect(screen.getByText('Behavioral Interview Prep')).toBeInTheDocument();
    expect(screen.getByText(/STAR method/i)).toBeInTheDocument();
  });

  it('renders STAR method overview', () => {
    renderPage();
    expect(screen.getByText('STAR Method')).toBeInTheDocument();
    expect(screen.getByText(/Situation/)).toBeInTheDocument();
    expect(screen.getByText(/Action/)).toBeInTheDocument();
    expect(screen.getByText(/Result/)).toBeInTheDocument();
  });

  it('renders review cards section', () => {
    renderPage();
    expect(screen.getByText('STAR Method Cards')).toBeInTheDocument();
    expect(screen.getByText('Review All Cards')).toBeInTheDocument();
  });

  it('renders practice interview section', () => {
    renderPage();
    expect(screen.getByText('Practice Behavioral Interview')).toBeInTheDocument();
    expect(screen.getByText('Start Mock Interview')).toBeInTheDocument();
  });

  it('displays categories after loading', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Leadership')).toBeInTheDocument();
    });
    expect(screen.getByText('Conflict Resolution')).toBeInTheDocument();
    expect(screen.getByText('Teamwork')).toBeInTheDocument();
    expect(screen.getByText('Problem Solving')).toBeInTheDocument();
    expect(screen.getByText('Communication')).toBeInTheDocument();
    expect(screen.getByText('Adaptability')).toBeInTheDocument();
  });

  it('shows card counts per category', async () => {
    renderPage();
    await waitFor(() => {
      const cardBadges = screen.getAllByText('5 cards');
      expect(cardBadges).toHaveLength(6);
    });
  });

  it('navigates to review when clicking Review All Cards', async () => {
    renderPage();
    fireEvent.click(screen.getByText('Review All Cards'));
    expect(mockNavigate).toHaveBeenCalledWith('/review');
  });

  it('navigates to review with topicId when clicking category', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText('Leadership')).toBeInTheDocument();
    });
    // Click the category card
    fireEvent.click(screen.getByText('Leadership').closest('div.card')!);
    expect(mockNavigate).toHaveBeenCalledWith('/review?topicId=1');
  });

  it('starts mock interview on button click', async () => {
    (post as ReturnType<typeof vi.fn>).mockResolvedValue({
      id: 'interview-123',
      topicArea: 'BEHAVIORAL',
      difficulty: 'MEDIUM',
      state: 'ASKING',
    });

    renderPage();
    fireEvent.click(screen.getByText('Start Mock Interview'));

    await waitFor(() => {
      expect(post).toHaveBeenCalledWith('/interviews/start', {
        topicArea: 'BEHAVIORAL',
        difficulty: 'MEDIUM',
      });
      expect(mockNavigate).toHaveBeenCalledWith('/interviews/interview-123');
    });
  });

  it('shows fallback categories when API fails', async () => {
    (get as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Network error'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Leadership')).toBeInTheDocument();
    });
    expect(screen.getByText('Adaptability')).toBeInTheDocument();
  });

  it('shows total card count in header', async () => {
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/30 flashcards across 6 categories/)).toBeInTheDocument();
    });
  });
});
