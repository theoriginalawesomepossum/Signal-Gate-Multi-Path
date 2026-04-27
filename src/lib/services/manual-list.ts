import { describe, it, expect, beforeEach } from "vitest";
import { ManualListService } from "./manual-list";

describe("Manual List Service", () => {
  beforeEach(() => {
    ManualListService.reset();
  });

  describe("addToBlockList", () => {
    it("should add entry to block list", () => {
      const entry = ManualListService.addToBlockList("+18005551234", "Telemarketer", "Spam calls");

      expect(entry).toBeDefined();
      expect(entry?.phoneNumber).toBe("+18005551234");
      expect(entry?.action).toBe("BLOCK");
      expect(entry?.label).toBe("Telemarketer");
      expect(entry?.notes).toBe("Spam calls");
    });

    it("should reject invalid phone numbers", () => {
      const entry = ManualListService.addToBlockList("invalid", "Test");

      expect(entry).toBeNull();
    });

    it("should reject empty labels", () => {
      const entry = ManualListService.addToBlockList("+18005551234", "");

      expect(entry).toBeNull();
    });

    it("should prevent duplicates", () => {
      ManualListService.addToBlockList("+18005551234", "First");
      const entry2 = ManualListService.addToBlockList("+18005551234", "Second");

      expect(entry2).toBeNull();
    });
  });

  describe("addToAllowList", () => {
    it("should add entry to allow list", () => {
      const entry = ManualListService.addToAllowList("+14155551234", "My Bank");

      expect(entry).toBeDefined();
      expect(entry?.action).toBe("ALLOW");
    });
  });

  describe("removeFromBlockList", () => {
    it("should remove entry from block list", () => {
      const entry = ManualListService.addToBlockList("+18005551234", "Telemarketer");

      expect(ManualListService.getBlockList().length).toBe(1);

      const removed = ManualListService.removeFromBlockList(entry!.id);

      expect(removed).toBe(true);
      expect(ManualListService.getBlockList().length).toBe(0);
    });

    it("should return false for non-existent ID", () => {
      const removed = ManualListService.removeFromBlockList(999);

      expect(removed).toBe(false);
    });
  });

  describe("updateBlockListEntry", () => {
    it("should update entry label and notes", () => {
      const entry = ManualListService.addToBlockList("+18005551234", "Old Label");
      const updated = ManualListService.updateBlockListEntry(entry!.id, "New Label", "New notes");

      expect(updated?.label).toBe("New Label");
      expect(updated?.notes).toBe("New notes");
    });

    it("should return null for non-existent ID", () => {
      const updated = ManualListService.updateBlockListEntry(999, "New Label");

      expect(updated).toBeNull();
    });
  });

  describe("getBlockList", () => {
    it("should return all block list entries", () => {
      ManualListService.addToBlockList("+18005551111", "First");
      ManualListService.addToBlockList("+18005552222", "Second");

      const list = ManualListService.getBlockList();

      expect(list.length).toBe(2);
    });
  });

  describe("getAllowList", () => {
    it("should return all allow list entries", () => {
      ManualListService.addToAllowList("+14155551111", "First");
      ManualListService.addToAllowList("+14155552222", "Second");

      const list = ManualListService.getAllowList();

      expect(list.length).toBe(2);
    });
  });

  describe("getAllEntries", () => {
    it("should return entries from both lists", () => {
      ManualListService.addToBlockList("+18005551111", "Block");
      ManualListService.addToAllowList("+14155552222", "Allow");

      const all = ManualListService.getAllEntries();

      expect(all.length).toBe(2);
    });
  });

  describe("search", () => {
    beforeEach(() => {
      ManualListService.addToBlockList("+18005551111", "Telemarketer");
      ManualListService.addToBlockList("+18005552222", "Scammer");
      ManualListService.addToAllowList("+14155553333", "My Bank");
    });

    it("should search by phone number", () => {
      const results = ManualListService.search("1111");

      expect(results.length).toBe(1);
      expect(results[0].phoneNumber).toContain("1111");
    });

    it("should search by label", () => {
      const results = ManualListService.search("Telemarketer");

      expect(results.length).toBe(1);
      expect(results[0].label).toBe("Telemarketer");
    });

    it("should be case-insensitive", () => {
      const results = ManualListService.search("BANK");

      expect(results.length).toBe(1);
      expect(results[0].label).toBe("My Bank");
    });
  });

  describe("isBlocked", () => {
    it("should return true if number is blocked", () => {
      ManualListService.addToBlockList("+18005551234", "Test");

      expect(ManualListService.isBlocked("+18005551234")).toBe(true);
    });

    it("should return false if number is not blocked", () => {
      expect(ManualListService.isBlocked("+18005551234")).toBe(false);
    });
  });

  describe("isAllowed", () => {
    it("should return true if number is allowed", () => {
      ManualListService.addToAllowList("+14155551234", "Test");

      expect(ManualListService.isAllowed("+14155551234")).toBe(true);
    });

    it("should return false if number is not allowed", () => {
      expect(ManualListService.isAllowed("+14155551234")).toBe(false);
    });
  });

  describe("getStats", () => {
    beforeEach(() => {
      ManualListService.addToBlockList("+18005551111", "First");
      ManualListService.addToBlockList("+18005552222", "Second");
      ManualListService.addToAllowList("+14155553333", "Third");
    });

    it("should calculate correct statistics", () => {
      const stats = ManualListService.getStats();

      expect(stats.totalEntries).toBe(3);
      expect(stats.blockEntries).toBe(2);
      expect(stats.allowEntries).toBe(1);
    });
  });

  describe("moveEntry", () => {
    it("should move entry from block to allow list", () => {
      const entry = ManualListService.addToBlockList("+18005551234", "Test");

      expect(ManualListService.getBlockList().length).toBe(1);
      expect(ManualListService.getAllowList().length).toBe(0);

      const moved = ManualListService.moveEntry(entry!.id, "BLOCK", "ALLOW");

      expect(moved).toBeDefined();
      expect(moved?.action).toBe("ALLOW");
      expect(ManualListService.getBlockList().length).toBe(0);
      expect(ManualListService.getAllowList().length).toBe(1);
    });

    it("should prevent moving to same list", () => {
      const entry = ManualListService.addToBlockList("+18005551234", "Test");

      const moved = ManualListService.moveEntry(entry!.id, "BLOCK", "BLOCK");

      expect(moved).toBeNull();
    });

    it("should prevent moving to list with duplicate", () => {
      ManualListService.addToBlockList("+18005551234", "Block");
      ManualListService.addToAllowList("+18005551234", "Allow");

      const moved = ManualListService.moveEntry(1, "BLOCK", "ALLOW");

      expect(moved).toBeNull();
      expect(ManualListService.getBlockList().length).toBe(1); // Still in block list
    });
  });

  describe("exportAsCSV", () => {
    it("should export entries as CSV", () => {
      ManualListService.addToBlockList("+18005551234", "Telemarketer", "Spam");
      ManualListService.addToAllowList("+14155552222", "My Bank");

      const csv = ManualListService.exportAsCSV();

      expect(csv).toContain("Phone Number");
      expect(csv).toContain("+18005551234");
      expect(csv).toContain("BLOCK");
      expect(csv).toContain("Telemarketer");
    });
  });

  describe("importFromCSV", () => {
    it("should import entries from CSV", () => {
      const csv = `Phone Number,Action,Label,Notes,Created,Updated
"+18005551234","BLOCK","Telemarketer","Spam calls","2026-01-01T00:00:00.000Z","2026-01-01T00:00:00.000Z"
"+14155552222","ALLOW","My Bank","","2026-01-01T00:00:00.000Z","2026-01-01T00:00:00.000Z"`;

      const result = ManualListService.importFromCSV(csv);

      expect(result.imported).toBe(2);
      expect(result.failed).toBe(0);
      expect(ManualListService.getAllEntries().length).toBe(2);
    });

    it("should handle invalid rows", () => {
      const csv = `Phone Number,Action,Label,Notes,Created,Updated
"+18005551234","BLOCK","Telemarketer","","2026-01-01T00:00:00.000Z","2026-01-01T00:00:00.000Z"
"invalid","BLOCK","","","2026-01-01T00:00:00.000Z","2026-01-01T00:00:00.000Z"`;

      const result = ManualListService.importFromCSV(csv);

      expect(result.imported).toBe(1);
      expect(result.failed).toBe(1);
      expect(result.errors.length).toBeGreaterThan(0);
    });
  });

  describe("clearAll", () => {
    it("should clear all entries", () => {
      ManualListService.addToBlockList("+18005551111", "First");
      ManualListService.addToAllowList("+14155552222", "Second");

      const cleared = ManualListService.clearAll();

      expect(cleared).toBe(2);
      expect(ManualListService.getAllEntries().length).toBe(0);
    });
  });
});
