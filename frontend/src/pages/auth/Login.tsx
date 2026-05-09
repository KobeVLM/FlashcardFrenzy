import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { Mail, Lock, Layers, ArrowRight } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/decks';

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!email || !password) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post('/api/v1/auth/login', { email, password });
      const { accessToken, refreshToken, ...user } = response.data.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify(user));
      
      navigate(from, { replace: true });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials or network error.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-background min-h-screen flex flex-col items-center justify-center p-6 overflow-hidden relative">
      {/* Decorative Elements */}
      <div className="absolute -top-24 -left-24 w-96 h-96 bg-primary/5 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-primary/10 rounded-full blur-3xl pointer-events-none"></div>

      {/* Logo Area */}
      <div className="flex items-center gap-3 mb-12 relative z-10">
        <div className="w-12 h-12 bg-primary rounded-2xl flex items-center justify-center shadow-lg transform -rotate-3 group hover:rotate-0 transition-transform cursor-default">
          <Layers size={24} className="text-white" />
        </div>
        <div className="flex flex-col">
          <h1 className="font-black text-dark text-2xl tracking-tighter leading-none">FLASHCARD</h1>
          <h2 className="font-bold text-primary text-xl tracking-widest leading-none mt-1">FRENZY</h2>
        </div>
      </div>

      {/* Card */}
      <div className="bg-surface w-full max-w-[440px] rounded-3xl shadow-level-2 border border-border overflow-hidden relative z-10">
        <div className="p-10">
          <div className="mb-10 text-center">
            <h3 className="text-3xl font-black text-dark mb-3 tracking-tight">Welcome back</h3>
            <p className="text-secondary font-medium">Continue your path to mastery.</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-6">
            {error && (
              <div className="p-4 bg-red-50 border border-red-100 text-red-600 rounded-xl text-sm font-bold flex items-center gap-3 animate-shake">
                <div className="w-1.5 h-1.5 bg-red-600 rounded-full"></div>
                {error}
              </div>
            )}
            
            <div className="space-y-2">
              <label className="text-dark font-bold text-xs uppercase tracking-widest block ml-1">Email Address</label>
              <div className="relative group">
                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-muted group-focus-within:text-dark transition-colors">
                  <Mail size={18} />
                </div>
                <input 
                  type="email" 
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="scholar@example.com" 
                  className="w-full bg-background/50 border border-border rounded-xl py-4 pr-4 pl-12 text-dark placeholder-muted focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-dark transition-all font-medium"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex justify-between items-center ml-1">
                <label className="text-dark font-bold text-xs uppercase tracking-widest block">Password</label>
                <button type="button" className="text-xs font-bold text-primary hover:underline">Forgot?</button>
              </div>
              <div className="relative group">
                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-muted group-focus-within:text-dark transition-colors">
                  <Lock size={18} />
                </div>
                <input 
                  type="password" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••" 
                  className="w-full bg-background/50 border border-border rounded-xl py-4 pr-4 pl-12 text-dark placeholder-muted focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-dark transition-all font-medium"
                  required
                />
              </div>
            </div>

            <button 
              type="submit" 
              disabled={loading}
              className="w-full bg-primary hover:bg-primary-dark text-white font-black py-4 rounded-xl transition-all shadow-md disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center gap-2 group"
            >
              {loading ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>
                  Sign In
                  <ArrowRight size={20} className="group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </button>
          </form>

        </div>

        <div className="bg-background/50 p-6 text-center border-t border-border">
          <p className="text-secondary text-sm font-medium">
            New to the frenzy?{' '}
            <Link to="/register" className="font-black text-primary hover:underline">
              Create an account
            </Link>
          </p>
        </div>
      </div>

      {/* Footer metadata */}
      <div className="mt-12 text-center relative z-10">
        <p className="text-muted text-xs font-bold uppercase tracking-[0.2em]">Precision Learning • Systematic Mastery</p>
      </div>
    </div>
  );
}
