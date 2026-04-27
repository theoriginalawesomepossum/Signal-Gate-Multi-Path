/**
 * Multipoint Hub Service
 * 
 * Core business logic for managing multiple data sources (local files, remote URLs)
 * with conflict resolution, de-duplication, and source tracking.
 */

import { BlockEntry, AllowEntry, DataSource, NewBlockEntry, NewAllowEntry } from "@/lib/db/schema";

/**
 * Phone number validation and normalization
 */
export class PhoneNumberValidator {
  /**
   * Validate phone number format (E.164 or standard formats)
   * Accepts: +1234567890, 1234567890, +1-234-567-8900, (123) 456-7890, etc.
   */
  static isValid(phoneNumber: string): boolean {
    if (!phoneNumber || typeof phoneNumber !== "string") return false;

    // Remove common formatting characters
    const cleaned = phoneNumber.replace(/[\s\-()\.]/g, "");

    // Must be at least 7 digits
    if (cleaned.replace(/\D/g, "").length < 7) return false;

    // E.164 format: +1-15 digits
    if (cleaned.startsWith("+")) {
      return /^\+\d{1,15}$/.test(cleaned);
    }

    // Standard format: 7-15 digits
    return /^\d{7,15}$/.test(cleaned);
  }

  /**
   * Normalize phone number to E.164 format
   * Assumes US numbers if no country code provided
   */
  static normalize(phoneNumber: string): string {
    if (!phoneNumber) return "";

    let cleaned = phoneNumber.replace(/[\s\-()\.]/g, "");

    // If already in E.164 format, return as-is
    if (cleaned.startsWith("+")) {
      return cleaned;
    }

    // Remove leading 1 for US numbers (1234567890 -> 234567890)
    if (cleaned.startsWith("1") && cleaned.length === 11) {
      cleaned = cleaned.substring(1);
    }

    // Add +1 prefix for US numbers
    if (cleaned.length === 10) {
      return `+1${cleaned}`;
    }

    // For other lengths, just add + prefix
    return `+${cleaned}`;
  }

  /**
   * Check if a string is a pattern (regex or prefix)
   * Patterns start with + or contain regex special characters
   */
  static isPattern(value: string): boolean {
    if (!value) return false;

    // Prefix patterns: +1800, +44, etc. (short codes, not full numbers)
    if (value.startsWith("+") && value.length > 1 && value.length <= 5) {
      return true;
    }

    // Regex patterns: contain special regex characters (excluding leading +)
    const checkValue = value.startsWith("+") ? value.substring(1) : value;
    const regexChars = /[\[\](){}.*?^$|\\]/;
    return regexChars.test(checkValue);
  }
}

/**
 * Conflict Resolution Engine
 * Implements the priority hierarchy for determining if a call should be blocked
 */
export class ConflictResolver {
  /**
   * Priority hierarchy for call screening:
   * 1. Allow-list (highest priority - always allow)
   * 2. Block-list (block if matched)
   * 3. Default (allow if no match)
   */
  static resolveConflict(
    phoneNumber: string,
    allowEntries: AllowEntry[],
    blockEntries: BlockEntry[]
  ): "ALLOW" | "BLOCK" {
    const normalizedNumber = PhoneNumberValidator.normalize(phoneNumber);

    // Step 1: Check Allow-list (highest priority)
    if (this.matchesEntries(normalizedNumber, allowEntries)) {
      return "ALLOW";
    }

    // Step 2: Check Block-list
    if (this.matchesEntries(normalizedNumber, blockEntries)) {
      return "BLOCK";
    }

    // Step 3: Default (allow if no match)
    return "ALLOW";
  }

