/**
 * Integration Tests: Multipoint Hub System
 * 
 * Tests the complete workflow of importing, syncing, and resolving conflicts
 * across multiple data sources (local files and remote URLs).
 */

import { describe, it, expect, beforeEach } from "vitest";
import { FileImportService } from "./file-import";
import { RemoteSyncService } from "./remote-sync";
import {
  PhoneNumberValidator,
  ConflictResolver,
  DeduplicationEngine,
  ListParser,
  DataSourceManager,
} from "./multipoint-hub";
import { DataSource, NewBlockEntry } from "@/lib/db/schema";

describe("Multipoint Hub Integration Tests", () => {
  describe("Workflow 1: Local File Import → Database Storage → De-duplication", () => {
    it("should import CSV file and de-duplicate entries", () => {
      // Step 1: Create CSV content
      const csv = `+18005551234, BLOCK, Telemarketer, 3
+14155551234, BLOCK, Spam, 4
+18005551234, BLOCK, Telemarketer, 2
+18005559999, BLOCK, Scam, 5`;

      // Step 2: Validate file
      const validation = FileImportService.validateFile(csv, "test.csv");
      expect(validation.valid).toBe(true);

      // Step 3: Generate preview
      const preview = FileImportService.generatePreview(csv, "test.csv", 256);
      expect(preview.validRows).toBeGreaterThan(0);
      expect(preview.sampleRows.length).toBeGreaterThan(0);

      // Step 4: Parse entries
      const entries = FileImportService.parseFile(csv, "test.csv", 1);
      expect(entries.length).toBe(4);

      // Step 5: De-duplicate (simulate database operation)
      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(entries as any);
      expect(deduplicated.length).toBe(3); // Should remove one duplicate
      expect(deduplicated[0].priority).toBe(3); // Higher priority kept
    });

    it("should handle mixed valid and invalid entries", () => {
      const csv = `+18005551234, BLOCK, Valid, 3
invalid-number, BLOCK, Invalid, 3
+14155551234, BLOCK, Valid, 4
, BLOCK, Empty, 3`;

      const entries = FileImportService.parseFile(csv, "test.csv", 1);

      // Should only have valid entries
      expect(entries.length).toBe(2);
      expect(entries.every((e) => PhoneNumberValidator.isValid(e.phoneNumber))).toBe(true);
    });
  });

  describe("Workflow 2: Remote URL Sync → Parse → Merge with Existing", () => {
    it("should sync remote file and merge with existing entries", () => {
      // Step 1: Simulate existing entries in database
      const existingEntries: NewBlockEntry[] = [
        {
          phoneNumber: "+18005551234",
          action: "BLOCK",
          label: "Existing",
          priority: 3,
          sourceId: 1,
          isPattern: 0,
          isManual: 0,
        },
        {
          phoneNumber: "+14155551234",
          action: "BLOCK",
          label: "Existing",
          priority: 3,
          sourceId: 1,
          isPattern: 0,
          isManual: 0,
        },
      ];

      // Step 2: Simulate remote file content
      const remoteCSV = `+18005551234, BLOCK, Remote, 5
+19175551234, BLOCK, Remote, 4`;

      // Step 3: Parse remote entries
      const remoteEntries = FileImportService.parseFile(remoteCSV, "remote.csv", 2);
      expect(remoteEntries.length).toBe(2);

      // Step 4: Merge all entries
      const allEntries = [...existingEntries, ...remoteEntries];

      // Step 5: De-duplicate (keep higher priority)
      const merged = DeduplicationEngine.deduplicateBlockEntries(allEntries as any);
      expect(merged.length).toBe(3); // +18005551234 (higher priority), +14155551234, +19175551234
      expect(merged[0].priority).toBe(5); // Remote entry has higher priority
    });
  });

  describe("Workflow 3: Conflict Resolution - Allow-list Priority", () => {
    it("should prioritize Allow-list over Block-list", () => {
      // Step 1: Create block entries
      const blockEntries: NewBlockEntry[] = [
        {
          phoneNumber: "+1800",
          action: "BLOCK",
          label: "Block all 800s",
          priority: 3,
          sourceId: 1,
          isPattern: 1,
          isManual: 0,
        },
      ];

      // Step 2: Create allow entries
      const allowEntries: NewBlockEntry[] = [
        {
          phoneNumber: "+18005551234",
          action: "ALLOW",
          label: "My bank",
          priority: 5,
          sourceId: 2,
          isPattern: 0,
          isManual: 1,
        },
      ];

      // Step 3: Test conflict resolution
      const result1 = ConflictResolver.resolveConflict(
        "+18005551234",
        allowEntries as any,
        blockEntries as any
      );
      expect(result1).toBe("ALLOW"); // Allow-list wins

      // Step 4: Test different number (not in allow-list)
      const result2 = ConflictResolver.resolveConflict(
        "+18005559999",
        allowEntries as any,
        blockEntries as any
      );
      expect(result2).toBe("BLOCK"); // Block-list applies
    });

    it("should handle multiple sources with different priorities", () => {
      // Simulate 3 sources: Personal (manual), Community (URL), Professional (URL)
      const personalBlocks: NewBlockEntry[] = [
        {
          phoneNumber: "+14155551234",
          action: "BLOCK",
          label: "Personal block",
          priority: 5,
          sourceId: 1,
          isPattern: 0,
          isManual: 1,
        },
      ];

      const communityBlocks: NewBlockEntry[] = [
        {
          phoneNumber: "+14155551234",
          action: "BLOCK",
          label: "Community report",
          priority: 3,
          sourceId: 2,
          isPattern: 0,
          isManual: 0,
        },
      ];

      const professionalAllows: NewBlockEntry[] = [
        {
          phoneNumber: "+14155551234",
          action: "ALLOW",
          label: "Work contact",
          priority: 4,
          sourceId: 3,
          isPattern: 0,
          isManual: 0,
        },
      ];

      // Merge all entries
      const allBlocks = [...personalBlocks, ...communityBlocks];
      const merged = DeduplicationEngine.deduplicateBlockEntries(allBlocks as any);

      // Conflict resolution: Allow-list has priority
      const result = ConflictResolver.resolveConflict(
        "+14155551234",
        professionalAllows as any,
        merged as any
      );
      expect(result).toBe("ALLOW"); // Professional allow wins
    });
  });

  describe("Workflow 4: Error Scenarios & Recovery", () => {
    it("should handle corrupted CSV gracefully", () => {
      const corruptedCSV = `+18005551234, BLOCK, Valid, 3
\x00\x01\x02corrupted data
+14155551234, BLOCK, Valid, 4`;

      // Sanitize first
      const sanitized = FileImportService.sanitizeContent(corruptedCSV);

      // Parse (should skip corrupted line)
      const entries = FileImportService.parseFile(sanitized, "test.csv", 1);

      // Should have valid entries only
      expect(entries.length).toBeGreaterThan(0);
      expect(entries.every((e) => PhoneNumberValidator.isValid(e.phoneNumber))).toBe(true);
    });

    it("should handle file with BOM and various encodings", () => {
      const csvWithBOM = "\ufeff+18005551234, BLOCK, Test, 3\n+14155551234, BLOCK, Test, 4";

      // Sanitize
      const sanitized = FileImportService.sanitizeContent(csvWithBOM);
      expect(sanitized).not.toContain("\ufeff");

      // Parse
      const entries = FileImportService.parseFile(sanitized, "test.csv", 1);
      expect(entries.length).toBe(2);
    });

    it("should validate data source configuration", () => {
      // Valid local file source
      const localSource = {
        name: "My Blocks",
        type: "local_file" as const,
        sourceUrl: "/path/to/file.csv",
      };
      expect(DataSourceManager.validateSource(localSource).valid).toBe(true);

      // Valid remote URL source
      const remoteSource = {
        name: "Community List",
        type: "remote_url" as const,
        sourceUrl: "https://example.com/spam-list.csv",
      };
      expect(DataSourceManager.validateSource(remoteSource).valid).toBe(true);

      // Invalid: missing name
      const invalidSource = {
        type: "local_file" as const,
        sourceUrl: "/path/to/file.csv",
      };
      expect(DataSourceManager.validateSource(invalidSource).valid).toBe(false);

      // Invalid: bad URL
      const badUrlSource = {
        name: "Bad URL",
        type: "remote_url" as const,
        sourceUrl: "not-a-url",
      };
      expect(DataSourceManager.validateSource(badUrlSource).valid).toBe(false);
    });
  });

  describe("Workflow 5: Performance - Large File Handling", () => {
    it("should handle 10k row CSV efficiently", () => {
      // Generate 10k phone numbers
      const lines = Array(10000)
        .fill(0)
        .map((_, i) => {
          const num = String(2000000000 + i).slice(-10);
          return `+1${num}, BLOCK, Auto, 3`;
        })
        .join("\n");

      const startTime = Date.now();

      // Parse
      const entries = FileImportService.parseFile(lines, "large.csv", 1);

      const parseTime = Date.now() - startTime;

      expect(entries.length).toBe(10000);
      expect(parseTime).toBeLessThan(5000); // Should parse in < 5 seconds
    });

    it("should de-duplicate 10k entries efficiently", () => {
      // Create 10k entries with 20% duplicates
      const entries: NewBlockEntry[] = Array(10000)
        .fill(0)
        .map((_, i) => {
          const phoneNumber = `+1${String(2000000000 + (i % 8000)).slice(-10)}`;
          return {
            phoneNumber,
            action: "BLOCK" as const,
            label: `Entry ${i}`,
            priority: 3,
            sourceId: 1,
            isPattern: 0,
            isManual: 0,
          };
        });

      const startTime = Date.now();

      // De-duplicate
      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(entries as any);

      const dedupeTime = Date.now() - startTime;

      expect(deduplicated.length).toBeLessThan(entries.length);
      expect(dedupeTime).toBeLessThan(1000); // Should de-dupe in < 1 second
    });

    it("should estimate import time accurately", () => {
      const fileSize = 1024 * 1024; // 1MB
      const rowCount = 10000;

      const estimatedTime = FileImportService.estimateImportTime(fileSize, rowCount);

      expect(estimatedTime).toBeGreaterThan(0);
      expect(estimatedTime).toBeLessThan(10000); // Should be reasonable estimate
    });
  });

  describe("Workflow 6: Sync Scheduling & Status Tracking", () => {
    it("should track sync status across multiple sources", () => {
      const now = Math.floor(Date.now() / 1000);

      const sources: DataSource[] = [
        {
          id: 1,
          name: "Personal",
          type: "local_file",
          sourceUrl: "/path/to/personal.csv",
          enabled: 1,
          lastSync: now - 86400,
          syncStatus: "success",
          syncError: null,
          rowCount: 100,
          createdAt: now,
          updatedAt: now,
        },
        {
          id: 2,
          name: "Community",
          type: "remote_url",
          sourceUrl: "https://example.com/community.csv",
          enabled: 1,
          lastSync: now - 8 * 86400, // 8 days ago
          syncStatus: "success",
          syncError: null,
          rowCount: 5000,
          createdAt: now,
          updatedAt: now,
        },
        {
          id: 3,
          name: "Professional",
          type: "remote_url",
          sourceUrl: "https://example.com/professional.csv",
          enabled: 1,
          lastSync: null,
          syncStatus: "pending",
          syncError: null,
          rowCount: 0,
          createdAt: now,
          updatedAt: now,
        },
      ];

      // Check which sources are due for sync
      const dueSources = sources.filter((s) => RemoteSyncService.isDueForSync(s, 7));

      expect(dueSources.length).toBe(2); // Community and Professional
      expect(dueSources.map((s) => s.id)).toContain(2);
      expect(dueSources.map((s) => s.id)).toContain(3);
    });
  });

  describe("Workflow 7: End-to-End: Import → Sync → Resolve → Query", () => {
    it("should complete full workflow from import to conflict resolution", () => {
      // Step 1: Import local file
      const localCSV = `+18005551234, BLOCK, Telemarketer, 3
+14155551234, BLOCK, Spam, 4`;

      const localEntries = FileImportService.parseFile(localCSV, "local.csv", 1);
      expect(localEntries.length).toBe(2);

      // Step 2: Simulate remote sync
      const remoteCSV = `+18005551234, BLOCK, Remote, 5
+19175551234, BLOCK, Remote, 4`;

      const remoteEntries = FileImportService.parseFile(remoteCSV, "remote.csv", 2);
      expect(remoteEntries.length).toBe(2);

      // Step 3: Merge and de-duplicate
      const allEntries = [...localEntries, ...remoteEntries];
      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(allEntries as any);
      expect(deduplicated.length).toBe(3);

      // Step 4: Create allow-list
      const allowEntries: NewBlockEntry[] = [
        {
          phoneNumber: "+14155551234",
          action: "ALLOW",
          label: "Whitelist",
          priority: 5,
          sourceId: 3,
          isPattern: 0,
          isManual: 1,
        },
      ];

      // Step 5: Resolve conflicts for incoming calls
      const testCalls = [
        { number: "+18005551234", expected: "BLOCK" }, // In block-list
        { number: "+14155551234", expected: "ALLOW" }, // In allow-list (priority)
        { number: "+19175551234", expected: "BLOCK" }, // In block-list
        { number: "+12125551234", expected: "ALLOW" }, // Not in any list (default allow)
      ];

      for (const testCall of testCalls) {
        const result = ConflictResolver.resolveConflict(
          testCall.number,
          allowEntries as any,
          deduplicated as any
        );
        expect(result).toBe(testCall.expected);
      }
    });
  });
});
