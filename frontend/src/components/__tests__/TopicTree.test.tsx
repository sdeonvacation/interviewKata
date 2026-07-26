import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { TopicTree } from '../TopicTree';
import { Topic, TopicArea } from '@/types';

const mockTopics: Topic[] = [
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

function renderTree(props: Partial<Parameters<typeof TopicTree>[0]> = {}) {
  return render(
    <MemoryRouter>
      <TopicTree topics={mockTopics} {...props} />
    </MemoryRouter>
  );
}

describe('TopicTree', () => {
  it('renders topic names', () => {
    renderTree();
    expect(screen.getByText('Collections')).toBeInTheDocument();
    expect(screen.getByText('Concurrency')).toBeInTheDocument();
  });

  it('shows card counts', () => {
    renderTree();
    expect(screen.getByText('10 cards')).toBeInTheDocument();
    expect(screen.getByText('8 cards')).toBeInTheDocument();
  });

  it('calls onSelect with clicked topic', () => {
    const onSelect = vi.fn();
    renderTree({ onSelect });
    fireEvent.click(screen.getByText('Concurrency'));
    expect(onSelect).toHaveBeenCalledWith(mockTopics[1]);
  });

  it('renders empty state message when no topics', () => {
    render(
      <MemoryRouter>
        <TopicTree topics={[]} />
      </MemoryRouter>
    );
    expect(screen.getByText('No subtopics found.')).toBeInTheDocument();
  });

  it('does not crash when onSelect is not provided', () => {
    renderTree();
    expect(() => fireEvent.click(screen.getByText('Collections'))).not.toThrow();
  });

  it('shows Study button for each topic', () => {
    renderTree();
    const studyButtons = screen.getAllByTitle('Study this topic with AI tutor');
    expect(studyButtons).toHaveLength(2);
  });
});
