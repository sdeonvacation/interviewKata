import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { FlashCard } from '../FlashCard';

describe('FlashCard', () => {
  const defaultProps = {
    front: 'What is a HashMap?',
    back: 'A hash table-based Map implementation.',
    isFlipped: false,
    onFlip: vi.fn(),
  };

  it('renders front content', () => {
    render(<FlashCard {...defaultProps} />);
    expect(screen.getByText('What is a HashMap?')).toBeInTheDocument();
  });

  it('renders back content', () => {
    render(<FlashCard {...defaultProps} />);
    expect(screen.getByText('A hash table-based Map implementation.')).toBeInTheDocument();
  });

  it('calls onFlip when clicked', () => {
    const onFlip = vi.fn();
    const { container } = render(<FlashCard {...defaultProps} onFlip={onFlip} />);
    const flipTarget = container.querySelector('[style*="transformStyle"]');
    fireEvent.click(flipTarget!);
    expect(onFlip).toHaveBeenCalledTimes(1);
  });

  it('applies rotateY(180deg) when flipped', () => {
    const { container } = render(<FlashCard {...defaultProps} isFlipped={true} />);
    const inner = container.querySelector('[style*="transformStyle"]');
    expect(inner?.getAttribute('style')).toContain('rotateY(180deg)');
  });

  it('does not rotate when not flipped', () => {
    const { container } = render(<FlashCard {...defaultProps} isFlipped={false} />);
    const inner = container.querySelector('[style*="transformStyle"]');
    expect(inner?.getAttribute('style')).toContain('rotateY(0deg)');
  });

  it('renders code blocks in back with pre element', () => {
    const back = '```java\nint x = 5;\n```';
    render(<FlashCard {...defaultProps} back={back} />);
    const pre = document.querySelector('pre');
    expect(pre).toBeInTheDocument();
    expect(pre?.textContent).toContain('int x = 5;');
  });
});
