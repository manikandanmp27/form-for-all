import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProfileProvider } from './context/ProfileContext';
import { VoiceProvider } from './context/VoiceContext';

import { Navbar } from './components/layout/Navbar';
import { Footer } from './components/layout/Footer';

import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OnboardingPage } from './pages/OnboardingPage';
import { DashboardPage } from './pages/DashboardPage';
import { NewFormPage } from './pages/NewFormPage';
import { FormFillingPage } from './pages/FormFillingPage';
import { ReviewPage } from './pages/ReviewPage';
import { CompletePage } from './pages/CompletePage';
import { ProfilePage } from './pages/ProfilePage';

import { ChatbotWidget } from './components/chatbot/ChatbotWidget';

// Protected Route Wrapper
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <ProfileProvider>
        <VoiceProvider>
          <BrowserRouter>
            <div className="flex flex-col min-h-screen bg-slate-50 text-slate-900 font-sans selection:bg-teal-200 selection:text-teal-900">
              <Navbar />
              <div className="flex-1 flex flex-col">
                <Routes>
                  {/* Public Journey */}
                  <Route path="/" element={<LandingPage />} />
                  <Route path="/login" element={<LoginPage />} />
                  <Route path="/register" element={<RegisterPage />} />
                  <Route path="/onboarding" element={<OnboardingPage />} />

                  {/* Accessible Application Core Routes */}
                  <Route path="/dashboard" element={<DashboardPage />} />
                  <Route path="/forms/new" element={<NewFormPage />} />
                  <Route path="/forms/:sessionId" element={<FormFillingPage />} />
                  <Route path="/forms/:sessionId/review" element={<ReviewPage />} />
                  <Route path="/forms/:sessionId/complete" element={<CompletePage />} />
                  <Route path="/risk-alerts" element={<DashboardPage />} />
                  <Route path="/profile" element={<ProfilePage />} />

                  {/* Fallback Catch-All */}
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </div>
              <ChatbotWidget />
              <Footer />
            </div>
          </BrowserRouter>
        </VoiceProvider>
      </ProfileProvider>
    </AuthProvider>
  );
}
