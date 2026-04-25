/**
 * Call Log Service
 * 
 * Manages the history of blocked and allowed calls with filtering,
 * sorting, and action capabilities.
 */

import { PhoneNumberValidator } from "./multipoint-hub";

export type CallAction = "BLOCKED" | "ALLOWED";

export interface CallLogEntry {
  id: number;
  phoneNumber: string;
  action: CallAction;
  timestamp: number; // Unix timestamp
  source?: string; // Which rule blocked it (e.g., "Prefix: +1800", "Manual", "Community List")
  contactName?: string; // Contact name if available
  duration: number; // Call duration in seconds (0 if blocked)
}

export interface CallLogFilter {
  action?: CallAction;
  startDate?: number;
  endDate?: number;
  searchQuery?: string;
}

export interface CallLogStats {
  totalCalls: number;
  blockedCalls: number;
  allowedCalls: number;
  blockRate: number; // Percentage
  uniqueNumbers: number;
}

/**
 * Call Log Service
 */
export class CallLogService {
  private static entries: CallLogEntry[] = [];
  private static nextId = 1;

  /**
   * Add a call log entry
   */
  static addEntry(
    phoneNumber: string,
    action: CallAction,
    source?: string,
    contactName?: string,
    duration: number = 0
  ): CallLogEntry | null {
    // Validate phone number
    if (!PhoneNumberValidator.isValid(phoneNumber)) {
      console.error(`Invalid phone number: ${phoneNumber}`);
      return null;
    }

    const entry: CallLogEntry = {
      id: this.nextId++,
      phoneNumber: PhoneNumberValidator.normalize(phoneNumber),
      action,
      timestamp: Math.floor(Date.now() / 1000),
      source,
      contactName,
      duration,
    };

    this.entries.unshift(entry); // Add to beginning (most recent first)

    return entry;
  }

  /**
   * Get all call log entries
   */
  static getEntries(): CallLogEntry[] {
    return [...this.entries];
  }

  /**
   * Get filtered call log entries
   */
  static getFilteredEntries(filter: CallLogFilter): CallLogEntry[] {
    let filtered = [...this.entries];

    // Filter by action
    if (filter.action) {
      filtered = filtered.filter((e) => e.action === filter.action);
    }

    // Filter by date range
    if (filter.startDate) {
      filtered = filtered.filter((e) => e.timestamp >= filter.startDate!);
    }
    if (filter.endDate) {
      filtered = filtered.filter((e) => e.timestamp <= filter.endDate!);
    }

    // Filter by search query (phone number or contact name)
    if (filter.searchQuery) {
      const query = filter.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (e) =>
          e.phoneNumber.toLowerCase().includes(query) ||
          (e.contactName && e.contactName.toLowerCase().includes(query))
      );
    }

    return filtered;
  }

  /**
   * Get call log entry by ID
   */
  static getEntry(id: number): CallLogEntry | undefined {
    return this.entries.find((e) => e.id === id);
  }

  /**
   * Delete a call log entry
   */
  static deleteEntry(id: number): boolean {
    const index = this.entries.findIndex((e) => e.id === id);
    if (index !== -1) {
      this.entries.splice(index, 1);
      return true;
    }
    return false;
  }

  /**
   * Clear all call log entries
   */
  static clearAll(): number {
    const count = this.entries.length;
    this.entries = [];
    return count;
  }

  /**
   * Get call log statistics
   */
  static getStats(): CallLogStats {
    const total = this.entries.length;
    const blocked = this.entries.filter((e) => e.action === "BLOCKED").length;
    const allowed = this.entries.filter((e) => e.action === "ALLOWED").length;

    // Count unique phone numbers
    const uniqueNumbers = new Set(this.entries.map((e) => e.phoneNumber)).size;

    return {
      totalCalls: total,
      blockedCalls: blocked,
      allowedCalls: allowed,
      blockRate: total > 0 ? (blocked / total) * 100 : 0,
      uniqueNumbers,
    };
  }

  /**
   * Get calls from a specific number
   */
  static getCallsFromNumber(phoneNumber: string): CallLogEntry[] {
    const normalized = PhoneNumberValidator.normalize(phoneNumber);
    return this.entries.filter((e) => e.phoneNumber === normalized);
  }

  /**
   * Get most recent calls
   */
  static getRecentCalls(limit: number = 10): CallLogEntry[] {
    return this.entries.slice(0, limit);
  }

  /**
   * Get calls from today
   */
  static getCallsFromToday(): CallLogEntry[] {
    const now = Math.floor(Date.now() / 1000);
    const startOfDay = now - (now % 86400); // Midnight today

    return this.entries.filter((e) => e.timestamp >= startOfDay);
  }

  /**
   * Get calls from this week
   */
  static getCallsFromThisWeek(): CallLogEntry[] {
    const now = Math.floor(Date.now() / 1000);
    const startOfWeek = now - ((now % 604800) || 604800); // Monday

    return this.entries.filter((e) => e.timestamp >= startOfWeek);
  }

  /**
   * Format timestamp for display
   */
  static formatTime(timestamp: number): string {
    const now = Math.floor(Date.now() / 1000);
    const diff = now - timestamp;

    if (diff < 60) return "Just now";
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`;

    const date = new Date(timestamp * 1000);
    return date.toLocaleDateString();
  }

  /**
   * Export call log as CSV
   */
  static exportAsCSV(): string {
    const headers = ["Phone Number", "Action", "Source", "Contact Name", "Date/Time", "Duration"];
    const rows = this.entries.map((e) => [
      e.phoneNumber,
      e.action,
      e.source || "-",
      e.contactName || "-",
      new Date(e.timestamp * 1000).toISOString(),
      `${e.duration}s`,
    ]);

    const csv = [headers, ...rows].map((row) => row.map((cell) => `"${cell}"`).join(",")).join("\n");

    return csv;
  }

  /**
   * Get call statistics by source
   */
  static getStatsBySource(): Record<string, number> {
    const stats: Record<string, number> = {};

    this.entries.forEach((e) => {
      const source = e.source || "Unknown";
      stats[source] = (stats[source] || 0) + 1;
    });

    return stats;
  }

  /**
   * Get most blocked numbers
   */
  static getMostBlockedNumbers(limit: number = 10): Array<{ number: string; count: number }> {
    const counts: Record<string, number> = {};

    this.entries
      .filter((e) => e.action === "BLOCKED")
      .forEach((e) => {
        counts[e.phoneNumber] = (counts[e.phoneNumber] || 0) + 1;
      });

    return Object.entries(counts)
      .map(([number, count]) => ({ number, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, limit);
  }

  /**
   * Reset the service (for testing)
   */
  static reset(): void {
    this.entries = [];
    this.nextId = 1;
  }
}
