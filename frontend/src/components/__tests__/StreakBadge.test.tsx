import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StreakBadge } from '../StreakBadge';

describe('StreakBadge', () => {
  it('renders streak count', () => {
    render(<StreakBadge streak={5} />);
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('shows glow effect when streak > 0', () => {
    const { container } = render(<StreakBadge streak={3} />);
    const wrapper = container.firstElementChild;
    expect(wrapper?.className).toContain('shadow-amber');
  });

  it('does not show glow when streak is 0', () => {
    const { container } = render(<StreakBadge streak={0} />);
    const wrapper = container.firstElementChild;
    expect(wrapper?.className).not.toContain('shadow-amber');
  });

  it('adds pulse animation when streak >= 7', () => {
    const { container } = render(<StreakBadge streak={7} />);
    const wrapper = container.firstElementChild;
    expect(wrapper?.className).toContain('animate-pulse');
  });

  it('does not pulse when streak < 7', () => {
    const { container } = render(<StreakBadge streak={6} />);
    const wrapper = container.firstElementChild;
    expect(wrapper?.className).not.toContain('animate-pulse');
  });
});
