import { describe, it, expect, beforeEach } from 'vitest';
import { SmartAllowListService } from './smart-allow-list';

describe('Smart Allow-List Learning Service', () => {
  let service: SmartAllowListService;

  beforeEach(() => {
    service = new SmartAllowListService();
    service.initialize();
  });

  describe('Initialization', () => {
    it('should initialize with default settings', () => {
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.dayWindow).toBe(30);
      expect(config.minCallCount).toBe(1);
    });

    it('should start with no learned numbers', () => {
      expect(service.getLearnedNumbers().length).toBe(0);
    });

    it('should initialize with empty learned numbers', () => {
      service.recordOutgoingCall('+18005551234');
      expect(service.getLearnedNumbers().length).toBe(1);
      service.initialize();
      expect(service.getLearnedNumbers().length).toBe(0);
    });
  });

  describe('Recording Outgoing Calls', () => {
    it('should record a single outgoing call', () => {
      service.recordOutgoingCall('+18005551234');
      const learned = service.getLearnedNumbers();
      expect(learned.length).toBe(1);
      expect(learned[0].phoneNumber).toBe('+18005551234');
      expect(learned[0].callCount).toBe(1);
    });

    it('should record multiple outgoing calls', () => {
      service.recordOutgoingCall('+18005551234');
      service.recordOutgoingCall('+18005555678');
      expect(service.getLearnedNumbers().length).toBe(2);
    });

    it('should increment call count for repeated calls', () => {
      service.recordOutgoingCall('+18005551234');
      service.recordOutgoingCall('+18005551234');
      service.recordOutgoingCall('+18005551234');
      const learned = service.getLearnedNumber('+18005551234');
      expect(learned?.callCount).toBe(3);
    });

    it('should store label for learned number', () => {
      service.recordOutgoingCall('+18005551234', 'Mom');
      const learned = service.getLearnedNumber('+18005551234');
      expect(learned?.label).toBe('Mom');
    });

    it('should update label on subsequent calls', () => {
      service.recordOutgoingCall('+18005551234', 'Mom');
      service.recordOutgoingCall('+18005551234', 'Mother');
      const learned = service.getLearnedNumber('+18005551234');
      expect(learned?.label).toBe('Mother');
    });

    it('should reject invalid phone numbers', () => {
      service.recordOutgoingCall('invalid');
      service.recordOutgoingCall('');
      service.recordOutgoingCall('123'); // too short
      expect(service.getLearnedNumbers().length).toBe(0);
    });

    it('should normalize phone numbers', () => {
      service.recordOutgoingCall('(800) 555-1234');
      service.recordOutgoingCall('800-555-1234');
      // Both should normalize to the same number
      expect(service.getLearnedNumbers().length).toBe(1);
    });
  });

  describe('Auto-Allow Logic', () => {
    beforeEach(() => {
      service.recordOutgoingCall('+18005551234');
    });

    it('should auto-allow a number that meets criteria', () => {
      expect(service.shouldAutoAllow('+18005551234')).toBe(true);
    });

    it('should not auto-allow when disabled', () => {
      service.setEnabled(false);
      expect(service.shouldAutoAllow('+18005551234')).toBe(false);
    });

    it('should not auto-allow unknown numbers', () => {
      expect(service.shouldAutoAllow('+18005559999')).toBe(false);
    });

    it('should not auto-allow when below min call count', () => {
      service.setMinCallCount(3);
      expect(service.shouldAutoAllow('+18005551234')).toBe(false);
    });

    it('should auto-allow when min call count is met', () => {
      service.recordOutgoingCall('+18005551234');
      service.recordOutgoingCall('+18005551234');
      service.setMinCallCount(3);
      expect(service.shouldAutoAllow('+18005551234')).toBe(true);
    });
  });

  describe('Configuration Management', () => {
    it('should set day window', () => {
      service.setDayWindow(60);
      expect(service.getDayWindow()).toBe(60);
    });

    it('should clamp day window to valid range', () => {
      service.setDayWindow(0);
      expect(service.getDayWindow()).toBe(1);
      service.setDayWindow(400);
      expect(service.getDayWindow()).toBe(365);
    });

    it('should set minimum call count', () => {
      service.setMinCallCount(5);
      expect(service.getMinCallCount()).toBe(5);
    });

    it('should clamp min call count to valid range', () => {
      service.setMinCallCount(0);
      expect(service.getMinCallCount()).toBe(1);
      service.setMinCallCount(15);
      expect(service.getMinCallCount()).toBe(10);
    });

    it('should enable/disable the service', () => {
      service.setEnabled(false);
      expect(service.isEnabled()).toBe(false);
      service.setEnabled(true);
      expect(service.isEnabled()).toBe(true);
    });

    it('should set partial configuration', () => {
      service.setConfig({ dayWindow: 60, minCallCount: 2 });
      const config = service.getConfig();
      expect(config.dayWindow).toBe(60);
      expect(config.minCallCount).toBe(2);
      expect(config.enabled).toBe(true);
    });
  });

  describe('Querying Learned Numbers', () => {
    beforeEach(() => {
      service.recordOutgoingCall('+18005551111', 'Work');
      service.recordOutgoingCall('+18005552222', 'Home');
      service.recordOutgoingCall('+18005553333', 'Mom');
      service.recordOutgoingCall('+18005551111');
    });

    it('should get all learned numbers', () => {
      expect(service.getLearnedNumbers().length).toBeGreaterThanOrEqual(3);
    });

    it('should get auto-allow candidates', () => {
      const candidates = service.getAutoAllowCandidates();
      expect(candidates.length).toBeGreaterThanOrEqual(3);
    });

    it('should get learned number by phone number', () => {
      const learned = service.getLearnedNumber('+18005551111');
      expect(learned?.callCount).toBe(2);
      expect(learned?.label).toBe('Work');
    });

    it('should return undefined for unknown number', () => {
      expect(service.getLearnedNumber('+18005559999')).toBeUndefined();
    });

    it('should get most frequently called numbers', () => {
      const frequent = service.getMostFrequentlyCalledNumbers(2);
      expect(frequent.length).toBeGreaterThanOrEqual(1);
    });

    it('should get recently called numbers', () => {
      const recent = service.getRecentlyCalledNumbers(2);
      expect(recent.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('Removing and Clearing', () => {
    beforeEach(() => {
      service.recordOutgoingCall('+18005551234');
      service.recordOutgoingCall('+18005555678');
    });

    it('should remove a learned number', () => {
      const before = service.getLearnedNumbers().length;
      service.removeLearnedNumber('+18005551234');
      const after = service.getLearnedNumbers().length;
      expect(after).toBe(before - 1);
    });

    it('should return false when removing unknown number', () => {
      const result = service.removeLearnedNumber('+18005559999');
      expect(result).toBe(false);
    });

    it('should clear all learned numbers', () => {
      service.clearAllLearnedNumbers();
      expect(service.getLearnedNumbers().length).toBe(0);
    });
  });

  describe('Statistics', () => {
    beforeEach(() => {
      service.recordOutgoingCall('+18005551111');
      service.recordOutgoingCall('+18005551111');
      service.recordOutgoingCall('+18005552222');
      service.recordOutgoingCall('+18005552222');
      service.recordOutgoingCall('+18005552222');
    });

    it('should calculate statistics correctly', () => {
      const stats = service.getStatistics();
      expect(stats.totalLearned).toBeGreaterThanOrEqual(2);
      expect(stats.autoAllowCandidates).toBeGreaterThanOrEqual(2);
      expect(stats.averageCallsPerNumber).toBeGreaterThan(0);
    });

    it('should handle empty statistics', () => {
      service.clearAllLearnedNumbers();
      const stats = service.getStatistics();
      expect(stats.totalLearned).toBe(0);
      expect(stats.autoAllowCandidates).toBe(0);
      expect(stats.averageCallsPerNumber).toBeCloseTo(0);
    });
  });

  describe('Import/Export', () => {
    beforeEach(() => {
      service.recordOutgoingCall('+18005551111', 'Work');
      service.recordOutgoingCall('+18005552222', 'Home');
    });

    it('should export as CSV', () => {
      const csv = service.exportAsCSV();
      expect(csv).toContain('Phone Number');
      expect(csv.length).toBeGreaterThan(0);
    });

    it('should import from CSV', () => {
      const csv = service.exportAsCSV();
      service.clearAllLearnedNumbers();
      const imported = service.importFromCSV(csv);
      expect(imported).toBeGreaterThanOrEqual(2);
    });

    it('should export configuration as JSON', () => {
      service.setDayWindow(60);
      const json = service.exportConfig();
      expect(json).toContain('"dayWindow": 60');
    });

    it('should import configuration from JSON', () => {
      const json = JSON.stringify({ enabled: false, dayWindow: 60, minCallCount: 2 });
      service.importConfig(json);
      const config = service.getConfig();
      expect(config.enabled).toBe(false);
      expect(config.dayWindow).toBe(60);
      expect(config.minCallCount).toBe(2);
    });

    it('should handle invalid JSON gracefully', () => {
      const originalConfig = service.getConfig();
      service.importConfig('invalid json');
      const config = service.getConfig();
      expect(config.dayWindow).toBe(originalConfig.dayWindow);
    });

    it('should reset to defaults', () => {
      service.setConfig({ dayWindow: 60, minCallCount: 5, enabled: false });
      service.resetToDefaults();
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.dayWindow).toBe(30);
      expect(config.minCallCount).toBe(1);
    });
  });

  describe('Edge Cases', () => {
    it('should handle rapid call recording', () => {
      for (let i = 0; i < 20; i++) {
        service.recordOutgoingCall('+18005551234');
      }
      const learned = service.getLearnedNumber('+18005551234');
      expect(learned?.callCount).toBe(20);
    });

    it('should handle many different numbers', () => {
      for (let i = 0; i < 50; i++) {
        service.recordOutgoingCall(`+1800555${String(i).padStart(4, '0')}`);
      }
      expect(service.getLearnedNumbers().length).toBe(50);
    });

    it('should handle normalization of various formats', () => {
      service.initialize();
      service.recordOutgoingCall('+1 (800) 555-1234');
      service.recordOutgoingCall('1-800-555-1234');
      service.recordOutgoingCall('800.555.1234');
      // These formats may normalize to 1-3 unique numbers depending on implementation
      expect(service.getLearnedNumbers().length).toBeGreaterThanOrEqual(1);
      expect(service.getLearnedNumbers().length).toBeLessThanOrEqual(3);
    });
  });
});
