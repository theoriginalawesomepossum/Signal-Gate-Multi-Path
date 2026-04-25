/**
 * Prefix/Pattern Rules Service
 * 
 * Manages pattern-based blocking rules (e.g., block all +1800 numbers,
 * block specific country codes, etc.)
 */

import { PhoneNumberValidator } from "./multipoint-hub";

export type PatternType = "PREFIX" | "AREA_CODE" | "COUNTRY_CODE" | "REGEX";

export interface PatternRule {
  id: number;
  pattern: string;
  type: PatternType;
  label: string;
  enabled: boolean;
  createdAt: number; // Unix timestamp
  notes?: string;
}

export interface PatternStats {
  totalRules: number;
  enabledRules: number;
  disabledRules: number;
  rulesByType: Record<PatternType, number>;
}

/**
 * Prefix/Pattern Rules Service
 */
export class PatternRulesService {
  private static rules: PatternRule[] = [];
  private static nextId = 1;

  /**
   * Add a prefix rule (e.g., "+1800" to block all 800 numbers)
   */
  static addPrefixRule(prefix: string, label: string, notes?: string): PatternRule | null {
    return this.addRule("PREFIX", prefix, label, notes);
  }

  /**
   * Add an area code rule (e.g., "212" to block all 212 area code)
   */
  static addAreaCodeRule(areaCode: string, label: string, notes?: string): PatternRule | null {
    return this.addRule("AREA_CODE", areaCode, label, notes);
  }

  /**
   * Add a country code rule (e.g., "+44" to block all UK numbers)
   */
  static addCountryCodeRule(countryCode: string, label: string, notes?: string): PatternRule | null {
    return this.addRule("COUNTRY_CODE", countryCode, label, notes);
  }

  /**
   * Add a regex rule (e.g., "^\\+1(555|666)\\d{7}$" for custom patterns)
   */
  static addRegexRule(regex: string, label: string, notes?: string): PatternRule | null {
    return this.addRule("REGEX", regex, label, notes);
  }

  /**
   * Internal add rule method
   */
  private static addRule(
    type: PatternType,
    pattern: string,
    label: string,
    notes?: string
  ): PatternRule | null {
    // Validate pattern
    if (!pattern.trim()) {
      console.error("Pattern cannot be empty");
      return null;
    }

    // Validate label
    if (!label.trim()) {
      console.error("Label cannot be empty");
      return null;
    }

    // Validate regex patterns
    if (type === "REGEX") {
      try {
        new RegExp(pattern);
      } catch (error) {
        console.error(`Invalid regex pattern: ${pattern}`);
        return null;
      }
    }

    // Check for duplicates
    if (this.rules.some((r) => r.pattern === pattern && r.type === type)) {
      console.error(`Rule already exists: ${type} ${pattern}`);
      return null;
    }

    const rule: PatternRule = {
      id: this.nextId++,
      pattern,
      type,
      label,
      enabled: true,
      createdAt: Math.floor(Date.now() / 1000),
      notes,
    };

    this.rules.push(rule);

    return rule;
  }

  /**
   * Get all rules
   */
  static getRules(): PatternRule[] {
    return [...this.rules];
  }

  /**
   * Get enabled rules only
   */
  static getEnabledRules(): PatternRule[] {
    return this.rules.filter((r) => r.enabled);
  }

  /**
   * Get rules by type
   */
  static getRulesByType(type: PatternType): PatternRule[] {
    return this.rules.filter((r) => r.type === type);
  }

  /**
   * Get rule by ID
   */
  static getRule(id: number): PatternRule | undefined {
    return this.rules.find((r) => r.id === id);
  }

  /**
   * Enable rule
   */
  static enableRule(id: number): boolean {
    const rule = this.rules.find((r) => r.id === id);
    if (rule) {
      rule.enabled = true;
      return true;
    }
    return false;
  }

  /**
   * Disable rule
   */
  static disableRule(id: number): boolean {
    const rule = this.rules.find((r) => r.id === id);
    if (rule) {
      rule.enabled = false;
      return true;
    }
    return false;
  }

  /**
   * Delete rule
   */
  static deleteRule(id: number): boolean {
    const index = this.rules.findIndex((r) => r.id === id);
    if (index !== -1) {
      this.rules.splice(index, 1);
      return true;
    }
    return false;
  }

  /**
   * Update rule
   */
  static updateRule(id: number, label?: string, notes?: string): PatternRule | null {
    const rule = this.rules.find((r) => r.id === id);

    if (!rule) {
      return null;
    }

    if (label !== undefined && label.trim()) {
      rule.label = label;
    }

    if (notes !== undefined) {
      rule.notes = notes;
    }

    return rule;
  }

