import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, BookOpen, Layers, Flame, Search, Filter, MoreVertical, LayoutGrid } from 'lucide-react';
import api from '../../services/api';

interface Deck {
  id: string;
  title: string;
  description: string;
  categoryId?: string;
  userId: string;
  createdAt: string;
  updatedAt: string;
  _count?: {
    flashcards: number;
  };
}

interface User {
  id: string;
  fullName: string;
  email: string;
}

export default function MyDecks() {
  const [decks, setDecks] = useState<Deck[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();
  const userRaw = localStorage.getItem('user');
  let user: User | null = null;
  try {
    user = userRaw && userRaw !== 'undefined' ? JSON.parse(userRaw) : null;
  } catch (e) {
    console.error("Failed to parse user in MyDecks", e);
  }


  useEffect(() => {
    fetchMyDecks();
  }, []);

  const fetchMyDecks = async () => {
    try {
      setLoading(true);
      const response = await api.get('/decks/my');
      setDecks(response.data.data || []);
    } catch (err: any) {
      setError('Failed to load your decks. Please try again later.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filteredDecks = decks.filter(deck => 
    deck.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex-1 p-8 md:p-14 overflow-y-auto w-full max-w-[1400px] mx-auto animate-in fade-in duration-700">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-16 gap-8">
        <div className="space-y-3">
          <div className="flex items-center gap-3 text-primary mb-2">
            <LayoutGrid size={20} />
            <span className="text-[10px] font-black uppercase tracking-[0.3em]">Personal Library</span>
          </div>
          <h1 className="text-5xl font-black text-dark tracking-tighter leading-tight">
            Greetings, {user?.fullName?.split(' ')[0] || 'Scholar'}
          </h1>
          <p className="text-secondary text-xl font-medium max-w-xl leading-relaxed">
            Your intellectual capital spans <span className="text-primary font-black">{decks.length} collections</span>. Ready to sharpen your edge?
          </p>
        </div>
        <Link 
          to="/decks/create" 
          className="bg-primary hover:bg-primary-dark text-white font-black py-4.5 px-9 rounded-2xl flex items-center gap-3 transition-all shadow-xl shadow-primary/20 active:scale-95 group"
        >
          <Plus size={20} className="group-hover:rotate-90 transition-transform duration-300" />
          Create New Deck
        </Link>
      </div>

      {/* Stats Area - Ambient Depth */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-10 mb-20">
        <div className="bg-white rounded-[32px] p-10 shadow-scholarly border border-border/50 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-background rounded-full -mr-16 -mt-16 group-hover:scale-110 transition-transform duration-500" />
          <div className="relative z-10">
            <div className="w-14 h-14 bg-primary/5 rounded-2xl flex items-center justify-center text-primary mb-6 shadow-inner">
              <Layers size={28} />
            </div>
            <p className="text-muted text-[11px] font-black uppercase tracking-[0.2em] mb-2">Total Collections</p>
            <p className="text-dark text-4xl font-black tracking-tight">{decks.length}</p>
          </div>
        </div>

        <div className="bg-dark rounded-[32px] p-10 shadow-2xl relative overflow-hidden group">
          <div className="absolute inset-0 bg-gradient-to-br from-primary/20 to-transparent opacity-50" />
          <div className="relative z-10">
            <div className="w-14 h-14 bg-white/10 rounded-2xl flex items-center justify-center text-primary mb-6 backdrop-blur-sm">
              <BookOpen size={28} />
            </div>
            <p className="text-white/60 text-[11px] font-black uppercase tracking-[0.2em] mb-2">Due for Review</p>
            <p className="text-white text-4xl font-black tracking-tight">{decks.length > 0 ? 12 : 0}</p>
          </div>
        </div>

        <div className="bg-white rounded-[32px] p-10 shadow-scholarly border border-border/50 relative overflow-hidden group">
          <div className="relative z-10">
            <div className="w-14 h-14 bg-orange-50 rounded-2xl flex items-center justify-center text-amber-500 mb-6 shadow-inner">
              <Flame size={28} />
            </div>
            <p className="text-muted text-[11px] font-black uppercase tracking-[0.2em] mb-2">Study Streak</p>
            <p className="text-dark text-4xl font-black tracking-tight">4 <span className="text-xl text-secondary">Days</span></p>
          </div>
        </div>
      </div>

      {/* Deck Controls */}
      <div className="flex flex-col md:flex-row items-center justify-between mb-10 gap-6">
        <div className="flex items-center gap-4">
          <h2 className="text-3xl font-black text-dark tracking-tighter">Your Collection</h2>
          <div className="h-1 w-12 bg-primary/20 rounded-full" />
        </div>
        <div className="flex gap-4 w-full md:w-auto">
          <div className="relative flex-1 md:w-80">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted" size={18} />
            <input 
              type="text" 
              placeholder="Filter by title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-white border border-border rounded-2xl py-3.5 pl-12 pr-5 text-sm font-medium focus:outline-none focus:ring-4 focus:ring-primary/5 focus:border-primary/50 transition-all shadow-sm"
            />
          </div>
          <button className="p-3.5 bg-white border border-border rounded-2xl text-secondary hover:bg-background hover:text-dark transition-all shadow-sm active:scale-95">
            <Filter size={20} />
          </button>
        </div>
      </div>

      {error && (
        <div className="p-5 bg-red-50 text-red-600 rounded-2xl mb-8 border border-red-100 font-bold flex items-center gap-4 animate-in slide-in-from-top-2">
          <div className="w-2.5 h-2.5 bg-red-600 rounded-full animate-pulse" />
          {error}
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
          {[1, 2, 3].map((n) => (
            <div key={n} className="bg-white rounded-[32px] border border-border h-72 animate-pulse shadow-sm"></div>
          ))}
        </div>
      ) : filteredDecks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
          {filteredDecks.map((deck) => (
            <div key={deck.id} className="scholarly-card group cursor-pointer" onClick={() => navigate(`/decks/${deck.id}`)}>
              <div className="h-2 w-full bg-background relative">
                <div className="absolute top-0 left-0 h-full bg-primary w-[35%] transition-all duration-1000 group-hover:w-[45%]" />
              </div>
              <div className="p-9 flex flex-col h-full">
                <div className="flex justify-between items-start mb-6">
                  <span className="chip chip-learning !rounded-xl !px-3 !py-1.5">
                    Learning
                  </span>
                  <button onClick={(e) => { e.stopPropagation(); }} className="text-muted hover:text-dark transition-colors p-1">
                    <MoreVertical size={20} />
                  </button>
                </div>
                
                <h3 className="text-2xl font-black text-dark mb-3 line-clamp-1 group-hover:text-primary transition-colors">
                  {deck.title}
                </h3>
                
                <p className="text-secondary text-sm mb-10 flex-1 line-clamp-2 leading-relaxed font-medium">
                  {deck.description || 'Manage and master this collection of flashcards.'}
                </p>
                
                <div className="flex items-center justify-between pt-8 border-t border-border/50">
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 bg-background rounded-xl flex items-center justify-center text-primary group-hover:bg-primary/10 transition-colors">
                      <Layers size={16} />
                    </div>
                    <span className="text-muted font-black text-[11px] uppercase tracking-widest">{deck._count?.flashcards || 0} Cards</span>
                  </div>
                  
                  <div className="text-primary group-hover:translate-x-1 transition-transform">
                    <Plus size={20} />
                  </div>
                </div>
              </div>
            </div>
          ))}

          {/* Create New Placeholder */}
          <Link to="/decks/create" className="bg-background/20 border-3 border-dashed border-border/60 rounded-[32px] flex flex-col items-center justify-center p-10 hover:bg-background/40 hover:border-primary/40 transition-all group min-h-[320px]">
            <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-muted group-hover:text-primary group-hover:shadow-xl transition-all mb-6">
              <Plus size={28} />
            </div>
            <h3 className="font-black text-secondary text-xl group-hover:text-dark transition-colors">Expand Library</h3>
            <p className="text-muted text-sm text-center mt-2 font-medium">Add a new subject to your repertoire</p>
          </Link>
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-32 text-center animate-in zoom-in-95 duration-500">
          <div className="w-24 h-24 bg-background rounded-[40px] flex items-center justify-center text-muted mb-8 shadow-inner border border-border">
            <Layers size={40} />
          </div>
          <h3 className="text-3xl font-black text-dark mb-3 tracking-tighter">Your Shelf is Empty</h3>
          <p className="text-secondary max-w-sm mx-auto text-lg font-medium leading-relaxed">
            The path to mastery begins with a single card. Let's build your intellectual foundation.
          </p>
          <div className="flex gap-5 mt-12">
            <Link to="/decks/create" className="bg-primary text-white font-black py-4 px-10 rounded-2xl shadow-xl shadow-primary/20 hover:scale-105 transition-all">Start Creating</Link>
            <Link to="/decks" className="bg-white border border-border text-dark font-black py-4 px-10 rounded-2xl hover:bg-background transition-all">Explore Others</Link>
          </div>
        </div>
      )}
    </div>
  );
}
