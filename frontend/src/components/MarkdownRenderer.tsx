import ReactMarkdown from 'react-markdown';

interface MarkdownRendererProps {
  content: string;
}

export function MarkdownRenderer({ content }: MarkdownRendererProps) {
  return (
    <div className="prose-dark space-y-3">
      <ReactMarkdown
        components={{
          h1: ({ children }) => (
            <h1 className="font-['Outfit'] font-bold text-[#f0f6fc] text-2xl mt-4 mb-2">{children}</h1>
          ),
          h2: ({ children }) => (
            <h2 className="font-['Outfit'] font-bold text-[#f0f6fc] text-xl mt-4 mb-2">{children}</h2>
          ),
          h3: ({ children }) => (
            <h3 className="font-['Outfit'] font-bold text-[#f0f6fc] text-lg mt-3 mb-1">{children}</h3>
          ),
          h4: ({ children }) => (
            <h4 className="font-semibold text-[#f0f6fc] text-base mt-2 mb-1">{children}</h4>
          ),
          p: ({ children }) => (
            <p className="text-[#8b949e] leading-relaxed">{children}</p>
          ),
          strong: ({ children }) => (
            <strong className="font-semibold text-[#f0f6fc]">{children}</strong>
          ),
          em: ({ children }) => (
            <em className="italic text-[#8b949e]">{children}</em>
          ),
          ul: ({ children }) => (
            <ul className="text-[#8b949e] list-disc pl-5 space-y-1">{children}</ul>
          ),
          ol: ({ children }) => (
            <ol className="text-[#8b949e] list-decimal pl-5 space-y-1">{children}</ol>
          ),
          li: ({ children }) => (
            <li className="text-[#8b949e]">{children}</li>
          ),
          code: ({ className, children }) => {
            const isBlock = className?.includes('language-');
            if (isBlock) {
              return (
                <code className="block">{children}</code>
              );
            }
            return (
              <code className="bg-[#161b22] px-1.5 py-0.5 rounded text-amber-400 font-mono text-sm">
                {children}
              </code>
            );
          },
          pre: ({ children }) => (
            <pre className="bg-[#0d1117] border-l-2 border-amber-500 rounded-lg p-4 font-mono text-sm text-[#f0f6fc] overflow-x-auto">
              {children}
            </pre>
          ),
          a: ({ href, children }) => (
            <a
              href={href}
              className="text-amber-400 hover:underline"
              target="_blank"
              rel="noopener noreferrer"
            >
              {children}
            </a>
          ),
          hr: () => <hr className="border-white/[0.06] my-4" />,
          blockquote: ({ children }) => (
            <blockquote className="border-l-2 border-amber-500/50 pl-4 italic text-[#8b949e]">
              {children}
            </blockquote>
          ),
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}

export default MarkdownRenderer;
