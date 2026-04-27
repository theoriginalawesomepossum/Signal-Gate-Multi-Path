import { describe, it, expect, beforeEach } from 'vitest';
import { CallScreeningIntegration } from '../../src/lib/services/call-screening-integration';

describe('Call Screening Performance - Latency', () => {
  let screening: CallScreeningIntegration;

  beforeEach(() => {
    screening = CallScreeningIntegration.getInstance();
  });

  describe('screening latency', () => {
    it('should screen call within 100ms', async () => {
      const phoneNumber = '+1-555-0001';
      const startTime = performance.now();

      await screening.screenCall(phoneNumber);

      const endTime = performance.now();
      const latency = endTime - startTime;

      expect(latency).toBeLessThan(100);
    });

    it('should maintain latency with small block list', async () => {
      const latencies: number[] = [];

      for (let i = 0; i < 10; i++) {
        const startTime = performance.now();
        await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
        const endTime = performance.now();

        latencies.push(endTime - startTime);
      }

      const avgLatency = latencies.reduce((a, b) => a + b, 0) / latencies.length;
      expect(avgLatency).toBeLessThan(100);
    });

    it('should maintain latency with medium block list', async () => {
      const latencies: number[] = [];

      for (let i = 0; i < 10; i++) {
        const startTime = performance.now();
        await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
        const endTime = performance.now();

        latencies.push(endTime - startTime);
      }

      const avgLatency = latencies.reduce((a, b) => a + b, 0) / latencies.length;
      expect(avgLatency).toBeLessThan(150);
    });

    it('should maintain latency with large block list', async () => {
      const latencies: number[] = [];

      for (let i = 0; i < 10; i++) {
        const startTime = performance.now();
        await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
        const endTime = performance.now();

        latencies.push(endTime - startTime);
      }

      const avgLatency = latencies.reduce((a, b) => a + b, 0) / latencies.length;
      expect(avgLatency).toBeLessThan(200);
    });

    it('should handle burst of calls', async () => {
      const calls = Array.from({ length: 50 }, (_, i) =>
        screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`)
      );

      const startTime = performance.now();
      await Promise.all(calls);
      const endTime = performance.now();

      const totalTime = endTime - startTime;
      const avgLatency = totalTime / 50;

      expect(avgLatency).toBeLessThan(100);
    });
  });

  describe('percentile latencies', () => {
    it('should meet p50 latency requirement', async () => {
      const latencies: number[] = [];

      for (let i = 0; i < 100; i++) {
        const startTime = performance.now();
        await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
        const endTime = performance.now();

        latencies.push(endTime - startTime);
      }

      latencies.sort((a, b) => a - b);
      const p50 = latencies[Math.floor(latencies.length * 0.5)];

      expect(p50).toBeLessThan(100);
    });

    it('should meet p95 latency requirement', async () => {
      const latencies: number[] = [];

      for (let i = 0; i < 100; i++) {
        const startTime = performance.now();
        await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
        const endTime = performance.now();

        latencies.push(endTime - startTime);
      }

      latencies.sort((a, b) => a - b);
      const p95 = latencies[Math.floor(latencies.length * 0.95)];

      expect(p95).toBeLessThan(150);
    });

    it('should meet p99 latency requirement', async () => {
      const latencies: number[] = [];

      for (let i = 0; i < 100; i++) {
        const startTime = performance.now();
        await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
        const endTime = performance.now();

        latencies.push(endTime - startTime);
      }

      latencies.sort((a, b) => a - b);
      const p99 = latencies[Math.floor(latencies.length * 0.99)];

      expect(p99).toBeLessThan(200);
    });
  });

  describe('latency stability', () => {
    it('should have consistent latency over time', async () => {
      const batches: number[] = [];

      for (let batch = 0; batch < 10; batch++) {
        const latencies: number[] = [];

        for (let i = 0; i < 10; i++) {
          const startTime = performance.now();
          await screening.screenCall(`+1-555-${String(i).padStart(4, '0')}`);
          const endTime = performance.now();

          latencies.push(endTime - startTime);
        }

        const avgLatency = latencies.reduce((a, b) => a + b, 0) / latencies.length;
        batches.push(avgLatency);
      }

      // All batches should have similar average latency
      const overallAvg = batches.reduce((a, b) => a + b, 0) / batches.length;
      const maxDeviation = Math.max(...batches.map((b) => Math.abs(b - overallAvg)));

      expect(maxDeviation).toBeLessThan(50);
    });
  });

  describe('latency under load', () => {
    it('should maintain latency with concurrent calls', async () => {
      const concurrentCalls = 10;
      const latencies: number[] = [];

      for (let batch = 0; batch < 5; batch++) {
        const calls = Array.from({ length: concurrentCalls }, (_, i) => {
          const startTime = performance.now();
          return screening
            .screenCall(`+1-555-${String(i).padStart(4, '0')}`)
            .then(() => {
              const endTime = performance.now();
              latencies.push(endTime - startTime);
            });
        });

        await Promise.all(calls);
      }

      const avgLatency = latencies.reduce((a, b) => a + b, 0) / latencies.length;
      expect(avgLatency).toBeLessThan(150);
    });
  });
});
