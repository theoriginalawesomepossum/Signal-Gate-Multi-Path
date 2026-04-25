/**
 * Contact Group Whitelist Service
 * Manages whitelisting of phone numbers based on Android Contacts groups
 * Supports Favorites and custom contact groups
 */

export interface ContactGroup {
  id: string;
  name: string;
  type: 'FAVORITES' | 'FAMILY' | 'FRIENDS' | 'WORK' | 'CUSTOM';
  enabled: boolean;
  contactCount: number;
}

export interface ContactEntry {
  id: string;
  phoneNumber: string;
  name: string;
  groups: string[]; // group IDs
  isFavorite: boolean;
}

export interface ContactGroupConfig {
  enabled: boolean;
  allowFavorites: boolean;
  allowedGroups: string[]; // group IDs
  autoSync: boolean;
  lastSync: number;
}

export class ContactGroupWhitelistService {
  private config: ContactGroupConfig = {
    enabled: true,
    allowFavorites: true,
    allowedGroups: [],
    autoSync: true,
    lastSync: 0,
  };

  private groups: Map<string, ContactGroup> = new Map();
  private contacts: Map<string, ContactEntry> = new Map();
  private phoneToContacts: Map<string, ContactEntry[]> = new Map();

  /**
   * Initialize the service with default groups
   */
  initialize(): void {
    this.groups.clear();
    this.contacts.clear();
    this.phoneToContacts.clear();

    // Add default groups
    this.groups.set('favorites', {
      id: 'favorites',
      name: 'Favorites',
      type: 'FAVORITES',
      enabled: true,
      contactCount: 0,
    });

    this.groups.set('family', {
      id: 'family',
      name: 'Family',
      type: 'FAMILY',
      enabled: false,
      contactCount: 0,
    });

    this.groups.set('friends', {
      id: 'friends',
      name: 'Friends',
      type: 'FRIENDS',
      enabled: false,
      contactCount: 0,
    });

    this.groups.set('work', {
      id: 'work',
      name: 'Work',
      type: 'WORK',
      enabled: false,
      contactCount: 0,
    });
  }

  /**
   * Add a contact to a group
   */
  addContact(contact: ContactEntry): void {
    if (!this.isValidPhoneNumber(contact.phoneNumber)) {
      return;
    }

    const normalized = this.normalizePhoneNumber(contact.phoneNumber);
    this.contacts.set(contact.id, contact);

    // Update phone to contacts mapping
    if (!this.phoneToContacts.has(normalized)) {
      this.phoneToContacts.set(normalized, []);
    }
    this.phoneToContacts.get(normalized)!.push(contact);

    // Update group contact counts
    contact.groups.forEach((groupId) => {
      const group = this.groups.get(groupId);
      if (group) {
        group.contactCount++;
      }
    });

    if (contact.isFavorite) {
      const favGroup = this.groups.get('favorites');
      if (favGroup) {
        favGroup.contactCount++;
      }
    }
  }

  /**
   * Remove a contact
   */
  removeContact(contactId: string): boolean {
    const contact = this.contacts.get(contactId);
    if (!contact) {
      return false;
    }

    const normalized = this.normalizePhoneNumber(contact.phoneNumber);
    const contacts = this.phoneToContacts.get(normalized);
    if (contacts) {
      const index = contacts.indexOf(contact);
      if (index > -1) {
        contacts.splice(index, 1);
      }
    }

    this.contacts.delete(contactId);

    // Update group contact counts
    contact.groups.forEach((groupId) => {
      const group = this.groups.get(groupId);
      if (group && group.contactCount > 0) {
        group.contactCount--;
      }
    });

    if (contact.isFavorite) {
      const favGroup = this.groups.get('favorites');
      if (favGroup && favGroup.contactCount > 0) {
        favGroup.contactCount--;
      }
    }

    return true;
  }

