import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { TopicBrowser } from '../TopicBrowser';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

const mockRootTopics = [
  {
    id: 'root-java',
    name: 'Java Core',
    area: 'JAVA_CORE',
    parentId: null,
    description: null,
    sortOrder: 1,
    childCount: 6,
    cardCount: 40,
  },
  {
    id: 'root-spring',
    name: 'Spring Boot',
    area: 'SPRING_BOOT',
    parentId: null,
    description: null,
    sortOrder: 2,
    childCount: 4,
    cardCount: 20,
  },
];

const mockChildren = [
  {
    id: 'child-1',
    name: 'Collections',
    area: 'JAVA_CORE',
    parentId: 'root-java',
    description: null,
    sortOrder: 1,
    childCount: 0,
    cardCount: 12,
  },
  {
    id: 'child-2',
    name: 'Concurrency',
    area: 'JAVA_CORE',
    parentId: 'root-java',
    description: null,
    sortOrder: 2,
    childCount: 0,
    cardCount: 8,
  },
];

function renderTopicBrowser() {
  return render(
    <MemoryRouter>
      <TopicBrowser />
    </MemoryRouter>
  );
}

describe('TopicBrowser', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state initially', () => {
    vi.mocked(client.get).mockReturnValue(new Promise(() => {}));
    renderTopicBrowser();
    expect(screen.getByText('Loading topics...')).toBeInTheDocument();
  });

  it('renders area cards after loading', async () => {
    vi.mocked(client.get).mockResolvedValue(mockRootTopics);
    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('Java Core')).toBeInTheDocument();
      expect(screen.getByText('Spring Boot')).toBeInTheDocument();
    });
  });

  it('shows error state on failure', async () => {
    vi.mocked(client.get).mockRejectedValue(new Error('Network error'));
    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });
  });

  it('fetches children when area card is clicked', async () => {
    vi.mocked(client.get)
      .mockResolvedValueOnce(mockRootTopics)
      .mockResolvedValueOnce(mockChildren);

    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('Java Core')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Java Core'));

    await waitFor(() => {
      expect(client.get).toHaveBeenCalledWith('/topics/root-java/children');
      expect(screen.getByText('Collections')).toBeInTheDocument();
      expect(screen.getByText('Concurrency')).toBeInTheDocument();
    });
  });

  it('shows card counts for child topics', async () => {
    vi.mocked(client.get)
      .mockResolvedValueOnce(mockRootTopics)
      .mockResolvedValueOnce(mockChildren);

    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('Java Core')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Java Core'));

    await waitFor(() => {
      expect(screen.getByText('12 cards')).toBeInTheDocument();
      expect(screen.getByText('8 cards')).toBeInTheDocument();
    });
  });

  it('navigates to review when subtopic is clicked', async () => {
    vi.mocked(client.get)
      .mockResolvedValueOnce(mockRootTopics)
      .mockResolvedValueOnce(mockChildren);

    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('Java Core')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Java Core'));

    await waitFor(() => {
      expect(screen.getByText('Collections')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Collections'));
    expect(mockNavigate).toHaveBeenCalledWith('/review?topicId=child-1');
  });

  it('collapses area when clicked again', async () => {
    vi.mocked(client.get)
      .mockResolvedValueOnce(mockRootTopics)
      .mockResolvedValueOnce(mockChildren);

    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('Java Core')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Java Core'));
    await waitFor(() => {
      expect(screen.getByText('Collections')).toBeInTheDocument();
    });

    // After expansion, "Java Core" appears in both button and header
    fireEvent.click(screen.getAllByText('Java Core')[0]);
    expect(screen.queryByText('Collections')).not.toBeInTheDocument();
  });

  it('shows empty state when no topics', async () => {
    vi.mocked(client.get).mockResolvedValue([]);
    renderTopicBrowser();
    await waitFor(() => {
      expect(screen.getByText('No topics available yet.')).toBeInTheDocument();
    });
  });
});
