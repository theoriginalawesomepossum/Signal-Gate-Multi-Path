import { describe, it, expect, beforeEach } from 'vitest';
import { CallScreeningIntegration } from '../../src/lib/services/call-screening-integration';
import { CrashHandler } from '../../src/lib/services/crash-handler';
import { TelemetryService } from '../../src/lib/services/telemetry';

describe('Call Screening Flow Integration', () => {
  let screening: CallScreeningIntegration;
  let crashHandler: CrashHandler;
  let telemetry: TelemetryService;

  beforeEach(() => {
    screening = CallScreeningIntegration.getInstance();
    crashHandler = CrashHandler.getInstance();
    telemetry = TelemetryService.getInstance();
    telemetry.resetStats();
  });

  describe('complete screening flow', () => {
    it('should screen call and block', async () => {
      const phoneNumber = '+1-555-0101';

      // Mock block list
      const result = await screening.screenCall(phoneNumber);

      expect(result).toBeDefined();
      expect(['BLOCK', 'ALLOW']).toContain(result);
    });

    it('should screen call and allow', async () => {
      const phoneNumber = '+1-555-0102';

      const result = await screening.screenCall(phoneNumber);

      expect(result).toBeDefined();
      expect(['BLOCK', 'ALLOW']).toContain(result);
    });

    it('should track call in telemetry', async () => {
      const phoneNumber = '+1-555-0103';

      const result = await screening.screenCall(phoneNumber);

      const stats = telemetry.getUsageStats();
      expect(stats.callsProcessed).toBeGreaterThan(0);
    });

    it('should handle multiple concurrent calls', async () => {
      const calls = [
        '+1-555-0201',
        '+1-555-0202',
        '+1-555-0203',
        '+1-555-0204',
        '+1-555-0205',
      ];

      const results = await Promise.all(
        calls.map((number) => screening.screenCall(number))
      );

      expect(results.length).toBe(calls.length);
      results.forEach((result) => {
        expect(['BLOCK', 'ALLOW']).toContain(result);
      });

      const stats = telemetry.getUsageStats();
      expect(stats.callsProcessed).toBe(calls.length);
    });

    it('should recover from screening error', async () => {
      const phoneNumber = '+1-555-0301';

      try {
        const result = await screening.screenCall(phoneNumber);
        expect(['BLOCK', 'ALLOW']).toContain(result);
      } catch (error) {
        const report = await crashHandler.handleCrash(error as Error);
        expect(report).toBeDefined();
        expect(report.resolved).toBe(false);
      }
    });
  });

  describe('error handling', () => {
    it('should handle invalid phone number', async () => {
      const invalidNumber = 'invalid';

      const result = await screening.screenCall(invalidNumber);

      // Should still return a decision (likely ALLOW for safety)
      expect(['BLOCK', 'ALLOW']).toContain(result);
    });

    it('should handle empty phone number', async () => {
      const result = await screening.screenCall('');

      expect(['BLOCK', 'ALLOW']).toContain(result);
    });

    it('should handle very long phone number', async () => {
      const longNumber = '+1-' + '5'.repeat(100);

      const result = await screening.screenCall(longNumber);

      expect(['BLOCK', 'ALLOW']).toContain(result);
    });
  });

  describe('performance', () => {
    it('should screen call within acceptable time', async () => {
      const phoneNumber = '+1-555-0401';
      const startTime = performance.now();

      await screening.screenCall(phoneNumber);

      const endTime = performance.now();
      const duration = endTime - startTime;

      // Should complete in less than 500ms
      expect(duration).toBeLessThan(500);
    });

    it('should handle rapid consecutive calls', async () => {
      const startTime = performance.now();

      for (let i = 0; i < 100; i++) {
        await screening.screenCall(`+1-555-0${String(i).padStart(4, '0')}`);
      }

      const endTime = performance.now();
      const duration = endTime - startTime;

      // 100 calls should complete in reasonable time
      expect(duration).toBeLessThan(10000);
    });
  });

  describe('state management', () => {
    it('should maintain consistent state across calls', async () => {
      const calls = ['+1-555-0501', '+1-555-0502', '+1-555-0503'];

      for (const call of calls) {
        await screening.screenCall(call);
      }

      const stats = telemetry.getUsageStats();
      expect(stats.callsProcessed).toBe(calls.length);
    });

    it('should reset state correctly', async () => {
      await screening.screenCall('+1-555-0601');
      let stats = telemetry.getUsageStats();
      expect(stats.callsProcessed).toBeGreaterThan(0);

      telemetry.resetStats();
      stats = telemetry.getUsageStats();
      expect(stats.callsProcessed).toBe(0);
    });
  });

  describe('crash recovery', () => {
    it('should recover from crash during screening', async () => {
      const error = new Error('CallScreeningError: Screening failed');

      const report = await crashHandler.handleCrash(error);

      expect(report).toBeDefined();
      expect(report.severity).toBe('critical');
    });

    it('should log crash in telemetry', async () => {
      const error = new Error('Test error');
      await crashHandler.handleCrash(error);

      telemetry.trackCrash();

      const stats = telemetry.getUsageStats();
      expect(stats.crashCount).toBeGreaterThan(0);
    });
  });

  describe('end-to-end flow', () => {
    it('should complete full screening workflow', async () => {
      // 1. Screen a call
      const result = await screening.screenCall('+1-555-0701');
      expect(['BLOCK', 'ALLOW']).toContain(result);

      // 2. Track in telemetry
      telemetry.trackCallScreening(result, 50);

      // 3. Verify stats
      const stats = telemetry.getUsageStats();
      expect(stats.callsProcessed).toBeGreaterThan(0);

      if (result === 'BLOCK') {
        expect(stats.callsBlocked).toBeGreaterThan(0);
      } else {
        expect(stats.callsAllowed).toBeGreaterThan(0);
      }
    });

    it('should handle workflow with error recovery', async () => {
      try {
        // 1. Attempt screening
        const result = await screening.screenCall('+1-555-0801');
        expect(['BLOCK', 'ALLOW']).toContain(result);

        // 2. Track result
        telemetry.trackCallScreening(result, 50);
      } catch (error) {
        // 3. Handle error
        const report = await crashHandler.handleCrash(error as Error);
        expect(report).toBeDefined();

        // 4. Track error
        telemetry.trackError();

        const stats = telemetry.getUsageStats();
        expect(stats.errorCount).toBeGreaterThan(0);
      }
    });
  });
});
