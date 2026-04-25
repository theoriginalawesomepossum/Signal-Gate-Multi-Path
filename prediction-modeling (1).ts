/**
 * Prediction Modeling Engine for SignalGate-MultiPoint
 * 
 * This module simulates database performance across different hardware profiles
 * and performance tiers (Full-Throttle, Center-Point, FPP).
 */

export type HardwareProfile = "low-end" | "mid-range" | "high-end";
export type PerformanceTier = "full-throttle" | "center-point" | "fpp";

export interface HardwareSpec {
  name: string;
  ram: number; // MB
  cpu: number; // GHz
  storageSpeed: number; // MB/s (read)
  examples: string[];
}

export interface PerformanceMetrics {
  importTime: number; // ms
  lookupLatency: number; // ms
  memoryUsage: number; // MB
  cpuUsage: number; // percentage
  estimatedBatteryDrain: number; // mAh per hour
}

export interface PredictionResult {
  hardware: HardwareProfile;
  tier: PerformanceTier;
  rowCount: number;
  metrics: PerformanceMetrics;
  recommendation: string;
  isOptimal: boolean;
}

/**
 * Hardware profiles representing typical Android devices
 */
const HARDWARE_PROFILES: Record<HardwareProfile, HardwareSpec> = {
  "low-end": {
    name: "Low-End (Budget Android)",
    ram: 2048, // 2GB
    cpu: 1.3,
    storageSpeed: 50,
    examples: ["Samsung Galaxy A10", "Moto E6", "Xiaomi Redmi 8A"],
  },
  "mid-range": {
    name: "Mid-Range (Standard Android)",
    ram: 4096, // 4GB
    cpu: 2.0,
    storageSpeed: 150,
    examples: ["Samsung Galaxy A50", "Moto G7", "OnePlus Nord N10"],
  },
  "high-end": {
    name: "High-End (Premium Android)",
    ram: 8192, // 8GB
    cpu: 2.8,
    storageSpeed: 300,
    examples: ["Samsung Galaxy S21", "OnePlus 9", "Google Pixel 6"],
  },
};

/**
 * Row count recommendations for each tier
 */
const TIER_CONFIGURATIONS: Record<PerformanceTier, { rowCount: number; description: string }> = {
  "full-throttle": {
    rowCount: 500000,
    description: "Maximum performance: 500k+ rows, all features enabled",
  },
  "center-point": {
    rowCount: 100000,
    description: "Balanced: 100k rows, optimized for most devices",
  },
  "fpp": {
    rowCount: 10000,
    description: "Fewest Points Possible: 10k rows, minimal overhead",
  },
};

/**
 * Simulate CSV import time based on hardware and row count
 * Formula: (rowCount / storageSpeed) * cpuFactor * ramFactor
 */
function simulateImportTime(
  hardware: HardwareSpec,
  rowCount: number,
  tier: PerformanceTier
): number {
  const baseThroughput = hardware.storageSpeed; // MB/s
  const cpuFactor = 2.8 / hardware.cpu; // Normalize to high-end CPU
  const ramFactor = hardware.ram < 2048 ? 1.5 : hardware.ram < 4096 ? 1.1 : 1.0;

  // Estimate: ~100 bytes per row (phone number + metadata)
  const dataSizeMB = (rowCount * 100) / (1024 * 1024);
  const importTimeMs = (dataSizeMB / baseThroughput) * 1000 * cpuFactor * ramFactor;

  return Math.round(importTimeMs);
}

/**
 * Simulate database lookup latency (binary search on sorted list)
 * Formula: log2(rowCount) * hardwareFactor
 */
function simulateLookupLatency(
  hardware: HardwareSpec,
  rowCount: number
): number {
  const binarySearchSteps = Math.log2(rowCount);
  const hardwareFactor = 2.8 / hardware.cpu; // Normalize to high-end CPU
  const lookupTimeMs = binarySearchSteps * 0.1 * hardwareFactor; // ~0.1ms per step on high-end

  return Math.round(lookupTimeMs * 100) / 100; // Round to 2 decimals
}

/**
 * Estimate memory usage for storing the block/allow lists
 * Formula: (rowCount * bytesPerRow) + overhead
 */
function estimateMemoryUsage(rowCount: number): number {
  const bytesPerRow = 100; // Phone number + metadata
  const overhead = 50; // Database overhead in MB
  const totalMB = (rowCount * bytesPerRow) / (1024 * 1024) + overhead;

  return Math.round(totalMB);
}

/**
 * Estimate CPU usage during lookup operations
 * Formula: (lookupLatency / 1000) * 100 (as percentage of single core)
 */
function estimateCpuUsage(lookupLatency: number): number {
  return Math.round((lookupLatency / 1000) * 100 * 10) / 10; // Round to 1 decimal
}

/**
 * Estimate battery drain from background sync and lookups
 * Formula: (importTime + lookupLatency * 1000) * hardwareFactor
 */
function estimateBatteryDrain(
  hardware: HardwareSpec,
  importTime: number,
  lookupLatency: number
): number {
  const cpuFactor = 2.8 / hardware.cpu;
  const drainPerHour = (importTime + lookupLatency * 1000) * cpuFactor * 0.001; // mAh per hour

  return Math.round(drainPerHour * 10) / 10;
}

/**
 * Generate a recommendation based on metrics
 */