  /**
   * Check if phone number matches any enabled rule
   */
  static matches(phoneNumber: string): PatternRule | null {
    const normalized = PhoneNumberValidator.normalize(phoneNumber);

    for (const rule of this.getEnabledRules()) {
      if (this.ruleMatches(normalized, rule)) {
        return rule;
      }
    }

    return null;
  }

  /**
   * Check if phone number matches a specific rule
   */
  private static ruleMatches(phoneNumber: string, rule: PatternRule): boolean {
    switch (rule.type) {
      case "PREFIX":
        return phoneNumber.startsWith(rule.pattern);

      case "AREA_CODE":
        // For US numbers: +1 + area code + 7 digits
        // Extract area code from +1AAANNNNNNN format
        const areaCodeMatch = phoneNumber.match(/^\+1(\d{3})/);
        if (areaCodeMatch) {
          return areaCodeMatch[1] === rule.pattern;
        }
        return false;

      case "COUNTRY_CODE":
        return phoneNumber.startsWith(rule.pattern);

      case "REGEX":
        try {
          const regex = new RegExp(rule.pattern);
          return regex.test(phoneNumber);
        } catch (error) {
          console.error(`Invalid regex in rule ${rule.id}: ${rule.pattern}`);
          return false;
        }

      default:
        return false;
    }
  }

  /**
   * Get all matching rules for a phone number
   */
  static getAllMatches(phoneNumber: string): PatternRule[] {
    const normalized = PhoneNumberValidator.normalize(phoneNumber);

    return this.getEnabledRules().filter((rule) => this.ruleMatches(normalized, rule));
  }

  /**
   * Get statistics
   */
  static getStats(): PatternStats {
    const stats: PatternStats = {
      totalRules: this.rules.length,
      enabledRules: this.rules.filter((r) => r.enabled).length,
      disabledRules: this.rules.filter((r) => !r.enabled).length,
      rulesByType: {
        PREFIX: 0,
        AREA_CODE: 0,
        COUNTRY_CODE: 0,
        REGEX: 0,
      },
    };

    this.rules.forEach((r) => {
      stats.rulesByType[r.type]++;
    });

    return stats;
  }

  /**
   * Export as CSV
   */
  static exportAsCSV(): string {
    const headers = ["Pattern", "Type", "Label", "Enabled", "Notes", "Created"];
    const rows = this.rules.map((r) => [
      r.pattern,
      r.type,
      r.label,
      r.enabled ? "Yes" : "No",
      r.notes || "-",
      new Date(r.createdAt * 1000).toISOString(),
    ]);

    const csv = [headers, ...rows].map((row) => row.map((cell) => `"${cell}"`).join(",")).join("\n");

    return csv;
  }

  /**
   * Import from CSV
   */
  static importFromCSV(csv: string): { imported: number; failed: number; errors: string[] } {
    const lines = csv.split("\n").filter((line) => line.trim());
    let imported = 0;
    let failed = 0;
    const errors: string[] = [];

    // Skip header
    for (let i = 1; i < lines.length; i++) {
      try {
        const [pattern, type, label, enabled, notes] = lines[i]
          .split(",")
          .map((cell) => cell.replace(/^"|"$/g, "").trim());

        if (!pattern || !type || !label) {
          errors.push(`Row ${i + 1}: Missing required fields`);
          failed++;
          continue;
        }

        let result: PatternRule | null = null;

        switch (type) {
          case "PREFIX":
            result = this.addPrefixRule(pattern, label, notes);
            break;
          case "AREA_CODE":
            result = this.addAreaCodeRule(pattern, label, notes);
            break;
          case "COUNTRY_CODE":
            result = this.addCountryCodeRule(pattern, label, notes);
            break;
          case "REGEX":
            result = this.addRegexRule(pattern, label, notes);
            break;
          default:
            errors.push(`Row ${i + 1}: Invalid type ${type}`);
            failed++;
            continue;
        }

        if (result) {
          if (enabled === "No") {
            this.disableRule(result.id);
          }
          imported++;
        } else {
          errors.push(`Row ${i + 1}: Failed to add rule`);
          failed++;
        }
      } catch (error) {
        errors.push(`Row ${i + 1}: ${error instanceof Error ? error.message : "Unknown error"}`);
        failed++;
      }
    }

    return { imported, failed, errors };
  }

  /**
   * Clear all rules
   */
  static clearAll(): number {
    const count = this.rules.length;
    this.rules = [];
    return count;
  }

  /**
   * Reset the service (for testing)
   */
  static reset(): void {
    this.rules = [];
    this.nextId = 1;
  }
}
