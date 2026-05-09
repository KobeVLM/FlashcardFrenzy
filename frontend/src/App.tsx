import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';

// Deck & Flashcard Pages
import BrowseDecks from './pages/decks/BrowseDecks';
import MyDecks from './pages/decks/MyDecks';
import DeckDetail from './pages/decks/DeckDetail';
import DeckForm from './pages/decks/DeckForm';
import FlashcardForm from './pages/flashcards/FlashcardForm';

// Quiz Pages
import StudyMode from './pages/quiz/StudyMode';
import QuizHistory from './pages/quiz/QuizHistory';

// Settings & Admin
import Settings from './pages/settings/Settings';
import AdminDashboard from './pages/admin/AdminDashboard';

function App() {
  const isAuthenticated = !!localStorage.getItem('accessToken');

  return (
    <BrowserRouter>
      <Routes>
        {/* Auth Routes (No Layout) */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Study Mode — Full-page, no sidebar */}
        <Route
          path="/quiz/:deckId"
          element={
            <ProtectedRoute>
              <StudyMode />
            </ProtectedRoute>
          }
        />

        {/* Main App Routes (With Sidebar) */}
        <Route path="/" element={<MainLayout />}>
          {/* Root: redirect to /decks if logged in, else /login */}
          <Route
            index
            element={<Navigate to={isAuthenticated ? '/decks' : '/login'} replace />}
          />

          {/* Public — visible to everyone */}
          <Route path="decks" element={<BrowseDecks />} />
          <Route path="decks/:id" element={<DeckDetail />} />

          {/* Protected — requires login */}
          <Route
            path="decks/my"
            element={<ProtectedRoute><MyDecks /></ProtectedRoute>}
          />
          <Route
            path="decks/create"
            element={<ProtectedRoute><DeckForm /></ProtectedRoute>}
          />
          <Route
            path="decks/:id/edit"
            element={<ProtectedRoute><DeckForm /></ProtectedRoute>}
          />
          <Route
            path="decks/:id/cards/create"
            element={<ProtectedRoute><FlashcardForm /></ProtectedRoute>}
          />
          <Route
            path="cards/:cardId/edit"
            element={<ProtectedRoute><FlashcardForm /></ProtectedRoute>}
          />
          <Route
            path="quiz/history"
            element={<ProtectedRoute><QuizHistory /></ProtectedRoute>}
          />
          <Route
            path="settings"
            element={<ProtectedRoute><Settings /></ProtectedRoute>}
          />

          {/* Admin only */}
          <Route
            path="admin"
            element={<ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>}
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
