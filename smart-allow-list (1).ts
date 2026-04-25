/**
 * Smart Allow-List Learning Service
 * Auto-allows numbers called by the user within a specified time period
 */

export interface LearnedNumber {
  phoneNumber: string;
  lastCalled: number; // timestamp
  callCount: number;
  label?: string;
}

export interface SmartAllowConfig {
  enabled: boolean;
  dayWindow: number; // days to look back (1-365)
  minCallCount: number; // minimum calls to auto-allow (1-10)
}

/**
 * Smart Allow-List Learning Service
 */
export class SmartAllowListService {
  private config: SmartAllowConfig = {
    enabled: true,
    dayWindow: 30,
    minCallCount: 1,
  };

  private learnedNumbers: Map<string, LearnedNumber> = new Map();

  /**
   * Initialize the service
   */
  initialize(): void {
    this.learnedNumbers.clear();
  }

  /**
   * Record an outgoing call
   */
  recordOutgoingCall(phoneNumber: string, label?: string): void {
    const normalized = this.normalizePhoneNumber(phoneNumber);

    if (!normalized) {
      console.error('Invalid phone number:', phoneNumber);
      return;
    }

    const existing = this.learnedNumbers.get(normalized);

    if (existing) {
      existing.lastCalled = Date.now();
      existing.callCount++;
      if (label) {
        existing.label = label;
      }
    } else {
      this.learnedNumbers.set(normalized, {
        phoneNumber: normalized,
        lastCalled: Date.now(),
        callCount: 1,
        label,
      });
    }
  }

  /**
   * Check if a number should be auto-allowed
   */
  shouldAutoAllow(phoneNumber: string): boolean {
    if (!this.config.enabled) {
      return false;
    }

    const normalized = this.normalizePhoneNumber(phoneNumber);

    if (!normalized) {
      return false;
    }

    const learned = this.learnedNumbers.get(normalized);

    if (!learned) {
      return false;
    }

    // Check if within time window
    const dayWindowMs = this.config.dayWindow * 24 * 60 * 60 * 1000;
    const timeSinceLastCall = Date.now() - learned.lastCalled;

    if (timeSinceLastCall > dayWindowMs) {
      return false;
    }

    // Check if meets minimum call count
    if (learned.callCount < this.config.minCallCount) {
      return false;
    }

    return true;
  }

  /**
   * Get all learned numbers
   */
  getLearnedNumbers(): LearnedNumber[] {
    return Array.from(this.learnedNumbers.values());
  }

  /**
   * Get learned numbers that should be auto-allowed
   */
  getAutoAllowCandidates(): LearnedNumber[] {
    return this.getLearnedNumbers().filter((learned) => {
      const dayWindowMs = this.config.dayWindow * 24 * 60 * 60 * 1000;
      const timeSinceLastCall = Date.now() - learned.lastCalled;
      return timeSinceLastCall <= dayWindowMs && learned.callCount >= this.config.minCallCount;
    });
  }

  /**
   * Get learned number by phone number
   */
  getLearnedNumber(phoneNumber: string): LearnedNumber | undefined {
    const normalized = this.normalizePhoneNumber(phoneNumber);
    return normalized ? this.learnedNumbers.get(normalized) : undefined;
  }

  /**
   * Remove a learned number
   */
  removeLearnedNumber(phoneNumber: string): boolean {
    const normalized = this.normalizePhoneNumber(phoneNumber);
    return normalized ? this.learnedNumbers.delete(normalized) : false;
  }

  /**
   * Clear all learned numbers
   */
  clearAllLearnedNumbers(): void {
    this.learnedNumbers.clear();
  }

  /**
   * Set configuration
   */
  setConfig(config: Partial<SmartAllowConfig>): void {
    if (config.enabled !== undefined) {
      this.config.enabled = config.enabled;
    }
    if (config.dayWindow !== undefined) {
      this.config.dayWindow = Math.max(1, Math.min(365, config.dayWindow));
    }
    if (config.minCallCount !== undefined) {
      this.config.minCallCount = Math.max(1, Math.min(10, config.minCallCount));
    }
  }

  /**
   * Get current configuration
   */
  getConfig(): SmartAllowConfig {
    return { ...this.config };
  }

