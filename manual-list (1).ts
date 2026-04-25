/**
 * Manual Block/Allow List Service
 * 
 * Manages manually created block and allow lists with add, remove, edit,
 * and search capabilities.
 */

import { PhoneNumberValidator } from "./multipoint-hub";

export type ListAction = "BLOCK" | "ALLOW";

export interface ManualListEntry {
  id: number;
  phoneNumber: string;
  action: ListAction;
  label: string;
  notes?: string;
  createdAt: number; // Unix timestamp
  updatedAt: number;
}

export interface ListStats {
  totalEntries: number;
  blockEntries: number;
  allowEntries: number;
}

/**
 * Manual Block/Allow List Service
 */
export class ManualListService {
  private static blockList: ManualListEntry[] = [];
  private static allowList: ManualListEntry[] = [];
  private static nextId = 1;

  /**
   * Add entry to block list
   */
  static addToBlockList(
    phoneNumber: string,
    label: string,
    notes?: string
  ): ManualListEntry | null {
    return this.addEntry("BLOCK", phoneNumber, label, notes);
  }

  /**
   * Add entry to allow list
   */
  static addToAllowList(
    phoneNumber: string,
    label: string,
    notes?: string
  ): ManualListEntry | null {
    return this.addEntry("ALLOW", phoneNumber, label, notes);
  }

  /**
   * Internal add entry method
   */
  private static addEntry(
    action: ListAction,
    phoneNumber: string,
    label: string,
    notes?: string
  ): ManualListEntry | null {
    // Validate phone number
    if (!PhoneNumberValidator.isValid(phoneNumber)) {
      console.error(`Invalid phone number: ${phoneNumber}`);
      return null;
    }

    // Validate label
    if (!label.trim()) {
      console.error("Label cannot be empty");
      return null;
    }

    const normalized = PhoneNumberValidator.normalize(phoneNumber);
    const list = action === "BLOCK" ? this.blockList : this.allowList;

    // Check for duplicates
    if (list.some((e) => e.phoneNumber === normalized)) {
      console.error(`Number already exists in ${action} list`);
      return null;
    }

    const now = Math.floor(Date.now() / 1000);
    const entry: ManualListEntry = {
      id: this.nextId++,
      phoneNumber: normalized,
      action,
      label,
      notes,
      createdAt: now,
      updatedAt: now,
    };

    list.push(entry);

    return entry;
  }

  /**
   * Remove entry from block list
   */
  static removeFromBlockList(id: number): boolean {
    return this.removeEntry("BLOCK", id);
  }

  /**
   * Remove entry from allow list
   */
  static removeFromAllowList(id: number): boolean {
    return this.removeEntry("ALLOW", id);
  }

  /**
   * Internal remove entry method
   */
  private static removeEntry(action: ListAction, id: number): boolean {
    const list = action === "BLOCK" ? this.blockList : this.allowList;
    const index = list.findIndex((e) => e.id === id);

    if (index !== -1) {
      list.splice(index, 1);
      return true;
    }

    return false;
  }

  /**
   * Update entry in block list
   */
  static updateBlockListEntry(
    id: number,
    label?: string,
    notes?: string
  ): ManualListEntry | null {
    return this.updateEntry("BLOCK", id, label, notes);
  }

  /**
   * Update entry in allow list
   */
  static updateAllowListEntry(
    id: number,
    label?: string,
    notes?: string
  ): ManualListEntry | null {
    return this.updateEntry("ALLOW", id, label, notes);
  }

  /**
   * Internal update entry method
   */
  private static updateEntry(
    action: ListAction,
    id: number,
    label?: string,
    notes?: string
  ): ManualListEntry | null {
    const list = action === "BLOCK" ? this.blockList : this.allowList;
    const entry = list.find((e) => e.id === id);

    if (!entry) {
      return null;
    }

    if (label !== undefined && label.trim()) {
      entry.label = label;
    }

    if (notes !== undefined) {
      entry.notes = notes;
    }

    entry.updatedAt = Math.floor(Date.now() / 1000);

    return entry;
  }

