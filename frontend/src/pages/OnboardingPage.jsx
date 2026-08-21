import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';
import { StepProgress } from '../components/common/StepProgress';
import { Globe, Volume2, Brain, Eye, ArrowRight, ArrowLeft, CheckCircle2, Sparkles } from 'lucide-react';

const LANGUAGES = [
  { name: 'English', native: 'English' },
  { name: 'Spanish', native: 'Español' },
  { name: 'Hindi', native: 'हिन्दी' },
  { name: 'Tamil', native: 'தமிழ்' },
  { name: 'Telugu', native: 'తెలుగు' },
  { name: 'Bengali', native: 'বাংলা' },
  { name: 'Marathi', native: 'मराठी' },
  { name: 'Gujarati', native: 'ગુજરાતી' },
  { name: 'French', native: 'Français' },
  { name: 'German', native: 'Deutsch' },
];

const ACCESSIBILITY_NEEDS = [
  { id: 'NONE', label: 'None / Standard', desc: 'Default form filling view' },
  { id: 'VISUAL', label: 'Visual Assist', desc: 'High contrast text, large touch targets, voice synthesis' },
  { id: 'COGNITIVE', label: 'Cognitive Calm', desc: 'One simple field per screen, plain language, zero noise' },
  { id: 'MOTOR', label: 'Motor Friendly', desc: 'Large tap areas and keyboard-focused navigation' },
  { id: 'LITERACY', label: 'Plain Language & Voice', desc: 'Read-aloud assistance and simplified explanations' },
];

