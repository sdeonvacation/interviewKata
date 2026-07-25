import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ChatBubble } from '../ChatBubble';

describe('ChatBubble', () => {
  it('renders AI message content', () => {
    render(<ChatBubble role="AI" content="Hello, candidate!" />);
    expect(screen.getByText('Hello, candidate!')).toBeInTheDocument();
  });

  it('renders USER message content', () => {
    render(<ChatBubble role="USER" content="My answer is..." />);
    expect(screen.getByText('My answer is...')).toBeInTheDocument();
  });

  it('aligns AI messages to the left', () => {
    const { container } = render(<ChatBubble role="AI" content="Hi" />);
    const wrapper = container.firstElementChild;
    expect(wrapper?.className).toContain('justify-start');
  });

  it('aligns USER messages to the right', () => {
    const { container } = render(<ChatBubble role="USER" content="Hi" />);
    const wrapper = container.firstElementChild;
    expect(wrapper?.className).toContain('justify-end');
  });

  it('shows timestamp when provided', () => {
    render(<ChatBubble role="AI" content="Hi" timestamp="2024-01-15T10:30:00Z" />);
    // Timestamp should be rendered (format depends on locale)
    const timeEl = screen.getByText(/\d{1,2}:\d{2}/);
    expect(timeEl).toBeInTheDocument();
  });

  it('does not show timestamp when not provided', () => {
    const { container } = render(<ChatBubble role="AI" content="Hi" />);
    const timeEl = container.querySelector('.text-\\[\\#484f58\\]');
    expect(timeEl).not.toBeInTheDocument();
  });

  it('uses surface bg for AI bubbles', () => {
    const { container } = render(<ChatBubble role="AI" content="Hi" />);
    const bubble = container.querySelector('[class*="bg-[#161b22]"]');
    expect(bubble).toBeInTheDocument();
  });

  it('uses amber bg for USER bubbles', () => {
    const { container } = render(<ChatBubble role="USER" content="Hi" />);
    const bubble = container.querySelector('[class*="bg-amber"]');
    expect(bubble).toBeInTheDocument();
  });
});
