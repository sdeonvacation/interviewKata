import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { StudyHistory } from '../StudyHistory';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockSessions = [
  {
    id: 'session-1',
    topicId: 'topic-1',
    topicName: 'HashMap Internals',
    topicArea: 'JAVA_CORE',
    startedAt: '2026-01-01T10:00:00',
    lastActivityAt: '2026-01-02T12:30:00',
    messageCount: 6,
    preview: 'I want to learn about HashMap Internals.',
  },
  {
    id: 'session-2',
    topicId: 'topic-2',
    topicName: 'Spring DI',
    topicArea: 'SPRING_BOOT',
    startedAt: '2026-01-01T09:00:00',
    lastActivityAt: '2026-01-01T09:45:00',
    messageCount: 4,
    preview: 'Explain dependency injection.',
  },
];

function renderHistory() {
  return render(
    <MemoryRouter>
      <StudyHistory />
    </MemoryRouter>
  );
}

describe('StudyHistory', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state initially', () => {
    vi.mocked(client.get).mockReturnValue(new Promise(() => {}));
    renderHistory();
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('renders a list of study sessions', async () => {
    vi.mocked(client.get).mockResolvedValue(mockSessions);
    renderHistory();

    await waitFor(() => {
      expect(screen.getByText('HashMap Internals')).toBeInTheDocument();
      expect(screen.getByText('Spring DI')).toBeInTheDocument();
    });

    expect(screen.getByText('JAVA_CORE')).toBeInTheDocument();
    expect(screen.getByText('I want to learn about HashMap Internals.')).toBeInTheDocument();
    expect(screen.getByText('6 messages')).toBeInTheDocument();
  });

  it('links each session card to the topic study route', async () => {
    vi.mocked(client.get).mockResolvedValue(mockSessions);
    renderHistory();

    await waitFor(() => {
      expect(screen.getByText('HashMap Internals')).toBeInTheDocument();
    });

    const link = screen.getByText('HashMap Internals').closest('a');
    expect(link).toHaveAttribute('href', '/study/topic-1');
  });

  it('shows empty state when there are no sessions', async () => {
    vi.mocked(client.get).mockResolvedValue([]);
    renderHistory();

    await waitFor(() => {
      expect(screen.getByText('No study sessions yet.')).toBeInTheDocument();
    });
  });

  it('shows error state when fetch fails', async () => {
    vi.mocked(client.get).mockRejectedValue(new Error('boom'));
    renderHistory();

    await waitFor(() => {
      expect(screen.getByText('boom')).toBeInTheDocument();
    });
  });
});
