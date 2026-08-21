import React from 'react';

export const StepProgress = ({ currentStep, totalSteps, label }) => {
  const percentage = Math.min(100, Math.max(0, Math.round((currentStep / totalSteps) * 100)));

  return (
    <div className="w-full space-y-1.5" aria-label="Progress bar">
      <div className="flex justify-between items-center text-xs font-semibold text-slate-600">
        <span>{label || `Step ${currentStep} of ${totalSteps}`}</span>
        <span className="text-teal-700 font-bold">{percentage}%</span>
      </div>
      <div className="w-full bg-slate-200 h-2.5 rounded-full overflow-hidden">
        <div
          className="bg-teal-600 h-full rounded-full transition-all duration-300 ease-out"
          style={{ width: `${percentage}%` }}
          role="progressbar"
          aria-valuenow={currentStep}
          aria-valuemin={1}
          aria-valuemax={totalSteps}
          aria-valuetext={`${currentStep} of ${totalSteps} completed`}
        />
      </div>
    </div>
  );
};
