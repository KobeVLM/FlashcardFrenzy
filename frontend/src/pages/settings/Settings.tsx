import { useState } from 'react';
import api from '../../services/api';

interface UserInfo {
  fullName?: string;
  email?: string;
  role?: string;
}

export default function Settings() {
  const raw = localStorage.getItem('user');
  let user: UserInfo = {} as UserInfo;
  try {
    user = raw && raw !== 'undefined' ? JSON.parse(raw) : {} as UserInfo;
  } catch (e) {
    console.error("Failed to parse user in Settings", e);
  }

  // Change password state
  const [pwForm, setPwForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [pwLoading, setPwLoading] = useState(false);
  const [pwError, setPwError] = useState('');
  const [pwSuccess, setPwSuccess] = useState(false);

  // Delete account dialog
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const handlePwChange = (field: string, value: string) => {
    setPwForm((f) => ({ ...f, [field]: value }));
    setPwError('');
    setPwSuccess(false);
  };

  const handleSavePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      setPwError('New passwords do not match.');
      return;
    }
    if (pwForm.newPassword.length < 8) {
      setPwError('New password must be at least 8 characters.');
      return;
    }
    setPwLoading(true);
    try {
      await api.put('/auth/password', {
        currentPassword: pwForm.currentPassword,
        newPassword: pwForm.newPassword,
      });
      setPwSuccess(true);
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setPwError(msg ?? 'Failed to update password. Please try again.');
    } finally {
      setPwLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      await api.delete('/auth/account');
      localStorage.clear();
      window.location.href = '/login';
    } catch {
      setShowDeleteConfirm(false);
      alert('Failed to delete account. Please try again.');
    }
  };

  const inputClass =
    'w-full bg-white border border-[#e5dfdc] rounded-lg px-4 py-3.5 text-[#1a1a1a] text-base placeholder-[#6b7280] focus:outline-none focus:ring-2 focus:ring-[rgba(26,26,26,0.15)]';
  const labelClass =
    'block text-[#876f64] text-sm font-semibold uppercase tracking-wider mb-2';

  return (
    <div className="flex flex-col gap-10 max-w-2xl" id="settings-page">
      {/* ── Header ── */}
      <div>
        <h1 className="text-4xl font-black text-[#1a1a1a] tracking-tight">Settings</h1>
        <p className="text-[#876f64] text-base mt-2">
          Manage your account preferences and security settings.
        </p>
      </div>

      {/* ── Account Info ── */}
      <section className="flex flex-col gap-6">
        <div className="border-b border-[#e5dfdc] pb-3">
          <h2 className="text-xl font-bold text-[#1a1a1a]">Account</h2>
        </div>

        <div className="grid grid-cols-[160px_1fr] gap-y-6">
          <span className={`${labelClass} pt-0.5`}>Full Name</span>
          <span className="text-[#1a1a1a] font-medium text-base self-center">
            {user.fullName ?? '—'}
          </span>

          <div className="border-t border-[rgba(229,223,220,0.5)] col-span-2" />

          <span className={`${labelClass} pt-0.5`}>Email</span>
          <span className="text-[#1a1a1a] text-base self-center">
            {user.email ?? '—'}
          </span>

          <div className="border-t border-[rgba(229,223,220,0.5)] col-span-2" />

          <span className={`${labelClass} pt-0.5`}>Role</span>
          <span className="text-[#1a1a1a] text-base self-center capitalize">
            {(user.role ?? 'USER').toLowerCase()}
          </span>
        </div>
      </section>

      {/* ── Change Password ── */}
      <section className="flex flex-col gap-6">
        <div className="border-b border-[#e5dfdc] pb-3">
          <h2 className="text-xl font-bold text-[#1a1a1a]">Change Password</h2>
        </div>

        <form onSubmit={handleSavePassword} className="flex flex-col gap-5 max-w-md">
          <div>
            <label className={labelClass}>Current Password</label>
            <input
              id="current-password"
              type="password"
              className={inputClass}
              placeholder="••••••••"
              value={pwForm.currentPassword}
              onChange={(e) => handlePwChange('currentPassword', e.target.value)}
              required
            />
          </div>
          <div>
            <label className={labelClass}>New Password</label>
            <input
              id="new-password"
              type="password"
              className={inputClass}
              placeholder="••••••••"
              value={pwForm.newPassword}
              onChange={(e) => handlePwChange('newPassword', e.target.value)}
              required
            />
          </div>
          <div>
            <label className={labelClass}>Confirm New Password</label>
            <input
              id="confirm-password"
              type="password"
              className={inputClass}
              placeholder="••••••••"
              value={pwForm.confirmPassword}
              onChange={(e) => handlePwChange('confirmPassword', e.target.value)}
              required
            />
          </div>

          {pwError && (
            <p className="text-sm text-red-600 font-medium">{pwError}</p>
          )}
          {pwSuccess && (
            <p className="text-sm text-green-600 font-medium">
              ✓ Password updated successfully.
            </p>
          )}

          <div className="pt-2">
            <button
              id="save-password-btn"
              type="submit"
              disabled={pwLoading}
              className="bg-[#1a1a1a] text-white font-bold px-8 py-3 rounded-lg text-sm shadow-sm hover:bg-[#333] disabled:opacity-60 transition-colors"
            >
              {pwLoading ? 'Saving...' : 'Save Password'}
            </button>
          </div>
        </form>
      </section>

      {/* ── Danger Zone ── */}
      <section className="flex flex-col gap-6 pt-4">
        <div className="border-b border-[#fee2e2] pb-3">
          <h2 className="text-xl font-bold text-[#991b1b]">Danger Zone</h2>
        </div>

        <div className="bg-[rgba(254,242,242,0.5)] border border-[#fee2e2] rounded-xl p-6 flex items-center justify-between">
          <div>
            <p className="font-bold text-[#1a1a1a] text-sm">Delete your account</p>
            <p className="text-[#876f64] text-xs mt-1">
              Once you delete your account, there is no going back. Please be certain.
            </p>
          </div>
          <button
            id="delete-account-btn"
            onClick={() => setShowDeleteConfirm(true)}
            className="border-2 border-[#1a1a1a] text-[#1a1a1a] font-bold px-6 py-2 rounded-lg text-sm hover:bg-[#1a1a1a] hover:text-white transition-colors ml-4 shrink-0"
          >
            Delete Account
          </button>
        </div>
      </section>

      {/* ── Support Footer ── */}
      <div className="border-t border-[#e5dfdc] pt-8 pb-4">
        <p className="text-[#876f64] text-xs text-center">
          Flashcard Frenzy v2.4.0 — Made with focus in mind.
        </p>
      </div>

      {/* ── Delete Confirm Modal ── */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-8 max-w-sm w-full mx-4 shadow-2xl">
            <h3 className="text-xl font-black text-[#1a1a1a] mb-2">Are you sure?</h3>
            <p className="text-[#876f64] text-sm mb-6">
              This action is <strong>permanent</strong>. All your decks and flashcards will be
              deleted. This cannot be undone.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="flex-1 border-2 border-[#e5dfdc] text-[#876f64] font-bold py-2.5 rounded-lg text-sm hover:border-[#1a1a1a] hover:text-[#1a1a1a] transition-colors"
              >
                Cancel
              </button>
              <button
                id="confirm-delete-btn"
                onClick={handleDeleteAccount}
                className="flex-1 bg-red-600 text-white font-bold py-2.5 rounded-lg text-sm hover:bg-red-700 transition-colors"
              >
                Yes, Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
