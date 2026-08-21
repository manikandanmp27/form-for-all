import React from 'react';
import { AlertTriangle, XCircle, Info } from 'lucide-react';

export const ErrorAlert = ({ message, type = 'error', onClose }) => {
  if (!message) return null;

  const styles = {
    error: {
      bg: 'bg-rose-50 border-rose-200 text-rose-900',
      icon: <XCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />,
    },
    warning: {
      bg: 'bg-amber-50 border-amber-200 text-amber-900',
      icon: <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />,
    },
    info: {
      bg: 'bg-sky-50 border-sky-200 text-sky-900',
      icon: <Info className="w-5 h-5 text-sky-600 shrink-0 mt-0.5" />,
    },
  };

  const currentStyle = styles[type] || styles.error;

  return (
    <div
      role="alert"
      aria-live="assertive"
      className={`p-4 rounded-xl border ${currentStyle.bg} flex items-start justify-between gap-3 shadow-xs animate-fade-in my-4`}
    >
      <div className="flex items-start gap-3">
        {currentStyle.icon}
        <div>
          <p className="text-sm font-medium leading-relaxed">{message}</p>
        </div>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className="text-slate-400 hover:text-slate-700 p-1 rounded-md focus:ring-2 focus:ring-slate-400"
          aria-label="Dismiss error notification"
        >
          &times;
        </button>
      )}
    </div>
  );
};
