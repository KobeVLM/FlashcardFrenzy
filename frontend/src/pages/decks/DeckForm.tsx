import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ChevronLeft, Info } from 'lucide-react';
import api from '../../services/api';

export default function DeckForm() {
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isEditing) {
      fetchDeck();
    }
  }, [id]);

  const fetchDeck = async () => {
    try {
      const response = await api.get(`/decks/${id}`);
      const deck = response.data.data;
      setTitle(deck.title);
      setCategory(deck.category || '');
      setDescription(deck.description || '');
    } catch (err) {
      console.error(err);
      setError('Failed to fetch deck details.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Deck title is required');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const payload = { title, category, description };
      
      if (isEditing) {
        await api.put(`/decks/${id}`, payload);
        navigate(`/decks/my`);
      } else {
        const response = await api.post('/decks', payload);
        navigate(`/decks/${response.data.data.id}`);
      }
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to save deck');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-1 overflow-y-auto w-full relative bg-[#fcf6ef]">
      {/* Top Bar */}
      <div className="sticky top-0 z-10 backdrop-blur-sm bg-[#fcf6ef]/80 border-b border-[rgba(26,26,26,0.05)] h-16 px-8 flex items-center justify-between">
        <Link to="/decks/my" className="flex items-center gap-2 text-[#475569] hover:text-[#0f172a] transition-colors font-medium text-sm">
          <ChevronLeft size={16} />
          Back to Decks
        </Link>
      </div>

      {/* Main Content */}
      <div className="max-w-2xl mx-auto py-12 px-6">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-[#0f172a] tracking-tight mb-2">
            {isEditing ? 'Edit Deck' : 'Create New Deck'}
          </h1>
          <p className="text-[#475569]">
            Organize your learning by grouping flashcards into topical decks.
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-lg text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="bg-white border border-[rgba(26,26,26,0.1)] rounded-xl p-8 shadow-sm mb-8">
          
          <div className="mb-6">
            <label className="block text-sm font-semibold text-[#0f172a] mb-2">
              Deck Title <span className="text-[#df6020]">*</span>
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Advanced Neuroscience, Spanish Basics"
              className="w-full bg-[#fcf6ef] border border-[rgba(26,26,26,0.1)] rounded-lg px-4 py-3 text-[#0f172a] placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-[#df6020]/20 focus:border-[#df6020] transition-all"
            />
          </div>

          <div className="mb-6">
            <label className="block text-sm font-semibold text-[#0f172a] mb-2">
              Category <span className="text-gray-400 font-normal">(Optional)</span>
            </label>
            <input
              type="text"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              placeholder="e.g., Science, Language"
              className="w-full bg-[#fcf6ef] border border-[rgba(26,26,26,0.1)] rounded-lg px-4 py-3 text-[#0f172a] placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-[#df6020]/20 focus:border-[#df6020] transition-all"
            />
          </div>

          <div className="mb-8">
            <label className="block text-sm font-semibold text-[#0f172a] mb-2">
              Description <span className="text-gray-400 font-normal">(Optional)</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="What is this deck about? Adding a description helps you keep track of your goals."
              rows={3}
              className="w-full bg-[#fcf6ef] border border-[rgba(26,26,26,0.1)] rounded-lg px-4 py-3 text-[#0f172a] placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-[#df6020]/20 focus:border-[#df6020] transition-all resize-none"
            />
          </div>

          <div className="flex gap-4 justify-center border-t border-[rgba(26,26,26,0.05)] pt-6">
            <Link to="/decks/my" className="w-1/2 flex justify-center py-3 px-4 border border-[rgba(26,26,26,0.2)] rounded-lg font-semibold text-[#0f172a] hover:bg-gray-50 transition-colors">
              Cancel
            </Link>
            <button
              type="submit"
              disabled={loading}
              className="w-1/2 bg-[#0f172a] hover:bg-[#1e293b] text-white py-3 px-4 rounded-lg font-semibold transition-colors shadow-sm disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {loading ? 'Saving...' : 'Save Deck'}
            </button>
          </div>

        </form>

        {!isEditing && (
          <div className="bg-[rgba(223,96,32,0.05)] border border-[rgba(223,96,32,0.1)] rounded-lg p-4 flex gap-4 items-start">
            <Info className="text-[#df6020] shrink-0 mt-0.5" size={20} />
            <div>
              <h4 className="font-semibold text-[#0f172a] text-sm mb-1">Quick Tip</h4>
              <p className="text-sm text-[#475569]">
                You can add flashcards immediately after creating the deck. Use tags to further categorize your cards within the deck.
              </p>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