  /**
   * Check if phone number matches any entries (exact or pattern)
   */
  private static matchesEntries(
    phoneNumber: string,
    entries: (BlockEntry | AllowEntry)[]
  ): boolean {
    for (const entry of entries) {
      if (this.matchesEntry(phoneNumber, entry)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if phone number matches a single entry
   */
  private static matchesEntry(
    phoneNumber: string,
    entry: BlockEntry | AllowEntry
  ): boolean {
    const pattern = entry.phoneNumber;

    // Exact match
    if (phoneNumber === pattern) {
      return true;
    }

    // Prefix match (e.g., +1800 matches +18005551234)
    if (pattern.startsWith("+") && pattern.length <= 5) {
      return phoneNumber.startsWith(pattern);
    }

    // Regex pattern match
    try {
      const regex = new RegExp(pattern);
      return regex.test(phoneNumber);
    } catch {
      // Invalid regex, skip
      return false;
    }
  }
}

/**
 * De-duplication Engine
 * Prevents duplicate entries across multiple sources
 */
export class DeduplicationEngine {
  /**
   * De-duplicate entries, keeping the highest priority version
   * If priorities are equal, keep the first occurrence
   */
  static deduplicateBlockEntries(entries: BlockEntry[]): BlockEntry[] {
    const seen = new Map<string, BlockEntry>();

    for (const entry of entries) {
      const key = entry.phoneNumber;

      if (!seen.has(key)) {
        seen.set(key, entry);
      } else {
        const existing = seen.get(key)!;
        // Keep the one with higher priority
        if ((entry.priority || 3) > (existing.priority || 3)) {
          seen.set(key, entry);
        }
      }
    }

    return Array.from(seen.values());
  }

  /**
   * De-duplicate allow entries
   */
  static deduplicateAllowEntries(entries: AllowEntry[]): AllowEntry[] {
    const seen = new Map<string, AllowEntry>();

    for (const entry of entries) {
      const key = entry.phoneNumber;

      if (!seen.has(key)) {
        seen.set(key, entry);
      }
    }

    return Array.from(seen.values());
  }
}

/**
 * CSV/XLSX Parser
 * Parses block/allow lists from various file formats
 */
export class ListParser {
  /**
   * Parse CSV content into block entries
   * Expected format: phone_number, action, label, priority
   * Example: +18005551234, BLOCK, Telemarketer, 3
   */
  static parseCSV(content: string, sourceId: number): NewBlockEntry[] {
    const lines = content.split("\n");
    const entries: NewBlockEntry[] = [];

    for (const line of lines) {
      if (!line.trim()) continue;

      const parts = line.split(",").map((p) => p.trim());

      if (parts.length < 1) continue;

      const phoneNumber = parts[0];
      const action = (parts[1] || "BLOCK").toUpperCase();
      const label = parts[2] || "";
      const priority = parseInt(parts[3] || "3", 10);

      if (!PhoneNumberValidator.isValid(phoneNumber)) {
        console.warn(`Invalid phone number: ${phoneNumber}`);
        continue;
      }

      entries.push({
        phoneNumber: PhoneNumberValidator.normalize(phoneNumber),
        action: action as "BLOCK" | "ALLOW",
        label,
        priority: Math.max(1, Math.min(5, priority)), // Clamp to 1-5
        sourceId,
        isPattern: PhoneNumberValidator.isPattern(phoneNumber) ? 1 : 0,
        isManual: 0,
      });
    }

    return entries;
  }

  /**
   * Parse text file (one number per line)
   * Simple format for quick imports
   */
  static parseTXT(content: string, sourceId: number): NewBlockEntry[] {
    const lines = content.split("\n");
    const entries: NewBlockEntry[] = [];

    for (const line of lines) {
      const phoneNumber = line.trim();

      if (!phoneNumber) continue;

      if (!PhoneNumberValidator.isValid(phoneNumber)) {
        console.warn(`Invalid phone number: ${phoneNumber}`);
        continue;
      }

      entries.push({
        phoneNumber: PhoneNumberValidator.normalize(phoneNumber),
        action: "BLOCK",
        label: "",
        priority: 3,
        sourceId,
        isPattern: PhoneNumberValidator.isPattern(phoneNumber) ? 1 : 0,
        isManual: 0,
      });
    }

    return entries;
  }

  /**
   * Detect file format from content
   */
  static detectFormat(content: string): "csv" | "txt" | "unknown" {
    const firstLine = content.split("\n")[0];

    // CSV: contains commas
    if (firstLine.includes(",")) {
      return "csv";
    }

    // TXT: just phone numbers
    if (PhoneNumberValidator.isValid(firstLine.trim())) {
      return "txt";
    }

    return "unknown";
  }
}

/**
 * Data Source Manager
 * Manages the lifecycle of data sources (add, update, delete, sync)
 */
export class DataSourceManager {
  /**
   * Validate data source configuration
   */
  static validateSource(source: Partial<DataSource>): { valid: boolean; error?: string } {
    if (!source.name || source.name.trim().length === 0) {
      return { valid: false, error: "Source name is required" };
    }

    if (!source.type || !["local_file", "remote_url"].includes(source.type)) {
      return { valid: false, error: "Invalid source type" };
    }

    if (source.type === "remote_url" && !source.sourceUrl) {
      return { valid: false, error: "URL is required for remote sources" };
    }

    if (source.type === "remote_url" && source.sourceUrl) {
      try {
        new URL(source.sourceUrl);
      } catch {
        return { valid: false, error: "Invalid URL format" };
      }
    }

    return { valid: true };
  }

  /**
   * Calculate storage usage for a source
   */
  static calculateStorageUsage(entryCount: number): number {
    // Estimate: ~100 bytes per entry (phone number + metadata)
    return (entryCount * 100) / (1024 * 1024); // Convert to MB
  }
}

/**
 * Sync Scheduler
 * Manages background sync scheduling and execution
 */
export class SyncScheduler {
  /**
   * Calculate next sync time based on schedule
   * Default: weekly (7 days)
   */
  static getNextSyncTime(lastSyncTime: number | null, intervalDays: number = 7): number {
    const now = Math.floor(Date.now() / 1000);

    if (!lastSyncTime) {
      return now; // Sync immediately if never synced
    }

    const nextSync = lastSyncTime + intervalDays * 24 * 60 * 60;
    return Math.max(now, nextSync);
  }

  /**
   * Check if a source is due for sync
   */
  static isDueForSync(lastSyncTime: number | null, intervalDays: number = 7): boolean {
    const now = Math.floor(Date.now() / 1000);
    const nextSyncTime = this.getNextSyncTime(lastSyncTime, intervalDays);
    return now >= nextSyncTime;
  }
}
