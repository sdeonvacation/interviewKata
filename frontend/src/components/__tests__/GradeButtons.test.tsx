import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { GradeButtons } from '../GradeButtons';

describe('GradeButtons', () => {
  it('renders all 5 grade buttons', () => {
    render(<GradeButtons onGrade={vi.fn()} />);
    expect(screen.getByText('Again')).toBeInTheDocument();
    expect(screen.getByText('Hard')).toBeInTheDocument();
    expect(screen.getByText('Good')).toBeInTheDocument();
    expect(screen.getByText('Easy')).toBeInTheDocument();
    expect(screen.getByText('Perfect')).toBeInTheDocument();
  });

  it('calls onGrade with correct value when clicked', () => {
    const onGrade = vi.fn();
    render(<GradeButtons onGrade={onGrade} />);

    fireEvent.click(screen.getByText('Again'));
    expect(onGrade).toHaveBeenCalledWith(1);

    fireEvent.click(screen.getByText('Hard'));
    expect(onGrade).toHaveBeenCalledWith(2);

    fireEvent.click(screen.getByText('Good'));
    expect(onGrade).toHaveBeenCalledWith(3);

    fireEvent.click(screen.getByText('Easy'));
    expect(onGrade).toHaveBeenCalledWith(4);

    fireEvent.click(screen.getByText('Perfect'));
    expect(onGrade).toHaveBeenCalledWith(5);
  });

  it('disables buttons when disabled prop is true', () => {
    render(<GradeButtons onGrade={vi.fn()} disabled={true} />);
    const buttons = screen.getAllByRole('button');
    buttons.forEach((btn: HTMLElement) => {
      expect(btn).toBeDisabled();
    });
  });

  it('does not call onGrade when disabled', () => {
    const onGrade = vi.fn();
    render(<GradeButtons onGrade={onGrade} disabled={true} />);
    fireEvent.click(screen.getByText('Good'));
    expect(onGrade).not.toHaveBeenCalled();
  });

  it('uses correct color classes for each button', () => {
    render(<GradeButtons onGrade={vi.fn()} />);
    const againBtn = screen.getByText('Again').closest('button');
    const easyBtn = screen.getByText('Easy').closest('button');
    expect(againBtn?.className).toContain('rose-500');
    expect(easyBtn?.className).toContain('emerald-500');
  });
});
