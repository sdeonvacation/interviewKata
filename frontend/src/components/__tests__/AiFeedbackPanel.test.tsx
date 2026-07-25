import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AiFeedbackPanel } from '../AiFeedbackPanel';

describe('AiFeedbackPanel', () => {
  it('shows feedback text when provided', () => {
    render(<AiFeedbackPanel feedback="Great solution! Consider edge cases." />);
    expect(screen.getByText('Great solution! Consider edge cases.')).toBeInTheDocument();
  });

  it('shows empty state when no feedback', () => {
    render(<AiFeedbackPanel feedback={null} />);
    expect(screen.getByText('Submit your solution to receive AI feedback')).toBeInTheDocument();
  });

  it('shows loading skeleton when loading', () => {
    const { container } = render(<AiFeedbackPanel feedback={null} loading={true} />);
    const skeletons = container.querySelectorAll('.animate-pulse');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('shows AI Feedback header', () => {
    render(<AiFeedbackPanel feedback="text" />);
    expect(screen.getByText('AI Feedback')).toBeInTheDocument();
  });

  it('has gradient top border', () => {
    const { container } = render(<AiFeedbackPanel feedback="text" />);
    const gradient = container.querySelector('.bg-gradient-to-r');
    expect(gradient).toBeInTheDocument();
  });
});