export const OnboardingPage = () => {
  const { profile, updateProfile } = useProfile();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [prefLang, setPrefLang] = useState(profile.preferredLanguage || 'English');
  const [prefVoice, setPrefVoice] = useState(profile.voicePreference || false);
  const [overwhelmed, setOverwhelmed] = useState(profile.cognitiveLoadPreference === 'LOW');
  const [need, setNeed] = useState(profile.accessibilityNeed || 'NONE');
  const [isSaving, setIsSaving] = useState(false);

  const totalSteps = 5; // 4 questions + 1 summary

  const handleNext = () => {
    if (step < totalSteps) setStep(step + 1);
  };

  const handleBack = () => {
    if (step > 1) setStep(step - 1);
  };

  const handleSave = async () => {
    setIsSaving(true);
    const updated = {
      preferredLanguage: prefLang,
      voicePreference: prefVoice,
      cognitiveLoadPreference: overwhelmed ? 'LOW' : 'STANDARD',
      accessibilityNeed: need,
    };

    try {
      await updateProfile(updated);
      navigate('/dashboard');
    } catch (err) {
      console.error('Failed to save profile:', err);
      navigate('/dashboard');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 flex items-center justify-center">
      <div className="max-w-xl w-full bg-white rounded-2xl border border-slate-200 shadow-lg p-6 sm:p-10 space-y-8 animate-fade-in">
        {/* Step Progress Header */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-teal-700 uppercase tracking-wider flex items-center gap-1">
              <Sparkles className="w-4 h-4" /> Accessibility Profile Setup
            </span>
            <span className="text-xs text-slate-500 font-medium">Step {step} of 5</span>
          </div>
          <StepProgress currentStep={step} totalSteps={totalSteps} />
        </div>

        {/* STEP 1: Language */}
        {step === 1 && (
          <div className="space-y-6">
            <div className="space-y-2">
              <h1 className="text-2xl font-extrabold text-slate-900 flex items-center gap-2">
                <Globe className="w-6 h-6 text-teal-600" /> What is your preferred language?
              </h1>
              <p className="text-slate-600 text-sm">
                Fill-For-Me will render field explanations and assistant prompts in your language.
              </p>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              {LANGUAGES.map((lang) => (
                <button
                  key={lang.name}
                  type="button"
                  onClick={() => setPrefLang(lang.name)}
                  aria-pressed={prefLang === lang.name}
                  className={`p-3.5 rounded-xl border text-left font-medium text-sm transition-all flex flex-col justify-between ${
                    prefLang === lang.name
                      ? 'border-teal-600 bg-teal-50 text-teal-950 ring-2 ring-teal-600 font-bold'
                      : 'border-slate-200 hover:border-slate-300 text-slate-700 bg-white'
                  }`}
                >
                  <span className="text-xs text-slate-500">{lang.native}</span>
                  <span className="text-base">{lang.name}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 2: Voice Preference */}
        {step === 2 && (
          <div className="space-y-6">
            <div className="space-y-2">
              <h1 className="text-2xl font-extrabold text-slate-900 flex items-center gap-2">
                <Volume2 className="w-6 h-6 text-amber-500" /> Would you like voice read-aloud assistance?
              </h1>
              <p className="text-slate-600 text-sm">
                We can automatically speak form questions out loud to make reading effortless.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <button
                type="button"
                onClick={() => setPrefVoice(true)}
                aria-pressed={prefVoice === true}
                className={`p-6 rounded-2xl border text-left transition-all ${
                  prefVoice === true
                    ? 'border-amber-500 bg-amber-50/70 text-slate-900 ring-2 ring-amber-500'
                    : 'border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                <div className="p-2.5 bg-amber-100 text-amber-700 rounded-xl inline-block mb-3">
                  <Volume2 className="w-6 h-6" />
                </div>
                <h3 className="font-bold text-lg">Enable Voice Read-Aloud</h3>
                <p className="text-xs text-slate-600 mt-1">Questions will be read out loud automatically.</p>
              </button>

              <button
                type="button"
                onClick={() => setPrefVoice(false)}
                aria-pressed={prefVoice === false}
                className={`p-6 rounded-2xl border text-left transition-all ${
                  prefVoice === false
                    ? 'border-teal-600 bg-teal-50 text-slate-900 ring-2 ring-teal-600'
                    : 'border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                <div className="p-2.5 bg-slate-100 text-slate-700 rounded-xl inline-block mb-3">
                  <Eye className="w-6 h-6" />
                </div>
                <h3 className="font-bold text-lg">Visual Text Only</h3>
                <p className="text-xs text-slate-600 mt-1">I prefer reading text without automatic voice output.</p>
              </button>
            </div>
          </div>
        )}

        {/* STEP 3: Form Overwhelm / Cognitive Load */}
        {step === 3 && (
          <div className="space-y-6">
            <div className="space-y-2">
              <h1 className="text-2xl font-extrabold text-slate-900 flex items-center gap-2">
                <Brain className="w-6 h-6 text-teal-600" /> Do long or complex forms feel overwhelming?
              </h1>
              <p className="text-slate-600 text-sm">
                Cognitive-Load mode presents exactly one question per card with zero distractions.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <button
                type="button"
                onClick={() => setOverwhelmed(true)}
                aria-pressed={overwhelmed === true}
                className={`p-6 rounded-2xl border text-left transition-all ${
                  overwhelmed === true
                    ? 'border-teal-600 bg-teal-50 text-slate-900 ring-2 ring-teal-600'
                    : 'border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                <div className="p-2.5 bg-teal-100 text-teal-800 rounded-xl inline-block mb-3">
                  <Brain className="w-6 h-6" />
                </div>
                <h3 className="font-bold text-lg">Yes, turn on Cognitive Mode</h3>
                <p className="text-xs text-slate-600 mt-1">Show one clear question at a time in extra-large text.</p>
              </button>

              <button
                type="button"
                onClick={() => setOverwhelmed(false)}
                aria-pressed={overwhelmed === false}
                className={`p-6 rounded-2xl border text-left transition-all ${
                  overwhelmed === false
                    ? 'border-slate-600 bg-slate-50 text-slate-900 ring-2 ring-slate-600'
                    : 'border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                <div className="p-2.5 bg-slate-200 text-slate-700 rounded-xl inline-block mb-3">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h3 className="font-bold text-lg">Standard View is Fine</h3>
                <p className="text-xs text-slate-600 mt-1">Standard form layout with step navigation.</p>
              </button>
            </div>
          </div>
        )}

        {/* STEP 4: Primary Accessibility Need */}
        {step === 4 && (
          <div className="space-y-6">
            <div className="space-y-2">
              <h1 className="text-2xl font-extrabold text-slate-900 flex items-center gap-2">
                <Eye className="w-6 h-6 text-indigo-600" /> Do you have any specific accessibility needs?
              </h1>
              <p className="text-slate-600 text-sm">
                Select your primary need so we can tailor contrast, target sizes, and help tooltips.
              </p>
            </div>

            <div className="space-y-3">
              {ACCESSIBILITY_NEEDS.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setNeed(item.id)}
                  aria-pressed={need === item.id}
                  className={`w-full p-4 rounded-xl border text-left transition-all flex items-center justify-between ${
                    need === item.id
                      ? 'border-indigo-600 bg-indigo-50/70 text-indigo-950 ring-2 ring-indigo-600 font-semibold'
                      : 'border-slate-200 hover:border-slate-300 text-slate-700 bg-white'
                  }`}
                >
                  <div>
                    <span className="block font-bold text-sm text-slate-900">{item.label}</span>
                    <span className="block text-xs text-slate-500 mt-0.5">{item.desc}</span>
                  </div>
                  {need === item.id && <CheckCircle2 className="w-5 h-5 text-indigo-600 shrink-0" />}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 5: Summary Review */}
        {step === 5 && (
          <div className="space-y-6">
            <div className="space-y-2">
              <h1 className="text-2xl font-extrabold text-slate-900 flex items-center gap-2">
                <CheckCircle2 className="w-6 h-6 text-teal-600" /> Review Your Profile
              </h1>
              <p className="text-slate-600 text-sm">Here is how Fill-For-Me will be customized for you:</p>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-xl p-5 space-y-4 text-sm">
              <div className="flex justify-between items-center border-b border-slate-200 pb-3">
                <span className="text-slate-600">Preferred Language</span>
                <span className="font-bold text-slate-900 bg-white px-2.5 py-1 rounded border border-slate-200">{prefLang}</span>
              </div>

              <div className="flex justify-between items-center border-b border-slate-200 pb-3">
                <span className="text-slate-600">Voice Read-Aloud</span>
                <span className={`font-bold px-2.5 py-1 rounded border ${prefVoice ? 'bg-amber-100 text-amber-800 border-amber-300' : 'bg-slate-200 text-slate-700'}`}>
                  {prefVoice ? 'ENABLED' : 'DISABLED'}
                </span>
              </div>

              <div className="flex justify-between items-center border-b border-slate-200 pb-3">
                <span className="text-slate-600">Cognitive-Load Mode</span>
                <span className={`font-bold px-2.5 py-1 rounded border ${overwhelmed ? 'bg-teal-100 text-teal-800 border-teal-300' : 'bg-slate-200 text-slate-700'}`}>
                  {overwhelmed ? 'ON (Calm View)' : 'OFF (Standard)'}
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-slate-600">Accessibility Need</span>
                <span className="font-bold text-slate-900 bg-white px-2.5 py-1 rounded border border-slate-200">{need}</span>
              </div>
            </div>
          </div>
        )}

        {/* Navigation Buttons */}
        <div className="flex items-center justify-between pt-4 border-t border-slate-100">
          {step > 1 ? (
            <button
              type="button"
              onClick={handleBack}
              className="px-4 py-2.5 rounded-xl border border-slate-300 hover:bg-slate-100 text-slate-700 font-semibold text-sm flex items-center gap-2 focus:ring-2 focus:ring-slate-400"
            >
              <ArrowLeft className="w-4 h-4" /> Back
            </button>
          ) : <div />}

          {step < totalSteps ? (
            <button
              type="button"
              onClick={handleNext}
              className="px-6 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 text-white font-bold text-sm flex items-center gap-2 shadow-xs focus:ring-4 focus:ring-teal-300 cursor-pointer"
            >
              <span>Next Question</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          ) : (
            <button
              type="button"
              onClick={handleSave}
              disabled={isSaving}
              className="px-8 py-3 rounded-xl bg-teal-600 hover:bg-teal-700 text-white font-extrabold text-base flex items-center gap-2 shadow-md focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer"
            >
              <span>{isSaving ? 'Saving Profile...' : 'Save & Open Dashboard'}</span>
              <CheckCircle2 className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