function generateRecommendation(
  hardware: HardwareProfile,
  tier: PerformanceTier,
  metrics: PerformanceMetrics,
  hardwareSpec?: HardwareSpec
): string {
  const { importTime, lookupLatency, memoryUsage } = metrics;
  const spec = hardwareSpec || HARDWARE_PROFILES[hardware];

  if (hardware === "low-end") {
    if (tier === "full-throttle") {
      return "⚠️ NOT RECOMMENDED: Full-Throttle requires at least 4GB RAM. Use Center-Point or FPP instead.";
    }
    if (tier === "center-point" && importTime > 5000) {
      return "⚠️ CAUTION: Import may take > 5 seconds. Consider FPP for better performance.";
    }
  }

  if (hardware === "mid-range") {
    if (tier === "full-throttle" && importTime > 3000) {
      return "✓ ACCEPTABLE: Full-Throttle works, but import takes 3+ seconds. Monitor performance.";
    }
  }

  if (hardware === "high-end") {
    if (tier === "full-throttle" && importTime < 1000) {
      return "✓ OPTIMAL: Full-Throttle performs excellently on this device.";
    }
  }

  if (lookupLatency > 10) {
    return "⚠️ LOOKUP LATENCY HIGH: Consider reducing row count or upgrading device.";
  }

  if (memoryUsage > spec.ram * 0.5) {
    return "⚠️ HIGH MEMORY USAGE: Database is consuming > 50% of available RAM.";
  }

  return "✓ GOOD: Performance is acceptable for this configuration.";
}

/**
 * Run prediction modeling for a specific hardware/tier combination
 */
export function predictPerformance(
  hardware: HardwareProfile,
  tier: PerformanceTier
): PredictionResult {
  const hardwareSpec = HARDWARE_PROFILES[hardware];
  const tierConfig = TIER_CONFIGURATIONS[tier];
  const rowCount = tierConfig.rowCount;

  const importTime = simulateImportTime(hardwareSpec, rowCount, tier);
  const lookupLatency = simulateLookupLatency(hardwareSpec, rowCount);
  const memoryUsage = estimateMemoryUsage(rowCount);
  const cpuUsage = estimateCpuUsage(lookupLatency);
  const batteryDrain = estimateBatteryDrain(hardwareSpec, importTime, lookupLatency);

  const metrics: PerformanceMetrics = {
    importTime,
    lookupLatency,
    memoryUsage,
    cpuUsage,
    estimatedBatteryDrain: batteryDrain,
  };

  const recommendation = generateRecommendation(hardware, tier, metrics, hardwareSpec);
  const isOptimal =
    importTime < 5000 &&
    lookupLatency < 10 &&
    memoryUsage < hardwareSpec.ram * 0.5 &&
    !recommendation.includes("NOT RECOMMENDED");

  return {
    hardware,
    tier,
    rowCount,
    metrics,
    recommendation,
    isOptimal,
  };
}

/**
 * Run comprehensive prediction modeling across all hardware/tier combinations
 */
export function runComprehensivePrediction(): PredictionResult[] {
  const results: PredictionResult[] = [];

  const hardwares: HardwareProfile[] = ["low-end", "mid-range", "high-end"];
  const tiers: PerformanceTier[] = ["fpp", "center-point", "full-throttle"];

  for (const hardware of hardwares) {
    for (const tier of tiers) {
      results.push(predictPerformance(hardware, tier));
    }
  }

  return results;
}

/**
 * Simulate a crash test with very large row counts (100k+)
 */
export function runCrashTest(rowCount: number = 500000): PredictionResult {
  // Use high-end device for crash test
  const hardwareSpec = HARDWARE_PROFILES["high-end"];

  const importTime = simulateImportTime(hardwareSpec, rowCount, "full-throttle");
  const lookupLatency = simulateLookupLatency(hardwareSpec, rowCount);
  const memoryUsage = estimateMemoryUsage(rowCount);
  const cpuUsage = estimateCpuUsage(lookupLatency);
  const batteryDrain = estimateBatteryDrain(hardwareSpec, importTime, lookupLatency);

  const metrics: PerformanceMetrics = {
    importTime,
    lookupLatency,
    memoryUsage,
    cpuUsage,
    estimatedBatteryDrain: batteryDrain,
  };

  const recommendation =
    memoryUsage > hardwareSpec.ram * 0.8
      ? "⚠️ CRASH RISK: Memory usage exceeds 80% of available RAM. App may crash."
      : "✓ STABLE: Crash test passed. App can handle large imports.";

  const isOptimal = memoryUsage < hardwareSpec.ram * 0.8;

  return {
    hardware: "high-end",
    tier: "full-throttle",
    rowCount,
    metrics,
    recommendation,
    isOptimal,
  };
}

/**
 * Format prediction results for display
 */
export function formatPredictionReport(result: PredictionResult): string {
  const hardwareSpec = HARDWARE_PROFILES[result.hardware];

  return `
╔════════════════════════════════════════════════════════════════╗
║ SignalGate-MultiPoint Performance Prediction Report           ║
╚════════════════════════════════════════════════════════════════╝

📱 Hardware Profile: ${hardwareSpec.name}
   Examples: ${hardwareSpec.examples.join(", ")}
   RAM: ${hardwareSpec.ram}MB | CPU: ${hardwareSpec.cpu}GHz | Storage: ${hardwareSpec.storageSpeed}MB/s

⚙️  Performance Tier: ${result.tier.toUpperCase()}
   Row Count: ${result.rowCount.toLocaleString()}

📊 Performance Metrics:
   Import Time: ${result.metrics.importTime}ms
   Lookup Latency: ${result.metrics.lookupLatency}ms
   Memory Usage: ${result.metrics.memoryUsage}MB
   CPU Usage: ${result.metrics.cpuUsage}%
   Battery Drain: ${result.metrics.estimatedBatteryDrain}mAh/hour

💡 Recommendation:
   ${result.recommendation}

Status: ${result.isOptimal ? "✅ OPTIMAL" : "⚠️ NEEDS ATTENTION"}
  `;
}
