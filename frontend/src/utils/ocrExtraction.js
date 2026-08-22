import { createWorker } from 'tesseract.js';

/**
 * Common field aliases mapped to normalized field keys
 */
const FIELD_ALIASES = [
  {
    key: 'applicantFullName',
    label: 'Full Name',
    aliases: ['name', 'full name', 'applicant name', 'candidate name', 'holder name', 'person name', 'first name', 'last name'],
    pattern: /(?:name|full name|applicant name|candidate name|holder name|first name)[\s:-]+([A-Za-z\s.]{3,40})/i,
  },
  {
    key: 'fatherName',
    label: "Father's Name",
    aliases: ["father name", "father's name", "father", "s/o", "d/o", "w/o", "guardian", "parent name"],
    pattern: /(?:father'?s?\s*name|father|s\/o|d\/o|w\/o|guardian)[\s:-]+([A-Za-z\s.]{3,40})/i,
  },
  {
    key: 'motherName',
    label: "Mother's Name",
    aliases: ["mother name", "mother's name", "mother"],
    pattern: /(?:mother'?s?\s*name|mother)[\s:-]+([A-Za-z\s.]{3,40})/i,
  },
  {
    key: 'dateOfBirth',
    label: 'Date of Birth',
    aliases: ['dob', 'date of birth', 'birth date', 'born', 'birth'],
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
    aliases: ['mobile', 'phone', 'contact', 'telephone', 'mobile no', 'phone no', 'cell'],
    pattern: /(?:mobile|phone|contact|tel|cell)[\s:-]+(\+?\d{1,3}[\s-]?)?([6-9]\d{9})/i,
  },
  {
    key: 'emailAddress',
    label: 'Email Address',
    aliases: ['email', 'e-mail', 'email address', 'mail id', 'mail'],
    pattern: /(?:email|e-mail|mail)[\s:-]+([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/i,
  },
  {
    key: 'aadhaarNumber',
    label: 'Aadhaar / ID Number',
    aliases: ['aadhaar', 'aadhaar no', 'uid', 'id number', 'id no', 'enrollment no'],
    pattern: /(?:aadhaar|uid|id no|id number)[\s:-]*(\d{4}\s?\d{4}\s?\d{4}|\d{12})/i,
  },
  {
    key: 'panNumber',
    label: 'PAN Card Number',
    aliases: ['pan', 'pan no', 'pan number', 'permanent account number'],
    pattern: /(?:pan|pan no|pan number)[\s:-]*([A-Z]{5}\d{4}[A-Z]{1})/i,
  },
  {
    key: 'passportNumber',
    label: 'Passport Number',
    aliases: ['passport', 'passport no', 'passport number'],
    pattern: /(?:passport|passport no)[\s:-]*([A-Z]{1}\d{7})/i,
  },
  {
    key: 'voterId',
    label: 'Voter ID Number',
    aliases: ['voter id', 'voter no', 'epic no', 'card no'],
    pattern: /(?:voter id|voter no|epic no)[\s:-]*([A-Z]{3}\d{7})/i,
  },
  {
    key: 'bankAccount',
    label: 'Bank Account Number',
    aliases: ['account no', 'acc no', 'bank account', 'a/c no', 'account number'],
    pattern: /(?:account no|acc no|bank account|a\/c no)[\s:-]*(\d{9,18})/i,
  },
  {
    key: 'ifscCode',
    label: 'IFSC Code',
    aliases: ['ifsc', 'ifsc code', 'bank ifsc'],
    pattern: /(?:ifsc|ifsc code)[\s:-]*([A-Z]{4}0[A-Z0-9]{6})/i,
  },
  {
    key: 'pincode',
    label: 'Pin Code',
    aliases: ['pincode', 'pin code', 'zip code', 'postal code', 'pin'],
    pattern: /(?:pincode|pin code|zip code|pin)[\s:-]*(\d{6})/i,
  },
  {
    key: 'permanentAddress',
    label: 'Address',
    aliases: ['address', 'residence', 'residential address', 'permanent address', 'location'],
    pattern: /(?:address|residence|residential address|location)[\s:-]+([A-Za-z0-9\s,.-]{10,120})/i,
  },
  {
    key: 'state',
    label: 'State',
    aliases: ['state', 'province'],
    pattern: /(?:state|province)[\s:-]+([A-Za-z\s]{3,25})/i,
  },
  {
    key: 'city',
    label: 'City / District',
    aliases: ['city', 'town', 'district'],
    pattern: /(?:city|town|district)[\s:-]+([A-Za-z\s]{3,25})/i,
  },
  {
    key: 'occupation',
    label: 'Occupation',
    aliases: ['occupation', 'profession', 'designation', 'job', 'work'],
    pattern: /(?:occupation|profession|designation|job)[\s:-]+([A-Za-z\s]{3,30})/i,
  },
  {
    key: 'income',
    label: 'Annual Income',
    aliases: ['income', 'annual income', 'salary'],
    pattern: /(?:income|annual income|salary)[\s:-]+([\d,]{4,10})/i,
  },
  {
    key: 'maritalStatus',
    label: 'Marital Status',
    aliases: ['marital status', 'married', 'single', 'unmarried'],
    pattern: /(?:marital status)[\s:-]+(married|single|unmarried|divorced|widowed)/i,
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
      (l) => /^[A-Z\s.]{3,35}$/i.test(l) && !/GOVERNMENT|INDIA|CARD|NUMBER|MALE|FEMALE|DATE|BIRTH|ADDRESS/i.test(l)
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

  // Strategy 4: Fallback line & Key-Value pair extraction for any unclassified text
  lines.forEach((line, idx) => {
    const kvMatch = line.match(/^([^:-]{2,20})[\s:-]+(.{2,80})$/);
    if (kvMatch) {
      results.push({
        fieldKey: 'kv_' + idx,
        label: kvMatch[1].trim(),
        value: kvMatch[2].trim(),
        confidence: 'LOW',
        isAutoFilled: true,
      });
    } else {
      results.push({
        fieldKey: 'line_' + idx,
        label: 'Text Line ' + (idx + 1),
        value: line,
        confidence: 'LOW',
        isAutoFilled: true,
      });
    }
  });

  return results;
}

/**
 * Match extracted OCR fields with the active form's field definitions
 */
export function mapExtractedToFormFields(extractedFields, currentFormFields) {
  if (!currentFormFields || currentFormFields.length === 0) return [];
  if (!extractedFields || extractedFields.length === 0) return [];

  const matched = [];
  const usedExtractedIndices = new Set();

  // Step 1: Explicit key & alias matching
  currentFormFields.forEach((field) => {
    const key = (field.fieldKey || '').toLowerCase();
    const label = (field.label || '').toLowerCase();

    const ocrIdx = extractedFields.findIndex((ef, eIdx) => {
      if (usedExtractedIndices.has(eIdx)) return false;
      const efKey = (ef.fieldKey || '').toLowerCase();
      const efLabel = (ef.label || '').toLowerCase();
      return (
        key === efKey ||
        label.includes(efLabel) ||
        efLabel.includes(label) ||
        FIELD_ALIASES.find((fa) => fa.key === efKey)?.aliases.some((alias) => key.includes(alias) || label.includes(alias))
      );
    });

    if (ocrIdx !== -1) {
      usedExtractedIndices.add(ocrIdx);
      matched.push({
        formFieldId: field.id,
        fieldKey: field.fieldKey,
        label: field.label,
        extractedValue: extractedFields[ocrIdx].value,
        confidence: extractedFields[ocrIdx].confidence || 'HIGH',
        isAutoFilled: true,
      });
    }
  });

  // Step 2: Sequential fallback matching for remaining unmatched form fields
  if (matched.length < currentFormFields.length) {
    const matchedFieldIds = new Set(matched.map((m) => m.formFieldId));
    const unusedFields = currentFormFields.filter((f) => !matchedFieldIds.has(f.id));
    const unusedOCR = extractedFields.filter((_, eIdx) => !usedExtractedIndices.has(eIdx));

    unusedFields.forEach((field, i) => {
      if (unusedOCR[i] && unusedOCR[i].value) {
        matched.push({
          formFieldId: field.id,
          fieldKey: field.fieldKey,
          label: field.label,
          extractedValue: unusedOCR[i].value,
          confidence: 'LOW',
          isAutoFilled: true,
        });
      }
    });
  }

  return matched;
}

/**
 * Extract actual line bounding boxes (x, y, width, height) from document image using Tesseract OCR
 * @param {File|Blob|HTMLCanvasElement|string} imageSource
 * @returns {Promise<Array<{ originalText: string, x: number, y: number, width: number, height: number }>>}
 */
export async function extractBoundingBoxesFromImage(imageSource) {
  let worker = null;
  try {
    worker = await createWorker('eng');
    const ret = await worker.recognize(imageSource);
    const lines = ret.data.lines || [];
    const regions = [];

    for (const line of lines) {
      const text = (line.text || '').trim();
      if (text.length >= 2 && line.bbox) {
        const x = Math.round(line.bbox.x0);
        const y = Math.round(line.bbox.y0);
        const width = Math.max(20, Math.round(line.bbox.x1 - line.bbox.x0));
        const height = Math.max(12, Math.round(line.bbox.y1 - line.bbox.y0));
        regions.push({ originalText: text, x, y, width, height });
      }
    }
    return regions;
  } catch (err) {
    console.warn('Bounding box extraction error:', err);
    return [];
  } finally {
    if (worker) {
      await worker.terminate();
    }
  }
}
