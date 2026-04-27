import { describe, it, expect, beforeEach } from "vitest";
import { AdvancedSettingsService } from "./advanced-settings";

describe("Advanced Settings Service", () => {
  beforeEach(() => {
    AdvancedSettingsService.reset();
  });

  describe("getSettings / updateSettings", () => {
    it("should get default settings", () => {
      const settings = AdvancedSettingsService.getSettings();

      expect(settings.performanceTier).toBe("CENTER_POINT");
      expect(settings.syncIntervalDays).toBe(7);
      expect(settings.enableAutoSync).toBe(true);
      expect(settings.enableCallScreening).toBe(true);
      expect(settings.multiPointEnabled).toBe(true);
    });

    it("should update settings", () => {
      AdvancedSettingsService.updateSettings({
        syncIntervalDays: 14,
        enableAutoSync: false,
      });

      const settings = AdvancedSettingsService.getSettings();

      expect(settings.syncIntervalDays).toBe(14);
      expect(settings.enableAutoSync).toBe(false);
    });
  });

  describe("setPerformanceTier", () => {
    it("should set performance tier to FPP", () => {
      const result = AdvancedSettingsService.setPerformanceTier("FPP");

      expect(result).toBe(true);
      expect(AdvancedSettingsService.getSettings().performanceTier).toBe("FPP");
      expect(AdvancedSettingsService.getSettings().maxDatabaseRows).toBe(10000);
    });

    it("should set performance tier to FULL_THROTTLE", () => {
      const result = AdvancedSettingsService.setPerformanceTier("FULL_THROTTLE");

      expect(result).toBe(true);
      expect(AdvancedSettingsService.getSettings().performanceTier).toBe("FULL_THROTTLE");
      expect(AdvancedSettingsService.getSettings().maxDatabaseRows).toBe(1000000);
    });

    it("should reject invalid tier", () => {
      const result = AdvancedSettingsService.setPerformanceTier("INVALID" as any);

      expect(result).toBe(false);
    });
  });

  describe("getPerformanceTierConfig", () => {
    it("should return tier configuration", () => {
      const config = AdvancedSettingsService.getPerformanceTierConfig("CENTER_POINT");

      expect(config).toBeDefined();
      expect(config?.maxRows).toBe(100000);
      expect(config?.minRAMGB).toBe(4);
    });

    it("should return null for invalid tier", () => {
      const config = AdvancedSettingsService.getPerformanceTierConfig("INVALID" as any);

      expect(config).toBeNull();
    });
  });

  describe("getAllPerformanceTiers", () => {
    it("should return all tier configurations", () => {
      const tiers = AdvancedSettingsService.getAllPerformanceTiers();

      expect(tiers.length).toBe(3);
      expect(tiers.map((t) => t.tier)).toContain("FPP");
      expect(tiers.map((t) => t.tier)).toContain("CENTER_POINT");
      expect(tiers.map((t) => t.tier)).toContain("FULL_THROTTLE");
    });
  });

  describe("canHandleTier", () => {
    it("should return true for sufficient RAM", () => {
      expect(AdvancedSettingsService.canHandleTier("FPP", 2)).toBe(true);
      expect(AdvancedSettingsService.canHandleTier("CENTER_POINT", 4)).toBe(true);
      expect(AdvancedSettingsService.canHandleTier("FULL_THROTTLE", 6)).toBe(true);
    });

    it("should return false for insufficient RAM", () => {
      expect(AdvancedSettingsService.canHandleTier("FULL_THROTTLE", 2)).toBe(false);
      expect(AdvancedSettingsService.canHandleTier("CENTER_POINT", 2)).toBe(false);
    });
  });

  describe("getRecommendedTier", () => {
    it("should recommend FPP for low RAM", () => {
      expect(AdvancedSettingsService.getRecommendedTier(1)).toBe("FPP");
      expect(AdvancedSettingsService.getRecommendedTier(2)).toBe("FPP");
    });

    it("should recommend CENTER_POINT for medium RAM", () => {
      expect(AdvancedSettingsService.getRecommendedTier(4)).toBe("CENTER_POINT");
      expect(AdvancedSettingsService.getRecommendedTier(5)).toBe("CENTER_POINT");
    });

    it("should recommend FULL_THROTTLE for high RAM", () => {
      expect(AdvancedSettingsService.getRecommendedTier(6)).toBe("FULL_THROTTLE");
      expect(AdvancedSettingsService.getRecommendedTier(8)).toBe("FULL_THROTTLE");
    });
  });

  describe("setSyncInterval", () => {
    it("should set valid sync interval", () => {
      const result = AdvancedSettingsService.setSyncInterval(14);

      expect(result).toBe(true);
      expect(AdvancedSettingsService.getSettings().syncIntervalDays).toBe(14);
    });

    it("should reject invalid intervals", () => {
      expect(AdvancedSettingsService.setSyncInterval(0)).toBe(false);
      expect(AdvancedSettingsService.setSyncInterval(366)).toBe(false);
      expect(AdvancedSettingsService.setSyncInterval(-1)).toBe(false);
    });
  });

  describe("toggle functions", () => {
    it("should toggle auto sync", () => {
      AdvancedSettingsService.toggleAutoSync(false);
      expect(AdvancedSettingsService.getSettings().enableAutoSync).toBe(false);

      AdvancedSettingsService.toggleAutoSync(true);
      expect(AdvancedSettingsService.getSettings().enableAutoSync).toBe(true);
    });

    it("should toggle call screening", () => {
      AdvancedSettingsService.toggleCallScreening(false);
      expect(AdvancedSettingsService.getSettings().enableCallScreening).toBe(false);
    });

    it("should toggle notifications", () => {
      AdvancedSettingsService.toggleNotifications(false);
      expect(AdvancedSettingsService.getSettings().enableNotifications).toBe(false);
    });

    it("should toggle error logging", () => {
      AdvancedSettingsService.toggleErrorLogging(false);
      expect(AdvancedSettingsService.getSettings().enableErrorLogs).toBe(false);
    });

    it("should toggle multipoint", () => {
      AdvancedSettingsService.toggleMultiPoint(false);
      expect(AdvancedSettingsService.getSettings().multiPointEnabled).toBe(false);
    });
  });

  describe("error logging", () => {
    it("should log info messages", () => {
      AdvancedSettingsService.logInfo("Test info", "TestSource");

      const logs = AdvancedSettingsService.getErrorLogs();

      expect(logs.length).toBe(1);
      expect(logs[0].level).toBe("INFO");
      expect(logs[0].message).toBe("Test info");
      expect(logs[0].source).toBe("TestSource");
    });

    it("should log warning messages", () => {
      AdvancedSettingsService.logWarning("Test warning", "TestSource");

      const logs = AdvancedSettingsService.getErrorLogsByLevel("WARNING");

      expect(logs.length).toBe(1);
      expect(logs[0].level).toBe("WARNING");
    });

    it("should log error messages", () => {
      AdvancedSettingsService.logError("Test error", "TestSource", "Error details");

      const logs = AdvancedSettingsService.getErrorLogsByLevel("ERROR");

      expect(logs.length).toBe(1);
      expect(logs[0].level).toBe("ERROR");
      expect(logs[0].details).toBe("Error details");
    });

    it("should not log when error logging is disabled", () => {
      AdvancedSettingsService.toggleErrorLogging(false);
      AdvancedSettingsService.logInfo("Test", "Source");

      const logs = AdvancedSettingsService.getErrorLogs();

      expect(logs.length).toBe(0);
    });

    it("should filter logs by level", () => {
      AdvancedSettingsService.logInfo("Info 1", "Source");
      AdvancedSettingsService.logWarning("Warning 1", "Source");
      AdvancedSettingsService.logError("Error 1", "Source");

      const infoLogs = AdvancedSettingsService.getErrorLogsByLevel("INFO");
      const warningLogs = AdvancedSettingsService.getErrorLogsByLevel("WARNING");
      const errorLogs = AdvancedSettingsService.getErrorLogsByLevel("ERROR");

      expect(infoLogs.length).toBe(1);
      expect(warningLogs.length).toBe(1);
      expect(errorLogs.length).toBe(1);
    });

    it("should clear error logs", () => {
      AdvancedSettingsService.logInfo("Test 1", "Source");
      AdvancedSettingsService.logInfo("Test 2", "Source");

      const cleared = AdvancedSettingsService.clearErrorLogs();

      expect(cleared).toBe(2);
      expect(AdvancedSettingsService.getErrorLogs().length).toBe(0);
    });
  });

  describe("getDiagnosticInfo", () => {
    it("should return diagnostic information", () => {
      AdvancedSettingsService.logInfo("Test sync", "Source");

      const diag = AdvancedSettingsService.getDiagnosticInfo(100, 50, 25, 1000);

      expect(diag.appVersion).toBe("1.0.0");
      expect(diag.performanceTier).toBe("CENTER_POINT");
      expect(diag.totalBlockListEntries).toBe(100);
      expect(diag.totalAllowListEntries).toBe(50);
      expect(diag.totalPatternRules).toBe(25);
      expect(diag.totalCallLogEntries).toBe(1000);
      expect(diag.errorLogCount).toBeGreaterThan(0);
    });
  });

  describe("exportErrorLogsAsCSV", () => {
    it("should export error logs as CSV", () => {
      AdvancedSettingsService.logInfo("Test info", "Source", "Details");
      AdvancedSettingsService.logError("Test error", "Source");

      const csv = AdvancedSettingsService.exportErrorLogsAsCSV();

      expect(csv).toContain("Timestamp");
      expect(csv).toContain("Level");
      expect(csv).toContain("INFO");
      expect(csv).toContain("ERROR");
      expect(csv).toContain("Test info");
    });
  });

  describe("resetToDefaults", () => {
    it("should reset all settings to defaults", () => {
      AdvancedSettingsService.setPerformanceTier("FPP");
      AdvancedSettingsService.setSyncInterval(14);
      AdvancedSettingsService.toggleAutoSync(false);

      const reset = AdvancedSettingsService.resetToDefaults();

      expect(reset.performanceTier).toBe("CENTER_POINT");
      expect(reset.syncIntervalDays).toBe(7);
      expect(reset.enableAutoSync).toBe(true);
    });
  });

  describe("exportSettings / importSettings", () => {
    it("should export and import settings", () => {
      AdvancedSettingsService.setPerformanceTier("FPP");
      AdvancedSettingsService.setSyncInterval(14);

      const exported = AdvancedSettingsService.exportSettings();
      AdvancedSettingsService.resetToDefaults();

      const imported = AdvancedSettingsService.importSettings(exported);

      expect(imported).toBe(true);
      expect(AdvancedSettingsService.getSettings().performanceTier).toBe("FPP");
      expect(AdvancedSettingsService.getSettings().syncIntervalDays).toBe(14);
    });

    it("should handle invalid JSON import", () => {
      const imported = AdvancedSettingsService.importSettings("invalid json");

      expect(imported).toBe(false);
    });
  });

  describe("disableMultiPointWarning", () => {
    it("should disable multipoint warning", () => {
      expect(AdvancedSettingsService.getSettings().disableMultiPointWarning).toBe(false);

      AdvancedSettingsService.disableMultiPointWarning();

      expect(AdvancedSettingsService.getSettings().disableMultiPointWarning).toBe(true);
    });
  });
});
