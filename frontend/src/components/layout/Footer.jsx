import React from 'react';
import { ShieldCheck, Heart, Volume2, Eye } from 'lucide-react';

export const Footer = () => {
  return (
    <footer className="bg-slate-900 text-slate-400 border-t border-slate-800 py-8 px-4 text-xs mt-auto">
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4 text-center md:text-left">
        <div className="space-y-1">
          <p className="text-slate-200 font-semibold flex items-center justify-center md:justify-start gap-1.5 text-sm">
            <ShieldCheck className="w-4 h-4 text-teal-400" /> Fill-For-Me Accessibility Co-Pilot
          </p>
          <p className="text-slate-400">
            Designed for cognitive ease, visual accessibility, and stress-free form filling.
          </p>
        </div>

        <div className="flex flex-wrap items-center justify-center gap-4 text-slate-300">
          <span className="flex items-center gap-1">
            <Volume2 className="w-3.5 h-3.5 text-amber-400" /> Voice Read-Aloud Ready
          </span>
          <span className="flex items-center gap-1">
            <Eye className="w-3.5 h-3.5 text-teal-400" /> High-Contrast WCAG Compliant
          </span>
          <span className="text-slate-500">|</span>
          <span className="text-slate-400">&copy; {new Date().getFullYear()} Fill-For-Me</span>
        </div>
      </div>
    </footer>
  );
};
