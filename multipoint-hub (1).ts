import { describe, it, expect } from "vitest";
import {
  PhoneNumberValidator,
  ConflictResolver,
  DeduplicationEngine,
  ListParser,
  DataSourceManager,
  SyncScheduler,
} from "./multipoint-hub";

describe("Multipoint Hub Service", () => {
  describe("PhoneNumberValidator", () => {
    it("should validate E.164 format", () => {
      expect(PhoneNumberValidator.isValid("+18005551234")).toBe(true);
      expect(PhoneNumberValidator.isValid("+441234567890")).toBe(true);
    });

    it("should validate standard US format", () => {
      expect(PhoneNumberValidator.isValid("8005551234")).toBe(true);
      expect(PhoneNumberValidator.isValid("(800) 555-1234")).toBe(true);
      expect(PhoneNumberValidator.isValid("800-555-1234")).toBe(true);
    });

    it("should reject invalid formats", () => {
      expect(PhoneNumberValidator.isValid("")).toBe(false);
      expect(PhoneNumberValidator.isValid("123")).toBe(false); // Too short
      expect(PhoneNumberValidator.isValid("abc")).toBe(false);
    });

    it("should normalize to E.164 format", () => {
      expect(PhoneNumberValidator.normalize("8005551234")).toBe("+18005551234");
      expect(PhoneNumberValidator.normalize("(800) 555-1234")).toBe("+18005551234");
      expect(PhoneNumberValidator.normalize("+18005551234")).toBe("+18005551234");
    });

    it("should detect patterns", () => {
      expect(PhoneNumberValidator.isPattern("+1800")).toBe(true); // Prefix pattern
      expect(PhoneNumberValidator.isPattern("+44")).toBe(true); // Country code pattern
      expect(PhoneNumberValidator.isPattern("+1[0-9]{10}")).toBe(true); // Regex pattern
      expect(PhoneNumberValidator.isPattern("+18005551234")).toBe(false); // Not a pattern
    });
  });

  describe("ConflictResolver", () => {
    it("should prioritize Allow-list over Block-list", () => {
      const allowEntries = [{ phoneNumber: "+18005551234" }];
      const blockEntries = [{ phoneNumber: "+18005551234" }];

      const result = ConflictResolver.resolveConflict(
        "8005551234",
        allowEntries as any,
        blockEntries as any
      );

      expect(result).toBe("ALLOW");
    });

    it("should block if in Block-list and not in Allow-list", () => {
      const allowEntries: any[] = [];
      const blockEntries = [{ phoneNumber: "+18005551234" }];

      const result = ConflictResolver.resolveConflict(
        "8005551234",
        allowEntries,
        blockEntries as any
      );

      expect(result).toBe("BLOCK");
    });

    it("should allow if not in any list", () => {
      const allowEntries: any[] = [];
      const blockEntries: any[] = [];

      const result = ConflictResolver.resolveConflict(
        "8005551234",
        allowEntries,
        blockEntries
      );

      expect(result).toBe("ALLOW");
    });

    it("should match prefix patterns", () => {
      const allowEntries: any[] = [];
      const blockEntries = [{ phoneNumber: "+1800" }]; // Block all 800 numbers

      const result = ConflictResolver.resolveConflict(
        "+18005551234",
        allowEntries,
        blockEntries as any
      );

      expect(result).toBe("BLOCK");
    });
  });

  describe("DeduplicationEngine", () => {
    it("should remove duplicate block entries", () => {
      const entries = [
        { phoneNumber: "+18005551234", priority: 3 },
        { phoneNumber: "+18005551234", priority: 2 },
        { phoneNumber: "+14155551234", priority: 3 },
      ];

      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(entries as any);

      expect(deduplicated).toHaveLength(2);
      expect(deduplicated[0].priority).toBe(3); // Higher priority kept
    });

    it("should keep first occurrence if priorities are equal", () => {
      const entries = [
        { phoneNumber: "+18005551234", priority: 3, label: "First" },
        { phoneNumber: "+18005551234", priority: 3, label: "Second" },
      ];

      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(entries as any);

      expect(deduplicated).toHaveLength(1);
      expect(deduplicated[0].label).toBe("First");
    });
  });

  describe("ListParser", () => {
    it("should parse CSV format", () => {
      const csv = `+18005551234, BLOCK, Telemarketer, 3
+14155551234, BLOCK, Spam, 4`;

      const entries = ListParser.parseCSV(csv, 1);

      expect(entries).toHaveLength(2);
      expect(entries[0].phoneNumber).toBe("+18005551234");
      expect(entries[0].label).toBe("Telemarketer");
      expect(entries[1].priority).toBe(4);
    });

    it("should parse TXT format (one number per line)", () => {
      const txt = `+18005551234
+14155551234
8005559999`;

      const entries = ListParser.parseTXT(txt, 1);

      expect(entries).toHaveLength(3);
      expect(entries[2].phoneNumber).toBe("+18005559999");
    });

    it("should skip invalid phone numbers", () => {
      const csv = `+18005551234, BLOCK, Valid, 3
invalid, BLOCK, Invalid, 3`;

      const entries = ListParser.parseCSV(csv, 1);

      expect(entries).toHaveLength(1);
    });

    it("should clamp priority to 1-5 range", () => {
      const csv = `+18005551234, BLOCK, Test, 10`;

      const entries = ListParser.parseCSV(csv, 1);

      expect(entries[0].priority).toBe(5);
    });

    it("should detect CSV format", () => {
      expect(ListParser.detectFormat("+18005551234, BLOCK, Test, 3")).toBe("csv");
      expect(ListParser.detectFormat("+18005551234\n+14155551234")).toBe("txt");
    });
  });

  describe("DataSourceManager", () => {
    it("should validate source configuration", () => {
      const validSource = {
        name: "My Blocks",
        type: "local_file" as const,
        sourceUrl: "/path/to/file.csv",
      };

      const result = DataSourceManager.validateSource(validSource);

      expect(result.valid).toBe(true);
    });

    it("should reject missing name", () => {
      const result = DataSourceManager.validateSource({
        type: "local_file" as const,
      });

      expect(result.valid).toBe(false);
      expect(result.error).toContain("name");
    });

    it("should reject invalid URL", () => {
      const result = DataSourceManager.validateSource({
        name: "Remote",
        type: "remote_url" as const,
        sourceUrl: "not-a-url",
      });

      expect(result.valid).toBe(false);
      expect(result.error).toContain("URL");
    });

    it("should calculate storage usage", () => {
      const usage = DataSourceManager.calculateStorageUsage(10000);

      expect(usage).toBeGreaterThan(0);
      expect(usage).toBeLessThan(2); // ~1MB for 10k entries
    });
  });

  describe("SyncScheduler", () => {
    it("should return current time if never synced", () => {
      const now = Math.floor(Date.now() / 1000);
      const nextSync = SyncScheduler.getNextSyncTime(null);

      expect(nextSync).toBeLessThanOrEqual(now + 1);
    });

    it("should calculate next sync time", () => {
      const lastSync = Math.floor(Date.now() / 1000) - 86400; // 1 day ago
      const nextSync = SyncScheduler.getNextSyncTime(lastSync, 7);

      expect(nextSync).toBeGreaterThan(lastSync);
    });

    it("should check if source is due for sync", () => {
      const oldSync = Math.floor(Date.now() / 1000) - 8 * 86400; // 8 days ago
      const recentSync = Math.floor(Date.now() / 1000) - 1 * 86400; // 1 day ago

      expect(SyncScheduler.isDueForSync(oldSync, 7)).toBe(true);
      expect(SyncScheduler.isDueForSync(recentSync, 7)).toBe(false);
    });
  });

  describe("Integration: Conflict Resolution with De-duplication", () => {
    it("should resolve conflicts after de-duplication", () => {
      const blockEntries = [
        { phoneNumber: "+18005551234", priority: 3 },
        { phoneNumber: "+18005551234", priority: 5 }, // Higher priority
      ];

      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(blockEntries as any);
      const allowEntries: any[] = [];

      const result = ConflictResolver.resolveConflict(
        "+18005551234",
        allowEntries,
        deduplicated as any
      );

      expect(result).toBe("BLOCK");
    });
  });

  describe("Integration: CSV Import with Validation", () => {
    it("should parse and validate CSV import", () => {
      const csv = `+18005551234, BLOCK, Telemarketer, 3
+14155551234, BLOCK, Spam, 4
invalid-number, BLOCK, Bad, 3`;

      const entries = ListParser.parseCSV(csv, 1);
      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(entries as any);

      expect(deduplicated).toHaveLength(2);
      expect(deduplicated[0].phoneNumber).toBe("+18005551234");
      expect(deduplicated[1].phoneNumber).toBe("+14155551234");
    });
  });
});
