import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MarkdownRenderer } from '../MarkdownRenderer';

describe('MarkdownRenderer', () => {
  it('renders headings with correct hierarchy', () => {
    render(<MarkdownRenderer content="# H1\n## H2\n### H3" />);
    expect(screen.getByText('H1').tagName).toBe('H1');
    expect(screen.getByText('H2').tagName).toBe('H2');
    expect(screen.getByText('H3').tagName).toBe('H3');
  });

  it('renders code blocks with pre element', () => {
    const content = '```\nconst x = 1;\n```';
    const { container } = render(<MarkdownRenderer content={content} />);
    const pre = container.querySelector('pre');
    expect(pre).toBeInTheDocument();
    expect(pre?.textContent).toContain('const x = 1;');
  });

  it('renders code blocks with amber border-left', () => {
    const content = '```\ncode\n```';
    const { container } = render(<MarkdownRenderer content={content} />);
    const pre = container.querySelector('pre');
    expect(pre?.className).toContain('border-amber-500');
  });

  it('renders inline code with amber color', () => {
    render(<MarkdownRenderer content="Use `useState` hook" />);
    const code = screen.getByText('useState');
    expect(code.tagName).toBe('CODE');
    expect(code.className).toContain('text-amber-400');
  });

  it('renders links with amber color', () => {
    render(<MarkdownRenderer content="[Click here](https://example.com)" />);
    const link = screen.getByText('Click here');
    expect(link.tagName).toBe('A');
    expect(link.className).toContain('text-amber-400');
    expect(link).toHaveAttribute('href', 'https://example.com');
  });

  it('renders unordered lists', () => {
    render(<MarkdownRenderer content="- Item 1\n- Item 2\n- Item 3" />);
    expect(screen.getByText('Item 1')).toBeInTheDocument();
    expect(screen.getByText('Item 2')).toBeInTheDocument();
    expect(screen.getByText('Item 3')).toBeInTheDocument();
  });

  it('renders paragraphs with secondary text color', () => {
    render(<MarkdownRenderer content="Hello world" />);
    const p = screen.getByText('Hello world');
    expect(p.tagName).toBe('P');
    expect(p.className).toContain('text-[#8b949e]');
  });

  it('renders bold text', () => {
    render(<MarkdownRenderer content="This is **bold** text" />);
    const bold = screen.getByText('bold');
    expect(bold.tagName).toBe('STRONG');
  });

  it('uses Outfit font for headings', () => {
    render(<MarkdownRenderer content="# Title" />);
    const h1 = screen.getByText('Title');
    expect(h1.className).toContain("font-['Outfit']");
  });
});
