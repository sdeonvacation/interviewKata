import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { get } from '@/api/client';
import { Guide } from '@/types';
import { MarkdownRenderer } from '@/components/MarkdownRenderer';
import { Clock, ArrowRight } from 'lucide-react';

export function GuidePage() {
  const { id } = useParams<{ id: string }>();
  const [guide, setGuide] = useState<Guide | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    get<Guide>(`/guides/${id}`)
      .then(setGuide)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load guide'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-pulse flex flex-col items-center gap-3">
          <div className="h-8 w-64 rounded bg-[#161b22]" />
          <div className="h-4 w-40 rounded bg-[#161b22]" />
          <div className="h-96 w-full max-w-3xl rounded-xl bg-[#161b22] mt-4" />
        </div>
      </div>
    );
  }

  if (error || !guide) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="card border-red-500/30">
          <p className="text-red-400">{error ?? 'Guide not found'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-bold text-[#f0f6fc] font-['Outfit']">
          {guide.title}
        </h1>
        <div className="flex items-center gap-3">
          <span className="flex items-center gap-1.5 text-sm text-[#8b949e]">
            <Clock className="h-4 w-4" />
            {guide.estimatedMinutes} min read
          </span>
          <span className="text-sm text-[#8b949e]">
            {guide.questionCount} questions
          </span>
        </div>
      </header>

      <article className="card">
        <MarkdownRenderer content={guide.contentMarkdown} />
      </article>

      <div className="flex justify-end">
        <Link
          to={`/quiz/${guide.id}`}
          className="btn-primary inline-flex items-center gap-2"
        >
          Take Quiz
          <ArrowRight className="h-4 w-4" />
        </Link>
      </div>
    </div>
  );
}

export default GuidePage;
