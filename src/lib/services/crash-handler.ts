import { Database } from '../db/schema';

/**
 * CrashReport - Structure for storing crash information
 */
export interface CrashReport {
  id: string;
  timestamp: number;
  errorMessage: string;
  stack: string;
  componentStack?: string;
  deviceInfo: {
    model: string;
    androidVersion: number;
    appVersion: string;
  };
  appState: {
    currentScreen: string;
    isCallScreening: boolean;
    databaseState: 'healthy' | 'corrupted' | 'locked';
  };
  severity: 'critical' | 'high' | 'medium' | 'low';
  resolved: boolean;
  resolutionMethod?: string;
  userAction?: string;
}

/**
 * CrashHandler - Handles crashes and provides recovery mechanisms
 * 
 * Features:
 * - Capture crash information
 * - Attempt automatic recovery
 * - Store crash reports locally
 * - Provide recovery options to user
 */
export class CrashHandler {
  private static instance: CrashHandler;
  private crashLog: CrashReport[] = [];
  private isScreeningActive = false;
  private db: Database | null = null;

  private constructor() {}

  static getInstance(): CrashHandler {
    if (!CrashHandler.instance) {
      CrashHandler.instance = new CrashHandler();
    }
    return CrashHandler.instance;
  }

  setDatabase(db: Database): void {
    this.db = db;
  }

  /**
   * Handle a crash with automatic recovery attempt
   */
  async handleCrash(error: Error, context?: any): Promise<CrashReport> {
    console.error('[CrashHandler] Crash detected:', error);

    const report: CrashReport = {
      id: this.generateUUID(),
      timestamp: Date.now(),
      errorMessage: error.message,
      stack: error.stack || '',
      componentStack: context?.componentStack,
      deviceInfo: {
        model: 'Unknown', // Would be populated from device info
        androidVersion: 13, // Would be populated from device info
        appVersion: '1.0.0', // Would be populated from package.json
      },
      appState: {
        currentScreen: context?.screen || 'Unknown',
        isCallScreening: this.isScreeningActive,
        databaseState: 'healthy',
      },
      severity: this.calculateSeverity(error),
      resolved: false,
    };

    // Save crash report
    await this.saveCrashReport(report);

    // Attempt recovery
    await this.attemptRecovery(error, context);

    return report;
  }

  /**
   * Attempt to recover from specific error types
   */
  private async attemptRecovery(error: Error, context?: any): Promise<void> {
    try {
      const errorName = error.constructor.name;

      if (errorName === 'CallScreeningError') {
        console.warn('[CrashHandler] Recovering from CallScreeningError');
        this.isScreeningActive = false;
        // Reset call screening service
      } else if (errorName === 'DatabaseError') {
        console.warn('[CrashHandler] Recovering from DatabaseError');
        // Attempt database repair
        if (this.db) {
          try {
            await this.db.execute('VACUUM');
            await this.db.execute('REINDEX');
          } catch (e) {
            console.error('[CrashHandler] Database repair failed:', e);
          }
        }
      } else if (errorName === 'PermissionError') {
        console.warn('[CrashHandler] Recovering from PermissionError');
        // Request permissions again
      } else {
        console.warn('[CrashHandler] Unknown error type, allowing graceful degradation');
      }
    } catch (recoveryError) {
      console.error('[CrashHandler] Recovery failed:', recoveryError);
    }
  }

  /**
   * Calculate error severity
   */
  private calculateSeverity(
    error: Error
  ): 'critical' | 'high' | 'medium' | 'low' {
    const message = error.message.toLowerCase();

    if (
      message.includes('database') ||
      message.includes('permission') ||
      message.includes('native')
    ) {
      return 'critical';
    }

    if (message.includes('timeout') || message.includes('network')) {
      return 'high';
    }

    if (message.includes('warning')) {
      return 'medium';
    }

    return 'low';
  }

  /**
   * Save crash report to local database
   */
  private async saveCrashReport(report: CrashReport): Promise<void> {
    try {
      this.crashLog.push(report);

      // In production, save to database
      if (this.db) {
        // await this.db.insert(crashReportsTable).values(report);
      }

      // Keep only last 100 crashes in memory
      if (this.crashLog.length > 100) {
        this.crashLog = this.crashLog.slice(-100);
      }

      console.log('[CrashHandler] Crash report saved:', report.id);
    } catch (error) {
      console.error('[CrashHandler] Failed to save crash report:', error);
    }
  }

  /**
   * Get all crash reports
   */
  async getCrashReports(): Promise<CrashReport[]> {
    return this.crashLog;
  }

  /**
   * Get crash reports by severity
   */
  async getCrashReportsBySeverity(
    severity: string
  ): Promise<CrashReport[]> {
    return this.crashLog.filter((report) => report.severity === severity);
  }

  /**
   * Get crash reports by date range
   */
  async getCrashReportsByDateRange(
    startTime: number,
    endTime: number
  ): Promise<CrashReport[]> {
    return this.crashLog.filter(
      (report) => report.timestamp >= startTime && report.timestamp <= endTime
    );
  }

  /**
   * Clear all crash reports
   */
  async clearCrashReports(): Promise<void> {
    this.crashLog = [];
    console.log('[CrashHandler] Crash reports cleared');
  }

  /**
   * Export crash reports as JSON
   */
  async exportCrashReports(): Promise<string> {
    return JSON.stringify(this.crashLog, null, 2);
  }

  /**
   * Mark crash as resolved
   */
  async markCrashResolved(
    crashId: string,
    resolutionMethod: string
  ): Promise<void> {
    const report = this.crashLog.find((r) => r.id === crashId);
    if (report) {
      report.resolved = true;
      report.resolutionMethod = resolutionMethod;
    }
  }

  /**
   * Set screening active state (for tracking)
   */
  setScreeningActive(active: boolean): void {
    this.isScreeningActive = active;
  }

  /**
   * Generate UUID for crash report
   */
  private generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }
}

/**
 * Global error handler for uncaught exceptions
 */
export function setupGlobalErrorHandler(): void {
  const handler = CrashHandler.getInstance();

  // Handle uncaught exceptions
  if (global.ErrorUtils) {
    global.ErrorUtils.setGlobalHandler((error: Error, isFatal: boolean) => {
      console.error('[GlobalErrorHandler] Uncaught error:', error, 'isFatal:', isFatal);
      handler.handleCrash(error, { isFatal });
    });
  }
}