  /**
   * Enable/disable smart allow-list learning
   */
  setEnabled(enabled: boolean): void {
    this.config.enabled = enabled;
  }

  /**
   * Check if enabled
   */
  isEnabled(): boolean {
    return this.config.enabled;
  }

  /**
   * Set day window (1-365 days)
   */
  setDayWindow(days: number): void {
    this.config.dayWindow = Math.max(1, Math.min(365, days));
  }

  /**
   * Get day window
   */
  getDayWindow(): number {
    return this.config.dayWindow;
  }

  /**
   * Set minimum call count (1-10)
   */
  setMinCallCount(count: number): void {
    this.config.minCallCount = Math.max(1, Math.min(10, count));
  }

  /**
   * Get minimum call count
   */
  getMinCallCount(): number {
    return this.config.minCallCount;
  }

  /**
   * Get statistics
   */
  getStatistics(): {
    totalLearned: number;
    autoAllowCandidates: number;
    averageCallsPerNumber: number;
  } {
    const learned = this.getLearnedNumbers();
    const candidates = this.getAutoAllowCandidates();
    const totalCalls = learned.reduce((sum, n) => sum + n.callCount, 0);

    return {
      totalLearned: learned.length,
      autoAllowCandidates: candidates.length,
      averageCallsPerNumber: learned.length > 0 ? totalCalls / learned.length : 0,
    };
  }

  /**
   * Get most frequently called numbers
   */
  getMostFrequentlyCalledNumbers(limit: number = 10): LearnedNumber[] {
    return this.getLearnedNumbers()
      .sort((a, b) => b.callCount - a.callCount)
      .slice(0, limit);
  }

  /**
   * Get recently called numbers
   */
  getRecentlyCalledNumbers(limit: number = 10): LearnedNumber[] {
    return this.getLearnedNumbers()
      .sort((a, b) => b.lastCalled - a.lastCalled)
      .slice(0, limit);
  }

  /**
   * Export learned numbers as CSV
   */
  exportAsCSV(): string {
    const header = 'Phone Number,Label,Call Count,Last Called\n';
    const rows = this.getLearnedNumbers()
      .map((n) => {
        const date = new Date(n.lastCalled).toISOString();
        return `"${n.phoneNumber}","${n.label || ''}",${n.callCount},"${date}"`;
      })
      .join('\n');

    return header + rows;
  }

  /**
   * Import learned numbers from CSV
   */
  importFromCSV(csv: string): number {
    const lines = csv.trim().split('\n');
    let imported = 0;

    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      const match = line.match(/"([^"]+)","([^"]*)","?(\d+)"?,"?([^"]*)"?/);

      if (match) {
        const [, phoneNumber, label, callCount, lastCalled] = match;
        const normalized = this.normalizePhoneNumber(phoneNumber);

        if (normalized) {
          this.learnedNumbers.set(normalized, {
            phoneNumber: normalized,
            label: label || undefined,
            callCount: parseInt(callCount, 10),
            lastCalled: new Date(lastCalled).getTime(),
          });
          imported++;
        }
      }
    }

    return imported;
  }

  /**
   * Export configuration as JSON
   */
  exportConfig(): string {
    return JSON.stringify(this.config, null, 2);
  }

  /**
   * Import configuration from JSON
   */
  importConfig(json: string): void {
    try {
      const config = JSON.parse(json) as SmartAllowConfig;
      if (config.enabled !== undefined && config.dayWindow !== undefined && config.minCallCount !== undefined) {
        this.setConfig(config);
      }
    } catch (error) {
      console.error('Failed to import smart allow-list config:', error);
    }
  }

  /**
   * Reset to defaults
   */
  resetToDefaults(): void {
    this.config = {
      enabled: true,
      dayWindow: 30,
      minCallCount: 1,
    };
  }

  /**
   * Normalize phone number (remove non-numeric characters except +)
   */
  private normalizePhoneNumber(phoneNumber: string): string {
    if (!phoneNumber) {
      return '';
    }

    const normalized = phoneNumber.replace(/[^\d+]/g, '');

    if (!normalized || normalized.length < 7) {
      return '';
    }

    return normalized;
  }
}

// Export singleton instance
export const smartAllowListService = new SmartAllowListService();
