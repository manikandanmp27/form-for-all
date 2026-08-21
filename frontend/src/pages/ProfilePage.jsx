import React, { useState } from 'react';
import { useProfile } from '../context/ProfileContext';
import { ProfileBar } from '../components/layout/ProfileBar';
import { Settings, Globe, Volume2, Brain, Eye, CheckCircle2, Save } from 'lucide-react';

const LANGUAGES = ['English', 'Spanish', 'Hindi', 'Tamil', 'Telugu', 'Bengali', 'Marathi', 'Gujarati', 'French', 'German'];

const ACCESSIBILITY_NEEDS = [
  { id: 'NONE', label: 'None / Standard View' },
  { id: 'VISUAL', label: 'Visual Assist (High contrast & voice read-aloud)' },
  { id: 'COGNITIVE', label: 'Cognitive Calm (Low-density single field view)' },
  { id: 'MOTOR', label: 'Motor Friendly (Large targets & keyboard focused)' },
  { id: 'LITERACY', label: 'Plain Language & Voice Assistance' },
];

export const ProfilePage = () => {
  const { profile, updateProfile } = useProfile();

  const [lang, setLang] = useState(profile.preferredLanguage || 'English');
  const [voice, setVoice] = useState(profile.voicePreference || false);
  const [cogMode, setCogMode] = useState(profile.cognitiveLoadPreference || 'STANDARD');
  const [need, setNeed] = useState(profile.accessibilityNeed || 'NONE');
  const [isSaved, setIsSaved] = useState(false);

  const handleSave = async (e) => {
    e.preventDefault();
    await updateProfile({
      preferredLanguage: lang,
      voicePreference: voice,
      cognitiveLoadPreference: cogMode,
      accessibilityNeed: need,
    });
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 3000);
  };

  return (
    <div className="bg-slate-50 min-h-screen flex flex-col">
      <ProfileBar />

      <main className="max-w-3xl mx-auto px-4 py-8 flex-1 w-full space-y-6">
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
            <div className="p-3 bg-teal-100 text-teal-700 rounded-xl">
              <Settings className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-2xl font-extrabold text-slate-900">Accessibility & Profile Settings</h1>
              <p className="text-slate-600 text-xs">
                Customize your Fill-For-Me experience to fit your personal comfort and needs.
              </p>
            </div>
          </div>

          {isSaved && (
            <div className="p-4 bg-teal-50 border border-teal-200 text-teal-900 rounded-xl text-sm font-semibold flex items-center gap-2 animate-fade-in">
              <CheckCircle2 className="w-5 h-5 text-teal-600" /> Accessibility settings saved successfully!
            </div>
          )}

          <form onSubmit={handleSave} className="space-y-6">
            {/* Preferred Language */}
            <div className="space-y-2">
              <label htmlFor="pref-lang" className="block text-sm font-bold text-slate-900 flex items-center gap-2">
                <Globe className="w-4 h-4 text-teal-600" /> Preferred Language
              </label>
              <select
                id="pref-lang"
                value={lang}
                onChange={(e) => setLang(e.target.value)}
                className="w-full p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-sm"
              >
                {LANGUAGES.map((l) => (
                  <option key={l} value={l}>
                    {l}
                  </option>
                ))}
              </select>
            </div>

            {/* Voice Read-Aloud */}
            <div className="space-y-2">
              <label className="block text-sm font-bold text-slate-900 flex items-center gap-2">
                <Volume2 className="w-4 h-4 text-amber-500" /> Text-to-Speech Voice Assistant
              </label>
              <div className="flex items-center gap-4 bg-slate-50 p-4 rounded-xl border border-slate-200">
                <button
                  type="button"
                  onClick={() => setVoice(!voice)}
                  aria-pressed={voice}
                  className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                    voice ? 'bg-amber-400 text-slate-950 font-bold' : 'bg-slate-200 text-slate-700'
                  }`}
                >
                  {voice ? 'ENABLED' : 'DISABLED'}
                </button>
                <span className="text-xs text-slate-600">
                  Automatically speak form questions and warning notifications out loud.
                </span>
              </div>
            </div>

            {/* Cognitive Load Preference */}
            <div className="space-y-2">
              <label className="block text-sm font-bold text-slate-900 flex items-center gap-2">
                <Brain className="w-4 h-4 text-teal-600" /> Visual Density & Cognitive Mode
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setCogMode('LOW')}
                  aria-pressed={cogMode === 'LOW'}
                  className={`p-4 rounded-xl border text-left text-xs font-semibold transition-all ${
                    cogMode === 'LOW'
                      ? 'border-teal-600 bg-teal-50 text-teal-950 ring-2 ring-teal-600'
                      : 'border-slate-200 text-slate-700'
                  }`}
                >
                  <span className="font-bold text-sm block">Cognitive Mode (LOW)</span>
                  Single field card view with extra large readable text.
                </button>

                <button
                  type="button"
                  onClick={() => setCogMode('STANDARD')}
                  aria-pressed={cogMode === 'STANDARD'}
                  className={`p-4 rounded-xl border text-left text-xs font-semibold transition-all ${
                    cogMode === 'STANDARD'
                      ? 'border-slate-600 bg-slate-100 text-slate-950 ring-2 ring-slate-600'
                      : 'border-slate-200 text-slate-700'
                  }`}
                >
                  <span className="font-bold text-sm block">Standard View</span>
                  Standard multi-step form view.
                </button>
              </div>
            </div>

            {/* Accessibility Need */}
            <div className="space-y-2">
              <label htmlFor="pref-need" className="block text-sm font-bold text-slate-900 flex items-center gap-2">
                <Eye className="w-4 h-4 text-indigo-600" /> Primary Accessibility Preference
              </label>
              <select
                id="pref-need"
                value={need}
                onChange={(e) => setNeed(e.target.value)}
                className="w-full p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-sm"
              >
                {ACCESSIBILITY_NEEDS.map((n) => (
                  <option key={n.id} value={n.id}>
                    {n.label}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="submit"
              className="bg-teal-600 hover:bg-teal-700 text-white font-extrabold px-8 py-3 rounded-xl shadow-md transition-colors text-sm flex items-center gap-2 cursor-pointer focus:ring-4 focus:ring-teal-300"
            >
              <Save className="w-4 h-4" /> Save Profile Preferences
            </button>
          </form>
        </div>
      </main>
    </div>
  );
};
