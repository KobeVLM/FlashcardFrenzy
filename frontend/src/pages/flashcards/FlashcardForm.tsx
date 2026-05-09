import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ChevronLeft, Info, X } from 'lucide-react';
import api from '../../services/api';

export default function FlashcardForm() {
  const { id: deckId, cardId } = useParams<{ id?: string, cardId?: string }>();
  const isEditing = !!cardId;
  const navigate = useNavigate();

  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [tagsInput, setTagsInput] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isEditing) {
      fetchCard();
    }
  }, [cardId]);

  const fetchCard = async () => {
    try {
      const response = await api.get(`/cards/${cardId}`);
      const card = response.data.data;
      setQuestion(card.question);
      setAnswer(card.answer);
      if (card.tags) {
        setTags(card.tags.split(',').map((t: string) => t.trim()));
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch flashcard details.');
    }
  };

  const handleAddTag = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      const newTag = tagsInput.trim().toUpperCase();
      if (newTag && !tags.includes(newTag)) {
        setTags([...tags, newTag]);
      }
      setTagsInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  const handleSubmit = async () => {
    if (!question.trim() || !answer.trim()) {
      setError('Both Front side (Question) and Back side (Answer) are required.');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const payload = { 
        question, 
        answer, 
        tags: tags.join(',') 
      };
      
      if (isEditing) {
        // Find deck id to redirect back.
        // It's possible we came from /decks/:id, but for edit route we might just have /cards/:cardId/edit
        // But let's assume we fetch the card which has deckId.
        const response = await api.put(`/cards/${cardId}`, payload);
        navigate(`/decks/${response.data.data.deckId}`);
      } else {
        await api.post(`/decks/${deckId}/cards`, payload);
        navigate(`/decks/${deckId}`);
      }
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to save flashcard');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-1 overflow-y-auto w-full relative bg-[#fcf6ef]">
      {/* Top Bar */}
      <div className="sticky top-0 z-10 backdrop-blur-sm bg-[#fcf6ef]/80 border-b border-[rgba(26,26,26,0.05)] h-16 px-8 flex items-center justify-between">
        <Link to={isEditing ? '#' : `/decks/${deckId}`} onClick={(e) => { if (isEditing) { e.preventDefault(); navigate(-1); } }} className="flex items-center gap-2 text-[#475569] hover:text-[#0f172a] transition-colors font-medium text-sm">
          <ChevronLeft size={16} />
          Back to Deck
        </Link>
      </div>

      {/* Main Content */}
      <div className="max-w-3xl mx-auto py-12 px-6">
        
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-2xl font-bold text-[#0f172a] tracking-tight">
            {isEditing ? 'Edit Card' : 'Add New Card'}
          </h1>
          <div className="flex items-center gap-3">
            <button onClick={() => navigate(-1)} className="text-[#475569] hover:text-[#0f172a] font-semibold text-sm px-4 py-2 transition-colors">
              Cancel
            </button>
            <button 
              onClick={handleSubmit} 
              disabled={loading}
              className="bg-[#0f172a] hover:bg-[#1e293b] text-white font-semibold text-sm px-6 py-2 rounded-lg transition-colors shadow-sm disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {loading ? 'Saving...' : 'Save Card'}
            </button>
          </div>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-lg text-sm">
            {error}
          </div>
        )}

        {/* Form Container */}
        <div className="space-y-8">
          
          {/* Front Side */}
          <div>
            <label className="block text-xs font-bold text-[#64748b] tracking-wider uppercase mb-2">
              Front side
            </label>
            <div className="relative">
              <textarea
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                placeholder="Enter the question, term, or prompt here..."
                className="w-full min-h-[180px] bg-white border-2 border-[rgba(26,26,26,0.05)] rounded-xl p-6 text-xl text-[#0f172a] placeholder:text-gray-300 focus:outline-none focus:border-[#df6020]/30 focus:ring-4 focus:ring-[#df6020]/10 transition-all resize-none shadow-sm pb-10"
              />
              <span className="absolute bottom-4 right-6 text-xs font-medium text-gray-400 uppercase tracking-widest pointer-events-none">
                Question
              </span>
            </div>
          </div>

          {/* Back Side */}
          <div>
            <label className="block text-xs font-bold text-[#64748b] tracking-wider uppercase mb-2">
              Back side
            </label>
            <div className="relative">
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder="Enter the answer, definition, or explanation..."
                className="w-full min-h-[180px] bg-white border-2 border-[rgba(26,26,26,0.05)] rounded-xl p-6 text-xl text-[#0f172a] placeholder:text-gray-300 focus:outline-none focus:border-[#df6020]/30 focus:ring-4 focus:ring-[#df6020]/10 transition-all resize-none shadow-sm pb-10"
              />
              <span className="absolute bottom-4 right-6 text-xs font-medium text-gray-400 uppercase tracking-widest pointer-events-none">
                Answer
              </span>
            </div>
          </div>

          {/* Tags */}
          <div>
            <label className="block text-xs font-bold text-[#64748b] tracking-wider uppercase mb-2">
              Tags
            </label>
            <div className="bg-white border-2 border-[rgba(26,26,26,0.05)] rounded-xl p-4 shadow-sm">
              <div className="flex flex-wrap gap-2 mb-2">
                {tags.map(tag => (
                  <span key={tag} className="bg-[rgba(223,96,32,0.1)] text-[#df6020] text-xs font-bold px-3 py-1.5 rounded-full flex items-center gap-1">
                    {tag}
                    <button onClick={() => handleRemoveTag(tag)} className="hover:text-red-600 transition-colors focus:outline-none">
                      <X size={12} />
                    </button>
                  </span>
                ))}
              </div>
              <input
                type="text"
                value={tagsInput}
                onChange={(e) => setTagsInput(e.target.value)}
                onKeyDown={handleAddTag}
                placeholder="Add tags separated by commas or enter..."
                className="w-full bg-transparent border-none text-sm text-[#0f172a] placeholder:text-gray-400 focus:outline-none p-1"
              />
            </div>
            <p className="text-xs text-[#64748b] mt-2">Tags help you organize and filter cards later.</p>
          </div>

          {/* Pro Tip */}
          <div className="bg-gray-50 border border-[rgba(26,26,26,0.05)] rounded-xl p-6 flex gap-4 items-start mt-12">
            <Info className="text-gray-400 shrink-0 mt-0.5" size={20} />
            <div>
              <h4 className="font-semibold text-[#0f172a] text-sm mb-1">Pro Tip</h4>
              <p className="text-sm text-[#64748b]">
                Keep your cards concise. One concept per card leads to 31% better retention during study sessions.
              </p>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
