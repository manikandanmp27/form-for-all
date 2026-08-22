import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { conversationApi } from '../api/conversationApi';
import { riskApi } from '../api/riskApi';
import { useProfile } from '../context/ProfileContext';
import { useVoice } from '../context/VoiceContext';
import { ProfileBar } from '../components/layout/ProfileBar';
import { StepProgress } from '../components/common/StepProgress';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { RiskWarningModal } from '../components/risk/RiskWarningModal';
import { Volume2, VolumeX, Mic, MicOff, HelpCircle, ArrowLeft, ArrowRight, CheckCircle2, Brain, Sparkles, ShieldAlert, FileText } from 'lucide-react';

export const FormFillingPage = () => {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const { isCognitiveMode, profile, t, tField } = useProfile();
  const { speak, isSpeaking, stopSpeaking, startListening, isListening } = useVoice();

  const [stepData, setStepData] = useState(null);
  const [answerValue, setAnswerValue] = useState('');
  const [showWhyAsked, setShowWhyAsked] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [pendingRisk, setPendingRisk] = useState(null);

  // Demo fallback steps if backend server is not reachable
  const getDemoFieldsForForm = () => {
    return [
      {
        id: 'field-1',
        fieldName: 'applicantFullName',
        label: 'Full Legal Name',
        description: 'Enter your full legal name exactly as printed on your government photo ID.',
        whyAsked: 'Required to verify your legal identity for form submission.',
        fieldType: 'TEXT',
        isRequired: true,
        defaultValue: '',
      },
      {
        id: 'field-2',
        fieldName: 'aadhaarNumber',
        label: 'Aadhaar / Enrollment Number',
        description: 'Enter your 12-digit Aadhaar number or 28-digit Enrollment ID.',
        whyAsked: 'Used by identity authority to fetch official verification details.',
        fieldType: 'TEXT',
        isRequired: true,
        defaultValue: '',
      },
      {
        id: 'field-3',
        fieldName: 'dateOfBirth',
        label: 'Date of Birth',
        description: 'Select your official birth date.',
        whyAsked: 'Used to verify legal age eligibility.',
        fieldType: 'DATE',
        isRequired: true,
        defaultValue: '',
      },
      {
        id: 'field-4',
        fieldName: 'permanentAddress',
        label: 'Permanent Residential Address',
        description: 'Provide the address where you currently live permanently.',
        whyAsked: 'Required for official postal communications and residency verification.',
        fieldType: 'TEXTAREA',
        isRequired: true,
        defaultValue: '',
      },
    ];
  };

  const demoFields = getDemoFieldsForForm();
  const [demoStepIndex, setDemoStepIndex] = useState(0);


  const fetchState = async () => {
    setIsLoading(true);
    try {
      const data = await conversationApi.getConversationState(sessionId);
      setStepData(data);
      if (data.currentField?.value) {
        setAnswerValue(data.currentField.value);
      } else {
        setAnswerValue('');
      }

      if (data.riskConfirmationRequired && data.pendingRiskFlag) {
        setPendingRisk(data.pendingRiskFlag);
      } else {
        setPendingRisk(null);
      }
    } catch (err) {
      console.warn('Backend state fetch failed, running in interactive client mode:', err.message);
      // Fallback demo state setup
      const currentDemoField = demoFields[demoStepIndex];
      setStepData({
        sessionId,
        currentStep: demoStepIndex + 1,
        totalSteps: demoFields.length,
        isCompleted: false,
        currentField: currentDemoField,
        riskConfirmationRequired: false,
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchState();
  }, [sessionId, demoStepIndex]);

  const rawField = stepData?.currentField || demoFields[demoStepIndex] || {};
  const translated = tField(rawField);
  const currentField = {
    ...translated,
    label: translated.label || rawField.label || rawField.simplifiedQuestionText || 'Form Question',
    description: translated.description || rawField.description || rawField.plainLanguageExplanation || rawField.defaultHelpText || 'Please provide details for this field.',
    whyAsked: translated.whyAsked || rawField.whyAsked || 'Required by form issuer to complete your application.',
    fieldType: translated.fieldType || rawField.fieldType || 'TEXT',
    isRequired: translated.required ?? translated.isRequired ?? rawField.required ?? rawField.isRequired ?? true,
  };

  // Auto read-aloud when voice preference is enabled
  useEffect(() => {
    if (profile.voicePreference && currentField?.label) {
      const textToRead = `${currentField.label}. ${currentField.description || ''}`;
      speak(textToRead, profile.preferredLanguage);
    }
  }, [stepData, profile.voicePreference, profile.preferredLanguage]);

  const handleNext = async (e) => {
    e?.preventDefault();
    setError('');

    if (currentField?.isRequired && !answerValue.trim()) {
      setError(`Please provide an answer for "${currentField.label}".`);
      return;
    }

    // Check high risk trigger
    if (currentField?.isHighRisk && !pendingRisk) {
      setPendingRisk({
        id: 'risk-demo-1',
        fieldLabel: currentField.label,
        warningTitle: t('beforeContinue'),
        warningReason: currentField.riskWarning || 'This field impacts account nominee legal ownership.',
        consequenceExplanation: 'Submitting this change will overwrite legal nominee records on file.',
      });
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await conversationApi.submitAnswer(sessionId, currentField.id, answerValue, 'NEXT');
      if (res.isCompleted || res.currentStep > res.totalSteps || currentStep >= totalSteps) {
        navigate(`/forms/${sessionId}/review`);
      } else {
        setStepData(res);
        setAnswerValue('');
      }
    } catch (err) {
      // Demo client navigation fallback
      if (demoStepIndex < demoFields.length - 1) {
        setDemoStepIndex((prev) => prev + 1);
        setAnswerValue('');
      } else {
        navigate(`/forms/${sessionId}/review`);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePrevious = async () => {
    if (demoStepIndex > 0) {
      setDemoStepIndex((prev) => prev - 1);
    } else if (stepData?.currentStep > 1) {
      try {
        const res = await conversationApi.submitAnswer(sessionId, currentField.id, answerValue, 'PREVIOUS');
        setStepData(res);
      } catch (err) {
        console.warn('Previous step error:', err);
      }
    }
  };

  const handleConfirmRisk = async () => {
    setIsSubmitting(true);
    try {
      if (pendingRisk?.id && !pendingRisk.id.startsWith('risk-demo')) {
        await riskApi.confirmRiskAlert(sessionId, pendingRisk.id, true);
      }
      setPendingRisk(null);
      if (demoStepIndex < demoFields.length - 1) {
        setDemoStepIndex((prev) => prev + 1);
        setAnswerValue('');
      } else {
        navigate(`/forms/${sessionId}/review`);
      }
    } catch (err) {
      console.error('Risk confirmation error:', err);
      setPendingRisk(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleVoiceInput = () => {
    startListening((transcript) => {
      setAnswerValue(transcript);
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center">
        <LoadingSpinner size="lg" label="Preparing your guided question..." />
      </div>
    );
  }

  const currentStep = stepData?.currentStep || 1;
  const totalSteps = stepData?.totalSteps || 4;

  return (
    <div className="bg-slate-50 min-h-screen flex flex-col">
      <ProfileBar />

      {/* Risk Warning Overlay Modal */}
      {pendingRisk && (
        <RiskWarningModal
          riskFlag={pendingRisk}
          onConfirm={handleConfirmRisk}
          onGoBack={() => setPendingRisk(null)}
          isSubmitting={isSubmitting}
        />
      )}

      <main className="max-w-4xl mx-auto px-4 py-8 flex-1 w-full space-y-6">
        {/* Header Progress */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="w-full sm:w-2/3 space-y-1">
            <span className="text-xs font-bold text-teal-700 uppercase tracking-wider">Guided Form Experience</span>
            <StepProgress currentStep={currentStep} totalSteps={totalSteps} />
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate(`/forms/${sessionId}/review`)}
              className="p-2.5 rounded-xl border border-teal-300 bg-teal-50 hover:bg-teal-100 text-teal-900 text-xs font-extrabold transition-all flex items-center gap-1.5 cursor-pointer shadow-xs"
              title="Go to Review Summary"
            >
              <FileText className="w-4 h-4 text-teal-700" /> {t('reviewAnswers')}
            </button>

            <button
              onClick={() => {
                const text = `${currentField.label}. ${currentField.description || ''}`;
                if (isSpeaking) stopSpeaking();
                else speak(text, profile.preferredLanguage);
              }}
              className={`p-2.5 rounded-xl border text-xs font-bold transition-all flex items-center gap-1.5 ${
                isSpeaking ? 'bg-amber-100 text-amber-900 border-amber-300' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
              title="Read Question Out Loud"
            >
              <Volume2 className="w-4 h-4" /> {isSpeaking ? t('stopVoice') : t('readQuestion')}
            </button>
          </div>
        </div>

        <ErrorAlert message={error} onClose={() => setError('')} />

        {/* FORM FILLING CONTENT - COGNITIVE LOAD MODE vs STANDARD MODE */}
        {isCognitiveMode ? (
          /* COGNITIVE LOAD MODE: Single visual dominant card, high contrast, zero noise */
          <div className="cognitive-mode-card p-8 sm:p-12 rounded-3xl space-y-8 animate-fade-in shadow-xl border-2 border-slate-900">
            <div className="space-y-3">
              <span className="text-xs font-extrabold text-teal-800 uppercase tracking-wider bg-teal-100 px-3 py-1 rounded-full border border-teal-300">
                Question {currentStep} of {totalSteps}
              </span>

              <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-950 tracking-tight leading-snug">
                {currentField.label}
              </h1>

              {currentField.description && (
                <p className="text-lg text-slate-700 leading-relaxed font-medium bg-slate-100 p-4 rounded-2xl border border-slate-300">
                  {currentField.description}
                </p>
              )}
            </div>

            {/* Why Asked Accordion */}
            {currentField.whyAsked && (
              <div>
                <button
                  type="button"
                  onClick={() => setShowWhyAsked(!showWhyAsked)}
                  className="text-teal-800 hover:text-teal-950 text-sm font-bold flex items-center gap-1.5 focus:outline-none focus:ring-2 focus:ring-teal-600 rounded px-1"
                >
                  <HelpCircle className="w-4 h-4" />
                  <span>{t('whyIsThisAsked')}</span>
                </button>
                {showWhyAsked && (
                  <p className="mt-2 text-xs text-slate-700 bg-teal-50 border border-teal-200 p-3 rounded-xl font-medium">
                    {currentField.whyAsked}
                  </p>
                )}
              </div>
            )}

            {/* Input Field */}
            <form onSubmit={handleNext} className="space-y-6">
              <div className="space-y-2">
                <div className="relative">
                  {currentField.fieldType === 'TEXTAREA' ? (
                    <textarea
                      rows={4}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder={t('typeAnswer')}
                      className="w-full text-lg p-4 rounded-2xl border-2 border-slate-900 focus:ring-4 focus:ring-teal-400 text-slate-950 bg-white font-medium shadow-inner"
                    />
                  ) : (
                    <input
                      type={currentField.fieldType === 'DATE' ? 'date' : 'text'}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder={t('typeAnswer')}
                      className="w-full text-xl p-4 rounded-2xl border-2 border-slate-900 focus:ring-4 focus:ring-teal-400 text-slate-950 bg-white font-medium shadow-inner"
                    />
                  )}

                  {/* Speech input button */}
                  <button
                    type="button"
                    onClick={handleVoiceInput}
                    className={`absolute right-3 bottom-3 p-2.5 rounded-xl border text-xs font-bold transition-all flex items-center gap-1 ${
                      isListening ? 'bg-rose-500 text-white animate-pulse' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                    }`}
                    title="Speak Answer"
                  >
                    <Mic className="w-4 h-4" /> {isListening ? t('listening') : t('dictate')}
                  </button>
                </div>
              </div>

              {/* Cognitive Mode Controls */}
              <div className="flex items-center justify-between pt-4 border-t-2 border-slate-200">
                <button
                  type="button"
                  onClick={handlePrevious}
                  disabled={currentStep === 1}
                  className="px-6 py-3.5 rounded-2xl border-2 border-slate-300 hover:bg-slate-100 text-slate-900 font-bold text-base flex items-center gap-2 disabled:opacity-30 cursor-pointer"
                >
                  <ArrowLeft className="w-5 h-5" /> {t('previousQuestion')}
                </button>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-8 py-3.5 rounded-2xl bg-teal-600 hover:bg-teal-700 text-white font-extrabold text-lg flex items-center gap-2 shadow-lg focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
                >
                  <span>{currentStep === totalSteps ? t('reviewAnswers') : t('nextQuestion')}</span>
                  <ArrowRight className="w-5 h-5" />
                </button>
              </div>
            </form>
          </div>
        ) : (
          /* STANDARD MODE LAYOUT */
          <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-md space-y-6 animate-fade-in">
            <div className="space-y-2 border-b border-slate-100 pb-4">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-500">Question {currentStep} of {totalSteps}</span>
                {currentField.isRequired && (
                  <span className="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
                    {t('requiredField')}
                  </span>
                )}
              </div>

              <h1 className="text-2xl font-extrabold text-slate-900">{currentField.label}</h1>

              {currentField.description && (
                <p className="text-slate-600 text-sm leading-relaxed">{currentField.description}</p>
              )}
            </div>

            {/* Why asked guidance */}
            {currentField.whyAsked && (
              <div className="bg-teal-50/70 border border-teal-200 p-4 rounded-xl text-xs space-y-1">
                <p className="font-bold text-teal-900 flex items-center gap-1">
                  <HelpCircle className="w-4 h-4 text-teal-700" /> {t('whyIsThisAsked')}
                </p>
                <p className="text-slate-700 font-medium">{currentField.whyAsked}</p>
              </div>
            )}

            {/* Input Form */}
            <form onSubmit={handleNext} className="space-y-6">
              <div className="space-y-2">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  {t('yourResponse')}
                </label>
                <div className="relative">
                  {currentField.fieldType === 'TEXTAREA' ? (
                    <textarea
                      rows={3}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder={t('typeAnswer')}
                      className="w-full text-base p-3.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-slate-900"
                    />
                  ) : (
                    <input
                      type={currentField.fieldType === 'DATE' ? 'date' : 'text'}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder={t('typeAnswer')}
                      className="w-full text-base p-3.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-slate-900"
                    />
                  )}

                  <button
                    type="button"
                    onClick={handleVoiceInput}
                    className={`absolute right-2 bottom-2 p-2 rounded-lg text-xs font-semibold flex items-center gap-1 ${
                      isListening ? 'bg-rose-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    <Mic className="w-3.5 h-3.5" /> {isListening ? t('listening') : t('dictate')}
                  </button>
                </div>
              </div>

              {/* Navigation Actions */}
              <div className="flex items-center justify-between pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={handlePrevious}
                  disabled={currentStep === 1}
                  className="px-5 py-2.5 rounded-xl border border-slate-300 hover:bg-slate-100 text-slate-800 font-semibold text-sm flex items-center gap-2 disabled:opacity-40 cursor-pointer"
                >
                  <ArrowLeft className="w-4 h-4" /> {t('previous')}
                </button>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-6 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 text-white font-bold text-sm flex items-center gap-2 shadow-xs focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
                >
                  <span>{currentStep === totalSteps ? t('goToReview') : t('nextStep')}</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </form>
          </div>
        )}
      </main>
    </div>
  );
};
