/**
 * Remote URL Sync Service
 * 
 * Handles fetching and syncing block/allow lists from remote URLs
 * with background task scheduling, conflict resolution, and error handling.
 */

import { FileImportService } from "./file-import";
import { ConflictResolver, DeduplicationEngine, ListParser } from "./multipoint-hub";
import { DataSource, NewBlockEntry, SyncHistoryEntry } from "@/lib/db/schema";

export interface SyncConfig {
  intervalDays: number; // Sync interval in days (default: 7)
  maxRetries: number; // Max retry attempts (default: 3)
  timeoutMs: number; // Request timeout in milliseconds (default: 30000)
  onWifiOnly: boolean; // Only sync on WiFi (default: false)
  requiresCharging: boolean; // Only sync when charging (default: false)
}

export interface SyncStatus {
  sourceId: number;
  status: "pending" | "syncing" | "success" | "error";
  progress: number; // 0-100
  entriesAdded: number;
  entriesUpdated: number;
  entriesRemoved: number;
  errorMessage?: string;
  lastSyncTime?: number;
  nextSyncTime?: number;
}

export interface SyncResult {
  sourceId: number;
  success: boolean;
  entriesAdded: number;
  entriesUpdated: number;
  entriesRemoved: number;
  syncDuration: number; // milliseconds
  errorMessage?: string;
}

/**
 * Remote URL Sync Service
 */
export class RemoteSyncService {
  private static readonly DEFAULT_CONFIG: SyncConfig = {
    intervalDays: 7,
    maxRetries: 3,
    timeoutMs: 30000,
    onWifiOnly: false,
    requiresCharging: false,
  };

