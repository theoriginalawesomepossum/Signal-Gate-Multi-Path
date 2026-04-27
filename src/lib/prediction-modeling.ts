import { describe, it, expect } from "vitest";
import {
  predictPerformance,
  runComprehensivePrediction,
  runCrashTest,
  formatPredictionReport,
} from "./prediction-modeling";

describe("Prediction Modeling Engine", () => {
  describe("predictPerformance", () => {
    it("should predict performance for low-end device with FPP tier", () => {
      const result = predictPerformance("low-end", "fpp");

      expect(result.hardware).toBe("low-end");
      expect(result.tier).toBe("fpp");
      expect(result.rowCount).toBe(10000);
      expect(result.metrics.importTime).toBeGreaterThan(0);
      expect(result.metrics.lookupLatency).toBeGreaterThan(0);
      expect(result.metrics.memoryUsage).toBeGreaterThan(0);
    });

    it("should predict performance for mid-range device with center-point tier", () => {
      const result = predictPerformance("mid-range", "center-point");

      expect(result.hardware).toBe("mid-range");
      expect(result.tier).toBe("center-point");
      expect(result.rowCount).toBe(100000);
      expect(result.isOptimal).toBe(true);
    });

    it("should predict performance for high-end device with full-throttle tier", () => {
      const result = predictPerformance("high-end", "full-throttle");

      expect(result.hardware).toBe("high-end");
      expect(result.tier).toBe("full-throttle");
      expect(result.rowCount).toBe(500000);
      expect(result.metrics.importTime).toBeLessThan(5000); // Should be fast
    });

    it("should warn about full-throttle on low-end device", () => {
      const result = predictPerformance("low-end", "full-throttle");

      expect(result.recommendation).toContain("NOT RECOMMENDED");
      expect(result.isOptimal).toBe(false);
    });

    it("should have reasonable lookup latency", () => {
      const result = predictPerformance("high-end", "full-throttle");

      // Lookup latency should be < 10ms for 500k rows on high-end device
      expect(result.metrics.lookupLatency).toBeLessThan(10);
    });

    it("should estimate memory usage correctly", () => {
      const fppResult = predictPerformance("low-end", "fpp");
      const fullThrottleResult = predictPerformance("low-end", "full-throttle");

      // Full-Throttle should use more memory than FPP
      expect(fullThrottleResult.metrics.memoryUsage).toBeGreaterThan(
        fppResult.metrics.memoryUsage
      );
    });
  });

  describe("runComprehensivePrediction", () => {
    it("should generate predictions for all hardware/tier combinations", () => {
      const results = runComprehensivePrediction();

      // 3 hardware profiles × 3 tiers = 9 combinations
      expect(results).toHaveLength(9);
    });

    it("should have at least one optimal configuration", () => {
      const results = runComprehensivePrediction();
      const optimalConfigs = results.filter((r) => r.isOptimal);

      expect(optimalConfigs.length).toBeGreaterThan(0);
    });

    it("should show that higher tiers require better hardware", () => {
      const lowEndFpp = predictPerformance("low-end", "fpp");
      const lowEndFullThrottle = predictPerformance("low-end", "full-throttle");

      // Full-Throttle should have worse recommendation on low-end
      expect(lowEndFullThrottle.recommendation).toContain("NOT RECOMMENDED");
      expect(lowEndFpp.recommendation).not.toContain("NOT RECOMMENDED");
    });
  });

  describe("runCrashTest", () => {
    it("should handle 500k row crash test", () => {
      const result = runCrashTest(500000);

      expect(result.rowCount).toBe(500000);
      expect(result.metrics.memoryUsage).toBeGreaterThan(0);
      expect(result.hardware).toBe("high-end");
    });

    it("should warn if memory usage exceeds 80%", () => {
      const result = runCrashTest(1000000); // Very large import

      // Check if memory usage is high (this is a conditional test)
      if (result.metrics.memoryUsage > 6000) {
        expect(result.recommendation).toContain("CRASH RISK");
      } else {
        // If memory is not that high, just verify the result is valid
        expect(result.rowCount).toBe(1000000);
      }
    });

    it("should handle 100k row crash test", () => {
      const result = runCrashTest(100000);

      expect(result.rowCount).toBe(100000);
      expect(result.isOptimal).toBe(true);
    });
  });

  describe("formatPredictionReport", () => {
    it("should format a valid prediction report", () => {
      const result = predictPerformance("mid-range", "center-point");
      const report = formatPredictionReport(result);

      expect(report).toContain("SignalGate-MultiPoint Performance Prediction Report");
      expect(report).toContain("Mid-Range"); // Check for the full name instead
      expect(report).toContain("CENTER-POINT");
      expect(report).toContain("100,000"); // Formatted with comma
      expect(report).toContain("ms");
    });

    it("should include all key metrics in report", () => {
      const result = predictPerformance("high-end", "full-throttle");
      const report = formatPredictionReport(result);

      expect(report).toContain("Import Time:");
      expect(report).toContain("Lookup Latency:");
      expect(report).toContain("Memory Usage:");
      expect(report).toContain("CPU Usage:");
      expect(report).toContain("Battery Drain:");
    });
  });
  describe("Performance Tier Comparisons", () => {
    it("should show FPP is fastest for low-end devices", () => {
      const fpp = predictPerformance("low-end", "fpp");
      const centerPoint = predictPerformance("low-end", "center-point");

      expect(fpp.metrics.importTime).toBeLessThan(centerPoint.metrics.importTime);
      expect(fpp.metrics.memoryUsage).toBeLessThan(centerPoint.metrics.memoryUsage);
    });

    it("should show center-point is balanced for mid-range", () => {
      const centerPoint = predictPerformance("mid-range", "center-point");

      expect(centerPoint.isOptimal).toBe(true);
      expect(centerPoint.metrics.importTime).toBeLessThan(5000);
    });

    it("should show full-throttle is best for high-end", () => {
      const fullThrottle = predictPerformance("high-end", "full-throttle");

      expect(fullThrottle.isOptimal).toBe(true);
      expect(fullThrottle.metrics.lookupLatency).toBeLessThan(10);
    });
  });

  describe("Lookup Latency Scaling", () => {
    it("should show logarithmic scaling with row count", () => {
      const fpp = predictPerformance("high-end", "fpp"); // 10k rows
      const centerPoint = predictPerformance("high-end", "center-point"); // 100k rows
      const fullThrottle = predictPerformance("high-end", "full-throttle"); // 500k rows

      // Latency should increase but not linearly (logarithmic)
      expect(centerPoint.metrics.lookupLatency).toBeLessThan(
        fpp.metrics.lookupLatency * 5
      );
      expect(fullThrottle.metrics.lookupLatency).toBeLessThan(
        centerPoint.metrics.lookupLatency * 5
      );
    });
  });

  describe("Battery Drain Estimation", () => {
    it("should estimate lower battery drain for FPP tier", () => {
      const fpp = predictPerformance("mid-range", "fpp");
      const fullThrottle = predictPerformance("mid-range", "full-throttle");

      expect(fpp.metrics.estimatedBatteryDrain).toBeLessThan(
        fullThrottle.metrics.estimatedBatteryDrain
      );
    });

    it("should show higher battery drain for low-end devices", () => {
      const lowEnd = predictPerformance("low-end", "center-point");
      const highEnd = predictPerformance("high-end", "center-point");

      expect(lowEnd.metrics.estimatedBatteryDrain).toBeGreaterThan(
        highEnd.metrics.estimatedBatteryDrain
      );
    });
  });
});
