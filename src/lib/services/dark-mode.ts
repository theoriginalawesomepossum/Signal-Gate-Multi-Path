import { describe, it, expect, beforeEach } from 'vitest';
import { DarkModeService, type ColorScheme } from './dark-mode';

describe('Dark Mode Service', () => {
  let service: DarkModeService;

  beforeEach(() => {
    service = new DarkModeService();
  });

  describe('Initialization', () => {
    it('should initialize with default light mode', () => {
      expect(service.getColorScheme()).toBe('light');
    });

    it('should initialize with dark mode enabled by default', () => {
      expect(service.isDarkModeEnabled()).toBe(true);
    });

    it('should initialize with auto-detect enabled', () => {
      const config = service.getConfig();
      expect(config.autoDetect).toBe(true);
    });

    it('should detect device color scheme on initialization', () => {
      service.initialize('dark');
      expect(service.getColorScheme()).toBe('dark');
    });
  });

  describe('Color Scheme Management', () => {
    it('should get current color scheme', () => {
      expect(service.getColorScheme()).toBe('light');
    });

    it('should set color scheme manually', () => {
      service.setColorScheme('dark');
      expect(service.getColorScheme()).toBe('dark');
    });

    it('should toggle between light and dark mode', () => {
      expect(service.getColorScheme()).toBe('light');
      service.toggleDarkMode();
      expect(service.getColorScheme()).toBe('dark');
      service.toggleDarkMode();
      expect(service.getColorScheme()).toBe('light');
    });

    it('should return available color schemes', () => {
      const schemes = service.getAvailableSchemes();
      expect(schemes).toContain('light');
      expect(schemes).toContain('dark');
      expect(schemes.length).toBe(2);
    });
  });

  describe('Dark Mode Toggle', () => {
    it('should enable dark mode', () => {
      service.setDarkModeEnabled(true);
      expect(service.isDarkModeEnabled()).toBe(true);
    });

    it('should disable dark mode', () => {
      service.setDarkModeEnabled(false);
      expect(service.isDarkModeEnabled()).toBe(false);
    });

    it('should return light colors when dark mode is disabled', () => {
      service.setDarkModeEnabled(false);
      service.setColorScheme('dark');
      const colors = service.getColors();
      expect(colors.overlayBackground).toContain('255');
    });

    it('should return dark colors when dark mode is enabled', () => {
      service.setDarkModeEnabled(true);
      service.setColorScheme('dark');
      const colors = service.getColors();
      expect(colors.overlayBackground).toContain('21');
    });
  });

  describe('Color Retrieval', () => {
    it('should get default light mode colors', () => {
      service.setColorScheme('light');
      const colors = service.getColors('default');
      expect(colors.overlayBackground).toContain('255');
      expect(colors.glowColor).toBe('#0a7ea4');
    });

    it('should get default dark mode colors', () => {
      service.setColorScheme('dark');
      const colors = service.getColors('default');
      expect(colors.overlayBackground).toContain('21');
      expect(colors.glowColor).toBe('#0a7ea4');
    });

    it('should get cyber green colors for light mode', () => {
      service.setColorScheme('light');
      const colors = service.getColors('cyberGreen');
      expect(colors.glowColor).toBe('#00FF41');
    });

    it('should get cyber green colors for dark mode', () => {
      service.setColorScheme('dark');
      const colors = service.getColors('cyberGreen');
      expect(colors.glowColor).toBe('#00FF41');
    });

    it('should get electric blue colors for light mode', () => {
      service.setColorScheme('light');
      const colors = service.getColors('electricBlue');
      expect(colors.glowColor).toBe('#00D4FF');
    });

    it('should get electric blue colors for dark mode', () => {
      service.setColorScheme('dark');
      const colors = service.getColors('electricBlue');
      expect(colors.glowColor).toBe('#00D4FF');
    });

    it('should get warning red colors for light mode', () => {
      service.setColorScheme('light');
      const colors = service.getColors('warningRed');
      expect(colors.glowColor).toBe('#FF4444');
    });

    it('should get warning red colors for dark mode', () => {
      service.setColorScheme('dark');
      const colors = service.getColors('warningRed');
      expect(colors.glowColor).toBe('#FF4444');
    });

    it('should return all available glow colors', () => {
      const glowColors = service.getAvailableGlowColors();
      expect(glowColors).toContain('default');
      expect(glowColors).toContain('cyberGreen');
      expect(glowColors).toContain('electricBlue');
      expect(glowColors).toContain('warningRed');
      expect(glowColors.length).toBe(4);
    });
  });

  describe('Auto-Detect', () => {
    it('should enable auto-detect', () => {
      service.setAutoDetect(true);
      const config = service.getConfig();
      expect(config.autoDetect).toBe(true);
    });

    it('should disable auto-detect', () => {
      service.setAutoDetect(false);
      const config = service.getConfig();
      expect(config.autoDetect).toBe(false);
    });

    it('should respect device preference when auto-detect is enabled', () => {
      service.setAutoDetect(true);
      service.initialize('dark');
      expect(service.getColorScheme()).toBe('dark');
    });

    it('should allow manual override when auto-detect is disabled', () => {
      service.setAutoDetect(false);
      service.initialize('dark');
      service.setColorScheme('light');
      expect(service.getColorScheme()).toBe('light');
    });
  });

  describe('Configuration Management', () => {
    it('should get current configuration', () => {
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.scheme).toBe('light');
      expect(config.autoDetect).toBe(true);
    });

    it('should export configuration as JSON', () => {
      service.setColorScheme('dark');
      const json = service.exportConfig();
      expect(json).toContain('"scheme": "dark"');
      expect(json).toContain('"enabled": true');
    });

    it('should import configuration from JSON', () => {
      const json = JSON.stringify({ enabled: false, scheme: 'dark', autoDetect: false });
      service.importConfig(json);
      const config = service.getConfig();
      expect(config.enabled).toBe(false);
      expect(config.scheme).toBe('dark');
      expect(config.autoDetect).toBe(false);
    });

    it('should handle invalid JSON gracefully', () => {
      const originalConfig = service.getConfig();
      const invalidJson = 'invalid json';
      service.importConfig(invalidJson);
      const config = service.getConfig();
      expect(config.enabled).toBe(originalConfig.enabled); // Should remain unchanged
    });

    it('should reset to defaults', () => {
      service.setColorScheme('dark');
      service.setDarkModeEnabled(false);
      service.setAutoDetect(false);
      service.resetToDefaults();
      const config = service.getConfig();
      expect(config.enabled).toBe(true);
      expect(config.scheme).toBe('light');
      expect(config.autoDetect).toBe(true);
    });
  });

  describe('Color Consistency', () => {
    it('should maintain color consistency across all glow colors in light mode', () => {
      service.setColorScheme('light');
      const glowColors = service.getAvailableGlowColors();
      glowColors.forEach((glowColor) => {
        const colors = service.getColors(glowColor);
        expect(colors.overlayBackground).toContain('255');
        expect(colors.buttonText).toBe('#11181C');
      });
    });

    it('should maintain color consistency across all glow colors in dark mode', () => {
      service.setColorScheme('dark');
      const glowColors = service.getAvailableGlowColors();
      glowColors.forEach((glowColor) => {
        const colors = service.getColors(glowColor);
        expect(colors.overlayBackground).toContain('21');
        expect(colors.buttonText).toBe('#ECEDEE');
      });
    });

    it('should have different text colors for light and dark modes', () => {
      service.setDarkModeEnabled(true);
      service.setColorScheme('light');
      const lightColors = service.getColors();
      service.setColorScheme('dark');
      const darkColors = service.getColors();
      expect(lightColors.buttonText).not.toBe(darkColors.buttonText);
      expect(lightColors.buttonText).toBe('#11181C');
      expect(darkColors.buttonText).toBe('#ECEDEE');
    });
  });

  describe('Edge Cases', () => {
    it('should handle multiple toggles correctly', () => {
      const initialScheme = service.getColorScheme();
      for (let i = 0; i < 5; i++) {
        service.toggleDarkMode();
      }
      expect(service.getColorScheme()).toBe('dark');
    });

    it('should handle rapid color scheme changes', () => {
      const schemes: Array<'light' | 'dark'> = ['dark', 'light', 'dark', 'light', 'dark'];
      schemes.forEach((scheme) => service.setColorScheme(scheme));
      expect(service.getColorScheme()).toBe('dark');
    });

    it('should preserve configuration after multiple operations', () => {
      service.setColorScheme('dark');
      service.setDarkModeEnabled(false);
      service.setAutoDetect(false);
      const config1 = service.getConfig();
      const config2 = service.getConfig();
      expect(JSON.stringify(config1)).toBe(JSON.stringify(config2));
    });
  });
});
