import React, { useEffect } from 'react';
import { AlertTriangle, ShieldAlert, ArrowLeft, CheckCircle2, Volume2 } from 'lucide-react';
import { useVoice } from '../../context/VoiceContext';
import { useProfile } from '../../context/ProfileContext';

export const RiskWarningModal = ({ riskFlag, onConfirm, onGoBack, isSubmitting }) => {
  const { speak } = useVoice();
  const { profile, t } = useProfile();

  useEffect(() => {
    if (profile.voicePreference && riskFlag) {
      const textToRead = `Warning. ${riskFlag.warningTitle || t('beforeContinue')}. ${riskFlag.consequenceExplanation || riskFlag.warningReason || ''}`;
      speak(textToRead, profile.preferredLanguage);
    }
  }, [riskFlag, profile.voicePreference, profile.preferredLanguage]);

  if (!riskFlag) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="risk-warning-title"
      className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in"
    >
      <div className="max-w-xl w-full bg-white rounded-3xl border-2 border-amber-500 shadow-2xl p-6 sm:p-8 space-y-6">
        {/* Warning Badge Header */}
        <div className="flex items-center gap-3 border-b border-amber-100 pb-4">
          <div className="p-3 bg-amber-100 text-amber-700 rounded-2xl shrink-0">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <div>
            <span className="text-xs font-extrabold uppercase tracking-wider text-amber-700 bg-amber-50 border border-amber-200 px-2.5 py-0.5 rounded-full">
              {t('importantNotice')}
            </span>
            <h2 id="risk-warning-title" className="text-xl font-extrabold text-slate-900 mt-1">
              {riskFlag.warningTitle || t('beforeContinue')}
            </h2>
          </div>
        </div>

        {/* Warning Content */}
        <div className="space-y-4 text-slate-800 text-sm sm:text-base leading-relaxed bg-amber-50/50 p-5 rounded-2xl border border-amber-200">
          <p className="font-bold text-slate-900 text-base">
            Field: <span className="text-amber-800">{riskFlag.fieldLabel || 'Selected Answer'}</span>
          </p>

          {riskFlag.warningReason && (
            <p className="text-slate-700">
              <strong className="text-amber-900 font-semibold">Why this matters: </strong>
              {riskFlag.warningReason}
            </p>
          )}

          {riskFlag.consequenceExplanation && (
            <div className="p-4 bg-white rounded-xl border border-amber-200 shadow-2xs space-y-1">
              <p className="font-bold text-rose-700 flex items-center gap-1.5 text-xs uppercase tracking-wide">
                <AlertTriangle className="w-4 h-4" /> {t('potentialImpact')}
              </p>
              <p className="text-slate-900 font-medium text-sm">
                {riskFlag.consequenceExplanation}
              </p>
            </div>
          )}
        </div>

        {/* Explicit Confirmation Actions */}
        <div className="flex flex-col sm:flex-row items-center justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onGoBack}
            disabled={isSubmitting}
            className="w-full sm:w-auto px-5 py-3 rounded-xl border-2 border-slate-300 hover:bg-slate-100 text-slate-800 font-bold text-sm flex items-center justify-center gap-2 focus:ring-4 focus:ring-slate-300 cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" /> {t('goBackChange')}
          </button>

          <button
            type="button"
            onClick={onConfirm}
            disabled={isSubmitting}
            className="w-full sm:w-auto px-6 py-3 rounded-xl bg-amber-600 hover:bg-amber-700 text-white font-extrabold text-sm flex items-center justify-center gap-2 shadow-md focus:ring-4 focus:ring-amber-300 cursor-pointer"
          >
            <CheckCircle2 className="w-4 h-4" /> {isSubmitting ? 'Confirming...' : t('understandContinue')}
          </button>
        </div>
      </div>
    </div>
  );
};
