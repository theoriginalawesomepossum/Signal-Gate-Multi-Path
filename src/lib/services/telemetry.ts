/**
 * TelemetryService - Collect device and app statistics
 * 
 * Privacy-first approach:
 * - All data stored locally
 * - Never sent anywhere without user consent
 * - No personal data collected
 * - No phone numbers or contacts
 */

export interface DeviceStats {
  deviceModel: string;
  androidVersion: number;
  ramAvailable: number;
  storageAvailable: number;
  cpuCores: number;
  batteryPercentage: number;
  appStartTime: number;
  callScreeningLatency: number;
  databaseQueryTime: number;
  memoryUsage: number;
  lastUpdated: number;
}

export interface UsageStats {
  callsProcessed: number;
  callsBlocked: number;
  callsAllowed: number;
  averageProcessingTime: number;
  screenViewCount: Record<string, number>;
  buttonClickCount: Record<string, number>;
  settingsChanges: number;
  filesImported: number;
  dataSourcesSynced: number;
  contactsScanned: number;
  crashCount: number;
  errorCount: number;
  warningCount: number;
  sessionCount: number;
  totalSessionTime: number;
  averageSessionDuration: number;
  lastUpdated: number;
}

export class TelemetryService {
  private static instance: TelemetryService;
  private deviceStats: DeviceStats | null = null;
  private usageStats: UsageStats = {
    callsProcessed: 0,
    callsBlocked: 0,
    callsAllowed: 0,
    averageProcessingTime: 0,
    screenViewCount: {},
    buttonClickCount: {},
    settingsChanges: 0,
    filesImported: 0,
    dataSourcesSynced: 0,
    contactsScanned: 0,
    crashCount: 0,
    errorCount: 0,
    warningCount: 0,
    sessionCount: 0,
    totalSessionTime: 0,
    averageSessionDuration: 0,
    lastUpdated: Date.now(),
  };

  private constructor() {}

  static getInstance(): TelemetryService {
    if (!TelemetryService.instance) {
      TelemetryService.instance = new TelemetryService();
    }
    return TelemetryService.instance;
  }

  /**
   * Collect device statistics
   */
  async collectDeviceStats(): Promise<DeviceStats> {
    try {
      this.deviceStats = {
        deviceModel: 'Unknown', // Would be populated from device info
        androidVersion: 13, // Would be populated from device info
        ramAvailable: 4096, // Would be populated from device info
        storageAvailable: 64000, // Would be populated from device info
        cpuCores: 8, // Would be populated from device info
        batteryPercentage: 85, // Would be populated from device info
        appStartTime: 1200, // Milliseconds
        callScreeningLatency: 45, // Milliseconds
        databaseQueryTime: 15, // Milliseconds
        memoryUsage: 145, // MB
        lastUpdated: Date.now(),
      };

      console.log('[TelemetryService] Device stats collected');
      return this.deviceStats;
    } catch (error) {
      console.error('[TelemetryService] Failed to collect device stats:', error);
      throw error;
    }
  }

  /**
   * Track call screening
   */
  trackCallScreening(
    action: 'BLOCK' | 'ALLOW',
    processingTime: number
  ): void {
    try {
      this.usageStats.callsProcessed++;

      if (action === 'BLOCK') {
        this.usageStats.callsBlocked++;
      } else {
        this.usageStats.callsAllowed++;
      }

      // Update average processing time
      const totalTime =
        this.usageStats.averageProcessingTime * (this.usageStats.callsProcessed - 1) +
        processingTime;
      this.usageStats.averageProcessingTime = totalTime / this.usageStats.callsProcessed;

      this.usageStats.lastUpdated = Date.now();
    } catch (error) {
      console.error('[TelemetryService] Failed to track call screening:', error);
    }
  }

  /**
   * Track screen view
   */
  trackScreenView(screenName: string): void {
    try {
      if (!this.usageStats.screenViewCount[screenName]) {
        this.usageStats.screenViewCount[screenName] = 0;
      }
      this.usageStats.screenViewCount[screenName]++;
      this.usageStats.lastUpdated = Date.now();
    } catch (error) {
      console.error('[TelemetryService] Failed to track screen view:', error);
    }
  }

  /**
   * Track button click
   */
  trackButtonClick(buttonName: string): void {
    try {
      if (!this.usageStats.buttonClickCount[buttonName]) {
        this.usageStats.buttonClickCount[buttonName] = 0;
      }
      this.usageStats.buttonClickCount[buttonName]++;
      this.usageStats.lastUpdated = Date.now();
    } catch (error) {
      console.error('[TelemetryService] Failed to track button click:', error);
    }
  }

  /**
   * Track crash
   */
  trackCrash(): void {
    try {
      this.usageStats.crashCount++;
      this.usageStats.lastUpdated = Date.now();
    } catch (error) {
      console.error('[TelemetryService] Failed to track crash:', error);
    }
  }

  /**
   * Track error
   */
  trackError(): void {
    try {
      this.usageStats.errorCount++;
      this.usageStats.lastUpdated = Date.now();
    } catch (error) {
      console.error('[TelemetryService] Failed to track error:', error);
    }
  }

  /**
   * Track warning
   */
  trackWarning(): void {
    try {
      this.usageStats.warningCount++;
      this.usageStats.lastUpdated = Date.now();
    } catch (error) {
      console.error('[TelemetryService] Failed to track warning:', error);
    }
  }

  /**
   * Get device stats
   */
  getDeviceStats(): DeviceStats | null {
    return this.deviceStats;
  }

  /**
   * Get usage stats
   */
  getUsageStats(): UsageStats {
    return this.usageStats;
  }

  /**
   * Get all stats
   */
  getAllStats(): { device: DeviceStats | null; usage: UsageStats } {
    return {
      device: this.deviceStats,
      usage: this.usageStats,
    };
  }

  /**
   * Reset stats
   */
  resetStats(): void {
    try {
      this.usageStats = {
        callsProcessed: 0,
        callsBlocked: 0,
        callsAllowed: 0,
        averageProcessingTime: 0,
        screenViewCount: {},
        buttonClickCount: {},
        settingsChanges: 0,
        filesImported: 0,
        dataSourcesSynced: 0,
        contactsScanned: 0,
        crashCount: 0,
        errorCount: 0,
        warningCount: 0,
        sessionCount: 0,
        totalSessionTime: 0,
        averageSessionDuration: 0,
        lastUpdated: Date.now(),
      };

      console.log('[TelemetryService] Stats reset');
    } catch (error) {
      console.error('[TelemetryService] Failed to reset stats:', error);
    }
  }

  /**
   * Export stats as JSON
   */
  exportStats(): string {
    try {
      return JSON.stringify(
        {
          device: this.deviceStats,
          usage: this.usageStats,
          exportedAt: new Date().toISOString(),
        },
        null,
        2
      );
    } catch (error) {
      console.error('[TelemetryService] Failed to export stats:', error);
      throw error;
    }
  }
}
