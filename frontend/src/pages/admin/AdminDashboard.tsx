import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';

interface AdminUser {
  id: number;
  email: string;
  fullName: string;
  role: string;
  createdAt: string;
}

interface Stats {
  totalUsers: number;
  totalDecks: number;
  totalFlashcards: number;
  totalQuizResults: number;
}

export default function AdminDashboard() {
  const navigate = useNavigate();

  // Role guard
  const raw = localStorage.getItem('user');
  let localUser = {} as any;
  try {
    localUser = raw && raw !== 'undefined' ? JSON.parse(raw) : {};
  } catch (e) {
    console.error("Failed to parse user in AdminDashboard", e);
  }
  if (localUser.role !== 'ADMIN') {
    navigate('/decks');
    return null;
  }

  const [stats, setStats] = useState<Stats | null>(null);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [deletingId, setDeletingId] = useState<number | null>(null);

  useEffect(() => {
    Promise.all([
      api.get('/admin/stats'),
      api.get('/admin/users'),
    ])
      .then(([statsRes, usersRes]) => {
        setStats(statsRes.data.data);
        setUsers(usersRes.data.data ?? []);
      })
      .catch(() => setError('Failed to load admin data.'))
      .finally(() => setLoading(false));
  }, []);

  const handleDeleteUser = async (id: number) => {
    if (!window.confirm('Delete this user? This cannot be undone.')) return;
    setDeletingId(id);
    try {
      await api.delete(`/admin/users/${id}`);
      setUsers((u) => u.filter((x) => x.id !== id));
      if (stats) setStats({ ...stats, totalUsers: stats.totalUsers - 1 });
    } catch {
      alert('Failed to delete user.');
    } finally {
      setDeletingId(null);
    }
  };

  const formatDate = (iso: string) => {
    try {
      return new Intl.DateTimeFormat('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
      }).format(new Date(iso));
    } catch {
      return iso;
    }
  };

  const filtered = users.filter(
    (u) =>
      u.fullName.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase()),
  );

  const statCards = stats
    ? [
        { label: 'Total Users', value: stats.totalUsers.toLocaleString(), trend: '+12%', up: true },
        { label: 'Total Decks', value: stats.totalDecks.toLocaleString(), trend: '+5.2%', up: true },
        { label: 'Total Flashcards', value: stats.totalFlashcards.toLocaleString(), trend: '+8.1%', up: true },
        { label: 'Quiz Sessions', value: stats.totalQuizResults.toLocaleString(), trend: '+3.4%', up: true },
      ]
    : [];

  return (
    <div className="flex flex-col gap-8" id="admin-dashboard-page">
      {/* ── Header ── */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-black text-[#1a1a1a] tracking-tight">
            System Administration
          </h1>
          <p className="text-[#6b6b6b] text-sm mt-1">
            Monitor platform health and manage users.
          </p>
        </div>
        {/* Search */}
        <div className="relative">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[rgba(107,107,107,0.6)]"
            fill="none" stroke="currentColor" viewBox="0 0 24 24"
          >
            <circle cx="11" cy="11" r="8" strokeWidth="2" />
            <path strokeLinecap="round" strokeWidth="2" d="M21 21l-4.35-4.35" />
          </svg>
          <input
            id="admin-search"
            type="text"
            placeholder="Search users..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="bg-[#f5efe6] pl-9 pr-4 py-2.5 rounded-lg text-sm text-[#1a1a1a] placeholder-[rgba(107,107,107,0.6)] focus:outline-none focus:ring-2 focus:ring-[rgba(26,26,26,0.15)] w-56"
          />
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-24">
          <div className="w-8 h-8 border-4 border-[#1a1a1a] border-t-transparent rounded-full animate-spin" />
        </div>
      ) : error ? (
        <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-red-700 text-sm font-medium">
          {error}
        </div>
      ) : (
        <>
          {/* ── Stats Cards ── */}
          <div className="grid grid-cols-4 gap-6">
            {statCards.map((s) => (
              <div
                key={s.label}
                className="bg-[rgba(245,239,230,0.4)] border border-[rgba(229,224,216,0.6)] rounded-xl p-6 shadow-sm"
              >
                <p className="text-[#6b6b6b] text-xs font-semibold uppercase tracking-widest mb-4">
                  {s.label}
                </p>
                <div className="flex items-end gap-2">
                  <p className="text-3xl font-bold text-[#1a1a1a] tracking-tight">{s.value}</p>
                  <span
                    className={`text-xs font-medium px-1.5 py-0.5 rounded mb-0.5 ${
                      s.up ? 'bg-[#d1fae5] text-[#047857]' : 'bg-[#fee2e2] text-[#b91c1c]'
                    }`}
                  >
                    {s.trend}
                  </span>
                </div>
              </div>
            ))}
          </div>

          {/* ── Users Table ── */}
          <div className="bg-[rgba(245,239,230,0.2)] border border-[#e5e0d8] rounded-xl overflow-hidden shadow-sm">
            {/* Table header bar */}
            <div className="flex items-center justify-between border-b border-[#e5e0d8] px-6 py-4">
              <h2 className="text-sm font-bold text-[#1a1a1a]">User Management</h2>
              <div className="flex gap-2">
                <button className="bg-[#1a1a1a] text-[#fcf6ef] text-xs font-bold px-3 py-1.5 rounded-lg">
                  Export CSV
                </button>
              </div>
            </div>

            {/* Column headers */}
            <div className="grid grid-cols-[2fr_2fr_1fr_1fr_100px] bg-[rgba(245,239,230,0.5)] border-b border-[#e5e0d8] px-6 py-3">
              {['User', 'Email', 'Role', 'Joined', 'Actions'].map((h) => (
                <span key={h} className="text-[#6b6b6b] text-[10px] font-bold uppercase tracking-widest">
                  {h}
                </span>
              ))}
            </div>

            {/* Rows */}
            {filtered.length === 0 ? (
              <div className="text-center py-10 text-[#6b6b6b] text-sm">
                No users found.
              </div>
            ) : (
              filtered.map((u, idx) => (
                <div
                  key={u.id}
                  className={`grid grid-cols-[2fr_2fr_1fr_1fr_100px] items-center px-6 py-4 ${
                    idx < filtered.length - 1
                      ? 'border-b border-[rgba(229,224,216,0.4)]'
                      : ''
                  } hover:bg-[rgba(245,239,230,0.3)] transition-colors`}
                >
                  {/* Name */}
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-[#1a1a1a] flex items-center justify-center text-white text-xs font-bold shrink-0">
                      {u.fullName.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <p className="font-bold text-[#1a1a1a] text-sm">{u.fullName}</p>
                    </div>
                  </div>

                  {/* Email */}
                  <p className="text-[#6b6b6b] text-sm truncate pr-4">{u.email}</p>

                  {/* Role */}
                  <span
                    className={`inline-block text-xs font-medium px-2.5 py-1 rounded-full ${
                      u.role === 'ADMIN'
                        ? 'bg-[#fef3c7] text-[#92400e]'
                        : 'bg-[#ecfdf5] text-[#047857]'
                    }`}
                  >
                    {u.role}
                  </span>

                  {/* Joined */}
                  <p className="text-[#6b6b6b] text-xs">{formatDate(u.createdAt)}</p>

                  {/* Actions */}
                  <div className="flex items-center gap-1">
                    <button
                      id={`delete-user-${u.id}`}
                      onClick={() => handleDeleteUser(u.id)}
                      disabled={deletingId === u.id || u.role === 'ADMIN'}
                      title={u.role === 'ADMIN' ? 'Cannot delete admin' : 'Delete user'}
                      className="p-2 rounded-lg hover:bg-red-50 text-[#6b6b6b] hover:text-red-600 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    >
                      {deletingId === u.id ? (
                        <div className="w-3.5 h-3.5 border-2 border-red-500 border-t-transparent rounded-full animate-spin" />
                      ) : (
                        <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      )}
                    </button>
                  </div>
                </div>
              ))
            )}

            {/* Pagination footer */}
            <div className="flex items-center justify-between border-t border-[#e5e0d8] px-6 py-4">
              <p className="text-[#6b6b6b] text-xs">
                Showing <strong className="text-[#1a1a1a]">{filtered.length}</strong> of{' '}
                <strong className="text-[#1a1a1a]">{users.length}</strong> users
              </p>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
