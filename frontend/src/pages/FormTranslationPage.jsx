import React, { useState, useEffect } from 'react';
import { translationApi } from '../api/translationApi';
import { ProfileBar } from '../components/layout/ProfileBar';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import {
  Languages,
  Upload,
  FileText,
  Download,
  Sparkles,
  RefreshCw,
  CheckCircle2,
  Globe,
  ArrowRight,
  Eye,
  ShieldCheck,
} from 'lucide-react';

import { extractBoundingBoxesFromImage } from '../utils/ocrExtraction';

export const FormTranslationPage = () => {
  const [file, setFile] = useState(null);
  const [filePreviewUrl, setFilePreviewUrl] = useState(null);
  const [targetLanguage, setTargetLanguage] = useState('en'); // Default target to English
  const [languages, setLanguages] = useState([]);
  const [isTranslating, setIsTranslating] = useState(false);
  const [translationResult, setTranslationResult] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadLanguages = async () => {
      try {
        const data = await translationApi.getSupportedLanguages();
        if (data && data.length > 0) {
          setLanguages(data);
        }
      } catch (err) {
        console.warn('Fallback languages dictionary used:', err);
        setLanguages([
          { code: 'en', name: 'English', nativeName: 'English' },
          { code: 'kn', name: 'Kannada', nativeName: 'ಕನ್ನಡ' },
          { code: 'hi', name: 'Hindi', nativeName: 'हिन्दी' },
          { code: 'ta', name: 'Tamil', nativeName: 'தமிழ்' },
          { code: 'te', name: 'Telugu', nativeName: 'తెలుగు' },
          { code: 'ml', name: 'Malayalam', nativeName: 'മലയാളം' },
          { code: 'mr', name: 'Marathi', nativeName: 'मराठी' },
          { code: 'bn', name: 'Bengali', nativeName: 'বাংলা' },
          { code: 'gu', name: 'Gujarati', nativeName: 'ગુજરાતી' },
          { code: 'pa', name: 'Punjabi', nativeName: 'ਪੰਜਾਬੀ' },
        ]);
      }
    };

    loadLanguages();
  }, []);

  const handleFileChange = (e) => {
    const selected = e.target.files?.[0];
    if (!selected) return;

    if (selected.size > 15 * 1024 * 1024) {
      setError('File size exceeds 15MB limit. Please upload a smaller file.');
      return;
    }

    setFile(selected);
    setFilePreviewUrl(URL.createObjectURL(selected));
    setTranslationResult(null);
    setError('');
  };

  const handleTranslate = async (e) => {
    e?.preventDefault();
    if (!file) {
      setError('Please select a PDF or document image to translate.');
      return;
    }

    setIsTranslating(true);
    setError('');

    try {
      let textRegions = [];
      if (file.type.startsWith('image/')) {
        try {
          textRegions = await extractBoundingBoxesFromImage(file);
        } catch (ocrErr) {
          console.warn('Client OCR bounding box extraction skipped:', ocrErr);
        }
      }

      const res = await translationApi.translateForm(file, targetLanguage, 'auto', textRegions);
      setTranslationResult(res);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Form layout translation failed. Please try a clearer document.');
    } finally {
      setIsTranslating(false);
    }
  };

  return (
    <div className="bg-slate-50 min-h-screen flex flex-col">
      <ProfileBar />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full space-y-8">
        {/* Header */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-teal-100 text-teal-800 text-xs font-extrabold border border-teal-200">
              <Globe className="w-3.5 h-3.5 text-teal-700" /> Multi-Language Regional Form Translator
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Visual Layout-Preserving Translator
            </h1>
            <p className="text-slate-600 text-xs sm:text-sm max-w-xl leading-relaxed">
              Upload any government, banking, or application form. Our backend erases text regions while preserving 100% of borders, boxes, checkboxes, and lines!
            </p>
          </div>

          <div className="p-4 bg-teal-50 border border-teal-200 rounded-2xl flex items-center gap-3 text-xs text-teal-900 font-bold shrink-0">
            <ShieldCheck className="w-5 h-5 text-teal-700" />
            <span>Preserves Borders, Tables & Checkboxes</span>
          </div>
        </div>

        <ErrorAlert message={error} onClose={() => setError('')} />

        {/* Translation Setup Controls Grid */}
        <div className="grid md:grid-cols-3 gap-6">
          {/* File Upload Box */}
          <div className="md:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
            <h2 className="font-extrabold text-slate-900 text-base flex items-center gap-2">
              <Upload className="w-4 h-4 text-teal-600" /> 1. Select Form Document
            </h2>

            <label className="border-2 border-dashed border-slate-300 hover:border-teal-500 rounded-2xl p-6 text-center bg-slate-50 hover:bg-teal-50/30 transition-all cursor-pointer block">
              <input
                type="file"
                accept=".pdf,image/png,image/jpeg,image/webp"
                onChange={handleFileChange}
                className="hidden"
              />
              <div className="space-y-2">
                <div className="w-10 h-10 bg-teal-100 text-teal-700 rounded-xl flex items-center justify-center mx-auto">
                  <FileText className="w-5 h-5" />
                </div>
                {file ? (
                  <div>
                    <p className="font-bold text-teal-900 text-sm">{file.name}</p>
                    <p className="text-xs text-slate-500">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                  </div>
                ) : (
                  <div>
                    <p className="font-bold text-slate-800 text-sm">Upload PDF or Form Image</p>
                    <p className="text-xs text-slate-500">Supports PDF, PNG, JPG (Max 15MB)</p>
                  </div>
                )}
              </div>
            </label>
          </div>

          {/* Language Selector & Trigger Box */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col justify-between space-y-4">
            <div className="space-y-3">
              <h2 className="font-extrabold text-slate-900 text-base flex items-center gap-2">
                <Languages className="w-4 h-4 text-indigo-600" /> 2. Target Language
              </h2>

              <select
                value={targetLanguage}
                onChange={(e) => setTargetLanguage(e.target.value)}
                className="w-full p-3 rounded-xl border border-slate-300 font-bold text-xs focus:ring-2 focus:ring-teal-600 bg-white text-slate-900"
              >
                {languages.map((lang) => (
                  <option key={lang.code} value={lang.code}>
                    {lang.name} ({lang.nativeName})
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              onClick={handleTranslate}
              disabled={!file || isTranslating}
              className="w-full bg-teal-600 hover:bg-teal-700 text-white font-extrabold py-3.5 px-4 rounded-xl shadow-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer text-xs"
            >
              {isTranslating ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin text-white" /> Translating Layout...
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4 text-amber-300" /> Translate Form Layout
                </>
              )}
            </button>
          </div>
        </div>

        {/* Translation Output Preview Grid */}
        {(filePreviewUrl || translationResult) && (
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-6 animate-fade-in">
            <div className="flex items-center justify-between border-b border-slate-100 pb-4">
              <h2 className="text-lg font-extrabold text-slate-900 flex items-center gap-2">
                <Eye className="w-5 h-5 text-teal-600" /> Translated Layout Comparison Preview
              </h2>

              {translationResult?.imageUrl && (
                <a
                  href={translationResult.imageUrl}
                  download="translated_form_layout.png"
                  target="_blank"
                  rel="noreferrer"
                  className="bg-teal-600 hover:bg-teal-700 text-white font-bold text-xs px-4 py-2 rounded-xl flex items-center gap-1.5 transition-colors shadow-xs"
                >
                  <Download className="w-3.5 h-3.5" /> Download Translated Form
                </a>
              )}
            </div>

            <div className="grid md:grid-cols-2 gap-6">
              {/* Original Document View */}
              <div className="space-y-2">
                <span className="text-xs font-bold text-slate-600 block">Original Document Preview</span>
                <div className="border border-slate-200 rounded-xl bg-slate-950 p-2 min-h-64 flex items-center justify-center max-h-[500px] overflow-hidden">
                  {file?.type?.includes('pdf') ? (
                    <div className="p-8 text-center text-slate-400 text-xs">
                      <FileText className="w-10 h-10 mx-auto text-teal-400 mb-2" />
                      <p className="font-bold">{file.name}</p>
                      <p>PDF Document Loaded</p>
                    </div>
                  ) : (
                    <img src={filePreviewUrl} alt="Original Form" className="max-h-[480px] object-contain" />
                  )}
                </div>
              </div>

              {/* Translated Layout Output View */}
              <div className="space-y-2">
                <span className="text-xs font-bold text-teal-800 flex items-center gap-1">
                  <Sparkles className="w-3.5 h-3.5 text-teal-600" /> Translated Form Layout ({targetLanguage.toUpperCase()})
                </span>
                <div className="border border-teal-200 rounded-xl bg-slate-950 p-2 min-h-64 flex items-center justify-center max-h-[500px] overflow-hidden relative">
                  {isTranslating ? (
                    <LoadingSpinner label="Erasing text and rendering translated overlay..." />
                  ) : translationResult?.imageUrl ? (
                    <img
                      src={translationResult.imageUrl}
                      alt="Translated Form Layout"
                      className="max-h-[480px] object-contain"
                    />
                  ) : (
                    <div className="p-8 text-center text-slate-400 text-xs space-y-1">
                      <Globe className="w-8 h-8 mx-auto text-slate-500" />
                      <p>Click "Translate Form Layout" to preview translated document</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};
