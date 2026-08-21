import React from 'react';
import { useParams, Link } from 'react-router-dom';
import { CheckCircle2, Download, LayoutDashboard, PlusCircle, Sparkles } from 'lucide-react';
import { ProfileBar } from '../components/layout/ProfileBar';

export const CompletePage = () => {
  const { sessionId } = useParams();

  return (
    <div className="bg-slate-50 min-h-screen flex flex-col">
      <ProfileBar />

      <main className="max-w-2xl mx-auto px-4 py-16 flex-1 w-full flex items-center justify-center">
        <div className="bg-white p-8 sm:p-12 rounded-3xl border border-slate-200 shadow-xl text-center space-y-6 animate-fade-in w-full">
          <div className="w-16 h-16 bg-teal-100 text-teal-700 rounded-full flex items-center justify-center mx-auto shadow-inner">
            <CheckCircle2 className="w-10 h-10" />
          </div>

          <div className="space-y-2">
            <span className="text-xs font-extrabold text-teal-700 uppercase tracking-widest bg-teal-50 px-3 py-1 rounded-full border border-teal-200">
              Form Completed Successfully
            </span>
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">You're All Set!</h1>
            <p className="text-slate-600 text-sm max-w-md mx-auto leading-relaxed">
              Your form has been processed and safely stored. You can download your filled copy below or return to your dashboard.
            </p>
          </div>

          <div className="p-4 bg-slate-50 border border-slate-200 rounded-2xl text-xs text-slate-600 space-y-1">
            <p className="font-bold text-slate-800">Session Reference:</p>
            <p className="font-mono text-teal-900">{sessionId}</p>
          </div>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <Link
              to="/dashboard"
              className="w-full sm:w-auto bg-teal-600 hover:bg-teal-700 text-white font-bold px-6 py-3 rounded-xl text-sm shadow-md transition-colors flex items-center justify-center gap-2 focus:ring-4 focus:ring-teal-300"
            >
              <LayoutDashboard className="w-4 h-4" /> Go to Dashboard
            </Link>

            <Link
              to="/forms/new"
              className="w-full sm:w-auto bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold px-6 py-3 rounded-xl text-sm border border-slate-300 transition-colors flex items-center justify-center gap-2"
            >
              <PlusCircle className="w-4 h-4" /> Fill Another Form
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
};
