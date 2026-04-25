/**
 * File Import Service
 * 
 * Handles importing block/allow lists from local files (CSV, XLSX, TXT)
 * with validation, error handling, and preview generation.
 */

import { PhoneNumberValidator, ListParser } from "./multipoint-hub";
import { NewBlockEntry } from "@/lib/db/schema";

export interface ImportPreview {
  fileName: string;
  fileSize: number;
  format: "csv" | "txt" | "xlsx" | "unknown";
  totalRows: number;
  validRows: number;
  invalidRows: number;
  sampleRows: Array<{
    phoneNumber: string;
    label: string;
    priority: number;
    isValid: boolean;
    error?: string;
  }>;
  errors: string[];
}

export interface ImportResult {
  success: boolean;
  entriesImported: number;
  entriesSkipped: number;
  errors: string[];
}

/**
 * File Import Service
 */
export class FileImportService {
  /**
   * Parse file content based on format detection
   */
  static parseFile(
    content: string,
    fileName: string,
    sourceId: number
  ): NewBlockEntry[] {
    const format = ListParser.detectFormat(content);

    switch (format) {
      case "csv":
        return ListParser.parseCSV(content, sourceId);
      case "txt":
        return ListParser.parseTXT(content, sourceId);
      default:
        console.warn(`Unknown format for file: ${fileName}`);
        return [];
    }
  }

  /**
   * Generate import preview from file content
   */
  static generatePreview(
    content: string,
    fileName: string,
    fileSize: number
  ): ImportPreview {
    const format = ListParser.detectFormat(content);
    const lines = content.split("\n").filter((line) => line.trim());

    const sampleRows: ImportPreview["sampleRows"] = [];
    let validRows = 0;
    let invalidRows = 0;
    const errors: string[] = [];

    // Parse first 10 rows for preview
    for (let i = 0; i < Math.min(10, lines.length); i++) {
      const line = lines[i];

      if (format === "csv") {
        const parts = line.split(",").map((p) => p.trim());
        const phoneNumber = parts[0];
        const label = parts[2] || "";
        const priority = parseInt(parts[3] || "3", 10);

        const isValid = PhoneNumberValidator.isValid(phoneNumber);

        if (isValid) {
          validRows++;
          sampleRows.push({
            phoneNumber: PhoneNumberValidator.normalize(phoneNumber),
            label,
            priority: Math.max(1, Math.min(5, priority)),
            isValid: true,
          });
        } else {
          invalidRows++;
          sampleRows.push({
            phoneNumber,
            label,
            priority,
            isValid: false,
            error: `Invalid phone number: ${phoneNumber}`,
          });
        }
      } else if (format === "txt") {
        const phoneNumber = line.trim();
        const isValid = PhoneNumberValidator.isValid(phoneNumber);

        if (isValid) {
          validRows++;
          sampleRows.push({
            phoneNumber: PhoneNumberValidator.normalize(phoneNumber),
            label: "",
            priority: 3,
            isValid: true,
          });
        } else {
          invalidRows++;
          sampleRows.push({
            phoneNumber,
            label: "",
            priority: 3,
            isValid: false,
            error: `Invalid phone number: ${phoneNumber}`,
          });
        }
      }
    }

    // Estimate total valid/invalid based on sample
    const estimatedValidRatio = validRows / (validRows + invalidRows || 1);
    const estimatedTotalValid = Math.round(lines.length * estimatedValidRatio);
    const estimatedTotalInvalid = lines.length - estimatedTotalValid;

    if (invalidRows > 0) {
      errors.push(
        `Found ${invalidRows} invalid rows in sample. ` +
          `Estimated ${estimatedTotalInvalid} invalid rows total.`
      );
    }

    if (format === "unknown") {
      errors.push("Could not detect file format. Expected CSV or TXT.");
    }

    return {
      fileName,
      fileSize,
      format,
      totalRows: lines.length,
      validRows: estimatedTotalValid,
      invalidRows: estimatedTotalInvalid,
      sampleRows,
      errors,
    };
  }

  /**
   * Validate file before import
   */
  static validateFile(content: string, fileName: string): { valid: boolean; error?: string } {
    if (!content || content.trim().length === 0) {
      return { valid: false, error: "File is empty" };
    }

    const format = ListParser.detectFormat(content);

    if (format === "unknown") {
      return { valid: false, error: "Could not detect file format (expected CSV or TXT)" };
    }

    const lines = content.split("\n").filter((line) => line.trim());

    if (lines.length === 0) {
      return { valid: false, error: "File contains no data" };
    }

    if (lines.length > 1000000) {
      return { valid: false, error: "File is too large (max 1 million rows)" };
    }

    return { valid: true };
  }

  /**
   * Sanitize content (UTF-8 encoding, remove invalid characters)
   */
  static sanitizeContent(content: string): string {
    // Remove BOM if present
    if (content.charCodeAt(0) === 0xfeff) {
      content = content.slice(1);
    }

    // Replace invalid UTF-8 sequences
    content = content
      .split("")
      .map((char) => {
        const code = char.charCodeAt(0);
        // Keep valid UTF-8 range and common symbols
        if (code < 32 && code !== 9 && code !== 10 && code !== 13) {
          return ""; // Remove control characters except tab, newline, carriage return
        }
        return char;
      })
      .join("");

    return content;
  }

  /**
   * Estimate import time based on file size and device performance
   */
  static estimateImportTime(fileSize: number, rowCount: number): number {
    // Rough estimate: 1ms per KB + 0.1ms per row
    const sizeTime = fileSize / 1024;
    const rowTime = rowCount * 0.1;

    return Math.round(sizeTime + rowTime);
  }

  /**
   * Format file size for display
   */
  static formatFileSize(bytes: number): string {
    if (bytes === 0) return "0 Bytes";

    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  }

  /**
   * Generate import summary
   */
  static generateSummary(preview: ImportPreview): string {
    return `
📁 File: ${preview.fileName}
📊 Size: ${FileImportService.formatFileSize(preview.fileSize)}
📋 Format: ${preview.format.toUpperCase()}
✓ Valid Rows: ${preview.validRows}
✗ Invalid Rows: ${preview.invalidRows}
📈 Total Rows: ${preview.totalRows}
${preview.errors.length > 0 ? `⚠️ Errors: ${preview.errors.join("; ")}` : ""}
    `.trim();
  }
}
