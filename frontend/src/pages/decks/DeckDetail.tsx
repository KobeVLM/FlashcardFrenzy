import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ChevronRight, Play, Plus, Edit, Trash2, BookOpen, Clock, BarChart3, Layers } from 'lucide-react';
import api from '../../services/api';

interface Flashcard {
  id: number;
  deckId: number;
  question: string;
  answer: string;
  tags?: string;
  createdAt: string;
}

interface Deck {
  id: number;
  title: string;
  category: string;
  description: string;
  userId: number;
  ownerName: string;
  createdAt: string;
}

export default function DeckDetail() {
  const { id } = useParams<{ id: string }>();
  const [deck, setDeck] = useState<Deck | null>(null);
  const [flashcards, setFlashcards] = useState<Flashcard[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchDeckDetails();
  }, [id]);

  const fetchDeckDetails = async () => {
    try {
      setLoading(true);
      setError('');
      
      // Fetch deck info first
      const deckRes = await api.get(`/decks/${id}`);
      setDeck(deckRes.data.data);
      
      // Then fetch cards
      try {
        const cardsRes = await api.get(`/decks/${id}/cards`);
        setFlashcards(cardsRes.data.data || []);
      } catch (cardsErr) {
        console.error('Failed to load flashcards:', cardsErr);
        // We don't set the global error here, as the deck info was loaded.
        // We could set a secondary error state if needed.
      }
    } catch (err: any) {
      setError(err.response?.status === 404 ? 'Deck not found.' : 'Failed to load deck details.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteCard = async (cardId: number) => {
    if (!confirm('Are you sure you want to delete this flashcard?')) return;
    try {
      await api.delete(`/cards/${cardId}`);
      setFlashcards(flashcards.filter(c => c.id !== cardId));
    } catch (err) {
      alert('Failed to delete flashcard.');
    }
  };

  if (loading) {
    return (
      <div className="flex-1 flex flex-col justify-center items-center min-h-[60vh] gap-4">
        <div className="w-12 h-12 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
        <p className="text-muted font-black text-[10px] uppercase tracking-[0.3em]">Gathering Knowledge...</p>
      </div>
    );
  }

  if (error || !deck) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center p-8 text-center min-h-[60vh]">
        <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center text-red-600 mb-6 border border-red-100 shadow-xl shadow-red-500/10">
          <Layers size={32} />
        </div>
        <h2 className="text-3xl font-black text-dark mb-3 tracking-tighter">Deck Unavailable</h2>
        <p className="text-secondary max-w-md mx-auto mb-8 font-medium leading-relaxed">
          {error || "We couldn't locate the collection you're looking for. It might have been moved or archived."}
        </p>
        <div className="flex gap-4">
          <Link to="/decks" className="px-8 py-4 bg-white border border-border rounded-2xl text-dark font-black transition-all hover:bg-background shadow-level-1">
            Back to Library
          </Link>
          <button onClick={fetchDeckDetails} className="px-8 py-4 bg-primary text-white rounded-2xl font-black transition-all hover:bg-primary-dark shadow-xl shadow-primary/20 active:scale-95">
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 p-8 md:p-14 overflow-y-auto w-full max-w-[1400px] mx-auto animate-in fade-in duration-700">
      <div className="max-w-[1000px] mx-auto">
        
        {/* Breadcrumbs */}
        <div className="flex items-center gap-3 mb-10 text-muted">
          <Link to="/decks/my" className="text-[10px] font-black uppercase tracking-[0.3em] hover:text-primary transition-colors">Library</Link>
          <ChevronRight size={14} className="opacity-30" />
          <span className="text-[10px] font-black uppercase tracking-[0.3em] text-dark">{deck.title}</span>
        </div>

        {/* Deck Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-8">
          <div className="flex items-center gap-4 flex-1">
            <div className="w-16 h-16 bg-[#df6020]/10 rounded-xl flex items-center justify-center shrink-0">
              <div className="w-8 h-8 bg-gradient-to-tr from-[#df6020] to-[#f9a8d4] rounded-sm opacity-80" style={{ clipPath: 'polygon(0 0, 100% 0, 100% 100%, 0 80%)' }}></div>
            </div>
            <div>
              <h1 className="font-bold text-[#0f172a] text-3xl tracking-tight mb-1">{deck.title}</h1>
              <p className="text-[#475569] text-base">{deck.description || 'No description'}</p>
            </div>
          </div>
          <div className="flex items-center gap-4 shrink-0">
            <button 
              onClick={() => navigate(`/quiz/${id}`)}
              className="bg-primary hover:bg-primary-dark text-white font-black py-3 px-8 rounded-2xl transition-all shadow-lg shadow-primary/20 flex items-center gap-2 active:scale-95"
            >
              <Play size={18} fill="currentColor" />
              Study Mode
            </button>
            <Link to={`/decks/${id}/cards/create`} className="border-2 border-[#e2e8f0] hover:bg-gray-50 text-[#0f172a] font-semibold py-2 px-6 rounded-lg transition-colors flex items-center gap-2">
              <Plus size={16} />
              Add Card
            </Link>
          </div>
        </div>

        {/* Stats Grid - Ambient Style */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-16">
          <div className="bg-white rounded-3xl p-6 shadow-scholarly border border-border/50">
            <div className="flex items-center gap-3 mb-3 text-primary">
              <BarChart3 size={16} />
              <p className="text-[10px] font-black uppercase tracking-widest text-muted">Mastery</p>
            </div>
            <p className="text-2xl font-black text-dark">0%</p>
          </div>
          <div className="bg-white rounded-3xl p-6 shadow-scholarly border border-border/50">
            <div className="flex items-center gap-3 mb-3 text-primary">
              <Layers size={16} />
              <p className="text-[10px] font-black uppercase tracking-widest text-muted">Total Cards</p>
            </div>
            <p className="text-2xl font-black text-dark">{flashcards.length}</p>
          </div>
          <div className="bg-white rounded-3xl p-6 shadow-scholarly border border-border/50">
            <div className="flex items-center gap-3 mb-3 text-primary">
              <Clock size={16} />
              <p className="text-[10px] font-black uppercase tracking-widest text-muted">New Today</p>
            </div>
            <p className="text-2xl font-black text-dark">0</p>
          </div>
          <div className="bg-white rounded-3xl p-6 shadow-scholarly border border-border/50">
            <div className="flex items-center gap-3 mb-3 text-primary">
              <BookOpen size={16} />
              <p className="text-[10px] font-black uppercase tracking-widest text-muted">Review</p>
            </div>
            <p className="text-2xl font-black text-dark">--</p>
          </div>
        </div>

        {/* Card List Section */}
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-[#0f172a] text-lg font-bold">Cards in this deck</h2>
        </div>

        <div className="space-y-6 mb-8">
          {flashcards.length === 0 ? (
            <div className="bg-background/20 border-3 border-dashed border-border/60 rounded-[32px] p-20 text-center">
              <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-muted mx-auto mb-6">
                <Plus size={28} />
              </div>
              <h3 className="text-xl font-black text-secondary">Library Empty</h3>
              <p className="text-muted text-sm mt-2 font-medium">Start building your knowledge base by adding your first card.</p>
            </div>
          ) : (
            flashcards.map((card) => {
              const tagsList = card.tags ? card.tags.split(',').map(t => t.trim()) : [];
              return (
                <div key={card.id} className="bg-white rounded-[32px] p-8 flex items-center justify-between group shadow-scholarly border border-border/50 hover:border-primary/30 transition-all">
                  <div className="flex-1">
                    <div className="flex items-start mb-4">
                      <div className="w-20 text-[10px] font-black uppercase tracking-widest text-primary pt-1.5 opacity-60">Prompt</div>
                      <div className="font-bold text-dark text-xl">{card.question}</div>
                    </div>
                    <div className="flex items-start mb-4 pt-4 border-t border-background">
                      <div className="w-20 text-[10px] font-black uppercase tracking-widest text-muted pt-1.5 opacity-60">Response</div>
                      <div className="text-secondary text-lg font-medium">{card.answer}</div>
                    </div>
                    {tagsList.length > 0 && (
                      <div className="flex gap-2 pl-20">
                        {tagsList.map(tag => (
                          <span key={tag} className="chip chip-learning !lowercase !tracking-normal !px-2.5 !py-1">
                            #{tag}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                  
                  <div className="flex flex-col gap-3 opacity-0 group-hover:opacity-100 transition-all translate-x-4 group-hover:translate-x-0">
                    <Link to={`/cards/${card.id}/edit`} className="p-3 bg-background text-muted hover:text-primary hover:bg-primary/5 rounded-2xl transition-all shadow-sm">
                      <Edit size={18} />
                    </Link>
                    <button onClick={() => handleDeleteCard(card.id)} className="p-3 bg-background text-muted hover:text-red-600 hover:bg-red-50 rounded-2xl transition-all shadow-sm">
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
