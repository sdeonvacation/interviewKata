import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProgressRing } from '../ProgressRing';

describe('ProgressRing', () => {
  it('renders percentage in center', () => {
    render(<ProgressRing progress={75} />);
    expect(screen.getByText('75%')).toBeInTheDocument();
  });

  it('renders label when provided', () => {
    render(<ProgressRing progress={50} label="Java" />);
    expect(screen.getByText('Java')).toBeInTheDocument();
  });

  it('does not render label when not provided', () => {
    const { container } = render(<ProgressRing progress={50} />);
    const spans = container.querySelectorAll('span');
    // Only the percentage span
    expect(spans).toHaveLength(1);
  });

  it('uses custom size', () => {
    const { container } = render(<ProgressRing progress={50} size={120} />);
    const svg = container.querySelector('svg');
    expect(svg?.getAttribute('width')).toBe('120');
    expect(svg?.getAttribute('height')).toBe('120');
  });

  it('rounds progress to nearest integer', () => {
    render(<ProgressRing progress={33.7} />);
    expect(screen.getByText('34%')).toBeInTheDocument();
  });

  it('handles 0% progress', () => {
    render(<ProgressRing progress={0} />);
    expect(screen.getByText('0%')).toBeInTheDocument();
  });

  it('handles 100% progress', () => {
    render(<ProgressRing progress={100} />);
    expect(screen.getByText('100%')).toBeInTheDocument();
  });
});
