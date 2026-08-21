import React from 'react';

export const LoadingSpinner = ({ label = 'Loading...', size = 'md' }) => {
  const sizeClasses = {
    sm: 'w-5 h-5 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
  };

  return (
    <div
      role="status"
      aria-live="polite"
      className="flex flex-col items-center justify-center p-6 space-y-3"
    >
      <div
        className={`${sizeClasses[size] || sizeClasses.md} border-teal-200 border-t-teal-600 rounded-full animate-spin`}
      />
      <span className="text-slate-600 font-medium text-sm">{label}</span>
      <span className="sr-only">{label}</span>
    </div>
  );
};
