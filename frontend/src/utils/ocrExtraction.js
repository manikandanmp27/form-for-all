import { createWorker } from 'tesseract.js';

/**
 * Common field aliases mapped to normalized field keys
 */
const FIELD_ALIASES = [
  {
    key: 'applicantFullName',
    label: 'Full Name',
    aliases: ['name', 'full name', 'applicant name', 'candidate name', 'holder name', 'person name'],
    pattern: /(?:name|full name|applicant name|candidate name|holder name)[\s:-]+([A-Za-z\s.]{3,40})/i,
  },
  {
    key: 'dateOfBirth',
    label: 'Date of Birth',
    aliases: ['dob', 'date of birth', 'birth date', 'born'],
    pattern: /(?:dob|date of birth|birth date|born)[\s:-]+(\d{2}[/-]\d{2}[/-]\d{4}|\d{4}[/-]\d{2}[/-]\d{2})/i,
  },
  {
    key: 'gender',
    label: 'Gender',
    aliases: ['gender', 'sex'],
    pattern: /(?:gender|sex)[\s:-]+(male|female|other|m|f)/i,
  },
  {
    key: 'contactPhone',
    label: 'Phone Number',
    aliases: ['mobile', 'phone', 'contact', 'telephone', 'mobile no', 'phone no'],
    pattern: /(?:mobile|phone|contact|tel)[\s:-]+(\+?\d{1,3}[\s-]?)?([6-9]\d{9})/i,
  },
  {
    key: 'aadhaarNumber',
    label: 'Aadhaar / ID Number',
    aliases: ['aadhaar', 'aadhaar no', 'uid', 'id number', 'id no', 'enrollment no'],
    pattern: /(?:aadhaar|uid|id no|id number)[\s:-]*(\d{4}\s?\d{4}\s?\d{4}|\d{12})/i,
  },
  {
    key: 'permanentAddress',
    label: 'Address',
    aliases: ['address', 'residence', 'residential address', 'permanent address'],
    pattern: /(?:address|residence|residential address)[\s:-]+([A-Za-z0-9\s,.-]{10,120})/i,
  },
];

/**
 * Perform client-side OCR using Tesseract.js
 * @param {File|Blob|HTMLCanvasElement|string} imageSource
 * @param {Function} onProgress (statusText, percentage)
 * @returns {Promise<{ rawText: string, extractedFields: Array, overallConfidence: number }>}
 */
export async function performClientSideOCR(imageSource, onProgress = () => {}) {
  let worker = null;
  try {
    onProgress('Initializing OCR engine...', 10);

    worker = await createWorker('eng');

    onProgress('Processing image and reading text...', 40);

    const ret = await worker.recognize(imageSource);
    const rawText = ret.data.text || '';
    const overallConfidence = Math.round(ret.data.confidence || 0);

    onProgress('Extracting & matching form fields...', 80);

    const extractedFields = parseOCRTextToFields(rawText, overallConfidence);

    onProgress('Completed', 100);

    return {
      rawText,
      overallConfidence,
      extractedFields,
    };
  } catch (err) {
    console.error('Tesseract OCR Processing Error:', err);
    throw new Error(err.message || 'Unable to read document. Please ensure image is clear.');
  } finally {
    if (worker) {
      await worker.terminate();
    }
  }
}

/**
 * Parse raw text using patterns & fallback heuristics
 */
function parseOCRTextToFields(text, baseConfidence) {
  const lines = text.split('\n').map((l) => l.trim()).filter(Boolean);
  const results = [];
  const foundKeys = new Set();

  // Strategy 1: Explicit pattern matches
  for (const def of FIELD_ALIASES) {
    const match = text.match(def.pattern);
    if (match && match[1]) {
      let val = match[1].trim();
      if (def.key === 'gender') {
        const lower = val.toLowerCase();
        if (lower.startsWith('m')) val = 'Male';
        else if (lower.startsWith('f')) val = 'Female';
        else val = 'Other';
      }
      results.push({
        fieldKey: def.key,
        label: def.label,
        value: val,
        confidence: baseConfidence >= 75 ? 'HIGH' : 'LOW',
        isAutoFilled: true,
      });
      foundKeys.add(def.key);
    }
  }

  // Strategy 2: Standard regex patterns for DOB / Aadhaar / Phone if not matched by label
  if (!foundKeys.has('dateOfBirth')) {
    const dobMatch = text.match(/\b(\d{2}[/-]\d{2}[/-]\d{4}|\d{4}[/-]\d{2}[/-]\d{2})\b/);
    if (dobMatch) {
      results.push({
        fieldKey: 'dateOfBirth',
        label: 'Date of Birth',
        value: dobMatch[1],
        confidence: 'LOW',
        isAutoFilled: true,
      });
      foundKeys.add('dateOfBirth');
    }
  }

  if (!foundKeys.has('aadhaarNumber')) {
    const aadhaarMatch = text.match(/\b(\d{4}\s\d{4}\s\d{4})\b/);
    if (aadhaarMatch) {
      results.push({
        fieldKey: 'aadhaarNumber',
        label: 'Aadhaar / ID Number',
        value: aadhaarMatch[1],
        confidence: baseConfidence >= 80 ? 'HIGH' : 'LOW',
        isAutoFilled: true,
      });
      foundKeys.add('aadhaarNumber');
    }
  }

  if (!foundKeys.has('contactPhone')) {
    const phoneMatch = text.match(/\b([6-9]\d{9})\b/);
    if (phoneMatch) {
      results.push({
        fieldKey: 'contactPhone',
        label: 'Phone Number',
        value: phoneMatch[1],
        confidence: 'LOW',
        isAutoFilled: true,
      });
      foundKeys.add('contactPhone');
    }
  }

  // Strategy 3: Heuristic name detection if no name label found
  if (!foundKeys.has('applicantFullName') && lines.length > 0) {
    const possibleName = lines.find(
      (l) => /^[A-Z\s.]{3,35}$/.test(l) && !/GOVERNMENT|INDIA|CARD|NUMBER|MALE|FEMALE|DATE|BIRTH|ADDRESS/i.test(l)
    );
    if (possibleName) {
      results.push({
        fieldKey: 'applicantFullName',
        label: 'Full Name',
        value: possibleName,
        confidence: 'LOW',
        isAutoFilled: true,
      });
    }
  }

  return results;
}

/**
 * Match extracted OCR fields with the active form's field definitions
 */
export function mapExtractedToFormFields(extractedFields, currentFormFields) {
  if (!extractedFields || extractedFields.length === 0) return [];
  if (!currentFormFields || currentFormFields.length === 0) return [];

  const matched = [];

  for (const field of currentFormFields) {
    const key = (field.fieldKey || '').toLowerCase();
    const label = (field.label || '').toLowerCase();

    const ocrMatch = extractedFields.find((ef) => {
      const efKey = ef.fieldKey.toLowerCase();
      const efLabel = ef.label.toLowerCase();
      return (
        key === efKey ||
        label.includes(efLabel) ||
        efLabel.includes(label) ||
        FIELD_ALIASES.find((fa) => fa.key === efKey)?.aliases.some((alias) => key.includes(alias) || label.includes(alias))
      );
    });

    if (ocrMatch) {
      matched.push({
        formFieldId: field.id,
        fieldKey: field.fieldKey,
        label: field.label,
        extractedValue: ocrMatch.value,
        confidence: ocrMatch.confidence,
        isAutoFilled: true,
      });
    }
  }

  return matched;
}
