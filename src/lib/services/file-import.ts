import { describe, it, expect } from "vitest";
import { FileImportService } from "./file-import";

describe("File Import Service", () => {
  describe("parseFile", () => {
    it("should parse CSV content", () => {
      const csv = `+18005551234, BLOCK, Telemarketer, 3
+14155551234, BLOCK, Spam, 4`;

      const entries = FileImportService.parseFile(csv, "test.csv", 1);

      expect(entries).toHaveLength(2);
      expect(entries[0].phoneNumber).toBe("+18005551234");
    });

    it("should parse TXT content", () => {
      const txt = `+18005551234
+14155551234`;

      const entries = FileImportService.parseFile(txt, "test.txt", 1);

      expect(entries).toHaveLength(2);
    });
  });

  describe("generatePreview", () => {
    it("should generate preview from CSV content", () => {
      const csv = `+18005551234, BLOCK, Telemarketer, 3
+14155551234, BLOCK, Spam, 4
invalid, BLOCK, Bad, 3`;

      const preview = FileImportService.generatePreview(csv, "test.csv", 256);

      expect(preview.fileName).toBe("test.csv");
      expect(preview.format).toBe("csv");
      expect(preview.totalRows).toBe(3);
      expect(preview.sampleRows.length).toBeGreaterThan(0);
      expect(preview.errors.length).toBeGreaterThan(0);
    });

    it("should show sample rows in preview", () => {
      const csv = `+18005551234, BLOCK, Test, 3`;

      const preview = FileImportService.generatePreview(csv, "test.csv", 100);

      expect(preview.sampleRows).toHaveLength(1);
      expect(preview.sampleRows[0].phoneNumber).toBe("+18005551234");
      expect(preview.sampleRows[0].isValid).toBe(true);
    });

    it("should mark invalid rows in preview", () => {
      const csv = `invalid, BLOCK, Bad, 3`;

      const preview = FileImportService.generatePreview(csv, "test.csv", 50);

      expect(preview.sampleRows[0].isValid).toBe(false);
      expect(preview.sampleRows[0].error).toContain("Invalid");
    });
  });

  describe("validateFile", () => {
    it("should accept valid CSV file", () => {
      const csv = `+18005551234, BLOCK, Test, 3`;

      const result = FileImportService.validateFile(csv, "test.csv");

      expect(result.valid).toBe(true);
    });

    it("should reject empty file", () => {
      const result = FileImportService.validateFile("", "test.csv");

      expect(result.valid).toBe(false);
      expect(result.error).toContain("empty");
    });

    it("should reject file with only whitespace", () => {
      const result = FileImportService.validateFile("   \n  \n  ", "test.csv");

      expect(result.valid).toBe(false);
    });

    it("should reject unknown format", () => {
      const unknown = "This is not a valid format";

      const result = FileImportService.validateFile(unknown, "test.txt");

      expect(result.valid).toBe(false);
      expect(result.error).toContain("format");
    });

    it("should reject file that is too large", () => {
      // Create a file with 1.1 million rows
      const lines = Array(1000001)
        .fill("+18005551234")
        .join("\n");

      const result = FileImportService.validateFile(lines, "huge.txt");

      expect(result.valid).toBe(false);
      expect(result.error).toContain("too large");
    });
  });

  describe("sanitizeContent", () => {
    it("should remove BOM", () => {
      const content = "\ufeff+18005551234";

      const sanitized = FileImportService.sanitizeContent(content);

      expect(sanitized).toBe("+18005551234");
    });

    it("should preserve valid content", () => {
      const content = `+18005551234, BLOCK, Test, 3
+14155551234, BLOCK, Spam, 4`;

      const sanitized = FileImportService.sanitizeContent(content);

      expect(sanitized).toContain("+18005551234");
      expect(sanitized).toContain("+14155551234");
    });

    it("should remove control characters", () => {
      const content = "+18005551234\x00\x01\x02"; // Null and control chars

      const sanitized = FileImportService.sanitizeContent(content);

      expect(sanitized).toBe("+18005551234");
    });

    it("should preserve tabs and newlines", () => {
      const content = "+18005551234\t+14155551234\n+14165551234";

      const sanitized = FileImportService.sanitizeContent(content);

      expect(sanitized).toContain("\t");
      expect(sanitized).toContain("\n");
    });
  });

  describe("estimateImportTime", () => {
    it("should estimate import time", () => {
      const time = FileImportService.estimateImportTime(10240, 100); // 10KB, 100 rows

      expect(time).toBeGreaterThan(0);
      expect(time).toBeLessThan(100); // Should be quick
    });

    it("should scale with file size", () => {
      const small = FileImportService.estimateImportTime(1024, 100);
      const large = FileImportService.estimateImportTime(10240, 100);

      expect(large).toBeGreaterThan(small);
    });

    it("should scale with row count", () => {
      const few = FileImportService.estimateImportTime(1024, 100);
      const many = FileImportService.estimateImportTime(1024, 10000);

      expect(many).toBeGreaterThan(few);
    });
  });

  describe("formatFileSize", () => {
    it("should format bytes", () => {
      expect(FileImportService.formatFileSize(0)).toBe("0 Bytes");
      expect(FileImportService.formatFileSize(512)).toContain("Bytes");
    });

    it("should format kilobytes", () => {
      expect(FileImportService.formatFileSize(1024)).toContain("KB");
    });

    it("should format megabytes", () => {
      expect(FileImportService.formatFileSize(1024 * 1024)).toContain("MB");
    });

    it("should format gigabytes", () => {
      expect(FileImportService.formatFileSize(1024 * 1024 * 1024)).toContain("GB");
    });
  });

  describe("generateSummary", () => {
    it("should generate import summary", () => {
      const preview = {
        fileName: "test.csv",
        fileSize: 1024,
        format: "csv" as const,
        totalRows: 100,
        validRows: 95,
        invalidRows: 5,
        sampleRows: [],
        errors: [],
      };

      const summary = FileImportService.generateSummary(preview);

      expect(summary).toContain("test.csv");
      expect(summary).toContain("CSV");
      expect(summary).toContain("95");
      expect(summary).toContain("5");
    });

    it("should include errors in summary", () => {
      const preview = {
        fileName: "test.csv",
        fileSize: 1024,
        format: "csv" as const,
        totalRows: 100,
        validRows: 90,
        invalidRows: 10,
        sampleRows: [],
        errors: ["Found invalid rows"],
      };

      const summary = FileImportService.generateSummary(preview);

      expect(summary).toContain("Found invalid rows");
    });
  });

  describe("Integration: Full Import Flow", () => {
    it("should validate, sanitize, and preview a CSV file", () => {
      let csv = "\ufeff+18005551234, BLOCK, Test, 3\n+14155551234, BLOCK, Spam, 4";

      // Validate
      const validation = FileImportService.validateFile(csv, "test.csv");
      expect(validation.valid).toBe(true);

      // Sanitize
      csv = FileImportService.sanitizeContent(csv);
      expect(csv).not.toContain("\ufeff");

      // Preview
      const preview = FileImportService.generatePreview(csv, "test.csv", 256);
      expect(preview.validRows).toBeGreaterThan(0);

      // Parse
      const entries = FileImportService.parseFile(csv, "test.csv", 1);
      expect(entries).toHaveLength(2);
    });
  });
});
