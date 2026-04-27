import { describe, it, expect, beforeEach } from 'vitest';
import { TelemetryService } from '../../src/lib/services/telemetry';

describe('TelemetryService', () => {
  let service: TelemetryService;

  beforeEach(() => {
    service = TelemetryService.getInstance();
    service.resetStats();
  });

  describe('collectDeviceStats', () => {
    it('should collect device statistics', async () => {
      const stats = await service.collectDeviceStats();

      expect(stats).toBeDefined();
      expect(stats.deviceModel).toBeDefined();
      expect(stats.androidVersion).toBeGreaterThan(0);
      expect(stats.ramAvailable).toBeGreaterThan(0);
      expect(stats.storageAvailable).toBeGreaterThan(0);
      expect(stats.cpuCores).toBeGreaterThan(0);
      expect(stats.batteryPercentage).toBeGreaterThanOrEqual(0);
      expect(stats.batteryPercentage).toBeLessThanOrEqual(100);
    });
  });

  describe('trackCallScreening', () => {
    it('should track blocked calls', () => {
      service.trackCallScreening('BLOCK', 50);

      const stats = service.getUsageStats();
      expect(stats.callsProcessed).toBe(1);
      expect(stats.callsBlocked).toBe(1);
      expect(stats.callsAllowed).toBe(0);
    });

    it('should track allowed calls', () => {
      service.trackCallScreening('ALLOW', 30);

      const stats = service.getUsageStats();
      expect(stats.callsProcessed).toBe(1);
      expect(stats.callsBlocked).toBe(0);
      expect(stats.callsAllowed).toBe(1);
    });

    it('should calculate average processing time', () => {
      service.trackCallScreening('BLOCK', 50);
      service.trackCallScreening('ALLOW', 30);
      service.trackCallScreening('BLOCK', 40);

      const stats = service.getUsageStats();
      expect(stats.callsProcessed).toBe(3);
      expect(stats.averageProcessingTime).toBe(40); // (50 + 30 + 40) / 3
    });
  });

  describe('trackScreenView', () => {
    it('should track screen views', () => {
      service.trackScreenView('Dashboard');
      service.trackScreenView('CallLog');
      service.trackScreenView('Dashboard');

      const stats = service.getUsageStats();
      expect(stats.screenViewCount['Dashboard']).toBe(2);
      expect(stats.screenViewCount['CallLog']).toBe(1);
    });
  });

  describe('trackButtonClick', () => {
    it('should track button clicks', () => {
      service.trackButtonClick('BlockButton');
      service.trackButtonClick('AllowButton');
      service.trackButtonClick('BlockButton');

      const stats = service.getUsageStats();
      expect(stats.buttonClickCount['BlockButton']).toBe(2);
      expect(stats.buttonClickCount['AllowButton']).toBe(1);
    });
  });

  describe('trackCrash', () => {
    it('should track crashes', () => {
      service.trackCrash();
      service.trackCrash();

      const stats = service.getUsageStats();
      expect(stats.crashCount).toBe(2);
    });
  });

  describe('trackError', () => {
    it('should track errors', () => {
      service.trackError();
      service.trackError();
      service.trackError();

      const stats = service.getUsageStats();
      expect(stats.errorCount).toBe(3);
    });
  });

  describe('trackWarning', () => {
    it('should track warnings', () => {
      service.trackWarning();

      const stats = service.getUsageStats();
      expect(stats.warningCount).toBe(1);
    });
  });

  describe('getDeviceStats', () => {
    it('should return device stats', async () => {
      await service.collectDeviceStats();
      const stats = service.getDeviceStats();

      expect(stats).toBeDefined();
      expect(stats?.deviceModel).toBeDefined();
    });
  });

  describe('getUsageStats', () => {
    it('should return usage stats', () => {
      service.trackCallScreening('BLOCK', 50);

      const stats = service.getUsageStats();
      expect(stats.callsProcessed).toBe(1);
      expect(stats.callsBlocked).toBe(1);
    });
  });

  describe('getAllStats', () => {
    it('should return all stats', async () => {
      await service.collectDeviceStats();
      service.trackCallScreening('BLOCK', 50);

      const allStats = service.getAllStats();
      expect(allStats.device).toBeDefined();
      expect(allStats.usage).toBeDefined();
      expect(allStats.usage.callsProcessed).toBe(1);
    });
  });

  describe('resetStats', () => {
    it('should reset all stats', () => {
      service.trackCallScreening('BLOCK', 50);
      service.trackCrash();

      let stats = service.getUsageStats();
      expect(stats.callsProcessed).toBeGreaterThan(0);
      expect(stats.crashCount).toBeGreaterThan(0);

      service.resetStats();

      stats = service.getUsageStats();
      expect(stats.callsProcessed).toBe(0);
      expect(stats.callsBlocked).toBe(0);
      expect(stats.callsAllowed).toBe(0);
      expect(stats.crashCount).toBe(0);
      expect(stats.errorCount).toBe(0);
    });
  });

  describe('exportStats', () => {
    it('should export stats as JSON', async () => {
      await service.collectDeviceStats();
      service.trackCallScreening('BLOCK', 50);

      const json = service.exportStats();
      expect(json).toBeDefined();

      const parsed = JSON.parse(json);
      expect(parsed.device).toBeDefined();
      expect(parsed.usage).toBeDefined();
      expect(parsed.exportedAt).toBeDefined();
    });
  });

  describe('privacy', () => {
    it('should not collect sensitive data', async () => {
      const stats = await service.collectDeviceStats();

      // Verify no phone numbers or personal data
      expect(JSON.stringify(stats).toLowerCase()).not.toContain('phone');
      expect(JSON.stringify(stats).toLowerCase()).not.toContain('contact');
      expect(JSON.stringify(stats).toLowerCase()).not.toContain('email');
    });
  });
});
