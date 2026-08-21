import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, FileCheck2, Brain, Sparkles, ArrowRight, Volume2, Globe, Heart } from 'lucide-react';
import { useProfile } from '../context/ProfileContext';

export const LandingPage = () => {
  const { setLanguage, profile } = useProfile();

  return (
    <div className="bg-slate-50 min-h-screen flex flex-col">
      {/* Hero Section */}
      <section className="bg-gradient-to-b from-teal-900 via-slate-900 to-slate-900 text-white pt-12 pb-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto text-center space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-teal-500/20 border border-teal-400/30 text-teal-300 text-xs font-semibold uppercase tracking-wider">
            <Sparkles className="w-4 h-4" /> AI-Powered Accessibility Co-Pilot
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-tight text-white">
            Complex forms made <span className="text-teal-400">calm & accessible.</span>
          </h1>

          <p className="text-lg sm:text-xl text-slate-300 max-w-2xl mx-auto leading-relaxed">
            Fill-For-Me translates complex documents into friendly, plain-language conversations. Built for low-stress filling with voice guidance and cognitive-load controls.
          </p>

          {/* Quick Language Selector on Landing */}
          <div className="pt-2 flex items-center justify-center gap-2">
            <span className="text-xs text-slate-400 flex items-center gap-1">
              <Globe className="w-3.5 h-3.5 text-teal-400" /> Preferred Language:
            </span>
            <select
              value={profile.preferredLanguage || 'English'}
              onChange={(e) => setLanguage(e.target.value)}
              className="bg-slate-800 text-teal-200 border border-slate-700 rounded px-2.5 py-1 text-xs focus:ring-2 focus:ring-teal-400 focus:outline-none"
            >
              {['English', 'Spanish', 'Hindi', 'Tamil', 'Telugu', 'Bengali', 'Marathi', 'Gujarati', 'French', 'German'].map((lang) => (
                <option key={lang} value={lang}>
                  {lang}
                </option>
              ))}
            </select>
          </div>

          {/* Primary CTAs */}
          <div className="pt-6 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/onboarding"
              className="w-full sm:w-auto bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold px-8 py-3.5 rounded-xl shadow-lg hover:shadow-teal-500/25 transition-all text-base flex items-center justify-center gap-2 focus:ring-4 focus:ring-teal-300"
            >
              <span>Setup Accessibility Profile</span>
              <ArrowRight className="w-5 h-5" />
            </Link>
            <Link
              to="/forms/new"
              className="w-full sm:w-auto bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 font-semibold px-6 py-3.5 rounded-xl transition-all text-base text-center"
            >
              Upload Form Now
            </Link>
          </div>
        </div>
      </section>

      {/* 3-Step Journey Explanation */}
      <section className="py-16 px-4 max-w-6xl mx-auto">
        <div className="text-center space-y-3 mb-12">
          <h2 className="text-3xl font-bold text-slate-900">How Fill-For-Me Works</h2>
          <p className="text-slate-600">Three simple steps to complete any document without anxiety.</p>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all space-y-4">
            <div className="w-12 h-12 rounded-xl bg-teal-100 text-teal-700 flex items-center justify-center font-bold text-xl">
              1
            </div>
            <h3 className="text-xl font-bold text-slate-900">Upload or Link</h3>
            <p className="text-slate-600 text-sm leading-relaxed">
              Upload a PDF document, an image of a form, or paste a web form link. Our system reads the fields automatically.
            </p>
          </div>

          <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all space-y-4">
            <div className="w-12 h-12 rounded-xl bg-indigo-100 text-indigo-700 flex items-center justify-center font-bold text-xl">
              2
            </div>
            <h3 className="text-xl font-bold text-slate-900">Guided Conversation</h3>
            <p className="text-slate-600 text-sm leading-relaxed">
              Answer one clear question at a time in plain language. Enable Cognitive-Load mode or Text-to-Speech voice assistance whenever needed.
            </p>
          </div>

          <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all space-y-4">
            <div className="w-12 h-12 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center font-bold text-xl">
              3
            </div>
            <h3 className="text-xl font-bold text-slate-900">Risk Review & Export</h3>
            <p className="text-slate-600 text-sm leading-relaxed">
              Receive smart safety warnings before high-impact changes. Review all answers clearly and download your completed form.
            </p>
          </div>
        </div>
      </section>

      {/* Feature Highlights Grid */}
      <section className="bg-slate-100 py-16 px-4 border-t border-slate-200">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-8">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 flex items-start gap-4">
            <div className="p-3 bg-teal-50 text-teal-700 rounded-xl">
              <Brain className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-lg">Cognitive-Load Mode</h4>
              <p className="text-slate-600 text-sm mt-1">
                Removes visual clutter, competing sidebars, and unnecessary animations so you can focus on one single field at a time.
              </p>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-200 flex items-start gap-4">
            <div className="p-3 bg-amber-50 text-amber-700 rounded-xl">
              <Volume2 className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-lg">Voice Read-Aloud</h4>
              <p className="text-slate-600 text-sm mt-1">
                Listen to questions spoken at a calm pace and speak your answers using browser voice input.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
