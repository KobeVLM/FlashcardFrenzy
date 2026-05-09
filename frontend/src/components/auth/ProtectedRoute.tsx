import { Navigate, useLocation } from 'react-router-dom';

interface Props {
  children: React.ReactNode;
  adminOnly?: boolean;
}

export default function ProtectedRoute({ children, adminOnly = false }: Props) {
  const location = useLocation();
  const token = localStorage.getItem('accessToken');
  const userRaw = localStorage.getItem('user');
  let user = null;
  try {
    user = userRaw && userRaw !== 'undefined' ? JSON.parse(userRaw) : null;
  } catch (e) {
    console.error("Failed to parse user in ProtectedRoute", e);
  }

  if (!token) {
    // Save the intended URL so we can redirect back after login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && user?.role !== 'ADMIN') {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4 text-center p-8">
        <div className="text-5xl">🚫</div>
        <h2 className="text-2xl font-bold text-[#0f172a]">Access Denied</h2>
        <p className="text-secondary">You don't have permission to view this page.</p>
      </div>
    );
  }

  return <>{children}</>;
}
