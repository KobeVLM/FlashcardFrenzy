import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

interface QuizResult {
  id: number;
  deckId: number;
  deckTitle: string;
  score: number;
  timeSpent: number;
  createdAt: string;
}

export default function QuizHistory() {
  const [results, setResults] = useState<QuizResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/quizzes/history')
      .then((res) => setResults(res.data.data ?? []))
      .catch(() => {
        setError('Failed to load quiz history.');
        setResults([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const formatTime = (s: number) => {
    if (!s) return '—';
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return m > 0 ? `${m}m ${sec}s` : `${sec}s`;
  };

  const formatDate = (iso: string) => {
    try {
      return new Intl.DateTimeFormat('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: '2-digit', minute: '2-digit',
      }).format(new Date(iso));
    } catch {
      return iso;
    }
  };

  const scoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600 bg-green-50';
    if (score >= 50) return 'text-yellow-600 bg-yellow-50';
    return 'text-red-600 bg-red-50';
  };

  return (
    <div className="flex flex-col gap-8" id="quiz-history-page">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-4xl font-black text-[#0f172a] tracking-tight">Quiz History</h1>
          <p className="text-[#64748b] text-base mt-1">
            Track your progress and past study sessions.
          </p>
        </div>
        <Link
          to="/decks"
          className="bg-[#df6020] text-white font-bold px-5 py-2.5 rounded-lg text-sm hover:bg-[#c4511a] transition-colors"
        >
          Study a Deck
        </Link>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex items-center justify-center py-24">
          <div className="w-8 h-8 border-4 border-[#df6020] border-t-transparent rounded-full animate-spin" />
        </div>
      ) : error ? (
        <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-red-700 text-sm font-medium">
          {error}
        </div>
      ) : results.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 gap-4 text-center">
          <div className="w-16 h-16 bg-[rgba(223,96,32,0.1)] rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-[#df6020]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5"
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <p className="font-bold text-[#0f172a] text-xl">No sessions yet</p>
          <p className="text-[#64748b] text-sm max-w-xs">
            Start a study session from any deck to see your results here.
          </p>
          <Link
            to="/decks"
            className="mt-2 bg-[#0f172a] text-white font-bold px-6 py-3 rounded-lg text-sm hover:bg-[#1e293b] transition-colors"
          >
            Browse Decks
          </Link>
        </div>
      ) : (
        <div className="bg-white border border-[#e2e8f0] rounded-xl overflow-hidden shadow-sm">
          {/* Table Header */}
          <div className="grid grid-cols-[2fr_1fr_1fr_1fr_1fr] bg-[rgba(248,246,246,0.8)] border-b border-[#e2e8f0] px-6 py-3">
            {['Deck', 'Score', 'Time', 'Date', 'Action'].map((h) => (
              <span key={h} className="text-xs font-bold uppercase tracking-widest text-[#64748b]">
                {h}
              </span>
            ))}
          </div>

          {/* Rows */}
          {results.map((r, idx) => (
            <div
              key={r.id}
              className={`grid grid-cols-[2fr_1fr_1fr_1fr_1fr] items-center px-6 py-4 ${
                idx < results.length - 1 ? 'border-b border-[rgba(226,232,240,0.5)]' : ''
              } hover:bg-[rgba(248,246,246,0.4)] transition-colors`}
            >
              {/* Deck */}
              <div>
                <p className="font-semibold text-[#0f172a] text-sm">{r.deckTitle}</p>
                <p className="text-[#94a3b8] text-xs mt-0.5">Deck #{r.deckId}</p>
              </div>

              {/* Score */}
              <div>
                <span className={`inline-block text-sm font-bold px-3 py-1 rounded-full ${scoreColor(r.score)}`}>
                  {r.score}%
                </span>
              </div>

              {/* Time */}
              <p className="text-[#334155] text-sm">{formatTime(r.timeSpent)}</p>

              {/* Date */}
              <p className="text-[#64748b] text-xs">{formatDate(r.createdAt)}</p>

              {/* Action */}
              <Link
                to={`/quiz/${r.deckId}`}
                className="text-[#df6020] text-xs font-bold hover:underline"
              >
                Retry →
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
