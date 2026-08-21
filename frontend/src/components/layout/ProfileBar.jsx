import React from 'react';
import { useProfile } from '../../context/ProfileContext';
import { useVoice } from '../../context/VoiceContext';
import { Volume2, VolumeX, Eye, Brain, Settings } from 'lucide-react';
import { Link } from 'react-router-dom';

const LANGUAGES = [
  'English',
  'Spanish',
  'Hindi',
  'Tamil',
  'Telugu',
  'Bengali',
  'Marathi',
  'Gujarati',
  'French',
  'German',
];

export const ProfileBar = () => {
  const { profile, toggleCognitiveMode, toggleVoicePreference, setLanguage, isCognitiveMode } = useProfile();
  const { isSpeaking, stopSpeaking } = useVoice();

  return (
    <div
      aria-label="Accessibility & Preferences Bar"
      className="bg-slate-900 text-slate-100 px-4 py-2.5 shadow-md flex flex-wrap items-center justify-between gap-3 text-sm border-b border-slate-800"
    >
      {/* Left: Language Selection */}
      <div className="flex items-center gap-2">
        <label htmlFor="language-select" className="font-medium text-slate-300 text-xs uppercase tracking-wider flex items-center gap-1">
          <GlobeIcon className="w-3.5 h-3.5 text-teal-400" /> Language:
        </label>
        <select
          id="language-select"
          value={profile.preferredLanguage || 'English'}
          onChange={(e) => setLanguage(e.target.value)}
          className="bg-slate-800 text-white rounded border border-slate-700 px-2 py-1 text-xs focus:ring-2 focus:ring-teal-500 focus:outline-none cursor-pointer"
        >
          {LANGUAGES.map((lang) => (
            <option key={lang} value={lang}>
              {lang}
            </option>
          ))}
        </select>
      </div>

      {/* Center: Accessibility Quick Controls */}
      <div className="flex items-center flex-wrap gap-2">
        {/* Cognitive Load Mode Toggle */}
        <button
          onClick={toggleCognitiveMode}
          aria-pressed={isCognitiveMode}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold transition-all ${
            isCognitiveMode
              ? 'bg-teal-500 text-slate-950 shadow-sm ring-2 ring-teal-300'
              : 'bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700'
          }`}
          title="Toggle Cognitive-Load Mode (Single field, calm layout)"
        >
          <Brain className="w-3.5 h-3.5" />
          <span>Cognitive Mode: {isCognitiveMode ? 'ON (Calm)' : 'OFF'}</span>
        </button>

        {/* Voice Preference Toggle */}
        <button
          onClick={() => {
            if (isSpeaking) stopSpeaking();
            toggleVoicePreference();
          }}
          aria-pressed={profile.voicePreference}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold transition-all ${
            profile.voicePreference
              ? 'bg-amber-400 text-slate-950 shadow-sm ring-2 ring-amber-200'
              : 'bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700'
          }`}
          title="Toggle Read-Aloud Voice Assistant"
        >
          {profile.voicePreference ? <Volume2 className="w-3.5 h-3.5" /> : <VolumeX className="w-3.5 h-3.5" />}
          <span>Voice: {profile.voicePreference ? 'ENABLED' : 'OFF'}</span>
        </button>

        {/* Accessibility Need Badge */}
        {profile.accessibilityNeed && profile.accessibilityNeed !== 'NONE' && (
          <span className="bg-indigo-950 text-indigo-200 border border-indigo-700 px-2.5 py-0.5 rounded-md text-xs flex items-center gap-1 font-medium">
            <Eye className="w-3.5 h-3.5" /> {profile.accessibilityNeed}
          </span>
        )}
      </div>

      {/* Right: Profile Link */}
      <div className="flex items-center gap-2">
        <Link
          to="/profile"
          className="text-slate-300 hover:text-teal-400 flex items-center gap-1 text-xs font-medium focus:ring-2 focus:ring-teal-500 rounded px-1.5 py-0.5"
        >
          <Settings className="w-3.5 h-3.5" /> Profile Settings
        </Link>
      </div>
    </div>
  );
};

const GlobeIcon = ({ className }) => (
  <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
  </svg>
);
