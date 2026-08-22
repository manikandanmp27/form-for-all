import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useProfile } from '../../context/ProfileContext';
import { FileText, User, LogOut, PlusCircle, LayoutDashboard, Sparkles, Languages } from 'lucide-react';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { t } = useProfile();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo */}
        <Link
          to="/"
          className="flex items-center gap-2 text-xl font-bold text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-600 rounded-md p-1"
        >
          <div className="w-9 h-9 rounded-lg bg-teal-600 flex items-center justify-center text-white shadow-sm">
            <Sparkles className="w-5 h-5" />
          </div>
          <span className="tracking-tight text-teal-900 font-extrabold">Fill-For-Me</span>
          <span className="text-xs px-2 py-0.5 rounded-full bg-teal-50 text-teal-700 font-medium border border-teal-200">
            Co-Pilot
          </span>
        </Link>

        {/* Center / Right Links */}
        <nav aria-label="Main Navigation" className="flex items-center gap-4 sm:gap-6">
          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className="text-slate-600 hover:text-teal-700 font-medium text-sm flex items-center gap-1.5 focus:ring-2 focus:ring-teal-500 rounded px-2 py-1"
              >
                <LayoutDashboard className="w-4 h-4 text-teal-600" /> {t('dashboard')}
              </Link>

              <Link
                to="/translate"
                className="text-slate-600 hover:text-teal-700 font-medium text-sm flex items-center gap-1.5 focus:ring-2 focus:ring-teal-500 rounded px-2 py-1"
              >
                <Languages className="w-4 h-4 text-indigo-600" /> Translate Form
              </Link>

              <Link
                to="/forms/new"
                className="bg-teal-600 hover:bg-teal-700 text-white text-sm font-semibold px-3.5 py-1.5 rounded-lg shadow-xs transition-colors flex items-center gap-1.5 focus:ring-2 focus:ring-teal-700 focus:outline-none"
              >
                <PlusCircle className="w-4 h-4" /> {t('uploadForm')}
              </Link>

              <div className="h-5 w-px bg-slate-200 hidden sm:block" />

              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-slate-700 hidden md:inline">
                  {user?.fullName || user?.email || 'User'}
                </span>
                <button
                  onClick={handleLogout}
                  className="text-slate-500 hover:text-rose-600 p-1.5 rounded-lg hover:bg-slate-100 transition-colors focus:ring-2 focus:ring-rose-500"
                  title={t('logout')}
                  aria-label={t('logout')}
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <>
              <Link
                to="/onboarding"
                className="text-slate-600 hover:text-teal-700 font-medium text-sm focus:ring-2 focus:ring-teal-500 rounded px-2 py-1"
              >
                Accessibility Setup
              </Link>
              <Link
                to="/login"
                className="text-teal-700 hover:text-teal-800 font-semibold text-sm focus:ring-2 focus:ring-teal-500 rounded px-3 py-1.5 border border-teal-200 hover:bg-teal-50"
              >
                {t('login')}
              </Link>
              <Link
                to="/register"
                className="bg-teal-600 hover:bg-teal-700 text-white text-sm font-semibold px-4 py-1.5 rounded-lg shadow-xs transition-colors focus:ring-2 focus:ring-teal-700"
              >
                {t('getStarted')}
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};
