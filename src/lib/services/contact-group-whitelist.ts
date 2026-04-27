import { describe, it, expect, beforeEach } from 'vitest';
import { ContactGroupWhitelistService, ContactEntry } from './contact-group-whitelist';

describe('Contact Group Whitelist Service', () => {
  let service: ContactGroupWhitelistService;

  beforeEach(() => {
    service = new ContactGroupWhitelistService();
    service.initialize();
  });

  describe('Initialization', () => {
    it('should initialize with default groups', () => {
      const groups = service.getGroups();
      expect(groups.length).toBe(4);
      expect(groups.map((g) => g.type)).toContain('FAVORITES');
      expect(groups.map((g) => g.type)).toContain('FAMILY');
      expect(groups.map((g) => g.type)).toContain('FRIENDS');
      expect(groups.map((g) => g.type)).toContain('WORK');
    });

    it('should start with no contacts', () => {
      const stats = service.getStatistics();
      expect(stats.totalContacts).toBe(0);
    });

    it('should have favorites group enabled by default', () => {
      const favGroup = service.getGroup('favorites');
      expect(favGroup?.enabled).toBe(true);
    });
  });

  describe('Adding Contacts', () => {
    it('should add a contact', () => {
      const contact: ContactEntry = {
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      };

      service.addContact(contact);
      const stats = service.getStatistics();
      expect(stats.totalContacts).toBe(1);
      expect(stats.favoriteCount).toBe(1);
    });

    it('should add multiple contacts', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '+18005555678',
        name: 'Dad',
        groups: ['family'],
        isFavorite: true,
      });

      const stats = service.getStatistics();
      expect(stats.totalContacts).toBe(2);
      expect(stats.favoriteCount).toBe(2);
    });

    it('should reject invalid phone numbers', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: 'invalid',
        name: 'Invalid',
        groups: [],
        isFavorite: false,
      });

      const stats = service.getStatistics();
      expect(stats.totalContacts).toBe(0);
    });

    it('should update group contact counts', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: false,
      });

      const familyGroup = service.getGroup('family');
      expect(familyGroup?.contactCount).toBe(1);
    });
  });

  describe('Whitelist Checking', () => {
    beforeEach(() => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '+18005555678',
        name: 'Work Friend',
        groups: ['work'],
        isFavorite: false,
      });
    });

    it('should whitelist favorites when enabled', () => {
      service.setAllowFavorites(true);
      expect(service.isWhitelisted('+18005551234')).toBe(true);
    });

    it('should not whitelist favorites when disabled', () => {
      service.setAllowFavorites(false);
      expect(service.isWhitelisted('+18005551234')).toBe(false);
    });

    it('should whitelist contacts in allowed groups', () => {
      service.setGroupEnabled('work', true);
      expect(service.isWhitelisted('+18005555678')).toBe(true);
    });

    it('should not whitelist contacts in disabled groups', () => {
      service.setGroupEnabled('work', false);
      expect(service.isWhitelisted('+18005555678')).toBe(false);
    });

    it('should not whitelist unknown numbers', () => {
      expect(service.isWhitelisted('+18005559999')).toBe(false);
    });

    it('should not whitelist when service is disabled', () => {
      service.setEnabled(false);
      expect(service.isWhitelisted('+18005551234')).toBe(false);
    });
  });

  describe('Removing Contacts', () => {
    beforeEach(() => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });
    });

    it('should remove a contact', () => {
      const before = service.getStatistics().totalContacts;
      service.removeContact('contact1');
      const after = service.getStatistics().totalContacts;
      expect(after).toBe(before - 1);
    });

    it('should return false when removing unknown contact', () => {
      const result = service.removeContact('unknown');
      expect(result).toBe(false);
    });

    it('should update group contact counts on removal', () => {
      service.removeContact('contact1');
      const familyGroup = service.getGroup('family');
      expect(familyGroup?.contactCount).toBe(0);
    });

    it('should remove from whitelist after removal', () => {
      service.setAllowFavorites(true);
      expect(service.isWhitelisted('+18005551234')).toBe(true);
      service.removeContact('contact1');
      expect(service.isWhitelisted('+18005551234')).toBe(false);
    });
  });

  describe('Group Management', () => {
    it('should get all groups', () => {
      const groups = service.getGroups();
      expect(groups.length).toBe(4);
    });

    it('should get specific group', () => {
      const group = service.getGroup('family');
      expect(group?.name).toBe('Family');
      expect(group?.type).toBe('FAMILY');
    });

    it('should enable/disable groups', () => {
      service.setGroupEnabled('family', true);
      let group = service.getGroup('family');
      expect(group?.enabled).toBe(true);

      service.setGroupEnabled('family', false);
      group = service.getGroup('family');
      expect(group?.enabled).toBe(false);
    });

    it('should return false when enabling unknown group', () => {
      const result = service.setGroupEnabled('unknown', true);
      expect(result).toBe(false);
    });

    it('should get contacts in group', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: false,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '+18005555678',
        name: 'Dad',
        groups: ['family'],
        isFavorite: false,
      });

      const familyContacts = service.getContactsInGroup('family');
      expect(familyContacts.length).toBe(2);
    });
  });

  describe('Phone Number Queries', () => {
    beforeEach(() => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+1 (800) 555-1234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });
    });

    it('should get contacts for phone number', () => {
      const contacts = service.getContactsForNumber('+18005551234');
      expect(contacts.length).toBeGreaterThanOrEqual(1);
    });

    it('should handle normalized phone numbers', () => {
      const contacts1 = service.getContactsForNumber('+18005551234');
      const contacts2 = service.getContactsForNumber('800-555-1234');
      expect(contacts1.length).toBe(contacts2.length);
    });

    it('should return empty array for unknown number', () => {
      const contacts = service.getContactsForNumber('+18005559999');
      expect(contacts.length).toBe(0);
    });
  });

  describe('Whitelisted Numbers', () => {
    beforeEach(() => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '+18005555678',
        name: 'Work Friend',
        groups: ['work'],
        isFavorite: false,
      });
    });

    it('should get all whitelisted numbers', () => {
      service.setAllowFavorites(true);
      service.setGroupEnabled('work', true);
      const whitelisted = service.getWhitelistedNumbers();
      expect(whitelisted.length).toBeGreaterThanOrEqual(2);
    });

    it('should exclude disabled groups from whitelist', () => {
      service.setAllowFavorites(true);
      service.setGroupEnabled('work', false);
      const whitelisted = service.getWhitelistedNumbers();
      expect(whitelisted.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('Statistics', () => {
    beforeEach(() => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '+18005555678',
        name: 'Dad',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact3',
        phoneNumber: '+18005559999',
        name: 'Work Friend',
        groups: ['work'],
        isFavorite: false,
      });
    });

    it('should calculate statistics correctly', () => {
      const stats = service.getStatistics();
      expect(stats.totalContacts).toBe(3);
      expect(stats.totalGroups).toBe(4);
      expect(stats.favoriteCount).toBe(2);
    });

    it('should update statistics after adding contact', () => {
      const before = service.getStatistics();
      service.addContact({
        id: 'contact4',
        phoneNumber: '+18005551111',
        name: 'Friend',
        groups: ['friends'],
        isFavorite: false,
      });
      const after = service.getStatistics();
      expect(after.totalContacts).toBe(before.totalContacts + 1);
    });
  });

  describe('Configuration', () => {
    it('should set configuration', () => {
      service.setConfig({ allowFavorites: false, autoSync: false });
      const config = service.getConfig();
      expect(config.allowFavorites).toBe(false);
      expect(config.autoSync).toBe(false);
    });

    it('should enable/disable service', () => {
      service.setEnabled(false);
      expect(service.isEnabled()).toBe(false);
      service.setEnabled(true);
      expect(service.isEnabled()).toBe(true);
    });

    it('should set allow favorites', () => {
      service.setAllowFavorites(false);
      expect(service.getAllowFavorites()).toBe(false);
      service.setAllowFavorites(true);
      expect(service.getAllowFavorites()).toBe(true);
    });

    it('should reset to defaults', () => {
      service.setConfig({ allowFavorites: false, autoSync: false });
      service.resetToDefaults();
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.allowFavorites).toBe(true);
      expect(config.autoSync).toBe(true);
    });
  });

  describe('Import/Export', () => {
    beforeEach(() => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '+18005555678',
        name: 'Dad',
        groups: ['family'],
        isFavorite: true,
      });
    });

    it('should export as CSV', () => {
      const csv = service.exportAsCSV();
      expect(csv).toContain('Phone Number');
      expect(csv).toContain('Mom');
      expect(csv).toContain('Dad');
    });

    it('should import from CSV', () => {
      const csv = service.exportAsCSV();
      service.resetToDefaults();
      const imported = service.importFromCSV(csv);
      expect(imported).toBeGreaterThanOrEqual(2);
    });

    it('should handle invalid CSV gracefully', () => {
      const imported = service.importFromCSV('invalid,csv,data');
      expect(imported).toBe(0);
    });
  });

  describe('Edge Cases', () => {
    it('should handle multiple contacts with same phone number', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Mom',
        groups: ['family'],
        isFavorite: true,
      });

      service.addContact({
        id: 'contact2',
        phoneNumber: '800-555-1234',
        name: 'Mom (Mobile)',
        groups: ['family'],
        isFavorite: true,
      });

      const contacts = service.getContactsForNumber('+18005551234');
      expect(contacts.length).toBeGreaterThanOrEqual(1);
    });

    it('should handle contacts in multiple groups', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '+18005551234',
        name: 'Colleague',
        groups: ['work', 'friends'],
        isFavorite: false,
      });

      const workContacts = service.getContactsInGroup('work');
      const friendContacts = service.getContactsInGroup('friends');
      expect(workContacts.length).toBeGreaterThanOrEqual(1);
      expect(friendContacts.length).toBeGreaterThanOrEqual(1);
    });

    it('should handle rapid contact additions', () => {
      for (let i = 0; i < 50; i++) {
        service.addContact({
          id: `contact${i}`,
          phoneNumber: `+1800555${String(i).padStart(4, '0')}`,
          name: `Contact ${i}`,
          groups: ['family'],
          isFavorite: false,
        });
      }

      const stats = service.getStatistics();
      expect(stats.totalContacts).toBe(50);
    });

    it('should handle phone number normalization edge cases', () => {
      service.addContact({
        id: 'contact1',
        phoneNumber: '(800) 555-1234',
        name: 'Test',
        groups: [],
        isFavorite: false,
      });

      const contacts1 = service.getContactsForNumber('+1 (800) 555-1234');
      const contacts2 = service.getContactsForNumber('800.555.1234');
      const contacts3 = service.getContactsForNumber('8005551234');

      expect(contacts1.length).toBeGreaterThanOrEqual(1);
      expect(contacts2.length).toBeGreaterThanOrEqual(1);
      expect(contacts3.length).toBeGreaterThanOrEqual(1);
    });
  });
});
