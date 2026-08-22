import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { formsApi } from '../api/formsApi';
import { useProfile } from '../context/ProfileContext';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { ProfileBar } from '../components/layout/ProfileBar';
import { PlusCircle, Link2, FileText, AlertTriangle, Play, CheckCircle2, Clock, Brain, ArrowRight } from 'lucide-react';

export const DashboardPage = () => {
  const [sessions, setSessions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const { profile, isCognitiveMode, t } = useProfile();
  const navigate = useNavigate();

  useEffect(() => {
    const loadSessions = async () => {
      setIsLoading(true);
      try {
        const data = await formsApi.getAllForms();
        setSessions(data || []);
      } catch (err) {
        console.warn('Could not load server sessions, loading demo sessions:', err.message);
        // Fallback demo session if backend returns empty or unavailable
        setSessions([
          {
            id: '00000000-0000-0000-0000-000000000001',
            formTitle: 'Bank Account Nomination Form',
            formSourceType: 'PDF_UPLOAD',
            sessionStatus: 'IN_PROGRESS',
            currentFieldIndex: 3,
            totalFields: 8,
            createdAt: new Date().toISOString(),
          },
        ]);
      } finally {
        setIsLoading(false);
      }
    };

    loadSessions();
  }, []);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
      case 'SUBMITTED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-teal-100 text-teal-800 border border-teal-200">
            <CheckCircle2 className="w-3.5 h-3.5" /> Completed
          </span>
        );
      case 'REVIEW_REQUIRED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800 border border-amber-200">
            <AlertTriangle className="w-3.5 h-3.5" /> Needs Review
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-sky-100 text-sky-800 border border-sky-200">
            <Clock className="w-3.5 h-3.5" /> In Progress
          </span>
        );
    }
  };

  return (
    <div className="bg-slate-50 min-h-screen flex flex-col">
      <ProfileBar />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full space-y-8">
        {/* Welcome Header */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div className="space-y-1">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              {t('dashboardTitle')}
            </h1>
            <p className="text-slate-600 text-sm">
              {t('dashboardSubtitle')}
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/forms/new"
              className="bg-teal-600 hover:bg-teal-700 text-white font-bold px-5 py-2.5 rounded-xl shadow-xs transition-colors flex items-center gap-2 text-sm focus:ring-4 focus:ring-teal-300"
            >
              <PlusCircle className="w-4 h-4" /> {t('uploadForm')}
            </Link>
          </div>
        </div>

        <ErrorAlert message={error} onClose={() => setError('')} />

        {/* Primary Action Cards Grid */}
        <div className="grid md:grid-cols-2 gap-6">
          {/* Upload PDF/Image Card */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs hover:border-teal-300 transition-all flex flex-col justify-between space-y-4">
            <div className="flex items-start gap-4">
              <div className="p-3 bg-teal-100 text-teal-700 rounded-xl">
                <FileText className="w-6 h-6" />
              </div>
              <div className="space-y-1">
                <h2 className="text-lg font-bold text-slate-900">{t('uploadDocument')}</h2>
                <p className="text-slate-600 text-xs leading-relaxed">
                  {t('uploadDocDesc')}
                </p>
              </div>
            </div>
            <Link
              to="/forms/new"
              className="bg-teal-50 hover:bg-teal-100 text-teal-800 font-bold px-4 py-2.5 rounded-xl text-xs text-center border border-teal-200 transition-colors flex items-center justify-center gap-1.5"
            >
              <span>{t('uploadForm')}</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {/* Paste Link Card */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs hover:border-indigo-300 transition-all flex flex-col justify-between space-y-4">
            <div className="flex items-start gap-4">
              <div className="p-3 bg-indigo-100 text-indigo-700 rounded-xl">
                <Link2 className="w-6 h-6" />
              </div>
              <div className="space-y-1">
                <h2 className="text-lg font-bold text-slate-900">{t('pasteUrl')}</h2>
                <p className="text-slate-600 text-xs leading-relaxed">
                  {t('pasteUrlDesc')}
                </p>
              </div>
            </div>
            <Link
              to="/forms/new"
              className="bg-indigo-50 hover:bg-indigo-100 text-indigo-800 font-bold px-4 py-2.5 rounded-xl text-xs text-center border border-indigo-200 transition-colors flex items-center justify-center gap-1.5"
            >
              <span>{t('pasteUrl')}</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>

        {/* Recent & Active Forms List */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-6">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <h2 className="text-xl font-extrabold text-slate-900 flex items-center gap-2">
              <Clock className="w-5 h-5 text-teal-600" /> {t('activeForms')}
            </h2>
            <span className="text-xs text-slate-500 font-medium">
              {sessions.length} {sessions.length === 1 ? 'form' : 'forms'} total
            </span>
          </div>

          {isLoading ? (
            <LoadingSpinner label="Loading active forms..." />
          ) : sessions.length === 0 ? (
            <div className="text-center py-12 space-y-3">
              <div className="w-12 h-12 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto">
                <FileText className="w-6 h-6" />
              </div>
              <h3 className="text-slate-800 font-bold text-base">{t('noFormsYet')}</h3>
              <p className="text-slate-500 text-xs max-w-sm mx-auto">
                Upload a document or paste a web form link above to begin your first guided form session.
              </p>
              <Link
                to="/forms/new"
                className="inline-flex items-center gap-1.5 bg-teal-600 hover:bg-teal-700 text-white font-bold text-xs px-4 py-2 rounded-lg transition-colors mt-2"
              >
                <PlusCircle className="w-4 h-4" /> {t('uploadForm')}
              </Link>
            </div>
          ) : (
            <div className="grid gap-4">
              {sessions.map((sess) => {
                const current = sess.currentFieldIndex || 0;
                const total = sess.totalFields || 1;
                const progressPct = Math.round((current / total) * 100);

                return (
                  <div
                    key={sess.id}
                    className="p-5 rounded-xl border border-slate-200 hover:border-teal-300 bg-white transition-all flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-2xs"
                  >
                    <div className="space-y-2 flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <h3 className="font-bold text-slate-900 text-base">{sess.formTitle || 'Untitled Form'}</h3>
                        {getStatusBadge(sess.sessionStatus)}
                      </div>

                      <div className="flex items-center gap-4 text-xs text-slate-500">
                        <span>Type: {sess.formSourceType || 'DOCUMENT'}</span>
                        <span>•</span>
                        <span>Progress: {current} / {total} fields</span>
                        {sess.createdAt && (
                          <>
                            <span>•</span>
                            <span>Created: {new Date(sess.createdAt).toLocaleDateString()}</span>
                          </>
                        )}
                      </div>

                      {/* Mini progress bar */}
                      <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden max-w-xs">
                        <div
                          className="bg-teal-600 h-full rounded-full"
                          style={{ width: `${progressPct}%` }}
                        />
                      </div>
                    </div>

                    <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
                      {sess.sessionStatus === 'COMPLETED' ? (
                        <Link
                          to={`/forms/${sess.id}/review`}
                          className="w-full sm:w-auto bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold px-4 py-2 rounded-lg text-xs transition-colors text-center"
                        >
                          {t('viewAnswers')}
                        </Link>
                      ) : (
                        <Link
                          to={`/forms/${sess.id}`}
                          className="w-full sm:w-auto bg-teal-600 hover:bg-teal-700 text-white font-bold px-4 py-2 rounded-lg text-xs transition-colors text-center flex items-center justify-center gap-1.5 shadow-2xs"
                        >
                          <Play className="w-3.5 h-3.5 fill-current" /> {t('continueForm')}
                        </Link>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};
