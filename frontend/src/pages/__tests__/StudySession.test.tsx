import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { StudySession } from '../StudySession';
import * as client from '@/api/client';

vi.mock('@/api/client');

vi.mock('@/components/MarkdownRenderer', () => ({
  MarkdownRenderer: ({ content }: { content: string }) => (
    <div data-testid="markdown">{content}</div>
  ),
}));

const sessionId = 'session-1';

function newSession(messages: { role: string; content: string; sequence: number }[] = []) {
  return {
    id: sessionId,
    topicId: 'topic-1',
    topicName: 'HashMap Internals',
    topicArea: 'JAVA_CORE',
    startedAt: '2026-01-01T10:00:00',
    lastActivityAt: '2026-01-01T10:00:00',
    messageCount: messages.length,
    messages,
  };
}

function renderStudy(topicId = 'topic-1') {
  return render(
    <MemoryRouter initialEntries={[`/study/${topicId}`]}>
      <Routes>
        <Route path="/study/:topicId" element={<StudySession />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('StudySession', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state initially', () => {
    vi.mocked(client.post).mockReturnValue(new Promise(() => {}));
    renderStudy();
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('shows error when resume fails', async () => {
    vi.mocked(client.post).mockRejectedValue(new Error('not found'));
    renderStudy('nonexistent');
    await waitFor(() => {
      expect(screen.getByText('Topic not found.')).toBeInTheDocument();
    });
  });

  it('renders header with topic name and Study Mode badge', async () => {
    vi.mocked(client.post)
      .mockResolvedValueOnce(newSession()) // resume
      .mockResolvedValueOnce({ role: 'AI', content: 'Welcome!', sequence: 1 }); // kickoff reply

    renderStudy('topic-1');

    await waitFor(() => {
      expect(screen.getByText('HashMap Internals')).toBeInTheDocument();
      expect(screen.getByText('Study Mode')).toBeInTheDocument();
    });
  });

  it('sends kickoff message for a brand-new session (empty messages)', async () => {
    vi.mocked(client.post)
      .mockResolvedValueOnce(newSession()) // resume, no messages
      .mockResolvedValueOnce({ role: 'AI', content: 'Great topic!', sequence: 1 });

    renderStudy('topic-1');

    await waitFor(() => {
      expect(vi.mocked(client.post)).toHaveBeenCalledWith(
        `/study/sessions/${sessionId}/messages`,
        { message: 'I want to learn about HashMap Internals. Where should we start?' }
      );
    });
  });

  it('does NOT send kickoff for a resumed session and renders persisted messages', async () => {
    vi.mocked(client.post).mockResolvedValueOnce(
      newSession([
        { role: 'USER', content: 'What is a bucket?', sequence: 0 },
        { role: 'AI', content: 'A bucket holds entries.', sequence: 1 },
      ])
    );

    renderStudy('topic-1');

    await waitFor(() => {
      expect(screen.getByText('What is a bucket?')).toBeInTheDocument();
      expect(screen.getByTestId('markdown')).toHaveTextContent('A bucket holds entries.');
    });

    // Only the resume call happened — no message POST.
    expect(vi.mocked(client.post)).toHaveBeenCalledTimes(1);
    expect(vi.mocked(client.post)).toHaveBeenCalledWith('/study/sessions/resume', {
      topicId: 'topic-1',
    });
  });

  it('displays AI response with markdown renderer', async () => {
    vi.mocked(client.post)
      .mockResolvedValueOnce(newSession())
      .mockResolvedValueOnce({ role: 'AI', content: '**HashMap** uses hashing.', sequence: 1 });

    renderStudy('topic-1');

    await waitFor(() => {
      expect(screen.getByTestId('markdown')).toHaveTextContent('**HashMap** uses hashing.');
    });
  });

  it('sends user input on Enter key to the messages endpoint', async () => {
    vi.mocked(client.post)
      .mockResolvedValueOnce(newSession())
      .mockResolvedValueOnce({ role: 'AI', content: 'Let me teach you!', sequence: 1 })
      .mockResolvedValueOnce({ role: 'AI', content: 'Good answer!', sequence: 3 });

    renderStudy('topic-1');

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Ask a question or respond...')).toBeInTheDocument();
    });

    const textarea = screen.getByPlaceholderText('Ask a question or respond...');
    fireEvent.change(textarea, { target: { value: 'What about load factor?' } });
    fireEvent.keyDown(textarea, { key: 'Enter', shiftKey: false });

    await waitFor(() => {
      expect(vi.mocked(client.post)).toHaveBeenCalledWith(
        `/study/sessions/${sessionId}/messages`,
        { message: 'What about load factor?' }
      );
    });
  });

  it('shows error message when AI call fails', async () => {
    vi.mocked(client.post)
      .mockResolvedValueOnce(newSession()) // resume ok
      .mockRejectedValueOnce(new Error('Network error')); // kickoff fails

    renderStudy('topic-1');

    await waitFor(() => {
      expect(
        screen.getByText("I'm having trouble connecting right now. Please try again.")
      ).toBeInTheDocument();
    });
  });

  it('disables input while loading', async () => {
    vi.mocked(client.post)
      .mockResolvedValueOnce(newSession())
      .mockReturnValueOnce(new Promise(() => {})); // kickoff never resolves

    renderStudy('topic-1');

    await waitFor(() => {
      const textarea = screen.getByPlaceholderText('Ask a question or respond...');
      expect(textarea).toBeDisabled();
    });
  });
});
