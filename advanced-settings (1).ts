/**
 * Advanced Settings Service
 * 
 * Manages app configuration, performance tiers, sync scheduling,
 * error logging, and diagnostic information.
 */

export type PerformanceTier = "FPP" | "CENTER_POINT" | "FULL_THROTTLE";

export interface AppSettings {
  performanceTier: PerformanceTier;
  syncIntervalDays: number;
  enableAutoSync: boolean;
  enableCallScreening: boolean;
  enableNotifications: boolean;
  enableErrorLogs: boolean;
  maxDatabaseRows: number;
  multiPointEnabled: boolean;
  disableMultiPointWarning: boolean;
}

export interface PerformanceTierConfig {
  tier: PerformanceTier;
  maxRows: number;
  description: string;
  minRAMGB: number;
  estimatedBatteryDrainPercent: number;
}

export interface ErrorLog {
  id: number;
  timestamp: number;
  level: "INFO" | "WARNING" | "ERROR";
  message: string;
  source: string;
  details?: string;
}

export interface DiagnosticInfo {
  appVersion: string;
  timestamp: number;
  performanceTier: PerformanceTier;
  totalBlockListEntries: number;
  totalAllowListEntries: number;
  totalPatternRules: number;
  totalCallLogEntries: number;
  estimatedDatabaseSizeMB: number;
  errorLogCount: number;
  lastSyncTime?: number;
  lastErrorTime?: number;
}

/**
 * Advanced Settings Service
 */
export class AdvancedSettingsService {
  private static settings: AppSettings = {
    performanceTier: "CENTER_POINT",
    syncIntervalDays: 7,
    enableAutoSync: true,
    enableCallScreening: true,
    enableNotifications: true,
    enableErrorLogs: true,
    maxDatabaseRows: 100000,
    multiPointEnabled: true,
    disableMultiPointWarning: false,
  };

  private static errorLogs: ErrorLog[] = [];
  private static nextErrorLogId = 1;

  // Performance tier configurations
  private static readonly TIER_CONFIGS: Record<PerformanceTier, PerformanceTierConfig> = {
    FPP: {
      tier: "FPP",
      maxRows: 10000,
      description: "Fewest Points Possible - Minimal data, maximum battery life",
      minRAMGB: 2,
      estimatedBatteryDrainPercent: 1,
    },
    CENTER_POINT: {
      tier: "CENTER_POINT",
      maxRows: 100000,
      description: "Balanced - Good coverage with reasonable performance",
      minRAMGB: 4,
      estimatedBatteryDrainPercent: 2,
    },
    FULL_THROTTLE: {
      tier: "FULL_THROTTLE",
      maxRows: 1000000,
      description: "Full Power - Maximum coverage and features",
      minRAMGB: 6,
      estimatedBatteryDrainPercent: 5,
    },
  };

  /**
   * Get current settings
   */
  static getSettings(): AppSettings {
    return { ...this.settings };
  }

  /**
   * Update settings
   */
  static updateSettings(updates: Partial<AppSettings>): AppSettings {
    this.settings = {
      ...this.settings,
      ...updates,
    };

    this.logInfo("Settings updated", "AdvancedSettings", JSON.stringify(updates));

    return { ...this.settings };
  }

  /**
   * Set performance tier
   */
  static setPerformanceTier(tier: PerformanceTier): boolean {
    if (!this.TIER_CONFIGS[tier]) {
      this.logError("Invalid performance tier", "AdvancedSettings", tier);
      return false;
    }

    const config = this.TIER_CONFIGS[tier];
    this.settings.performanceTier = tier;
    this.settings.maxDatabaseRows = config.maxRows;

    this.logInfo(`Performance tier changed to ${tier}`, "AdvancedSettings");

    return true;
  }

  /**
   * Get performance tier configuration
   */
  static getPerformanceTierConfig(tier: PerformanceTier): PerformanceTierConfig | null {
    return this.TIER_CONFIGS[tier] || null;
  }

