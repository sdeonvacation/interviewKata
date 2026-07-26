import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { TopicTree } from '../TopicTree';
import { Topic, TopicArea } from '@/types';
import * as client from '@/api/client';

vi.mock('@/api/client');

const mockTopicsWithCards: Topic[] = [
  {
    id: '1',
    name: 'Collections',
    area: TopicArea.JAVA_CORE,
    parentId: 'root-1',
    description: null,
    sortOrder: 1,
    childCount: 0,
    cardCount: 10,
  },
  {
    id: '2',
    name: 'Concurrency',
    area: TopicArea.JAVA_CORE,
    parentId: 'root-1',
    description: null,
    sortOrder: 2,
    childCount: 0,
    cardCount: 8,
  },
];

const mockTopicsWithZeroCards: Topic[] = [
  {
    id: '3',
    name: 'Generics',
    area: TopicArea.JAVA_CORE,
    parentId: 'root-1',
    description: null,
    sortOrder: 3,
    childCount: 0,
    cardCount: 0,
  },
  {
    id: '4',
    name: 'Streams',
    area: TopicArea.JAVA_CORE,
    parentId: 'root-1',
    description: null,
    sortOrder: 4,
    childCount: 0,
    cardCount: 5,
  },
];

describe('TopicTree - Generate Cards', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('does not show generate button for topics with cards', () => {
    render(<MemoryRouter><TopicTree topics={mockTopicsWithCards} /></MemoryRouter>);
    expect(screen.queryByText('Generate')).not.toBeInTheDocument();
  });

  it('shows generate button for topics with 0 cards', () => {
    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} /></MemoryRouter>);
    expect(screen.getByText('Generate')).toBeInTheDocument();
  });

  it('only shows generate button for zero-card topics', () => {
    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} /></MemoryRouter>);
    const generateButtons = screen.getAllByText('Generate');
    expect(generateButtons).toHaveLength(1); // Only Generics has 0 cards
  });

  it('calls API when generate button is clicked', async () => {
    const mockCards = [
      { id: 'c1', topicId: '3', topicName: 'Generics', front: 'Q?', back: 'A', difficulty: 'EASY', tags: [], status: 'NEW', codeSnippet: null, explanation: null, nextReview: null },
    ];
    vi.mocked(client.post).mockResolvedValue(mockCards);

    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} /></MemoryRouter>);
    fireEvent.click(screen.getByText('Generate'));

    await waitFor(() => {
      expect(client.post).toHaveBeenCalledWith('/topics/3/generate-cards');
    });
  });

  it('shows loading state while generating', async () => {
    vi.mocked(client.post).mockReturnValue(new Promise(() => {})); // Never resolves

    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} /></MemoryRouter>);
    fireEvent.click(screen.getByText('Generate'));

    await waitFor(() => {
      expect(screen.getByText('Generating...')).toBeInTheDocument();
    });
  });

  it('calls onCardsGenerated callback on success', async () => {
    const mockCards = [
      { id: 'c1', topicId: '3', topicName: 'Generics', front: 'Q?', back: 'A', difficulty: 'EASY', tags: [], status: 'NEW', codeSnippet: null, explanation: null, nextReview: null },
      { id: 'c2', topicId: '3', topicName: 'Generics', front: 'Q2?', back: 'A2', difficulty: 'MEDIUM', tags: [], status: 'NEW', codeSnippet: null, explanation: null, nextReview: null },
    ];
    vi.mocked(client.post).mockResolvedValue(mockCards);
    const onCardsGenerated = vi.fn();

    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} onCardsGenerated={onCardsGenerated} /></MemoryRouter>);
    fireEvent.click(screen.getByText('Generate'));

    await waitFor(() => {
      expect(onCardsGenerated).toHaveBeenCalledWith('3', 2);
    });
  });

  it('shows error message on failure', async () => {
    vi.mocked(client.post).mockRejectedValue(new Error('AI service unavailable'));

    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} /></MemoryRouter>);
    fireEvent.click(screen.getByText('Generate'));

    await waitFor(() => {
      expect(screen.getByText('AI service unavailable')).toBeInTheDocument();
    });
  });

  it('does not navigate when generate button is clicked', async () => {
    vi.mocked(client.post).mockResolvedValue([]);
    const onSelect = vi.fn();

    render(<MemoryRouter><TopicTree topics={mockTopicsWithZeroCards} onSelect={onSelect} /></MemoryRouter>);
    fireEvent.click(screen.getByText('Generate'));

    // onSelect should NOT be called - stopPropagation prevents it
    expect(onSelect).not.toHaveBeenCalled();
  });

  it('renders topic names and card counts', () => {
    render(<MemoryRouter><TopicTree topics={mockTopicsWithCards} /></MemoryRouter>);
    expect(screen.getByText('Collections')).toBeInTheDocument();
    expect(screen.getByText('Concurrency')).toBeInTheDocument();
    expect(screen.getByText('10 cards')).toBeInTheDocument();
    expect(screen.getByText('8 cards')).toBeInTheDocument();
  });

  it('calls onSelect with clicked topic', () => {
    const onSelect = vi.fn();
    render(<MemoryRouter><TopicTree topics={mockTopicsWithCards} onSelect={onSelect} /></MemoryRouter>);
    fireEvent.click(screen.getByText('Concurrency'));
    expect(onSelect).toHaveBeenCalledWith(mockTopicsWithCards[1]);
  });

  it('renders empty state message when no topics', () => {
    render(<MemoryRouter><TopicTree topics={[]} /></MemoryRouter>);
    expect(screen.getByText('No subtopics found.')).toBeInTheDocument();
  });
});
