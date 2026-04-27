import { describe, it, expect, beforeEach } from 'vitest';
import { CrashHandler } from '../../src/lib/services/crash-handler';

describe('CrashHandler', () => {
  let handler: CrashHandler;

  beforeEach(() => {
    handler = CrashHandler.getInstance();
  });

  describe('handleCrash', () => {
    it('should capture crash information', async () => {
      const error = new Error('Test error');
      const report = await handler.handleCrash(error);

      expect(report).toBeDefined();
      expect(report.errorMessage).toBe('Test error');
      expect(report.stack).toBeDefined();
      expect(report.timestamp).toBeGreaterThan(0);
    });

    it('should calculate severity correctly', async () => {
      const criticalError = new Error('CallScreeningError: Failed to screen call');
      const criticalReport = await handler.handleCrash(criticalError);
      expect(criticalReport.severity).toBe('critical');

      const highError = new Error('Network timeout');
      const highReport = await handler.handleCrash(highError);
      expect(highReport.severity).toBe('high');

      const lowError = new Error('Minor issue');
      const lowReport = await handler.handleCrash(lowError);
      expect(lowReport.severity).toBe('low');
    });

    it('should generate unique crash IDs', async () => {
      const error1 = new Error('Error 1');
      const error2 = new Error('Error 2');

      const report1 = await handler.handleCrash(error1);
      const report2 = await handler.handleCrash(error2);

      expect(report1.id).not.toBe(report2.id);
    });
  });

  describe('getCrashReports', () => {
    it('should retrieve all crash reports', async () => {
      await handler.clearCrashReports();

      const error1 = new Error('Error 1');
      const error2 = new Error('Error 2');

      await handler.handleCrash(error1);
      await handler.handleCrash(error2);

      const reports = await handler.getCrashReports();
      expect(reports.length).toBe(2);
    });
  });

  describe('getCrashReportsBySeverity', () => {
    it('should filter crash reports by severity', async () => {
      await handler.clearCrashReports();

      const criticalError = new Error('CallScreeningError: Critical');
      const lowError = new Error('Minor issue');

      await handler.handleCrash(criticalError);
      await handler.handleCrash(lowError);

      const criticalReports = await handler.getCrashReportsBySeverity('critical');
      const lowReports = await handler.getCrashReportsBySeverity('low');

      expect(criticalReports.length).toBeGreaterThan(0);
      expect(lowReports.length).toBeGreaterThan(0);
    });
  });

  describe('getCrashReportsByDateRange', () => {
    it('should filter crash reports by date range', async () => {
      await handler.clearCrashReports();

      const now = Date.now();
      const error = new Error('Test error');

      await handler.handleCrash(error);

      const reports = await handler.getCrashReportsByDateRange(now - 1000, now + 1000);
      expect(reports.length).toBeGreaterThan(0);
    });
  });

  describe('clearCrashReports', () => {
    it('should clear all crash reports', async () => {
      const error = new Error('Test error');
      await handler.handleCrash(error);

      let reports = await handler.getCrashReports();
      expect(reports.length).toBeGreaterThan(0);

      await handler.clearCrashReports();

      reports = await handler.getCrashReports();
      expect(reports.length).toBe(0);
    });
  });

  describe('exportCrashReports', () => {
    it('should export crash reports as JSON', async () => {
      await handler.clearCrashReports();

      const error = new Error('Test error');
      await handler.handleCrash(error);

      const json = await handler.exportCrashReports();
      expect(json).toBeDefined();

      const parsed = JSON.parse(json);
      expect(Array.isArray(parsed)).toBe(true);
      expect(parsed.length).toBeGreaterThan(0);
    });
  });

  describe('markCrashResolved', () => {
    it('should mark crash as resolved', async () => {
      await handler.clearCrashReports();

      const error = new Error('Test error');
      const report = await handler.handleCrash(error);

      await handler.markCrashResolved(report.id, 'Fixed in v1.0.1');

      const reports = await handler.getCrashReports();
      const resolved = reports.find((r) => r.id === report.id);

      expect(resolved?.resolved).toBe(true);
      expect(resolved?.resolutionMethod).toBe('Fixed in v1.0.1');
    });
  });

  describe('setScreeningActive', () => {
    it('should track screening active state', async () => {
      handler.setScreeningActive(true);

      const error = new Error('Test error');
      const report = await handler.handleCrash(error);

      expect(report.appState.isCallScreening).toBe(true);
    });
  });
});