  /**
   * Get all performance tier configurations
   */
  static getAllPerformanceTiers(): PerformanceTierConfig[] {
    return Object.values(this.TIER_CONFIGS);
  }

  /**
   * Check if device can handle performance tier
   */
  static canHandleTier(tier: PerformanceTier, deviceRAMGB: number): boolean {
    const config = this.TIER_CONFIGS[tier];
    if (!config) return false;

    return deviceRAMGB >= config.minRAMGB;
  }

  /**
   * Get recommended tier for device
   */
  static getRecommendedTier(deviceRAMGB: number): PerformanceTier {
    if (deviceRAMGB >= 6) return "FULL_THROTTLE";
    if (deviceRAMGB >= 4) return "CENTER_POINT";
    return "FPP";
  }

  /**
   * Set sync interval
   */
  static setSyncInterval(days: number): boolean {
    if (days < 1 || days > 365) {
      this.logError("Invalid sync interval", "AdvancedSettings", `${days} days`);
      return false;
    }

    this.settings.syncIntervalDays = days;
    this.logInfo(`Sync interval changed to ${days} days`, "AdvancedSettings");

    return true;
  }

  /**
   * Toggle auto sync
   */
  static toggleAutoSync(enabled: boolean): void {
    this.settings.enableAutoSync = enabled;
    this.logInfo(`Auto sync ${enabled ? "enabled" : "disabled"}`, "AdvancedSettings");
  }

  /**
   * Toggle call screening
   */
  static toggleCallScreening(enabled: boolean): void {
    this.settings.enableCallScreening = enabled;
    this.logInfo(`Call screening ${enabled ? "enabled" : "disabled"}`, "AdvancedSettings");
  }

  /**
   * Toggle notifications
   */
  static toggleNotifications(enabled: boolean): void {
    this.settings.enableNotifications = enabled;
    this.logInfo(`Notifications ${enabled ? "enabled" : "disabled"}`, "AdvancedSettings");
  }

  /**
   * Toggle error logging
   */
  static toggleErrorLogging(enabled: boolean): void {
    this.settings.enableErrorLogs = enabled;
    this.logInfo(`Error logging ${enabled ? "enabled" : "disabled"}`, "AdvancedSettings");
  }

  /**
   * Toggle multipoint
   */
  static toggleMultiPoint(enabled: boolean): void {
    this.settings.multiPointEnabled = enabled;
    this.logInfo(`MultiPoint ${enabled ? "enabled" : "disabled"}`, "AdvancedSettings");
  }

  /**
   * Disable multipoint warning
   */
  static disableMultiPointWarning(): void {
    this.settings.disableMultiPointWarning = true;
    this.logInfo("MultiPoint warning disabled", "AdvancedSettings");
  }

  /**
   * Log info message
   */
  static logInfo(message: string, source: string, details?: string): void {
    if (this.settings.enableErrorLogs) {
      this.addErrorLog("INFO", message, source, details);
    }
  }

  /**
   * Log warning message
   */
  static logWarning(message: string, source: string, details?: string): void {
    if (this.settings.enableErrorLogs) {
      this.addErrorLog("WARNING", message, source, details);
    }
  }

  /**
   * Log error message
   */
  static logError(message: string, source: string, details?: string): void {
    if (this.settings.enableErrorLogs) {
      this.addErrorLog("ERROR", message, source, details);
    }
  }

  /**
   * Internal add error log
   */
  private static addErrorLog(
    level: "INFO" | "WARNING" | "ERROR",
    message: string,
    source: string,
    details?: string
  ): void {
    const log: ErrorLog = {
      id: this.nextErrorLogId++,
      timestamp: Math.floor(Date.now() / 1000),
      level,
      message,
      source,
      details,
    };

    this.errorLogs.unshift(log); // Add to beginning

    // Keep only last 1000 logs
    if (this.errorLogs.length > 1000) {
      this.errorLogs = this.errorLogs.slice(0, 1000);
    }
  }

