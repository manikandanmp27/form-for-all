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
import { Volume2, VolumeX, Mic, MicOff, HelpCircle, ArrowLeft, ArrowRight, CheckCircle2, Brain, Sparkles, ShieldAlert } from 'lucide-react';

export const FormFillingPage = () => {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const { isCognitiveMode, profile } = useProfile();
  const { speak, isSpeaking, stopSpeaking, startListening, isListening } = useVoice();

  const [stepData, setStepData] = useState(null);
  const [answerValue, setAnswerValue] = useState('');
  const [showWhyAsked, setShowWhyAsked] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [pendingRisk, setPendingRisk] = useState(null);

  // Demo fallback steps if backend server is not reachable
  const demoFields = [
    {
      id: 'field-1',
      fieldName: 'applicantFullName',
      label: 'Full Legal Name',
      description: 'Enter your full legal name exactly as printed on your government photo ID.',
      whyAsked: 'Required to verify your legal identity for account ownership.',
      fieldType: 'TEXT',
      isRequired: true,
      defaultValue: '',
    },
    {
      id: 'field-2',
      fieldName: 'dateOfBirth',
      label: 'Date of Birth',
      description: 'Select your official birth date.',
      whyAsked: 'Used to verify legal age eligibility.',
      fieldType: 'DATE',
      isRequired: true,
      defaultValue: '',
    },
    {
      id: 'field-3',
      fieldName: 'nomineeName',
      label: 'Primary Nominee Name',
      description: 'Enter the full name of your designated primary beneficiary.',
      whyAsked: 'Assigns legal transfer rights in case of account settlement.',
      fieldType: 'TEXT',
      isRequired: true,
      isHighRisk: true,
      riskWarning: 'Changing your primary nominee will replace any existing legal beneficiary assigned to this account.',
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

  // Auto read-aloud when voice preference is enabled
  useEffect(() => {
    if (profile.voicePreference && stepData?.currentField) {
      const current = stepData.currentField;
      const textToRead = `${current.label}. ${current.description || ''}`;
      speak(textToRead);
    }
  }, [stepData, profile.voicePreference]);

  const handleNext = async (e) => {
    e?.preventDefault();
    setError('');

    const currentField = stepData?.currentField;
    if (currentField?.isRequired && !answerValue.trim()) {
      setError(`Please provide an answer for "${currentField.label}".`);
      return;
    }

    // Check high risk trigger
    if (currentField?.isHighRisk && !pendingRisk) {
      setPendingRisk({
        id: 'risk-demo-1',
        fieldLabel: currentField.label,
        warningTitle: 'Before you submit this answer',
        warningReason: currentField.riskWarning || 'This field impacts account nominee legal ownership.',
        consequenceExplanation: 'Submitting this change will overwrite legal nominee records on file.',
      });
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await conversationApi.submitAnswer(sessionId, currentField.id, answerValue, 'NEXT');
      if (res.isCompleted || res.currentStep > res.totalSteps) {
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
        const res = await conversationApi.submitAnswer(sessionId, stepData.currentField.id, answerValue, 'PREVIOUS');
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
      // Proceed to next field after confirmation
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

  const currentField = stepData?.currentField || {};
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
              onClick={() => {
                const text = `${currentField.label}. ${currentField.description || ''}`;
                if (isSpeaking) stopSpeaking();
                else speak(text);
              }}
              className={`p-2.5 rounded-xl border text-xs font-bold transition-all flex items-center gap-1.5 ${
                isSpeaking ? 'bg-amber-100 text-amber-900 border-amber-300' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
              title="Read Question Out Loud"
            >
              <Volume2 className="w-4 h-4" /> {isSpeaking ? 'Stop Voice' : 'Read Question'}
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
                  <span>Why is this asked?</span>
                </button>
                {showWhyAsked && (
                  <p className="mt-2 text-xs text-slate-700 bg-teal-50 border border-teal-200 p-3 rounded-xl">
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
                      placeholder="Type your answer here..."
                      className="w-full text-lg p-4 rounded-2xl border-2 border-slate-900 focus:ring-4 focus:ring-teal-400 text-slate-950 bg-white font-medium shadow-inner"
                    />
                  ) : (
                    <input
                      type={currentField.fieldType === 'DATE' ? 'date' : 'text'}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder="Type your answer here..."
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
                    <Mic className="w-4 h-4" /> {isListening ? 'Listening...' : 'Voice Input'}
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
                  <ArrowLeft className="w-5 h-5" /> Previous Question
                </button>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-8 py-3.5 rounded-2xl bg-teal-600 hover:bg-teal-700 text-white font-extrabold text-lg flex items-center gap-2 shadow-lg focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
                >
                  <span>{currentStep === totalSteps ? 'Review Answers' : 'Next Question'}</span>
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
                    Required Field
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
                  <HelpCircle className="w-4 h-4 text-teal-700" /> Why is this asked?
                </p>
                <p className="text-slate-700">{currentField.whyAsked}</p>
              </div>
            )}

            {/* Input Form */}
            <form onSubmit={handleNext} className="space-y-6">
              <div className="space-y-2">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Your Response:
                </label>
                <div className="relative">
                  {currentField.fieldType === 'TEXTAREA' ? (
                    <textarea
                      rows={3}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder="Type your response..."
                      className="w-full text-base p-3.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-slate-900"
                    />
                  ) : (
                    <input
                      type={currentField.fieldType === 'DATE' ? 'date' : 'text'}
                      value={answerValue}
                      onChange={(e) => setAnswerValue(e.target.value)}
                      placeholder="Type your response..."
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
                    <Mic className="w-3.5 h-3.5" /> Dictate
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
                  <ArrowLeft className="w-4 h-4" /> Previous
                </button>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-6 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 text-white font-bold text-sm flex items-center gap-2 shadow-xs focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
                >
                  <span>{currentStep === totalSteps ? 'Go to Review' : 'Next Step'}</span>
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