  /**
   * Check if a phone number is whitelisted
   */
  isWhitelisted(phoneNumber: string): boolean {
    if (!this.config.enabled) {
      return false;
    }

    const normalized = this.normalizePhoneNumber(phoneNumber);
    const contacts = this.phoneToContacts.get(normalized);

    if (!contacts || contacts.length === 0) {
      return false;
    }

    // Check if any contact is in an allowed group
    for (const contact of contacts) {
      // Check if contact is favorite and favorites are allowed
      if (contact.isFavorite && this.config.allowFavorites) {
        return true;
      }

      // Check if contact is in any allowed group
      for (const groupId of contact.groups) {
        if (this.config.allowedGroups.includes(groupId)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Get all contacts for a phone number
   */
  getContactsForNumber(phoneNumber: string): ContactEntry[] {
    const normalized = this.normalizePhoneNumber(phoneNumber);
    return this.phoneToContacts.get(normalized) || [];
  }

  /**
   * Get all contacts in a group
   */
  getContactsInGroup(groupId: string): ContactEntry[] {
    return Array.from(this.contacts.values()).filter((contact) =>
      contact.groups.includes(groupId)
    );
  }

  /**
   * Get all groups
   */
  getGroups(): ContactGroup[] {
    return Array.from(this.groups.values());
  }

  /**
   * Get a specific group
   */
  getGroup(groupId: string): ContactGroup | undefined {
    return this.groups.get(groupId);
  }

  /**
   * Enable/disable a group
   */
  setGroupEnabled(groupId: string, enabled: boolean): boolean {
    const group = this.groups.get(groupId);
    if (!group) {
      return false;
    }

    group.enabled = enabled;

    if (enabled && !this.config.allowedGroups.includes(groupId)) {
      this.config.allowedGroups.push(groupId);
    } else if (!enabled && this.config.allowedGroups.includes(groupId)) {
      const index = this.config.allowedGroups.indexOf(groupId);
      this.config.allowedGroups.splice(index, 1);
    }

    return true;
  }

  /**
   * Get all whitelisted phone numbers
   */
  getWhitelistedNumbers(): string[] {
    const whitelisted = new Set<string>();

    // Add favorites if enabled
    if (this.config.allowFavorites) {
      this.contacts.forEach((contact) => {
        if (contact.isFavorite) {
          whitelisted.add(this.normalizePhoneNumber(contact.phoneNumber));
        }
      });
    }

    // Add contacts from allowed groups
    this.config.allowedGroups.forEach((groupId) => {
      this.getContactsInGroup(groupId).forEach((contact) => {
        whitelisted.add(this.normalizePhoneNumber(contact.phoneNumber));
      });
    });

    return Array.from(whitelisted);
  }

  /**
   * Get statistics
   */
  getStatistics(): {
    totalContacts: number;
    totalGroups: number;
    whitelistedNumbers: number;
    favoriteCount: number;
  } {
    return {
      totalContacts: this.contacts.size,
      totalGroups: this.groups.size,
      whitelistedNumbers: this.getWhitelistedNumbers().length,
      favoriteCount: Array.from(this.contacts.values()).filter((c) => c.isFavorite).length,
    };
  }

  /**
   * Set configuration
   */
  setConfig(config: Partial<ContactGroupConfig>): void {
    this.config = { ...this.config, ...config };

    // Sync allowed groups with enabled groups
    if (config.allowFavorites !== undefined) {
      if (config.allowFavorites && !this.config.allowedGroups.includes('favorites')) {
        this.config.allowedGroups.push('favorites');
      } else if (!config.allowFavorites && this.config.allowedGroups.includes('favorites')) {
        const index = this.config.allowedGroups.indexOf('favorites');
        this.config.allowedGroups.splice(index, 1);
      }
    }
  }

  /**
   * Get configuration
   */
  getConfig(): ContactGroupConfig {
    return { ...this.config };
  }

  /**
   * Enable/disable the service
   */
  setEnabled(enabled: boolean): void {
    this.config.enabled = enabled;
  }

  /**
   * Check if service is enabled
   */
  isEnabled(): boolean {
    return this.config.enabled;
  }

  /**
   * Set allow favorites
   */
  setAllowFavorites(allow: boolean): void {
    this.config.allowFavorites = allow;

    if (allow && !this.config.allowedGroups.includes('favorites')) {
      this.config.allowedGroups.push('favorites');
    } else if (!allow && this.config.allowedGroups.includes('favorites')) {
      const index = this.config.allowedGroups.indexOf('favorites');
      this.config.allowedGroups.splice(index, 1);
    }
  }

  /**
   * Get allow favorites setting
   */
  getAllowFavorites(): boolean {
    return this.config.allowFavorites;
  }

  /**
   * Export as CSV
   */
  exportAsCSV(): string {
    const lines: string[] = ['Phone Number,Name,Groups,Is Favorite'];

    this.contacts.forEach((contact) => {
      const groupNames = contact.groups
        .map((id) => this.groups.get(id)?.name || id)
        .join(';');
      lines.push(
        `"${contact.phoneNumber}","${contact.name}","${groupNames}",${contact.isFavorite}`
      );
    });

    return lines.join('\n');
  }

  /**
   * Import from CSV
   */
  importFromCSV(csv: string): number {
    const lines = csv.split('\n').filter((line) => line.trim());
    let imported = 0;

    // Skip header
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      const match = line.match(/"([^"]+)","([^"]+)","([^"]*)","?(\w+)"?/);

      if (match) {
        const [, phoneNumber, name, groupsStr, isFavorite] = match;
        const groups = groupsStr
          .split(';')
          .filter((g) => g.trim())
          .map((g) => {
            const group = Array.from(this.groups.values()).find((gr) => gr.name === g.trim());
            return group?.id || g.trim();
          });

        this.addContact({
          id: `contact_${Date.now()}_${Math.random()}`,
          phoneNumber,
          name,
          groups,
          isFavorite: isFavorite === 'true',
        });

        imported++;
      }
    }

    return imported;
  }

  /**
   * Normalize phone number
   */
  private normalizePhoneNumber(phoneNumber: string): string {
    return phoneNumber
      .replace(/\D/g, '')
      .replace(/^1/, '')
      .slice(-10);
  }

  /**
   * Validate phone number
   */
  private isValidPhoneNumber(phoneNumber: string): boolean {
    const digits = phoneNumber.replace(/\D/g, '');
    return digits.length >= 10;
  }

  /**
   * Reset to defaults
   */
  resetToDefaults(): void {
    this.initialize();
    this.config = {
      enabled: true,
      allowFavorites: true,
      allowedGroups: [],
      autoSync: true,
      lastSync: 0,
    };
  }
}
