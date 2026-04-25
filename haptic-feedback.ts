import { describe, it, expect, beforeEach, vi } from 'vitest';
import { HapticFeedbackService, type HapticIntensity } from './haptic-feedback';

describe('Haptic Feedback Service', () => {
  let service: HapticFeedbackService;

  beforeEach(() => {
    service = new HapticFeedbackService();
    vi.spyOn(console, 'log').mockImplementation(() => {});
  });

  describe('Initialization', () => {
    it('should initialize with default settings', () => {
      expect(service.isEnabled()).toBe(true);
      expect(service.getIntensity()).toBe('medium');
      expect(service.doesRespectSilentMode()).toBe(true);
    });

    it('should initialize with device capability and silent mode', () => {
      service.initialize(false, true);
      expect(service.getSilentMode()).toBe(false);
      expect(service.getDeviceCapability()).toBe(true);
    });

    it('should initialize with silent mode enabled', () => {
      service.initialize(true, true);
      expect(service.getSilentMode()).toBe(true);
    });

    it('should initialize with device not capable', () => {
      service.initialize(false, false);
      expect(service.getDeviceCapability()).toBe(false);
    });
  });

  describe('Haptic Triggering', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should trigger block haptic when enabled', () => {
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });

    it('should trigger allow haptic when enabled', () => {
      const result = service.triggerAllowHaptic();
      expect(result).toBe(true);
    });

    it('should trigger manual action haptic when enabled', () => {
      const result = service.triggerManualActionHaptic();
      expect(result).toBe(true);
    });

    it('should trigger double-tap haptic pattern', () => {
      const result = service.triggerDoubleTapHaptic();
      expect(result).toBe(true);
    });

    it('should trigger success haptic pattern', () => {
      const result = service.triggerSuccessHaptic();
      expect(result).toBe(true);
    });

    it('should trigger error haptic pattern', () => {
      const result = service.triggerErrorHaptic();
      expect(result).toBe(true);
    });
  });

  describe('Silent Mode Handling', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should not trigger haptic when silent mode is on and respected', () => {
      service.setSilentMode(true);
      service.setRespectSilentMode(true);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(false);
    });

    it('should trigger haptic when silent mode is on but not respected', () => {
      service.setSilentMode(true);
      service.setRespectSilentMode(false);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });

    it('should trigger haptic when silent mode is off', () => {
      service.setSilentMode(false);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });

    it('should update silent mode status', () => {
      expect(service.getSilentMode()).toBe(false);
      service.setSilentMode(true);
      expect(service.getSilentMode()).toBe(true);
    });
  });

  describe('Device Capability', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should not trigger haptic when device is not capable', () => {
      service.setDeviceCapability(false);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(false);
    });

    it('should trigger haptic when device is capable', () => {
      service.setDeviceCapability(true);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });

    it('should update device capability', () => {
      expect(service.getDeviceCapability()).toBe(true);
      service.setDeviceCapability(false);
      expect(service.getDeviceCapability()).toBe(false);
    });
  });

  describe('Intensity Management', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should set intensity to light', () => {
      service.setIntensity('light');
      expect(service.getIntensity()).toBe('light');
    });

    it('should set intensity to medium', () => {
      service.setIntensity('medium');
      expect(service.getIntensity()).toBe('medium');
    });

    it('should set intensity to strong', () => {
      service.setIntensity('strong');
      expect(service.getIntensity()).toBe('strong');
    });

    it('should return all available intensities', () => {
      const intensities = service.getAvailableIntensities();
      expect(intensities).toContain('light');
      expect(intensities).toContain('medium');
      expect(intensities).toContain('strong');
      expect(intensities.length).toBe(3);
    });
  });

  describe('Enable/Disable', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should disable haptic feedback', () => {
      service.setEnabled(false);
      expect(service.isEnabled()).toBe(false);
    });

    it('should not trigger haptic when disabled', () => {
      service.setEnabled(false);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(false);
    });

    it('should enable haptic feedback', () => {
      service.setEnabled(false);
      service.setEnabled(true);
      expect(service.isEnabled()).toBe(true);
    });

    it('should trigger haptic when enabled', () => {
      service.setEnabled(true);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });
  });

  describe('Silent Mode Respect', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should respect silent mode by default', () => {
      expect(service.doesRespectSilentMode()).toBe(true);
    });

    it('should not respect silent mode when disabled', () => {
      service.setRespectSilentMode(false);
      expect(service.doesRespectSilentMode()).toBe(false);
    });

    it('should trigger haptic when silent mode is not respected', () => {
      service.setSilentMode(true);
      service.setRespectSilentMode(false);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });
  });

  describe('Configuration Management', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should get current configuration', () => {
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.intensity).toBe('medium');
      expect(config.respectSilentMode).toBe(true);
    });

    it('should export configuration as JSON', () => {
      service.setIntensity('strong');
      const json = service.exportConfig();
      expect(json).toContain('"intensity": "strong"');
      expect(json).toContain('"enabled": true');
    });

    it('should import configuration from JSON', () => {
      const json = JSON.stringify({ enabled: false, intensity: 'light', respectSilentMode: false });
      service.importConfig(json);
      const config = service.getConfig();
      expect(config.enabled).toBe(false);
      expect(config.intensity).toBe('light');
      expect(config.respectSilentMode).toBe(false);
    });

    it('should handle invalid JSON gracefully', () => {
      const originalConfig = service.getConfig();
      service.importConfig('invalid json');
      const config = service.getConfig();
      expect(config.enabled).toBe(originalConfig.enabled);
    });

    it('should reset to defaults', () => {
      service.setIntensity('strong');
      service.setEnabled(false);
      service.setRespectSilentMode(false);
      service.resetToDefaults();
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.intensity).toBe('medium');
      expect(config.respectSilentMode).toBe(true);
    });
  });

  describe('Complex Scenarios', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should handle all conditions preventing haptic', () => {
      service.setEnabled(false);
      service.setDeviceCapability(false);
      service.setSilentMode(true);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(false);
    });

    it('should trigger haptic with all conditions met', () => {
      service.setEnabled(true);
      service.setDeviceCapability(true);
      service.setSilentMode(false);
      service.setRespectSilentMode(true);
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });

    it('should handle intensity changes during operation', () => {
      service.setIntensity('light');
      expect(service.getIntensity()).toBe('light');
      service.setIntensity('strong');
      expect(service.getIntensity()).toBe('strong');
      const result = service.triggerBlockHaptic();
      expect(result).toBe(true);
    });

    it('should handle rapid haptic triggers', () => {
      for (let i = 0; i < 10; i++) {
        const result = service.triggerBlockHaptic();
        expect(result).toBe(true);
      }
    });

    it('should handle silent mode toggle during operation', () => {
      service.setSilentMode(false);
      expect(service.triggerBlockHaptic()).toBe(true);
      service.setSilentMode(true);
      expect(service.triggerBlockHaptic()).toBe(false);
      service.setSilentMode(false);
      expect(service.triggerBlockHaptic()).toBe(true);
    });
  });

  describe('Different Haptic Patterns', () => {
    beforeEach(() => {
      service.initialize(false, true);
    });

    it('should trigger different patterns based on action', () => {
      expect(service.triggerBlockHaptic()).toBe(true);
      expect(service.triggerAllowHaptic()).toBe(true);
      expect(service.triggerSuccessHaptic()).toBe(true);
      expect(service.triggerErrorHaptic()).toBe(true);
    });

    it('should respect intensity for all patterns', () => {
      service.setIntensity('light');
      expect(service.triggerBlockHaptic()).toBe(true);
      service.setIntensity('strong');
      expect(service.triggerBlockHaptic()).toBe(true);
    });

    it('should not trigger any pattern when disabled', () => {
      service.setEnabled(false);
      expect(service.triggerBlockHaptic()).toBe(false);
      expect(service.triggerAllowHaptic()).toBe(false);
      expect(service.triggerSuccessHaptic()).toBe(false);
      expect(service.triggerErrorHaptic()).toBe(false);
    });
  });
});
