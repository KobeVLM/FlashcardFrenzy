import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Search, Layers, ChevronRight, Filter, LayoutGrid } from 'lucide-react';
import api from '../../services/api';

interface Deck {
  id: string;
  title: string;
  description: string;
  categoryId?: string;
  categoryName?: string;
  ownerName?: string;
  userId: string;
  createdAt: string;
  cardCount?: number;
  mastery?: number;
}

export default function BrowseDecks() {
  const [decks, setDecks] = useState<Deck[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchDecks();
  }, []);

  const fetchDecks = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await api.get('/decks');
      // In a real app, this might return public decks. 
      // For now, we use the standard endpoint and handle data mapping.
      setDecks(response.data.data || []);
    } catch (err: any) {
      setError('Failed to load decks. Please try again later.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filteredDecks = decks.filter(deck => 
    deck.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    deck.description?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex-1 p-8 md:p-12 overflow-y-auto w-full max-w-[1200px] mx-auto">
      {/* Header Area */}
      <div className="flex flex-col md:flex-row md:items-end justify-between mb-16 gap-8">
        <div className="space-y-3">
          <div className="flex items-center gap-3 text-primary mb-2">
            <LayoutGrid size={20} />
            <span className="text-[10px] font-black uppercase tracking-[0.3em]">Knowledge Hub</span>
          </div>
          <h1 className="text-5xl font-black text-dark tracking-tighter leading-tight">
            Explore Discovery
          </h1>
          <p className="text-secondary text-xl font-medium max-w-xl leading-relaxed">
            Uncover collections curated by global scholars to expand your repertoire.
          </p>
        </div>
        
        <Link 
          to="/decks/create" 
          className="bg-primary hover:bg-primary-dark text-white font-black py-4.5 px-9 rounded-2xl flex items-center gap-3 transition-all shadow-xl shadow-primary/20 active:scale-95 group"
        >
          <Plus size={20} className="group-hover:rotate-90 transition-transform duration-300" />
          Create Your Own
        </Link>
      </div>

      {/* Search & Filter Bar */}
      <div className="flex flex-col md:flex-row gap-4 mb-10">
        <div className="flex-1 relative group">
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-muted group-focus-within:text-primary transition-colors">
            <Search size={20} />
          </div>
          <input 
            type="text" 
            placeholder="Search for subjects, languages, or skills..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-white border border-border rounded-xl py-4 pr-4 pl-12 text-dark placeholder-muted focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-level-1"
          />
        </div>
        <button className="px-6 py-4 bg-white border border-border rounded-xl text-dark font-semibold flex items-center gap-2 hover:bg-background transition-colors shadow-level-1">
          <Filter size={18} />
          Filters
        </button>
      </div>

      {/* Content */}
      {error && (
        <div className="flex flex-col items-center justify-center py-20 bg-red-50/30 rounded-[32px] border border-red-100 mb-10 animate-in fade-in slide-in-from-bottom-4">
          <div className="w-16 h-16 bg-red-50 rounded-2xl flex items-center justify-center text-red-500 mb-6 shadow-sm">
            <LayoutGrid size={28} />
          </div>
          <h3 className="text-xl font-black text-dark mb-2">Sync Interrupted</h3>
          <p className="text-secondary mb-8 max-w-sm mx-auto">{error}</p>
          <button 
            onClick={fetchDecks}
            className="px-8 py-3 bg-white border border-red-200 text-red-600 font-black rounded-xl hover:bg-red-50 transition-all active:scale-95 shadow-sm"
          >
            Retry Connection
          </button>
        </div>
      )}

      {loading ? (
        <div className="space-y-12">
          <div className="flex items-center gap-4 animate-pulse">
            <div className="w-8 h-8 bg-surface rounded-lg"></div>
            <div className="h-4 bg-surface rounded w-32"></div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <div key={n} className="bg-white rounded-[32px] border border-border h-72 animate-pulse p-8 shadow-scholarly">
                <div className="flex justify-between mb-6">
                  <div className="h-6 bg-background rounded-lg w-20"></div>
                  <div className="h-4 bg-background rounded w-24"></div>
                </div>
                <div className="h-8 bg-background rounded-xl w-3/4 mb-4"></div>
                <div className="h-4 bg-background rounded w-full mb-2"></div>
                <div className="h-4 bg-background rounded w-full mb-8"></div>
                <div className="flex justify-between items-end mt-auto">
                  <div className="h-4 bg-background rounded w-1/4"></div>
                  <div className="h-12 bg-background rounded-xl w-12"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : filteredDecks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {filteredDecks.map((deck) => (
            <div key={deck.id} className="scholarly-card p-7 flex flex-col h-full group">
              <div className="flex justify-between items-start mb-4">
                <span className="chip chip-new">
                  {deck.categoryName || 'General'}
                </span>
                <div className="text-muted text-xs font-medium">
                  By {deck.ownerName || 'Scholar'}
                </div>
              </div>
              
              <h3 className="text-2xl font-black text-dark mb-3 line-clamp-2 leading-tight">
                {deck.title}
              </h3>
              
              <p className="text-secondary text-sm mb-8 flex-1 line-clamp-3 leading-relaxed">
                {deck.description || 'No description provided. Dive in to see the flashcards!'}
              </p>
              
              <div className="flex items-center justify-between pt-6 border-t border-border/50">
                <div className="flex items-center gap-2 text-muted font-bold text-xs uppercase tracking-widest">
                  <Layers size={14} className="text-primary" />
                  <span>{deck.cardCount || 0} Cards</span>
                </div>
                
                <button 
                  onClick={() => navigate(`/decks/${deck.id}`)}
                  className="p-2.5 bg-background rounded-lg text-dark hover:bg-primary hover:text-white transition-all transform active:scale-95"
                >
                  <ChevronRight size={20} />
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-24 text-center">
          <div className="w-24 h-24 bg-surface rounded-full flex items-center justify-center text-muted mb-6 shadow-level-1 border border-border">
            <Search size={40} />
          </div>
          <h3 className="text-2xl font-black text-dark mb-2">No decks found</h3>
          <p className="text-secondary max-w-md mx-auto">
            We couldn't find any decks matching "{searchQuery}". Try a different term or create your own deck!
          </p>
          <button 
            onClick={() => setSearchQuery('')}
            className="mt-8 text-primary font-black hover:underline"
          >
            Clear Search
          </button>
        </div>
      )}
    </div>
  );
}
