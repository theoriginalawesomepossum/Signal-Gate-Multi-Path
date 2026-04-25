import { describe, it, expect, beforeEach } from "vitest";
import { CallLogService, CallLogEntry } from "./call-log";

describe("Call Log Service", () => {
  beforeEach(() => {
    CallLogService.reset();
  });

  describe("addEntry", () => {
    it("should add a blocked call entry", () => {
      const entry = CallLogService.addEntry("+18005551234", "BLOCKED", "Prefix: +1800");

      expect(entry).toBeDefined();
      expect(entry?.phoneNumber).toBe("+18005551234");
      expect(entry?.action).toBe("BLOCKED");
      expect(entry?.source).toBe("Prefix: +1800");
    });

    it("should add an allowed call entry", () => {
      const entry = CallLogService.addEntry("+14155551234", "ALLOWED", "Manual");

      expect(entry).toBeDefined();
      expect(entry?.action).toBe("ALLOWED");
    });

    it("should reject invalid phone numbers", () => {
      const entry = CallLogService.addEntry("invalid-number", "BLOCKED");

      expect(entry).toBeNull();
    });

    it("should normalize phone numbers", () => {
      const entry = CallLogService.addEntry("18005551234", "BLOCKED");

      expect(entry?.phoneNumber).toBe("+18005551234");
    });

    it("should add entries in reverse chronological order", () => {
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+18005552222", "BLOCKED");
      CallLogService.addEntry("+18005553333", "BLOCKED");

      const entries = CallLogService.getEntries();

      expect(entries[0].phoneNumber).toBe("+18005553333");
      expect(entries[1].phoneNumber).toBe("+18005552222");
      expect(entries[2].phoneNumber).toBe("+18005551111");
    });
  });

  describe("getEntries", () => {
    it("should return all entries", () => {
      CallLogService.addEntry("+18005551234", "BLOCKED");
      CallLogService.addEntry("+14155551234", "ALLOWED");

      const entries = CallLogService.getEntries();

      expect(entries.length).toBe(2);
    });

    it("should return empty array when no entries", () => {
      const entries = CallLogService.getEntries();

      expect(entries).toEqual([]);
    });
  });

  describe("getFilteredEntries", () => {
    beforeEach(() => {
      CallLogService.addEntry("+18005551111", "BLOCKED", "Prefix");
      CallLogService.addEntry("+14155552222", "ALLOWED", "Manual");
      CallLogService.addEntry("+18005553333", "BLOCKED", "Community");
    });

    it("should filter by action", () => {
      const blocked = CallLogService.getFilteredEntries({ action: "BLOCKED" });

      expect(blocked.length).toBe(2);
      expect(blocked.every((e) => e.action === "BLOCKED")).toBe(true);
    });

    it("should filter by search query (phone number)", () => {
      const results = CallLogService.getFilteredEntries({ searchQuery: "1111" });

      expect(results.length).toBe(1);
      expect(results[0].phoneNumber).toContain("1111");
    });

    it("should filter by search query (contact name)", () => {
      CallLogService.addEntry("+19175554444", "BLOCKED", undefined, "John Doe");

      const results = CallLogService.getFilteredEntries({ searchQuery: "John" });

      expect(results.length).toBe(1);
      expect(results[0].contactName).toBe("John Doe");
    });

    it("should combine multiple filters", () => {
      const results = CallLogService.getFilteredEntries({
        action: "BLOCKED",
        searchQuery: "1111",
      });

      expect(results.length).toBe(1);
      expect(results[0].action).toBe("BLOCKED");
      expect(results[0].phoneNumber).toContain("1111");
    });
  });

  describe("deleteEntry", () => {
    it("should delete an entry by ID", () => {
      const entry = CallLogService.addEntry("+18005551234", "BLOCKED");

      expect(CallLogService.getEntries().length).toBe(1);

      const deleted = CallLogService.deleteEntry(entry!.id);

      expect(deleted).toBe(true);
      expect(CallLogService.getEntries().length).toBe(0);
    });

    it("should return false for non-existent ID", () => {
      const deleted = CallLogService.deleteEntry(999);

      expect(deleted).toBe(false);
    });
  });

  describe("clearAll", () => {
    it("should clear all entries", () => {
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+18005552222", "BLOCKED");

      const cleared = CallLogService.clearAll();

      expect(cleared).toBe(2);
      expect(CallLogService.getEntries().length).toBe(0);
    });
  });

  describe("getStats", () => {
    beforeEach(() => {
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+14155552222", "ALLOWED");
      CallLogService.addEntry("+19175553333", "BLOCKED");
    });

    it("should calculate correct statistics", () => {
      const stats = CallLogService.getStats();

      expect(stats.totalCalls).toBe(4);
      expect(stats.blockedCalls).toBe(3);
      expect(stats.allowedCalls).toBe(1);
      expect(stats.blockRate).toBe(75);
      expect(stats.uniqueNumbers).toBe(3);
    });

    it("should handle empty log", () => {
      CallLogService.reset();

      const stats = CallLogService.getStats();

      expect(stats.totalCalls).toBe(0);
      expect(stats.blockRate).toBe(0);
    });
  });

  describe("getCallsFromNumber", () => {
    it("should get all calls from a specific number", () => {
      CallLogService.addEntry("+18005551234", "BLOCKED");
      CallLogService.addEntry("+18005551234", "BLOCKED");
      CallLogService.addEntry("+14155552222", "ALLOWED");

      const calls = CallLogService.getCallsFromNumber("+18005551234");

      expect(calls.length).toBe(2);
      expect(calls.every((c) => c.phoneNumber === "+18005551234")).toBe(true);
    });
  });

  describe("getRecentCalls", () => {
    it("should get recent calls with limit", () => {
      for (let i = 0; i < 15; i++) {
        CallLogService.addEntry(`+1800555${String(i).padStart(4, "0")}`, "BLOCKED");
      }

      const recent = CallLogService.getRecentCalls(10);

      expect(recent.length).toBe(10);
    });
  });

  describe("formatTime", () => {
    it("should format recent times", () => {
      const now = Math.floor(Date.now() / 1000);

      expect(CallLogService.formatTime(now)).toBe("Just now");
      expect(CallLogService.formatTime(now - 300)).toContain("m ago"); // 5 minutes ago
      expect(CallLogService.formatTime(now - 3600)).toContain("h ago");
      expect(CallLogService.formatTime(now - 86400)).toContain("d ago");
    });
  });

  describe("exportAsCSV", () => {
    it("should export call log as CSV", () => {
      CallLogService.addEntry("+18005551234", "BLOCKED", "Prefix", "Telemarketer", 0);
      CallLogService.addEntry("+14155552222", "ALLOWED", "Manual", "John Doe", 120);

      const csv = CallLogService.exportAsCSV();

      expect(csv).toContain("Phone Number");
      expect(csv).toContain("+18005551234");
      expect(csv).toContain("BLOCKED");
      expect(csv).toContain("Telemarketer");
    });
  });

  describe("getStatsBySource", () => {
    it("should calculate statistics by source", () => {
      CallLogService.addEntry("+18005551111", "BLOCKED", "Prefix");
      CallLogService.addEntry("+18005552222", "BLOCKED", "Prefix");
      CallLogService.addEntry("+14155553333", "BLOCKED", "Community");
      CallLogService.addEntry("+19175554444", "BLOCKED", "Manual");

      const stats = CallLogService.getStatsBySource();

      expect(stats["Prefix"]).toBe(2);
      expect(stats["Community"]).toBe(1);
      expect(stats["Manual"]).toBe(1);
    });
  });

  describe("getMostBlockedNumbers", () => {
    it("should return most blocked numbers", () => {
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+18005551111", "BLOCKED");
      CallLogService.addEntry("+14155552222", "BLOCKED");
      CallLogService.addEntry("+14155552222", "BLOCKED");
      CallLogService.addEntry("+19175553333", "BLOCKED");

      const mostBlocked = CallLogService.getMostBlockedNumbers(5);

      expect(mostBlocked.length).toBe(3);
      expect(mostBlocked[0].number).toBe("+18005551111");
      expect(mostBlocked[0].count).toBe(3);
      expect(mostBlocked[1].number).toBe("+14155552222");
      expect(mostBlocked[1].count).toBe(2);
    });
  });
});
