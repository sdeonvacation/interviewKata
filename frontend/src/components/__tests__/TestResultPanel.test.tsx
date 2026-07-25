import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TestResultPanel } from '../TestResultPanel';

describe('TestResultPanel', () => {
  it('renders test names', () => {
    const results = [
      { name: 'Test Addition', passed: true },
      { name: 'Test Subtraction', passed: false, expected: '5', actual: '3' },
    ];
    render(<TestResultPanel results={results} />);
    expect(screen.getByText('Test Addition')).toBeInTheDocument();
    expect(screen.getByText('Test Subtraction')).toBeInTheDocument();
  });

  it('shows pass/total count', () => {
    const results = [
      { name: 'A', passed: true },
      { name: 'B', passed: true },
      { name: 'C', passed: false },
    ];
    render(<TestResultPanel results={results} />);
    expect(screen.getByText('2/3 passed')).toBeInTheDocument();
  });

  it('shows expected vs actual for failed tests', () => {
    const results = [
      { name: 'Fail case', passed: false, expected: '10', actual: '7' },
    ];
    render(<TestResultPanel results={results} />);
    expect(screen.getByText(/Expected:/)).toBeInTheDocument();
    expect(screen.getByText(/Actual:/)).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
    expect(screen.getByText('7')).toBeInTheDocument();
  });

  it('does not show expected/actual for passing tests', () => {
    const results = [{ name: 'Pass case', passed: true }];
    render(<TestResultPanel results={results} />);
    expect(screen.queryByText(/Expected:/)).not.toBeInTheDocument();
  });

  it('shows all passed when everything passes', () => {
    const results = [
      { name: 'A', passed: true },
      { name: 'B', passed: true },
    ];
    render(<TestResultPanel results={results} />);
    expect(screen.getByText('2/2 passed')).toBeInTheDocument();
  });
});
