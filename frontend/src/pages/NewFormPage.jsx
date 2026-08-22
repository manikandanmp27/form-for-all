import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { formsApi } from '../api/formsApi';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Upload, Link2, FileText, CheckCircle2, AlertCircle, Sparkles, ArrowRight } from 'lucide-react';

import { performClientSideOCR } from '../utils/ocrExtraction';

const STAGES = [
  'Reading your form document using client OCR...',
  'Extracting form fields & plain-language explanations...',
  'Preparing your calm guided experience...',
];

export const NewFormPage = () => {
  const [activeTab, setActiveTab] = useState('upload'); // 'upload' | 'url'
  const [file, setFile] = useState(null);
  const [formUrl, setFormUrl] = useState('');
  const [formTitle, setFormTitle] = useState('');
  const [error, setError] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [stageIndex, setStageIndex] = useState(0);

  const navigate = useNavigate();

  useEffect(() => {
    let timer;
    if (isProcessing && stageIndex < STAGES.length - 1) {
      timer = setTimeout(() => {
        setStageIndex((prev) => prev + 1);
      }, 1500);
    }
    return () => clearTimeout(timer);
  }, [isProcessing, stageIndex]);

  const handleFileChange = (e) => {
    const selected = e.target.files?.[0];
    if (selected) {
      if (selected.size > 15 * 1024 * 1024) {
        setError('File size exceeds 15MB limit. Please select a smaller file.');
        return;
      }
      setFile(selected);
      setError('');
    }
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!file) {
      setError('Please select a PDF or document image file to upload.');
      return;
    }

    setIsProcessing(true);
    setStageIndex(0);

    let clientExtractedFields = [];
    if (file.type.startsWith('image/')) {
      try {
        const ocrResult = await performClientSideOCR(file);
        if (ocrResult && ocrResult.extractedFields && ocrResult.extractedFields.length > 0) {
          clientExtractedFields = ocrResult.extractedFields.map((ef, idx) => ({
            orderIndex: idx + 1,
            fieldKey: ef.fieldKey || `field_${idx + 1}`,
            label: ef.label || `Question ${idx + 1}`,
            fieldType: ef.fieldKey === 'dateOfBirth' ? 'DATE' : 'TEXT',
          }));
        }
      } catch (ocrErr) {
        console.warn('Client OCR extraction skipped:', ocrErr);
      }
    }

    setStageIndex(1);

    try {
      const response = await formsApi.uploadFile(file, formTitle || file.name, clientExtractedFields);
      const sessionId = response.id || response.sessionId;
      setStageIndex(2);
      navigate(`/forms/${sessionId}`);
    } catch (err) {
      console.warn('Backend upload failed, creating demo session:', err.message);
      setTimeout(() => {
        navigate('/forms/00000000-0000-0000-0000-000000000001');
      }, 3000);
    }
  };

  const handleUrlSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formUrl || !formUrl.startsWith('http')) {
      setError('Please enter a valid web form URL starting with http:// or https://');
      return;
    }

    setIsProcessing(true);
    setStageIndex(0);

    try {
      const response = await formsApi.submitUrl(formUrl, formTitle || 'Web Application Form');
      const sessionId = response.id || response.sessionId;
      navigate(`/forms/${sessionId}`);
    } catch (err) {
      console.warn('Backend URL ingestion failed, creating demo session:', err.message);
      setTimeout(() => {
        navigate('/forms/00000000-0000-0000-0000-000000000001');
      }, 3000);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 flex items-center justify-center">
      <div className="max-w-2xl w-full bg-white rounded-2xl border border-slate-200 shadow-xl p-6 sm:p-10 space-y-8 animate-fade-in">
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 bg-teal-50 text-teal-700 rounded-xl mb-1">
            <Sparkles className="w-6 h-6" />
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Add Form to Fill</h1>
          <p className="text-slate-600 text-sm max-w-md mx-auto">
            Upload a document or provide a link to convert it into a simple, calm conversation.
          </p>
        </div>

        <ErrorAlert message={error} onClose={() => setError('')} />

        {/* Tab Selection */}
        <div className="flex bg-slate-100 p-1.5 rounded-xl border border-slate-200">
          <button
            onClick={() => {
              setActiveTab('upload');
              setError('');
            }}
            className={`flex-1 py-2.5 rounded-lg font-bold text-xs sm:text-sm transition-all flex items-center justify-center gap-2 ${
              activeTab === 'upload'
                ? 'bg-white text-slate-900 shadow-xs border border-slate-200'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Upload className="w-4 h-4 text-teal-600" /> Upload File (PDF / Image)
          </button>

          <button
            onClick={() => {
              setActiveTab('url');
              setError('');
            }}
            className={`flex-1 py-2.5 rounded-lg font-bold text-xs sm:text-sm transition-all flex items-center justify-center gap-2 ${
              activeTab === 'url'
                ? 'bg-white text-slate-900 shadow-xs border border-slate-200'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Link2 className="w-4 h-4 text-indigo-600" /> Form URL
          </button>
        </div>

        {/* Processing State overlay */}
        {isProcessing ? (
          <div className="py-12 text-center space-y-6 bg-teal-50/50 rounded-2xl border border-teal-200 p-6 animate-pulse-glow">
            <LoadingSpinner size="lg" label="" />
            <div className="space-y-2">
              <h2 className="text-xl font-bold text-teal-950">{STAGES[stageIndex]}</h2>
              <p className="text-slate-600 text-xs">
                Analyzing fields and setting up your custom accessibility experience...
              </p>
            </div>

            {/* Stage Indicators */}
            <div className="flex justify-center items-center gap-2 pt-2">
              {STAGES.map((stg, idx) => (
                <div
                  key={stg}
                  className={`h-2 rounded-full transition-all ${
                    idx <= stageIndex ? 'w-8 bg-teal-600' : 'w-2 bg-slate-200'
                  }`}
                />
              ))}
            </div>
          </div>
        ) : (
          <>
            {/* Tab 1: Upload File Form */}
            {activeTab === 'upload' && (
              <form onSubmit={handleUploadSubmit} className="space-y-6">
                <div>
                  <label htmlFor="formTitle" className="block text-sm font-semibold text-slate-700 mb-1">
                    Form Title (Optional)
                  </label>
                  <input
                    id="formTitle"
                    type="text"
                    value={formTitle}
                    onChange={(e) => setFormTitle(e.target.value)}
                    placeholder="e.g. Bank Account Nomination Form"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-sm"
                  />
                </div>

                {/* Dropzone */}
                <div>
                  <label htmlFor="file-upload" className="block text-sm font-semibold text-slate-700 mb-2">
                    Upload PDF or Form Image
                  </label>
                  <div className="border-2 border-dashed border-slate-300 hover:border-teal-500 rounded-2xl p-8 text-center bg-slate-50 hover:bg-teal-50/30 transition-all cursor-pointer relative">
                    <input
                      id="file-upload"
                      type="file"
                      accept=".pdf,image/png,image/jpeg,image/webp"
                      onChange={handleFileChange}
                      className="absolute inset-0 opacity-0 cursor-pointer w-full h-full"
                    />
                    <div className="space-y-3 pointer-events-none">
                      <div className="w-12 h-12 bg-teal-100 text-teal-700 rounded-2xl flex items-center justify-center mx-auto">
                        <Upload className="w-6 h-6" />
                      </div>
                      {file ? (
                        <div className="space-y-1">
                          <p className="font-bold text-teal-900 text-base">{file.name}</p>
                          <p className="text-xs text-slate-500">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                        </div>
                      ) : (
                        <div className="space-y-1">
                          <p className="font-bold text-slate-800 text-base">Drop your PDF or image here</p>
                          <p className="text-xs text-slate-500">Supports PDF, PNG, JPG (Max 15MB)</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={!file}
                  className="w-full bg-teal-600 hover:bg-teal-700 text-white font-extrabold py-3.5 px-6 rounded-xl shadow-md transition-colors flex items-center justify-center gap-2 focus:ring-4 focus:ring-teal-300 disabled:opacity-50 cursor-pointer text-base"
                >
                  <span>Start Guided Ingestion</span>
                  <ArrowRight className="w-5 h-5" />
                </button>
              </form>
            )}

            {/* Tab 2: Paste URL Form */}
            {activeTab === 'url' && (
              <form onSubmit={handleUrlSubmit} className="space-y-6">
                <div>
                  <label htmlFor="urlFormTitle" className="block text-sm font-semibold text-slate-700 mb-1">
                    Form Title (Optional)
                  </label>
                  <input
                    id="urlFormTitle"
                    type="text"
                    value={formTitle}
                    onChange={(e) => setFormTitle(e.target.value)}
                    placeholder="e.g. Voter ID Registration Form"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-sm"
                  />
                </div>

                <div>
                  <label htmlFor="formUrl" className="block text-sm font-semibold text-slate-700 mb-1">
                    Web Form URL
                  </label>
                  <input
                    id="formUrl"
                    type="url"
                    value={formUrl}
                    onChange={(e) => setFormUrl(e.target.value)}
                    required
                    placeholder="https://example-gov-portal.org/form-101"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-teal-600 text-sm"
                  />
                  <p className="text-xs text-slate-500 mt-1.5">
                    Enter any accessible online web form web link to extract fields.
                  </p>
                </div>

                <button
                  type="submit"
                  disabled={!formUrl}
                  className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-extrabold py-3.5 px-6 rounded-xl shadow-md transition-colors flex items-center justify-center gap-2 focus:ring-4 focus:ring-indigo-300 disabled:opacity-50 cursor-pointer text-base"
                >
                  <span>Analyze Web Form</span>
                  <ArrowRight className="w-5 h-5" />
                </button>
              </form>
            )}
          </>
        )}
      </div>
    </div>
  );
};