  /**
   * Get all block list entries
   */
  static getBlockList(): ManualListEntry[] {
    return [...this.blockList];
  }

  /**
   * Get all allow list entries
   */
  static getAllowList(): ManualListEntry[] {
    return [...this.allowList];
  }

  /**
   * Get all entries (both lists)
   */
  static getAllEntries(): ManualListEntry[] {
    return [...this.blockList, ...this.allowList];
  }

  /**
   * Search entries by phone number or label
   */
  static search(query: string): ManualListEntry[] {
    const lowerQuery = query.toLowerCase();

    return this.getAllEntries().filter(
      (e) =>
        e.phoneNumber.toLowerCase().includes(lowerQuery) ||
        e.label.toLowerCase().includes(lowerQuery)
    );
  }

  /**
   * Check if number is in block list
   */
  static isBlocked(phoneNumber: string): boolean {
    const normalized = PhoneNumberValidator.normalize(phoneNumber);
    return this.blockList.some((e) => e.phoneNumber === normalized);
  }

  /**
   * Check if number is in allow list
   */
  static isAllowed(phoneNumber: string): boolean {
    const normalized = PhoneNumberValidator.normalize(phoneNumber);
    return this.allowList.some((e) => e.phoneNumber === normalized);
  }

  /**
   * Get entry by ID
   */
  static getEntry(id: number): ManualListEntry | undefined {
    return this.getAllEntries().find((e) => e.id === id);
  }

  /**
   * Get statistics
   */
  static getStats(): ListStats {
    return {
      totalEntries: this.blockList.length + this.allowList.length,
      blockEntries: this.blockList.length,
      allowEntries: this.allowList.length,
    };
  }

  /**
   * Move entry from one list to another
   */
  static moveEntry(id: number, fromAction: ListAction, toAction: ListAction): ManualListEntry | null {
    if (fromAction === toAction) {
      return null;
    }

    const fromList = fromAction === "BLOCK" ? this.blockList : this.allowList;
    const toList = toAction === "BLOCK" ? this.blockList : this.allowList;

    const index = fromList.findIndex((e) => e.id === id);
    if (index === -1) {
      return null;
    }

    const entry = fromList[index];
    fromList.splice(index, 1);

    // Check for duplicate in target list
    if (toList.some((e) => e.phoneNumber === entry.phoneNumber)) {
      // Add back to original list
      fromList.push(entry);
      return null;
    }

    entry.action = toAction;
    entry.updatedAt = Math.floor(Date.now() / 1000);
    toList.push(entry);

    return entry;
  }

  /**
   * Export as CSV
   */
  static exportAsCSV(): string {
    const headers = ["Phone Number", "Action", "Label", "Notes", "Created", "Updated"];
    const rows = this.getAllEntries().map((e) => [
      e.phoneNumber,
      e.action,
      e.label,
      e.notes || "-",
      new Date(e.createdAt * 1000).toISOString(),
      new Date(e.updatedAt * 1000).toISOString(),
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
        const [phoneNumber, action, label, notes] = lines[i]
          .split(",")
          .map((cell) => cell.replace(/^"|"$/g, "").trim());

        if (!phoneNumber || !action || !label) {
          errors.push(`Row ${i + 1}: Missing required fields`);
          failed++;
          continue;
        }

        const result =
          action === "BLOCK"
            ? this.addToBlockList(phoneNumber, label, notes)
            : this.addToAllowList(phoneNumber, label, notes);

        if (result) {
          imported++;
        } else {
          errors.push(`Row ${i + 1}: Failed to add entry`);
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
   * Clear all entries
   */
  static clearAll(): number {
    const count = this.blockList.length + this.allowList.length;
    this.blockList = [];
    this.allowList = [];
    return count;
  }

  /**
   * Reset the service (for testing)
   */
  static reset(): void {
    this.blockList = [];
    this.allowList = [];
    this.nextId = 1;
  }
}