  /**
   * Get error logs
   */
  static getErrorLogs(limit: number = 100): ErrorLog[] {
    return this.errorLogs.slice(0, limit);
  }

  /**
   * Get error logs by level
   */
  static getErrorLogsByLevel(level: "INFO" | "WARNING" | "ERROR", limit: number = 100): ErrorLog[] {
    return this.errorLogs.filter((log) => log.level === level).slice(0, limit);
  }

  /**
   * Clear error logs
   */
  static clearErrorLogs(): number {
    const count = this.errorLogs.length;
    this.errorLogs = [];
    return count;
  }

  /**
   * Export error logs as CSV
   */
  static exportErrorLogsAsCSV(): string {
    const headers = ["Timestamp", "Level", "Message", "Source", "Details"];
    const rows = this.errorLogs.map((log) => [
      new Date(log.timestamp * 1000).toISOString(),
      log.level,
      log.message,
      log.source,
      log.details || "-",
    ]);

    const csv = [headers, ...rows].map((row) => row.map((cell) => `"${cell}"`).join(",")).join("\n");

    return csv;
  }

  /**
   * Get diagnostic information
   */
  static getDiagnosticInfo(
    totalBlockListEntries: number,
    totalAllowListEntries: number,
    totalPatternRules: number,
    totalCallLogEntries: number
  ): DiagnosticInfo {
    const estimatedBytesPerEntry = 100; // Rough estimate
    const totalEntries = totalBlockListEntries + totalAllowListEntries + totalPatternRules + totalCallLogEntries;
    const estimatedDatabaseSizeMB = (totalEntries * estimatedBytesPerEntry) / (1024 * 1024);

    const lastError = this.errorLogs.find((log) => log.level === "ERROR");
    const lastSync = this.errorLogs.find(
      (log) => log.message.includes("sync") || log.message.includes("Sync")
    );

    return {
      appVersion: "1.0.0",
      timestamp: Math.floor(Date.now() / 1000),
      performanceTier: this.settings.performanceTier,
      totalBlockListEntries,
      totalAllowListEntries,
      totalPatternRules,
      totalCallLogEntries,
      estimatedDatabaseSizeMB,
      errorLogCount: this.errorLogs.length,
      lastSyncTime: lastSync?.timestamp,
      lastErrorTime: lastError?.timestamp,
    };
  }

  /**
   * Reset to defaults
   */
  static resetToDefaults(): AppSettings {
    this.settings = {
      performanceTier: "CENTER_POINT",
      syncIntervalDays: 7,
      enableAutoSync: true,
      enableCallScreening: true,
      enableNotifications: true,
      enableErrorLogs: true,
      maxDatabaseRows: 100000,
      multiPointEnabled: true,
      disableMultiPointWarning: false,
    };

    this.logInfo("Settings reset to defaults", "AdvancedSettings");

    return { ...this.settings };
  }

  /**
   * Export settings as JSON
   */
  static exportSettings(): string {
    return JSON.stringify(this.settings, null, 2);
  }

  /**
   * Import settings from JSON
   */
  static importSettings(json: string): boolean {
    try {
      const imported = JSON.parse(json);
      this.settings = {
        ...this.settings,
        ...imported,
      };
      this.logInfo("Settings imported", "AdvancedSettings");
      return true;
    } catch (error) {
      this.logError("Failed to import settings", "AdvancedSettings", error instanceof Error ? error.message : "Unknown error");
      return false;
    }
  }

  /**
   * Reset the service (for testing)
   */
  static reset(): void {
    this.settings = {
      performanceTier: "CENTER_POINT",
      syncIntervalDays: 7,
      enableAutoSync: true,
      enableCallScreening: true,
      enableNotifications: true,
      enableErrorLogs: true,
      maxDatabaseRows: 100000,
      multiPointEnabled: true,
      disableMultiPointWarning: false,
    };
    this.errorLogs = [];
    this.nextErrorLogId = 1;
  }
}
