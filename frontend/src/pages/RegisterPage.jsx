import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { UserPlus, Sparkles } from 'lucide-react';

export const RegisterPage = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!fullName || !email || !password) {
      setError('Please fill in your name, email, and password.');
      return;
    }

    if (password.length < 6) {
      setError('Password should be at least 6 characters long.');
      return;
    }

    setIsSubmitting(true);
    try {
      await register(fullName, email, password);
      navigate('/onboarding');
    } catch (err) {
      setError(err.message || 'Registration failed. Email might already be registered.');
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
          <h1 className="text-2xl font-extrabold text-slate-900">Create Account</h1>
          <p className="text-slate-600 text-sm">Join Fill-For-Me for an accessible, stress-free form experience.</p>
        </div>

        <ErrorAlert message={error} onClose={() => setError('')} />

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="fullName" className="block text-sm font-semibold text-slate-700 mb-1">
              Full Name
            </label>
            <input
              id="fullName"
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
              placeholder="Jane Doe"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-teal-600 focus:outline-none text-slate-900"
            />
          </div>

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
              placeholder="At least 6 characters"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-teal-600 focus:outline-none text-slate-900"
            />
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-teal-600 hover:bg-teal-700 text-white font-bold py-3 px-4 rounded-xl shadow-xs transition-colors flex items-center justify-center gap-2 focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
          >
            <UserPlus className="w-4 h-4" />
            <span>{isSubmitting ? 'Creating Account...' : 'Create Account'}</span>
          </button>
        </form>

        <div className="text-center pt-2 text-xs text-slate-600">
          Already have an account?{' '}
          <Link to="/login" className="text-teal-700 font-bold hover:underline">
            Sign in here
          </Link>
        </div>
      </div>
    </div>
  );
};
