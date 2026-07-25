import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CodeEditor } from '../CodeEditor';

describe('CodeEditor', () => {
  it('renders with initial value', () => {
    render(<CodeEditor value="const x = 1;" onChange={vi.fn()} />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveValue('const x = 1;');
  });

  it('calls onChange when typing', () => {
    const onChange = vi.fn();
    render(<CodeEditor value="" onChange={onChange} />);
    const textarea = screen.getByRole('textbox');
    fireEvent.change(textarea, { target: { value: 'hello' } });
    expect(onChange).toHaveBeenCalledWith('hello');
  });

  it('shows language indicator', () => {
    render(<CodeEditor value="" onChange={vi.fn()} language="java" />);
    expect(screen.getByText('java')).toBeInTheDocument();
  });

  it('defaults language to typescript', () => {
    render(<CodeEditor value="" onChange={vi.fn()} />);
    expect(screen.getByText('typescript')).toBeInTheDocument();
  });

  it('shows Code Editor label', () => {
    render(<CodeEditor value="" onChange={vi.fn()} />);
    expect(screen.getByText('Code Editor')).toBeInTheDocument();
  });

  it('renders line numbers', () => {
    render(<CodeEditor value="line1\nline2\nline3" onChange={vi.fn()} />);
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
  });

  it('sets textarea readOnly when prop is true', () => {
    render(<CodeEditor value="code" onChange={vi.fn()} readOnly={true} />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveAttribute('readonly');
  });
});
