import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { LogIn, Sparkles, UserCheck } from 'lucide-react';

export const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please fill in both your email address and password.');
      return;
    }

    setIsSubmitting(true);
    try {
      try {
        await login(email, password);
      } catch (loginErr) {
        // If account doesn't exist, auto-register on first sign-in for seamless experience
        if (loginErr.message && (loginErr.message.includes('not found') || loginErr.message.includes('Bad credentials') || loginErr.status === 401 || loginErr.status === 404)) {
          console.info('User not registered yet, creating seamless account...');
          await register(email.split('@')[0] || 'FillForMe User', email, password);
        } else {
          throw loginErr;
        }
      }
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Login failed. Please verify your credentials.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDemoLogin = async () => {
    setIsSubmitting(true);
    setError('');
    const demoEmail = 'guest@fillforme.com';
    const demoPassword = 'password123';

    try {
      try {
        await login(demoEmail, demoPassword);
      } catch (err) {
        await register('Demo Guest User', demoEmail, demoPassword);
      }
      navigate('/dashboard');
    } catch (err) {
      setError('Quick sign-in failed: ' + (err.message || 'Please try again.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl border border-slate-200 shadow-md p-8 space-y-6">
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 bg-teal-50 text-teal-700 rounded-xl mb-2">
            <Sparkles className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-extrabold text-slate-900">Welcome to Fill-For-Me</h1>
          <p className="text-slate-600 text-sm">Sign in to start filling your forms with accessibility AI.</p>
        </div>

        <ErrorAlert message={error} onClose={() => setError('')} />

        <button
          type="button"
          onClick={handleDemoLogin}
          disabled={isSubmitting}
          className="w-full bg-amber-500 hover:bg-amber-600 text-slate-950 font-extrabold py-3 px-4 rounded-xl shadow-xs transition-all flex items-center justify-center gap-2 border border-amber-400 focus:ring-4 focus:ring-amber-300 disabled:opacity-50 cursor-pointer text-sm"
        >
          <UserCheck className="w-4 h-4" />
          <span>⚡ Quick 1-Click Demo Sign-In</span>
        </button>

        <div className="relative flex py-1 items-center">
          <div className="flex-grow border-t border-slate-200"></div>
          <span className="flex-shrink mx-3 text-xs text-slate-400 font-semibold uppercase tracking-wider">Or Sign In / Register</span>
          <div className="flex-grow border-t border-slate-200"></div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="email" className="block text-sm font-semibold text-slate-700 mb-1">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@example.com"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-teal-600 focus:outline-none text-slate-900"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-semibold text-slate-700 mb-1">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-teal-600 focus:outline-none text-slate-900"
            />
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-teal-600 hover:bg-teal-700 text-white font-bold py-3 px-4 rounded-xl shadow-xs transition-colors flex items-center justify-center gap-2 focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
          >
            <LogIn className="w-4 h-4" />
            <span>{isSubmitting ? 'Signing in...' : 'Sign In'}</span>
          </button>
        </form>

        <div className="text-center pt-2 text-xs text-slate-600">
          Don't have an account yet?{' '}
          <Link to="/register" className="text-teal-700 font-bold hover:underline">
            Register here
          </Link>
        </div>
      </div>
    </div>
  );
};
