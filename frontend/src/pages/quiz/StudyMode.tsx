import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ChevronLeft, 
  RotateCcw, 
  CheckCircle2, 
  XCircle, 
  Trophy,
  BookOpen,
  ArrowRight,
  Layers,
  Layout
} from 'lucide-react';
import api from '../../services/api';

interface Flashcard {
  id: number;
  front: string;
  back: string;
}

interface Deck {
  id: number;
  title: string;
  flashcards: Flashcard[];
}

export default function StudyMode() {
  const { deckId } = useParams();
  const navigate = useNavigate();
  const [deck, setDeck] = useState<Deck | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [answers, setAnswers] = useState<{ id: number; correct: boolean }[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDeck();
  }, [deckId]);

  const fetchDeck = async () => {
    try {
      setLoading(true);
      // Fetch deck and cards
      const [deckRes, cardsRes] = await Promise.all([
        api.get(`/decks/${deckId}`),
        api.get(`/decks/${deckId}/cards`)
      ]);
      
      const cards = cardsRes.data.data.map((c: any) => ({
        id: c.id,
        front: c.question,
        back: c.answer
      }));

      setDeck({
        id: deckRes.data.data.id,
        title: deckRes.data.data.title,
        flashcards: cards
      });
    } catch (err) {
      setError('Failed to load study material.');
    } finally {
      setLoading(false);
    }
  };

  const handleFlip = () => setIsFlipped(!isFlipped);

  const handleAnswer = (correct: boolean) => {
    if (!deck) return;
    
    const newAnswers = [...answers, { id: deck.flashcards[currentIndex].id, correct }];
    setAnswers(newAnswers);

    if (currentIndex < deck.flashcards.length - 1) {
      setIsFlipped(false);
      setTimeout(() => {
        setCurrentIndex(currentIndex + 1);
      }, 300);
    } else {
      setShowResults(true);
      saveQuizResult(newAnswers);
    }
  };

  const saveQuizResult = async (finalAnswers: { id: number; correct: boolean }[]) => {
    try {
      const correct = finalAnswers.filter(a => a.correct).length;
      const score = Math.round((correct / (deck?.flashcards.length || 1)) * 100);
      await api.post('/quizzes/results', {
        deckId: Number(deckId),
        score,
        timeSpent: 0 // Simplification for now
      });
    } catch (err) {
      console.error('Failed to save progress:', err);
    }
  };

  const resetQuiz = () => {
    setCurrentIndex(0);
    setIsFlipped(false);
    setShowResults(false);
    setAnswers([]);
  };

  if (loading) return (
    <div className="flex-1 flex flex-col items-center justify-center p-8 min-h-screen bg-surface">
      <div className="w-16 h-16 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-6"></div>
      <p className="text-secondary font-black text-sm uppercase tracking-[0.2em] animate-pulse">Initializing Knowledge Base...</p>
    </div>
  );

  if (error || !deck || deck.flashcards.length === 0) return (
    <div className="flex-1 flex flex-col items-center justify-center p-8 min-h-screen bg-surface">
      <div className="w-20 h-20 bg-red-50 text-red-500 rounded-3xl flex items-center justify-center mb-6">
        <XCircle size={40} />
      </div>
      <h2 className="text-3xl font-black text-dark mb-4 tracking-tighter">Access Denied</h2>
      <p className="text-secondary text-center max-w-sm font-medium mb-10">{error || 'Session expired or invalid deck ID.'}</p>
      <button onClick={() => navigate('/decks')} className="bg-primary text-white font-black px-8 py-4 rounded-2xl shadow-xl shadow-primary/20 hover:scale-105 transition-all">
        Back to Library
      </button>
    </div>
  );

  const currentCard = deck.flashcards[currentIndex];
  const progress = ((currentIndex + 1) / deck.flashcards.length) * 100;

  if (showResults) {
    const score = answers.filter(a => a.correct).length;
    const percentage = Math.round((score / deck.flashcards.length) * 100);

    return (
      <div className="flex-1 p-8 md:p-14 overflow-y-auto w-full max-w-[1000px] mx-auto animate-in fade-in duration-700">
        <div className="bg-white rounded-[40px] shadow-scholarly border border-border/50 p-12 md:p-20 text-center relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-3 bg-gradient-to-r from-primary via-orange-400 to-primary" />
          
          <div className="w-28 h-28 bg-primary/10 rounded-[40px] flex items-center justify-center text-primary mx-auto mb-10 shadow-inner">
            <Trophy size={56} />
          </div>

          <h2 className="text-5xl font-black text-dark mb-4 tracking-tighter">Session Complete!</h2>
          <p className="text-secondary text-xl font-medium mb-16">You've successfully mastered this collection.</p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-20">
            <div className="p-8 bg-background/50 rounded-[32px] border border-border">
              <p className="text-muted text-[10px] font-black uppercase tracking-[0.2em] mb-2">Final Grade</p>
              <p className="text-primary text-5xl font-black tracking-tight">{percentage}%</p>
            </div>
            <div className="p-8 bg-background/50 rounded-[32px] border border-border">
              <p className="text-muted text-[10px] font-black uppercase tracking-[0.2em] mb-2">Correct Hits</p>
              <p className="text-dark text-5xl font-black tracking-tight">{score}</p>
            </div>
            <div className="p-8 bg-background/50 rounded-[32px] border border-border">
              <p className="text-muted text-[10px] font-black uppercase tracking-[0.2em] mb-2">Total Load</p>
              <p className="text-dark text-5xl font-black tracking-tight">{deck.flashcards.length}</p>
            </div>
          </div>

          <div className="flex flex-col md:flex-row gap-5 justify-center">
            <button 
              onClick={resetQuiz}
              className="bg-dark hover:bg-black text-white font-black py-5 px-12 rounded-2xl flex items-center justify-center gap-3 transition-all active:scale-95 shadow-xl"
            >
              <RotateCcw size={20} />
              Repeat Session
            </button>
            <button 
              onClick={() => navigate('/decks')}
              className="bg-white border border-border hover:bg-background text-dark font-black py-5 px-12 rounded-2xl flex items-center justify-center gap-3 transition-all active:scale-95 shadow-sm"
            >
              Return to Library
              <ArrowRight size={20} />
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col h-screen overflow-hidden animate-in fade-in duration-500 bg-surface">
      {/* Immersive Header */}
      <header className="p-8 md:px-14 flex items-center justify-between border-b border-border bg-white z-10">
        <div className="flex items-center gap-6">
          <button 
            onClick={() => navigate(`/decks/${deckId}`)}
            className="w-12 h-12 flex items-center justify-center rounded-2xl hover:bg-background border border-transparent hover:border-border transition-all group"
          >
            <ChevronLeft size={24} className="group-hover:-translate-x-1 transition-transform" />
          </button>
          <div>
            <div className="flex items-center gap-2 text-primary mb-1">
              <BookOpen size={16} />
              <span className="text-[10px] font-black uppercase tracking-[0.25em]">Active Session</span>
            </div>
            <h1 className="text-xl font-black text-dark tracking-tight line-clamp-1">{deck.title}</h1>
          </div>
        </div>

        <div className="hidden md:flex items-center gap-10">
          <div className="flex flex-col items-end">
            <span className="text-muted text-[10px] font-black uppercase tracking-widest mb-1.5">Progress</span>
            <div className="flex items-center gap-4">
              <div className="w-48 h-2.5 bg-background rounded-full overflow-hidden border border-border shadow-inner">
                <div 
                  className="h-full bg-primary transition-all duration-500" 
                  style={{ width: `${progress}%` }}
                />
              </div>
              <span className="text-dark font-black text-sm tabular-nums">{currentIndex + 1} / {deck.flashcards.length}</span>
            </div>
          </div>
        </div>
      </header>

      {/* 3D Study Canvas */}
      <main className="flex-1 relative flex flex-col items-center justify-center p-8">
        <div className="w-full max-w-[800px] h-[480px] perspective-[2000px] relative z-10 group">
          <div 
            onClick={handleFlip}
            className={`relative w-full h-full cursor-pointer transition-all duration-700 preserve-3d shadow-2xl rounded-[40px] ${isFlipped ? 'rotate-y-180' : ''}`}
          >
            {/* Front Side */}
            <div className="absolute inset-0 w-full h-full backface-hidden bg-white rounded-[40px] border border-border flex flex-col p-16 md:p-24 shadow-scholarly group-hover:shadow-scholarly-hover transition-all duration-500">
              <div className="flex justify-between items-start mb-auto">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 bg-primary/5 rounded-xl flex items-center justify-center text-primary">
                    <Layout size={16} />
                  </div>
                  <span className="text-muted text-[11px] font-black uppercase tracking-widest">Question</span>
                </div>
                <span className="w-10 h-10 bg-background rounded-full flex items-center justify-center text-muted border border-border shadow-inner font-bold text-xs">{currentIndex + 1}</span>
              </div>
              
              <div className="flex-1 flex flex-col justify-center text-center">
                <h2 className="text-4xl md:text-5xl font-black text-dark tracking-tight leading-tight">
                  {currentCard.front}
                </h2>
              </div>
              
              <div className="mt-auto flex justify-center pt-10">
                <div className="flex items-center gap-2 text-muted font-bold text-xs uppercase tracking-widest animate-bounce">
                  <RotateCcw size={14} />
                  <span>Click to reveal</span>
                </div>
              </div>
            </div>

            {/* Back Side */}
            <div className="absolute inset-0 w-full h-full backface-hidden bg-dark text-white rounded-[40px] flex flex-col p-16 md:p-24 rotate-y-180 shadow-2xl">
              <div className="absolute inset-0 bg-gradient-to-br from-white/5 to-transparent pointer-events-none" />
              
              <div className="flex justify-between items-start mb-auto relative z-10">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 bg-white/10 rounded-xl flex items-center justify-center text-primary">
                    <Layers size={16} />
                  </div>
                  <span className="text-white/40 text-[11px] font-black uppercase tracking-widest">Answer</span>
                </div>
              </div>
              
              <div className="flex-1 flex flex-col justify-center text-center relative z-10">
                <p className="text-3xl md:text-4xl font-black leading-relaxed tracking-tight">
                  {currentCard.back}
                </p>
              </div>
              
              <div className="mt-auto flex justify-center pt-10 relative z-10">
                <div className="px-6 py-2 bg-white/5 border border-white/10 rounded-full text-[10px] font-black uppercase tracking-widest text-white/50">
                  Select your result below
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Action Controls */}
        <div className={`mt-16 flex items-center gap-8 transition-all duration-500 ${isFlipped ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10 pointer-events-none'}`}>
          <button 
            onClick={(e) => { e.stopPropagation(); handleAnswer(false); }}
            className="group flex flex-col items-center gap-3"
          >
            <div className="w-20 h-20 bg-white border-2 border-red-100 rounded-[32px] flex items-center justify-center text-red-500 shadow-lg shadow-red-100 group-hover:bg-red-500 group-hover:text-white group-hover:scale-110 transition-all duration-300">
              <XCircle size={32} />
            </div>
            <span className="text-secondary font-black text-[10px] uppercase tracking-widest">Inaccurate</span>
          </button>

          <button 
            onClick={(e) => { e.stopPropagation(); handleAnswer(true); }}
            className="group flex flex-col items-center gap-3"
          >
            <div className="w-24 h-24 bg-primary rounded-[40px] flex items-center justify-center text-white shadow-2xl shadow-primary/30 group-hover:scale-110 active:scale-95 transition-all duration-300">
              <CheckCircle2 size={40} />
            </div>
            <span className="text-primary font-black text-xs uppercase tracking-[0.2em]">Correct</span>
          </button>

          <button 
            onClick={(e) => { e.stopPropagation(); handleFlip(); }}
            className="group flex flex-col items-center gap-3"
          >
            <div className="w-20 h-20 bg-white border-2 border-border rounded-[32px] flex items-center justify-center text-dark shadow-lg group-hover:bg-dark group-hover:text-white group-hover:scale-110 transition-all duration-300">
              <RotateCcw size={32} />
            </div>
            <span className="text-secondary font-black text-[10px] uppercase tracking-widest">Undo Flip</span>
          </button>
        </div>
      </main>
    </div>
  );
}
