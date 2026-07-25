import { Check, X } from 'lucide-react';

interface TestResult {
  name: string;
  passed: boolean;
  expected?: string;
  actual?: string;
}

interface TestResultPanelProps {
  results: TestResult[];
}

export function TestResultPanel({ results }: TestResultPanelProps) {
  const passedCount = results.filter((r) => r.passed).length;
  const totalCount = results.length;
  const allPassed = passedCount === totalCount;

  return (
    <div className="card">
      <h3 className="text-[#f0f6fc] font-semibold mb-4">Test Results</h3>

      <div className="space-y-0">
        {results.map((result, idx) => (
          <div
            key={idx}
            className="flex flex-col py-2 border-b border-white/[0.06] last:border-0"
          >
            <div className="flex items-center gap-3">
              {result.passed ? (
                <>
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span className="text-[#f0f6fc]">{result.name}</span>
                </>
              ) : (
                <>
                  <X className="w-4 h-4 text-rose-400 shrink-0" />
                  <span className="text-rose-400">{result.name}</span>
                </>
              )}
            </div>

            {!result.passed && (result.expected || result.actual) && (
              <div className="font-mono text-sm bg-[#0d1117] rounded p-2 mt-1 ml-7">
                {result.expected && (
                  <div className="text-emerald-400">
                    Expected: {result.expected}
                  </div>
                )}
                {result.actual && (
                  <div className="text-rose-400">
                    Actual: {result.actual}
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="mt-4 pt-3 border-t border-white/[0.06]">
        <span className={allPassed ? 'text-emerald-400' : 'text-rose-400'}>
          {passedCount}/{totalCount} passed
        </span>
      </div>
    </div>
  );
}

export type { TestResult };
