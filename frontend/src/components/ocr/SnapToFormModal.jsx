import React, { useState, useRef, useEffect } from 'react';
import {
  Camera,
  Upload,
  X,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Edit3,
  Check,
  FileText,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { performClientSideOCR, mapExtractedToFormFields } from '../../utils/ocrExtraction';

import { formsApi } from '../../api/formsApi';

export const SnapToFormModal = ({ isOpen, onClose, formFields = [], onApplyAutoFill }) => {
  const [activeTab, setActiveTab] = useState('upload'); // 'upload' | 'camera'
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState(null);

  // Camera state
  const videoRef = useRef(null);
  const [isCameraActive, setIsCameraActive] = useState(false);
  const [cameraError, setCameraError] = useState('');

  // OCR state
  const [isProcessing, setIsProcessing] = useState(false);
  const [progressStatus, setProgressStatus] = useState('');
  const [progressPercentage, setProgressPercentage] = useState(0);
  const [ocrError, setOcrError] = useState('');
  const [extractedFields, setExtractedFields] = useState([]);

  // Cleanup camera stream on unmount or tab switch
  useEffect(() => {
    if (activeTab !== 'camera') {
      stopCamera();
    }
    return () => {
      stopCamera();
    };
  }, [activeTab]);

  const startCamera = async () => {
    setCameraError('');
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
      });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        setIsCameraActive(true);
      }
    } catch (err) {
      console.warn('Camera access error:', err);
      setCameraError('Unable to access camera. Please allow camera permissions or upload an image file.');
    }
  };

  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      const stream = videoRef.current.srcObject;
      stream.getTracks().forEach((track) => track.stop());
      videoRef.current.srcObject = null;
    }
    setIsCameraActive(false);
  };

  const capturePhoto = () => {
    if (!videoRef.current) return;
    const canvas = document.createElement('canvas');
    canvas.width = videoRef.current.videoWidth || 1280;
    canvas.height = videoRef.current.videoHeight || 720;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(videoRef.current, 0, 0, canvas.width, canvas.height);

    canvas.toBlob((blob) => {
      if (blob) {
        const file = new File([blob], 'camera-capture.jpg', { type: 'image/jpeg' });
        setSelectedImage(file);
        setImagePreviewUrl(URL.createObjectURL(blob));
        stopCamera();
      }
    }, 'image/jpeg');
  };

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      setOcrError('Please select a valid image file (JPG, PNG, or WebP).');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      setOcrError('Image size exceeds 10MB limit. Please upload a smaller image.');
      return;
    }

    setSelectedImage(file);
    setImagePreviewUrl(URL.createObjectURL(file));
    setOcrError('');
    setExtractedFields([]);
  };

  const handleRunOCR = async () => {
    if (!selectedImage) return;

    setIsProcessing(true);
    setOcrError('');
    setProgressPercentage(10);
    setProgressStatus('Scanning document with Gemini Vision AI...');

    try {
      let mapped = [];
      try {
        const aiFields = await formsApi.extractValuesFromDocument(selectedImage);
        if (aiFields && aiFields.length > 0) {
          setProgressPercentage(80);
          setProgressStatus('Matching AI extracted fields to form...');
          mapped = mapExtractedToFormFields(
            aiFields.map((f) => ({
              fieldKey: f.fieldKey,
              label: f.label,
              value: f.extractedValue || f.value || '',
              confidence: 'HIGH',
            })),
            formFields
          );

          if (mapped.length === 0) {
            mapped = aiFields
              .map((f, idx) => ({
                formFieldId: formFields[idx]?.id || `custom_${idx}`,
                fieldKey: formFields[idx]?.fieldKey || f.fieldKey,
                label: formFields[idx]?.label || f.label || `Extracted ${idx + 1}`,
                extractedValue: f.extractedValue || f.value || '',
                confidence: 'HIGH',
                isAutoFilled: true,
              }))
              .filter((item) => item.extractedValue);
          }
        }
      } catch (aiErr) {
        console.warn('Backend Vision AI field extraction fallback to client Tesseract:', aiErr);
      }

      if (mapped.length === 0) {
        setProgressPercentage(40);
        setProgressStatus('Running fallback client Tesseract OCR...');
        const result = await performClientSideOCR(selectedImage, (statusText, percent) => {
          setProgressStatus(statusText);
          setProgressPercentage(percent);
        });

        mapped = mapExtractedToFormFields(result.extractedFields, formFields);

        if (mapped.length === 0 && result.extractedFields?.length > 0) {
          mapped = result.extractedFields.map((ef, idx) => ({
            formFieldId: formFields[idx]?.id || `custom_${idx}`,
            fieldKey: formFields[idx]?.fieldKey || ef.fieldKey,
            label: formFields[idx]?.label || ef.label || `Extracted Text ${idx + 1}`,
            extractedValue: ef.value,
            confidence: 'LOW',
            isAutoFilled: true,
          }));
        }
      }

      if (mapped.length === 0) {
        setOcrError("We couldn't read enough matching information from this document. Try uploading a clearer image or fill fields manually.");
        setExtractedFields([]);
      } else {
        setExtractedFields(mapped);
      }
    } catch (err) {
      setOcrError(err.message || "We couldn't read information from this document. Please try a clearer image.");
    } finally {
      setIsProcessing(false);
    }
  };

  const handleFieldValueChange = (index, newValue) => {
    setExtractedFields((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], extractedValue: newValue };
      return updated;
    });
  };

  const handleApply = () => {
    if (extractedFields.length > 0) {
      onApplyAutoFill(extractedFields);
      handleReset();
      onClose();
    }
  };

  const handleReset = () => {
    stopCamera();
    setSelectedImage(null);
    setImagePreviewUrl(null);
    setExtractedFields([]);
    setOcrError('');
    setIsProcessing(false);
    setProgressPercentage(0);
    setProgressStatus('');
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in">
      <div className="bg-white rounded-3xl border border-slate-200 shadow-2xl max-w-2xl w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-100 flex items-center justify-between bg-teal-600 text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-teal-500/30 rounded-xl">
              <Zap className="w-5 h-5 text-amber-300" />
            </div>
            <div>
              <h2 className="font-extrabold text-lg tracking-tight">Auto-Fill from Document</h2>
              <p className="text-xs text-teal-100 flex items-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5 text-teal-200" /> 100% Private Client-Side OCR (Tesseract.js)
              </p>
            </div>
          </div>
          <button
            onClick={() => {
              handleReset();
              onClose();
            }}
            className="p-2 rounded-xl text-teal-100 hover:text-white hover:bg-teal-700/50 transition-colors cursor-pointer"
            aria-label="Close auto-fill modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1">
          {/* Tab Selector if no image selected */}
          {!imagePreviewUrl && (
            <div className="flex bg-slate-100 p-1.5 rounded-2xl gap-2">
              <button
                type="button"
                onClick={() => setActiveTab('upload')}
                className={`flex-1 py-2.5 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all ${
                  activeTab === 'upload' ? 'bg-white text-teal-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <Upload className="w-4 h-4" /> Upload Document Image
              </button>
              <button
                type="button"
                onClick={() => {
                  setActiveTab('camera');
                  startCamera();
                }}
                className={`flex-1 py-2.5 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all ${
                  activeTab === 'camera' ? 'bg-white text-teal-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <Camera className="w-4 h-4" /> Capture with Camera
              </button>
            </div>
          )}

          {/* Camera View */}
          {activeTab === 'camera' && !imagePreviewUrl && (
            <div className="space-y-4">
              <div className="relative bg-slate-950 rounded-2xl overflow-hidden aspect-video flex items-center justify-center border border-slate-800">
                <video ref={videoRef} autoPlay playsInline className="w-full h-full object-cover" />
                {!isCameraActive && !cameraError && (
                  <p className="text-xs text-slate-400">Starting camera preview...</p>
                )}
                {cameraError && (
                  <div className="p-4 text-center space-y-2">
                    <AlertTriangle className="w-8 h-8 text-amber-400 mx-auto" />
                    <p className="text-xs text-slate-200 font-medium max-w-xs mx-auto">{cameraError}</p>
                  </div>
                )}
              </div>

              {isCameraActive && (
                <button
                  type="button"
                  onClick={capturePhoto}
                  className="w-full bg-teal-600 hover:bg-teal-700 text-white font-extrabold py-3 rounded-xl flex items-center justify-center gap-2 text-sm shadow-xs transition-colors cursor-pointer"
                >
                  <Camera className="w-4 h-4" /> Snap Document Photo
                </button>
              )}
            </div>
          )}

          {/* Upload Dropzone */}
          {activeTab === 'upload' && !imagePreviewUrl && (
            <label className="border-2 border-dashed border-slate-300 hover:border-teal-500 rounded-2xl p-8 flex flex-col items-center justify-center text-center cursor-pointer transition-colors bg-slate-50/50 hover:bg-teal-50/30 group">
              <div className="p-4 bg-teal-100 text-teal-700 rounded-2xl group-hover:scale-105 transition-transform">
                <Upload className="w-8 h-8" />
              </div>
              <h3 className="font-bold text-slate-900 text-base mt-3">Select Document Image</h3>
              <p className="text-xs text-slate-500 max-w-xs mt-1">
                Upload Aadhaar, ID card, mark sheet, or passport (JPG, PNG, WebP up to 10MB)
              </p>
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={handleFileSelect}
                className="hidden"
              />
            </label>
          )}

          {/* Image Preview & OCR Action */}
          {imagePreviewUrl && (
            <div className="space-y-4 border border-slate-200 p-4 rounded-2xl bg-slate-50">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                  <FileText className="w-4 h-4 text-teal-600" /> Selected Document
                </span>
                <button
                  type="button"
                  onClick={handleReset}
                  className="text-xs text-rose-600 hover:text-rose-700 font-bold flex items-center gap-1"
                >
                  <RefreshCw className="w-3.5 h-3.5" /> Choose Different Image
                </button>
              </div>

              <div className="max-h-48 overflow-hidden rounded-xl border border-slate-200 bg-slate-950 flex items-center justify-center">
                <img src={imagePreviewUrl} alt="Document Preview" className="max-h-48 object-contain" />
              </div>

              {!isProcessing && extractedFields.length === 0 && (
                <button
                  type="button"
                  onClick={handleRunOCR}
                  className="w-full bg-teal-600 hover:bg-teal-700 text-white font-extrabold py-3 rounded-xl flex items-center justify-center gap-2 text-sm shadow-xs transition-colors cursor-pointer"
                >
                  <Sparkles className="w-4 h-4 text-amber-300" /> Run Client OCR & Extract Fields
                </button>
              )}
            </div>
          )}

          {/* OCR Progress Bar */}
          {isProcessing && (
            <div className="space-y-2 p-4 bg-teal-50 border border-teal-200 rounded-2xl">
              <div className="flex items-center justify-between text-xs font-bold text-teal-950">
                <span className="flex items-center gap-1.5">
                  <RefreshCw className="w-3.5 h-3.5 animate-spin text-teal-700" /> {progressStatus}
                </span>
                <span>{progressPercentage}%</span>
              </div>
              <div className="w-full bg-teal-200 h-2 rounded-full overflow-hidden">
                <div
                  className="bg-teal-600 h-full transition-all duration-300 rounded-full"
                  style={{ width: `${progressPercentage}%` }}
                />
              </div>
            </div>
          )}

          {/* OCR Error Alert */}
          {ocrError && (
            <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex items-start gap-3 text-xs text-amber-900">
              <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
              <p className="font-medium leading-relaxed">{ocrError}</p>
            </div>
          )}

          {/* Extracted Fields Review List */}
          {extractedFields.length > 0 && (
            <div className="space-y-4 pt-2">
              <div className="flex items-center justify-between border-b border-slate-200 pb-3">
                <h3 className="font-extrabold text-slate-900 text-sm flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-teal-600" /> Review Extracted Information
                </h3>
                <span className="text-xs font-semibold text-slate-500">
                  {extractedFields.length} field{extractedFields.length > 1 ? 's' : ''} matched
                </span>
              </div>

              <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
                {extractedFields.map((ef, idx) => (
                  <div key={ef.formFieldId || idx} className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 space-y-1.5">
                    <div className="flex items-center justify-between text-xs">
                      <label className="font-bold text-slate-800">{ef.label}</label>
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          ef.confidence === 'HIGH'
                            ? 'bg-teal-100 text-teal-800 border border-teal-200'
                            : 'bg-amber-100 text-amber-800 border border-amber-200'
                        }`}
                      >
                        {ef.confidence === 'HIGH' ? 'High Confidence' : 'Review Needed'}
                      </span>
                    </div>

                    <input
                      type="text"
                      value={ef.extractedValue}
                      onChange={(e) => handleFieldValueChange(idx, e.target.value)}
                      className="w-full text-xs font-semibold p-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-teal-600 bg-white text-slate-900"
                    />
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={() => {
              handleReset();
              onClose();
            }}
            className="px-5 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-bold text-xs hover:bg-slate-100 transition-colors cursor-pointer"
          >
            Cancel
          </button>

          {extractedFields.length > 0 && (
            <button
              type="button"
              onClick={handleApply}
              className="px-6 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 text-white font-extrabold text-xs shadow-xs transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <Check className="w-4 h-4" /> Apply to Form
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
