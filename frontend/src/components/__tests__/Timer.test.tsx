import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Timer } from '../Timer';

describe('Timer', () => {
  it('displays formatted time MM:SS', () => {
    render(<Timer timeLeft={125} totalTime={300} isRunning={true} />);
    expect(screen.getByText('02:05')).toBeInTheDocument();
  });

  it('displays 00:00 at zero', () => {
    render(<Timer timeLeft={0} totalTime={300} isRunning={false} />);
    expect(screen.getByText('00:00')).toBeInTheDocument();
  });

  it('pads single digit seconds', () => {
    render(<Timer timeLeft={63} totalTime={120} isRunning={true} />);
    expect(screen.getByText('01:03')).toBeInTheDocument();
  });

  it('applies pulse animation when under 60 seconds', () => {
    const { container } = render(<Timer timeLeft={30} totalTime={300} isRunning={true} />);
    const timeText = container.querySelector('.animate-pulse');
    expect(timeText).toBeInTheDocument();
  });

  it('does not pulse when over 60 seconds', () => {
    const { container } = render(<Timer timeLeft={120} totalTime={300} isRunning={true} />);
    const timeText = container.querySelector('.animate-pulse');
    expect(timeText).not.toBeInTheDocument();
  });

  it('shows urgent color when under 60 seconds', () => {
    const { container } = render(<Timer timeLeft={30} totalTime={300} isRunning={true} />);
    const timeText = container.querySelector('[class*="text-rose-400"]');
    expect(timeText).toBeInTheDocument();
  });

  it('shows amber color when over 60 seconds', () => {
    const { container } = render(<Timer timeLeft={120} totalTime={300} isRunning={true} />);
    const timeText = container.querySelector('[class*="text-amber-400"]');
    expect(timeText).toBeInTheDocument();
  });
});
