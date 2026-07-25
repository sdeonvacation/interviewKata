import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DifficultyBadge } from '../DifficultyBadge';
import { Difficulty } from '@/types';

describe('DifficultyBadge', () => {
  it('renders EASY with emerald styling', () => {
    const { container } = render(<DifficultyBadge difficulty={Difficulty.EASY} />);
    expect(screen.getByText('EASY')).toBeInTheDocument();
    const badge = container.firstElementChild;
    expect(badge?.className).toContain('emerald');
  });

  it('renders MEDIUM with amber styling', () => {
    const { container } = render(<DifficultyBadge difficulty={Difficulty.MEDIUM} />);
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
    const badge = container.firstElementChild;
    expect(badge?.className).toContain('amber');
  });

  it('renders HARD with rose styling', () => {
    const { container } = render(<DifficultyBadge difficulty={Difficulty.HARD} />);
    expect(screen.getByText('HARD')).toBeInTheDocument();
    const badge = container.firstElementChild;
    expect(badge?.className).toContain('rose');
  });

  it('has pill shape (rounded-full)', () => {
    const { container } = render(<DifficultyBadge difficulty={Difficulty.EASY} />);
    const badge = container.firstElementChild;
    expect(badge?.className).toContain('rounded-full');
  });

  it('is uppercase', () => {
    const { container } = render(<DifficultyBadge difficulty={Difficulty.MEDIUM} />);
    const badge = container.firstElementChild;
    expect(badge?.className).toContain('uppercase');
  });
});
