import React from 'react';

interface MarkdownRendererProps {
  content: string;
}

function parseInline(text: string): React.ReactNode[] {
  const nodes: React.ReactNode[] = [];
  const regex = /(`[^`]+`)|(\*\*(.+?)\*\*)|(\*(.+?)\*)|(\[([^\]]+)\]\(([^)]+)\))/g;
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      nodes.push(text.slice(lastIndex, match.index));
    }

    if (match[1]) {
      nodes.push(
        <code
          key={match.index}
          className="bg-[#161b22] px-1.5 py-0.5 rounded text-amber-400 font-mono text-sm"
        >
          {match[1].slice(1, -1)}
        </code>
      );
    } else if (match[2]) {
      nodes.push(
        <strong key={match.index} className="font-semibold text-[#f0f6fc]">
          {match[3]}
        </strong>
      );
    } else if (match[4]) {
      nodes.push(<em key={match.index}>{match[5]}</em>);
    } else if (match[6]) {
      nodes.push(
        <a
          key={match.index}
          href={match[8]}
          className="text-amber-400 hover:underline"
          target="_blank"
          rel="noopener noreferrer"
        >
          {match[7]}
        </a>
      );
    }

    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < text.length) {
    nodes.push(text.slice(lastIndex));
  }

  return nodes;
}

export function MarkdownRenderer({ content }: MarkdownRendererProps) {
  const blocks: React.ReactNode[] = [];
  const lines = content.split('\n');
  let i = 0;

  while (i < lines.length) {
    const line = lines[i];

    // Code block
    if (line.startsWith('```')) {
      const codeLines: string[] = [];
      i++;
      while (i < lines.length && !lines[i].startsWith('```')) {
        codeLines.push(lines[i]);
        i++;
      }
      i++;
      blocks.push(
        <pre
          key={blocks.length}
          className="bg-[#0d1117] border-l-2 border-amber-500 rounded-lg p-4 font-mono text-sm text-[#f0f6fc] overflow-x-auto"
        >
          <code>{codeLines.join('\n')}</code>
        </pre>
      );
      continue;
    }

    // Empty line
    if (line.trim() === '') {
      i++;
      continue;
    }

    // Headings
    if (line.startsWith('### ')) {
      blocks.push(
        <h3 key={blocks.length} className="font-['Outfit'] font-bold text-[#f0f6fc] text-lg">
          {parseInline(line.slice(4))}
        </h3>
      );
      i++;
      continue;
    }
    if (line.startsWith('## ')) {
      blocks.push(
        <h2 key={blocks.length} className="font-['Outfit'] font-bold text-[#f0f6fc] text-xl">
          {parseInline(line.slice(3))}
        </h2>
      );
      i++;
      continue;
    }
    if (line.startsWith('# ')) {
      blocks.push(
        <h1 key={blocks.length} className="font-['Outfit'] font-bold text-[#f0f6fc] text-2xl">
          {parseInline(line.slice(2))}
        </h1>
      );
      i++;
      continue;
    }

    // List items
    if (/^[-*] /.test(line)) {
      const items: React.ReactNode[] = [];
      while (i < lines.length && /^[-*] /.test(lines[i])) {
        items.push(
          <li key={items.length}>{parseInline(lines[i].slice(2))}</li>
        );
        i++;
      }
      blocks.push(
        <ul key={blocks.length} className="text-[#8b949e] list-disc pl-5 space-y-1">
          {items}
        </ul>
      );
      continue;
    }

    // Paragraph
    blocks.push(
      <p key={blocks.length} className="text-[#8b949e] leading-relaxed">
        {parseInline(line)}
      </p>
    );
    i++;
  }

  return <div className="space-y-4">{blocks}</div>;
}

export default MarkdownRenderer;
