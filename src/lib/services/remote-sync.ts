import { describe, it, expect, beforeEach, vi } from "vitest";
import { RemoteSyncService, BackgroundSyncManager } from "./remote-sync";
import { DataSource } from "@/lib/db/schema";

describe("Remote Sync Service", () => {
  describe("isDueForSync", () => {
    it("should return true if never synced", () => {
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync: null,
        syncStatus: "pending",
        syncError: null,
        rowCount: 0,
        createdAt: Math.floor(Date.now() / 1000),
        updatedAt: Math.floor(Date.now() / 1000),
      };

      expect(RemoteSyncService.isDueForSync(source, 7)).toBe(true);
    });

    it("should return false if recently synced", () => {
      const now = Math.floor(Date.now() / 1000);
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync: now - 86400, // 1 day ago
        syncStatus: "success",
        syncError: null,
        rowCount: 100,
        createdAt: now,
        updatedAt: now,
      };

      expect(RemoteSyncService.isDueForSync(source, 7)).toBe(false);
    });

    it("should return true if sync interval has passed", () => {
      const now = Math.floor(Date.now() / 1000);
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync: now - 8 * 86400, // 8 days ago
        syncStatus: "success",
        syncError: null,
        rowCount: 100,
        createdAt: now,
        updatedAt: now,
      };

      expect(RemoteSyncService.isDueForSync(source, 7)).toBe(true);
    });
  });

  describe("getNextSyncTime", () => {
    it("should return current time if never synced", () => {
      const now = Math.floor(Date.now() / 1000);
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync: null,
        syncStatus: "pending",
        syncError: null,
        rowCount: 0,
        createdAt: now,
        updatedAt: now,
      };

      const nextSync = RemoteSyncService.getNextSyncTime(source, 7);

      expect(nextSync).toBeLessThanOrEqual(now + 1);
    });

    it("should calculate next sync time", () => {
      const now = Math.floor(Date.now() / 1000);
      const lastSync = now - 86400; // 1 day ago
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync,
        syncStatus: "success",
        syncError: null,
        rowCount: 100,
        createdAt: now,
        updatedAt: now,
      };

      const nextSync = RemoteSyncService.getNextSyncTime(source, 7);
      const expectedNextSync = lastSync + 7 * 86400;

      expect(nextSync).toBe(expectedNextSync);
    });
  });

  describe("getSyncStatus", () => {
    it("should return sync status", () => {
      const now = Math.floor(Date.now() / 1000);
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync: now - 86400,
        syncStatus: "success",
        syncError: null,
        rowCount: 100,
        createdAt: now,
        updatedAt: now,
      };

      const status = RemoteSyncService.getSyncStatus(source, 7);

      expect(status.sourceId).toBe(1);
      expect(status.status).toBe("success");
      expect(status.lastSyncTime).toBe(now - 86400);
    });

    it("should include error message if sync failed", () => {
      const now = Math.floor(Date.now() / 1000);
      const source: DataSource = {
        id: 1,
        name: "Test",
        type: "remote_url",
        sourceUrl: "https://example.com/list.csv",
        enabled: 1,
        lastSync: now - 86400,
        syncStatus: "error",
        syncError: "Network timeout",
        rowCount: 0,
        createdAt: now,
        updatedAt: now,
      };

      const status = RemoteSyncService.getSyncStatus(source, 7);

      expect(status.errorMessage).toBe("Network timeout");
    });
  });

  describe("formatSyncError", () => {
    it("should format HTTP errors", () => {
      const formatted = RemoteSyncService.formatSyncError("HTTP 404: Not Found");

      expect(formatted).toContain("Network error");
    });

    it("should format timeout errors", () => {
      const formatted = RemoteSyncService.formatSyncError("Request timeout");

      expect(formatted).toContain("timed out");
    });

    it("should format invalid format errors", () => {
      const formatted = RemoteSyncService.formatSyncError("Invalid file format");

      expect(formatted).toContain("Invalid file format");
    });

    it("should format fetch errors", () => {
      const formatted = RemoteSyncService.formatSyncError("fetch failed");

      expect(formatted).toContain("Network error");
    });
  });

  describe("validateConfig", () => {
    it("should accept valid config", () => {
      const result = RemoteSyncService.validateConfig({
        intervalDays: 7,
        maxRetries: 3,
        timeoutMs: 30000,
      });

      expect(result.valid).toBe(true);
    });

    it("should reject invalid interval", () => {
      const result = RemoteSyncService.validateConfig({
        intervalDays: 0,
      });

      expect(result.valid).toBe(false);
      expect(result.error).toContain("interval");
    });

    it("should reject invalid max retries", () => {
      const result = RemoteSyncService.validateConfig({
        maxRetries: 0,
      });

      expect(result.valid).toBe(false);
      expect(result.error).toContain("retries");
    });

    it("should reject invalid timeout", () => {
      const result = RemoteSyncService.validateConfig({
        timeoutMs: 500,
      });

      expect(result.valid).toBe(false);
      expect(result.error).toContain("Timeout");
    });
  });

  describe("generateSyncSummary", () => {
    it("should generate sync summary", () => {
      const results = [
        {
          sourceId: 1,
          success: true,
          entriesAdded: 100,
          entriesUpdated: 0,
          entriesRemoved: 0,
          syncDuration: 1000,
        },
        {
          sourceId: 2,
          success: false,
          entriesAdded: 0,
          entriesUpdated: 0,
          entriesRemoved: 0,
          syncDuration: 500,
          errorMessage: "Network error",
        },
      ];

      const summary = RemoteSyncService.generateSyncSummary(results as any);

      expect(summary).toContain("Successful: 1");
      expect(summary).toContain("Failed: 1");
      expect(summary).toContain("Total Entries: 100");
    });
  });
});

describe("Background Sync Manager", () => {
  describe("scheduleSync", () => {
    it("should schedule background sync", async () => {
      const result = await BackgroundSyncManager.scheduleSync(7);

      expect(result.scheduled).toBe(true);
      expect(result.nextSyncTime).toBeGreaterThan(0);
    });

    it("should reject invalid config", async () => {
      try {
        await BackgroundSyncManager.scheduleSync(7, { intervalDays: 0 });
        expect.fail("Should have thrown error");
      } catch (error) {
        expect(error).toBeDefined();
      }
    });
  });

  describe("cancelSync", () => {
    it("should cancel background sync", async () => {
      const result = await BackgroundSyncManager.cancelSync();

      expect(result.cancelled).toBe(true);
    });
  });

  describe("getSyncSchedule", () => {
    it("should get sync schedule", async () => {
      const result = await BackgroundSyncManager.getSyncSchedule();

      expect(result).toHaveProperty("scheduled");
    });
  });
});