  /**
   * Fetch content from remote URL with retry logic
   */
  static async fetchRemoteFile(
    url: string,
    config: Partial<SyncConfig> = {}
  ): Promise<{ content: string; size: number }> {
    const finalConfig = { ...this.DEFAULT_CONFIG, ...config };
    let lastError: Error | null = null;

    for (let attempt = 1; attempt <= finalConfig.maxRetries; attempt++) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), finalConfig.timeoutMs);

        const response = await fetch(url, {
          signal: controller.signal,
          headers: {
            "User-Agent": "SignalGate-MultiPoint/1.0",
          },
        });

        clearTimeout(timeoutId);

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const content = await response.text();
        const size = new Blob([content]).size;

        return { content, size };
      } catch (error) {
        lastError = error instanceof Error ? error : new Error(String(error));

        if (attempt < finalConfig.maxRetries) {
          // Exponential backoff: 1s, 2s, 4s
          const delayMs = Math.pow(2, attempt - 1) * 1000;
          await new Promise((resolve) => setTimeout(resolve, delayMs));
        }
      }
    }

    throw lastError || new Error("Failed to fetch remote file after retries");
  }

  /**
   * Sync a single remote source
   */
  static async syncRemoteSource(
    source: DataSource,
    config: Partial<SyncConfig> = {}
  ): Promise<SyncResult> {
    const startTime = Date.now();

    try {
      if (!source.sourceUrl) {
        throw new Error("Source URL is not configured");
      }

      // Fetch remote file
      const { content, size } = await this.fetchRemoteFile(source.sourceUrl, config);

      // Validate content
      const validation = FileImportService.validateFile(content, source.name);
      if (!validation.valid) {
        throw new Error(validation.error || "Invalid file format");
      }

      // Sanitize content
      const sanitized = FileImportService.sanitizeContent(content);

      // Parse entries
      const entries = FileImportService.parseFile(sanitized, source.name, source.id);

      // De-duplicate entries
      const deduplicated = DeduplicationEngine.deduplicateBlockEntries(entries as any);

      const syncDuration = Date.now() - startTime;

      return {
        sourceId: source.id,
        success: true,
        entriesAdded: deduplicated.length,
        entriesUpdated: 0,
        entriesRemoved: 0,
        syncDuration,
      };
    } catch (error) {
      const syncDuration = Date.now() - startTime;
      const errorMessage = error instanceof Error ? error.message : String(error);

      return {
        sourceId: source.id,
        success: false,
        entriesAdded: 0,
        entriesUpdated: 0,
        entriesRemoved: 0,
        syncDuration,
        errorMessage,
      };
    }
  }

  /**
   * Sync multiple remote sources
   */
  static async syncRemoteSources(
    sources: DataSource[],
    config: Partial<SyncConfig> = {}
  ): Promise<SyncResult[]> {
    const results: SyncResult[] = [];

    for (const source of sources) {
      if (source.type === "remote_url" && source.enabled) {
        const result = await this.syncRemoteSource(source, config);
        results.push(result);
      }
    }

    return results;
  }

  /**
   * Check if a source is due for sync
   */
  static isDueForSync(source: DataSource, intervalDays: number = 7): boolean {
    if (!source.lastSync) {
      return true; // Never synced, sync immediately
    }

    const now = Math.floor(Date.now() / 1000);
    const nextSyncTime = source.lastSync + intervalDays * 24 * 60 * 60;

    return now >= nextSyncTime;
  }

  /**
   * Calculate next sync time
   */
  static getNextSyncTime(source: DataSource, intervalDays: number = 7): number {
    const now = Math.floor(Date.now() / 1000);

    if (!source.lastSync) {
      return now; // Sync immediately
    }

    const nextSync = source.lastSync + intervalDays * 24 * 60 * 60;
    return Math.max(now, nextSync);
  }

  /**
   * Get sync status for a source
   */
  static getSyncStatus(source: DataSource, intervalDays: number = 7): SyncStatus {
    const isDue = this.isDueForSync(source, intervalDays);
    const nextSyncTime = this.getNextSyncTime(source, intervalDays);

    return {
      sourceId: source.id,
      status: source.syncStatus as any,
      progress: 0,
      entriesAdded: 0,
      entriesUpdated: 0,
      entriesRemoved: 0,
      errorMessage: source.syncError || undefined,
      lastSyncTime: source.lastSync ?? undefined,
      nextSyncTime: isDue ? Math.floor(Date.now() / 1000) : nextSyncTime,
    };
  }

  /**
   * Format sync error for display
   */
  static formatSyncError(error: string): string {
    if (error.includes("HTTP")) {
      return "Network error: Server returned an error";
    }

    if (error.includes("timeout")) {
      return "Sync timed out: Server took too long to respond";
    }

    if (error.includes("Invalid file format")) {
      return "Invalid file format: Expected CSV or TXT";
    }

    if (error.includes("fetch")) {
      return "Network error: Unable to connect";
    }

    return error;
  }

  /**
   * Validate sync configuration
   */
  static validateConfig(config: Partial<SyncConfig>): { valid: boolean; error?: string } {
    if (config.intervalDays !== undefined && config.intervalDays < 1) {
      return { valid: false, error: "Sync interval must be at least 1 day" };
    }

    if (config.maxRetries !== undefined && config.maxRetries < 1) {
      return { valid: false, error: "Max retries must be at least 1" };
    }

    if (config.timeoutMs !== undefined && config.timeoutMs < 1000) {
      return { valid: false, error: "Timeout must be at least 1 second" };
    }

    return { valid: true };
  }

  /**
   * Generate sync summary
   */
  static generateSyncSummary(results: SyncResult[]): string {
    const successful = results.filter((r) => r.success);
    const failed = results.filter((r) => !r.success);
    const totalEntries = successful.reduce((sum, r) => sum + r.entriesAdded, 0);
    const totalTime = results.reduce((sum, r) => sum + r.syncDuration, 0);

    return `
📊 Sync Summary
✓ Successful: ${successful.length}
✗ Failed: ${failed.length}
📝 Total Entries: ${totalEntries}
⏱️ Total Time: ${totalTime}ms
    `.trim();
  }
}

/**
 * Background Sync Task Manager
 * Manages scheduling of background sync tasks
 */
export class BackgroundSyncManager {
  /**
   * Schedule background sync task
   * Note: This is a placeholder for actual WorkManager integration
   * In a real app, this would use expo-background-fetch or native WorkManager
   */
  static async scheduleSync(
    intervalDays: number = 7,
    config: Partial<SyncConfig> = {}
  ): Promise<{ scheduled: boolean; nextSyncTime: number }> {
    const now = Math.floor(Date.now() / 1000);
    const nextSyncTime = now + intervalDays * 24 * 60 * 60;

    // Validate config
    const validation = RemoteSyncService.validateConfig(config);
    if (!validation.valid) {
      throw new Error(validation.error);
    }

    // In a real implementation, this would use:
    // - expo-background-fetch for iOS/Android
    // - Native WorkManager for Android
    // - Background Task API for web

    console.log(`Background sync scheduled for ${new Date(nextSyncTime * 1000).toISOString()}`);

    return {
      scheduled: true,
      nextSyncTime,
    };
  }

  /**
   * Cancel background sync task
   */
  static async cancelSync(): Promise<{ cancelled: boolean }> {
    console.log("Background sync cancelled");

    return { cancelled: true };
  }

  /**
   * Get current sync schedule
   */
  static async getSyncSchedule(): Promise<{
    scheduled: boolean;
    nextSyncTime?: number;
  }> {
    // In a real implementation, this would query the actual scheduler
    return { scheduled: false };
  }
}
