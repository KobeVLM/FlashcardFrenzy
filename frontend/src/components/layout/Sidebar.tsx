import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Layers, Folder, History, Shield, LogIn, UserPlus, LogOut, Settings, LayoutGrid } from 'lucide-react';

export default function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();

  // Read real auth state from localStorage
  const token = localStorage.getItem('accessToken');
  const userRaw = localStorage.getItem('user');
  
  let user = null;
  try {
    user = userRaw && userRaw !== 'undefined' ? JSON.parse(userRaw) : null;
  } catch (e) {
    console.error("Failed to parse user from localStorage", e);
  }

  const isAuthenticated = !!token;
  const isAdmin = user?.role === 'ADMIN';

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="w-72 bg-surface border-r border-border hidden md:flex flex-col h-screen sticky top-0 z-20 overflow-hidden">
      {/* Brand Logo - More premium feel */}
      <div className="p-10">
        <Link to="/" className="flex items-center gap-4 group">
          <div className="w-11 h-11 bg-primary rounded-2xl flex items-center justify-center shadow-lg transform group-hover:rotate-6 transition-all duration-300">
            <Layers size={24} className="text-white" />
          </div>
          <div className="flex flex-col">
            <span className="text-dark font-black text-lg tracking-tighter leading-none">FLASHCARD</span>
            <span className="text-primary font-bold text-[10px] tracking-[0.3em] leading-none mt-1.5 uppercase opacity-80">Frenzy</span>
          </div>
        </Link>
      </div>

      {/* Navigation Groups */}
      <div className="flex-1 px-6 space-y-10 overflow-y-auto py-4">
        {/* Core Discovery */}
        <section>
          <h4 className="px-4 text-[10px] font-black text-muted uppercase tracking-[0.25em] mb-5 opacity-60">Discovery</h4>
          <div className="space-y-1.5">
            <NavItem
              label="Explore Decks"
              path="/decks"
              icon={<LayoutGrid size={18} />}
              active={location.pathname === '/decks'}
            />
          </div>
        </section>

        {/* Personal Space */}
        {isAuthenticated && (
          <section>
            <h4 className="px-4 text-[10px] font-black text-muted uppercase tracking-[0.25em] mb-5 opacity-60">Library</h4>
            <div className="space-y-1.5">
              <NavItem
                label="My Collection"
                path="/decks/my"
                icon={<Folder size={18} />}
                active={location.pathname === '/decks/my'}
              />
              <NavItem
                label="Quiz Stats"
                path="/quiz/history"
                icon={<History size={18} />}
                active={location.pathname === '/quiz/history'}
              />
              <NavItem
                label="Preferences"
                path="/settings"
                icon={<Settings size={18} />}
                active={location.pathname === '/settings'}
              />
            </div>
          </section>
        )}

        {/* Admin Dashboard */}
        {isAdmin && (
          <section>
            <h4 className="px-4 text-[10px] font-black text-muted uppercase tracking-[0.25em] mb-5 opacity-60">Control</h4>
            <div className="space-y-1.5">
              <NavItem
                label="Admin Portal"
                path="/admin"
                icon={<Shield size={18} />}
                active={location.pathname === '/admin'}
              />
            </div>
          </section>
        )}
      </div>

      {/* Footer Account Section */}
      <div className="p-6 mt-auto">
        {isAuthenticated ? (
          <div className="bg-background/40 border border-border rounded-[24px] p-5 relative group overflow-hidden">
            <div className="absolute inset-0 bg-primary/5 opacity-0 group-hover:opacity-100 transition-opacity" />
            <div className="flex items-center gap-3 mb-5 relative z-10">
              <div className="w-12 h-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary font-black text-base border border-primary/10 shadow-inner">
                {user?.fullName?.charAt(0) || 'S'}
              </div>
              <div className="flex flex-col min-w-0">
                <p className="text-dark font-black text-sm truncate tracking-tight">{user?.fullName || 'Scholar'}</p>
                <div className="flex items-center gap-1.5">
                  <div className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-pulse" />
                  <p className="text-secondary text-[10px] font-bold uppercase tracking-wider truncate">{user?.role || 'USER'}</p>
                </div>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="w-full flex items-center justify-center gap-2.5 px-4 py-3 bg-white border border-border rounded-xl text-dark hover:bg-red-50 hover:text-red-600 hover:border-red-100 transition-all text-[11px] font-black uppercase tracking-widest shadow-sm active:scale-95 relative z-10"
            >
              <LogOut size={14} />
              End Session
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            <Link
              to="/login"
              className="w-full flex items-center justify-center gap-2.5 px-4 py-4 bg-primary text-white rounded-2xl text-[11px] font-black uppercase tracking-widest shadow-lg shadow-primary/20 hover:bg-primary-dark transition-all active:scale-95"
            >
              <LogIn size={16} />
              Sign In
            </Link>
            <Link
              to="/register"
              className="w-full flex items-center justify-center gap-2.5 px-4 py-4 bg-white border border-border text-dark rounded-2xl text-[11px] font-black uppercase tracking-widest hover:bg-background transition-all active:scale-95"
            >
              <UserPlus size={16} />
              Create Account
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}

function NavItem({
  label,
  path,
  icon,
  active,
}: {
  label: string;
  path: string;
  icon: React.ReactNode;
  active: boolean;
}) {
  return (
    <Link
      to={path}
      className={`flex items-center gap-3.5 px-5 py-3.5 rounded-2xl text-sm font-bold transition-all duration-300 relative group ${
        active
          ? 'bg-primary text-white shadow-lg shadow-primary/20 scale-[1.02]'
          : 'text-secondary hover:bg-background hover:text-dark'
      }`}
    >
      <span className={`${active ? 'text-white' : 'text-primary group-hover:scale-110 transition-transform'}`}>
        {icon}
      </span>
      <span className="tracking-tight">{label}</span>
      {active && (
        <div className="absolute right-3 w-1.5 h-1.5 bg-white/40 rounded-full" />
      )}
    </Link>
  );
}
