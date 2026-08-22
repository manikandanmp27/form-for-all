import React, { useState, useEffect, useRef } from 'react';
import {
  MessageSquare,
  Bot,
  User,
  Send,
  X,
  Minimize2,
  Trash2,
  Sparkles,
  RefreshCw,
  HelpCircle,
  ShieldAlert,
  FileText,
  Sliders,
} from 'lucide-react';
import { chatApi } from '../../api/chatApi';

const QUICK_PROMPTS = [
  {
    icon: FileText,
    label: 'How do I upload a form?',
    text: 'How do I upload a form PDF or web URL?',
  },
  {
    icon: ShieldAlert,
    label: 'What are Risk Alerts?',
    text: 'What are Risk & Security Alerts in Form-for-All?',
  },
  {
    icon: Sliders,
    label: 'Accessibility Settings',
    text: 'How do I adjust my Accessibility Profile and Cognitive Load settings?',
  },
];

export const ChatbotWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 'welcome-1',
      role: 'assistant',
      content:
        "Hello! I'm your **Form-for-All AI Assistant**. How can I help you upload, understand, or fill out forms today?",
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [unreadCount, setUnreadCount] = useState(0);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Auto-scroll to bottom of conversation
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
      setUnreadCount(0);
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen, messages]);

  const handleSendMessage = async (customMessage = null) => {
    const textToSend = customMessage || inputMessage.trim();
    if (!textToSend || isLoading) return;

    setErrorMessage(null);
    setInputMessage('');

    const userMsgObj = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: textToSend,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    const updatedMessages = [...messages, userMsgObj];
    setMessages(updatedMessages);
    setIsLoading(true);

    try {
      // Prepare history for API (exclude system/welcome IDs and strip unnecessary fields)
      const historyForApi = updatedMessages.slice(-8).map((m) => ({
        role: m.role,
        content: m.content,
      }));

      const res = await chatApi.sendMessage(textToSend, historyForApi);

      const aiReplyObj = {
        id: `ai-${Date.now()}`,
        role: 'assistant',
        content: res.reply || 'I processed your request, but received an empty response.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        provider: res.provider,
      };

      setMessages((prev) => [...prev, aiReplyObj]);
      if (!isOpen) {
        setUnreadCount((count) => count + 1);
      }
    } catch (err) {
      console.error('Chatbot API error:', err);
      setErrorMessage(
        err.message || 'Unable to connect to the assistant right now. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleClearHistory = () => {
    setMessages([
      {
        id: 'welcome-reset',
        role: 'assistant',
        content:
          "Conversation history cleared. How else can I assist you with your forms?",
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
    setErrorMessage(null);
  };

  return (
    <div className="fixed bottom-6 right-6 z-50 font-sans">
      {/* Floating Toggle Launcher Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="group relative flex items-center justify-center w-14 h-14 rounded-full bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-700 hover:to-emerald-700 text-white shadow-xl hover:shadow-2xl transition-all duration-300 transform hover:scale-105 focus:outline-none focus:ring-4 focus:ring-teal-400"
          aria-label="Open Form-for-All AI Assistant"
        >
          <Sparkles className="w-6 h-6 animate-pulse" />
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white shadow-md">
              {unreadCount}
            </span>
          )}
          <span className="absolute right-16 bg-slate-900 text-white text-xs font-semibold px-3 py-1.5 rounded-lg shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 whitespace-nowrap pointer-events-none">
            Ask Form-for-All AI 💬
          </span>
        </button>
      )}

      {/* Main Chat Drawer Window */}
      {isOpen && (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Form-for-All Chatbot Interface"
          className="w-[360px] sm:w-[410px] h-[560px] max-h-[85vh] bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl flex flex-col overflow-hidden transition-all duration-300 animate-in fade-in slide-in-from-bottom-5"
        >
          {/* Header */}
          <div className="bg-gradient-to-r from-teal-700 via-teal-800 to-slate-900 px-4 py-3.5 text-white flex items-center justify-between shadow-md">
            <div className="flex items-center gap-3">
              <div className="relative p-2 bg-teal-600/50 rounded-xl border border-teal-400/30">
                <Bot className="w-5 h-5 text-teal-200" />
                <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-emerald-400 border-2 border-teal-800 rounded-full"></span>
              </div>
              <div>
                <h3 className="font-semibold text-sm leading-tight flex items-center gap-1.5">
                  Form-for-All Assistant
                  <span className="text-[10px] font-medium bg-teal-500/30 text-teal-200 px-1.5 py-0.5 rounded border border-teal-400/30">
                    AI Powered
                  </span>
                </h3>
                <p className="text-[11px] text-teal-100/80 font-normal">
                  Ask about forms, risk alerts & features
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1 text-teal-100">
              <button
                onClick={handleClearHistory}
                title="Clear Conversation"
                className="p-1.5 hover:bg-white/10 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-teal-300"
              >
                <Trash2 className="w-4 h-4" />
              </button>
              <button
                onClick={() => setIsOpen(false)}
                title="Minimize Chat"
                className="p-1.5 hover:bg-white/10 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-teal-300"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* Chat Messages Stream */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/50 dark:bg-slate-950/40">
            {messages.map((msg) => {
              const isAssistant = msg.role === 'assistant';
              return (
                <div
                  key={msg.id}
                  className={`flex gap-2.5 ${
                    isAssistant ? 'justify-start' : 'justify-end'
                  }`}
                >
                  {isAssistant && (
                    <div className="w-7 h-7 rounded-lg bg-teal-600 text-white flex items-center justify-center flex-shrink-0 mt-0.5 shadow-sm">
                      <Bot className="w-4 h-4" />
                    </div>
                  )}

                  <div
                    className={`flex flex-col ${
                      isAssistant ? 'items-start' : 'items-end'
                    } max-w-[85%]`}
                  >
                    <div
                      className={`px-3.5 py-2.5 rounded-2xl text-xs leading-relaxed shadow-sm ${
                        isAssistant
                          ? 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 border border-slate-200/80 dark:border-slate-700/80 rounded-tl-xs'
                          : 'bg-teal-600 text-white font-normal rounded-tr-xs'
                      }`}
                    >
                      {msg.content}
                    </div>

                    <span className="text-[10px] text-slate-400 mt-1 px-1">
                      {msg.timestamp}
                    </span>
                  </div>

                  {!isAssistant && (
                    <div className="w-7 h-7 rounded-lg bg-slate-700 text-white flex items-center justify-center flex-shrink-0 mt-0.5 shadow-sm">
                      <User className="w-4 h-4" />
                    </div>
                  )}
                </div>
              );
            })}

            {/* Quick Suggestion Prompt Chips */}
            {messages.length <= 2 && !isLoading && (
              <div className="mt-4 pt-2 border-t border-slate-200/60 dark:border-slate-800">
                <p className="text-[11px] font-medium text-slate-500 dark:text-slate-400 mb-2 flex items-center gap-1">
                  <HelpCircle className="w-3.5 h-3.5 text-teal-600" />
                  Suggested Questions:
                </p>
                <div className="flex flex-col gap-1.5">
                  {QUICK_PROMPTS.map((prompt, idx) => {
                    const IconComp = prompt.icon;
                    return (
                      <button
                        key={idx}
                        onClick={() => handleSendMessage(prompt.text)}
                        className="text-left text-xs bg-white dark:bg-slate-800 hover:bg-teal-50 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 px-3 py-2 rounded-xl flex items-center gap-2 transition-colors duration-150 shadow-xs"
                      >
                        <IconComp className="w-3.5 h-3.5 text-teal-600 dark:text-teal-400 flex-shrink-0" />
                        <span className="truncate">{prompt.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Typing Indicator */}
            {isLoading && (
              <div className="flex gap-2.5 items-center">
                <div className="w-7 h-7 rounded-lg bg-teal-600 text-white flex items-center justify-center flex-shrink-0 shadow-sm">
                  <Bot className="w-4 h-4" />
                </div>
                <div className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-4 py-3 rounded-2xl rounded-tl-xs flex items-center gap-1.5 shadow-sm">
                  <span className="w-1.5 h-1.5 bg-teal-500 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-1.5 h-1.5 bg-teal-500 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-1.5 h-1.5 bg-teal-500 rounded-full animate-bounce"></span>
                </div>
              </div>
            )}

            {/* Error Message Box */}
            {errorMessage && (
              <div className="bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 p-3 rounded-xl text-xs flex items-start gap-2">
                <div className="flex-1">{errorMessage}</div>
                <button
                  onClick={() => handleSendMessage()}
                  className="text-xs font-semibold text-rose-700 underline hover:text-rose-900 flex items-center gap-1"
                >
                  <RefreshCw className="w-3 h-3" /> Retry
                </button>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Footer Text Input Bar */}
          <div className="p-3 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSendMessage();
              }}
              className="flex items-center gap-2"
            >
              <input
                ref={inputRef}
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask a question about Form-for-All..."
                disabled={isLoading}
                className="flex-1 bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-100 placeholder-slate-400 text-xs px-3.5 py-2.5 rounded-xl border border-transparent focus:border-teal-500 focus:bg-white dark:focus:bg-slate-800 focus:outline-none transition-colors"
              />
              <button
                type="submit"
                disabled={!inputMessage.trim() || isLoading}
                className="p-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 disabled:opacity-40 text-white transition-colors focus:outline-none focus:ring-2 focus:ring-teal-400 shadow-sm"
                aria-label="Send message"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
