/**
 * Dark Mode Service
 * Manages dark mode detection and provides color schemes for dark/light modes
 */

export type ColorScheme = 'light' | 'dark';

export interface DarkModeColors {
  overlayBackground: string;
  glowColor: string;
  statusBadgeBackground: string;
  statusBadgeText: string;
  buttonBackground: string;
  buttonText: string;
  sourceTagBackground: string;
  sourceTagText: string;
}

export interface DarkModeConfig {
  enabled: boolean;
  scheme: ColorScheme;
  autoDetect: boolean;
}

/**
 * Dark Mode Service
 */
export class DarkModeService {
  private config: DarkModeConfig = {
    enabled: true,
    scheme: 'light',
    autoDetect: true,
  };

  private colorSchemes: Record<ColorScheme, Record<string, DarkModeColors>> = {
    light: {
      default: {
        overlayBackground: 'rgba(255, 255, 255, 0.9)',
        glowColor: '#0a7ea4',
        statusBadgeBackground: '#E8F5E9',
        statusBadgeText: '#2E7D32',
        buttonBackground: 'rgba(255, 255, 255, 0.8)',
        buttonText: '#11181C',
        sourceTagBackground: 'rgba(0, 0, 0, 0.1)',
        sourceTagText: '#11181C',
      },
      cyberGreen: {
        overlayBackground: 'rgba(255, 255, 255, 0.9)',
        glowColor: '#00FF41',
        statusBadgeBackground: '#E8F5E9',
        statusBadgeText: '#2E7D32',
        buttonBackground: 'rgba(255, 255, 255, 0.8)',
        buttonText: '#11181C',
        sourceTagBackground: 'rgba(0, 255, 65, 0.15)',
        sourceTagText: '#11181C',
      },
      electricBlue: {
        overlayBackground: 'rgba(255, 255, 255, 0.9)',
        glowColor: '#00D4FF',
        statusBadgeBackground: '#E3F2FD',
        statusBadgeText: '#1565C0',
        buttonBackground: 'rgba(255, 255, 255, 0.8)',
        buttonText: '#11181C',
        sourceTagBackground: 'rgba(0, 212, 255, 0.15)',
        sourceTagText: '#11181C',
      },
      warningRed: {
        overlayBackground: 'rgba(255, 255, 255, 0.9)',
        glowColor: '#FF4444',
        statusBadgeBackground: '#FFEBEE',
        statusBadgeText: '#C62828',
        buttonBackground: 'rgba(255, 255, 255, 0.8)',
        buttonText: '#11181C',
        sourceTagBackground: 'rgba(255, 68, 68, 0.15)',
        sourceTagText: '#11181C',
      },
    },
    dark: {
      default: {
        overlayBackground: 'rgba(21, 23, 24, 0.95)',
        glowColor: '#0a7ea4',
        statusBadgeBackground: '#1B5E20',
        statusBadgeText: '#81C784',
        buttonBackground: 'rgba(30, 32, 34, 0.9)',
        buttonText: '#ECEDEE',
        sourceTagBackground: 'rgba(255, 255, 255, 0.1)',
        sourceTagText: '#ECEDEE',
      },
      cyberGreen: {
        overlayBackground: 'rgba(21, 23, 24, 0.95)',
        glowColor: '#00FF41',
        statusBadgeBackground: '#1B5E20',
        statusBadgeText: '#81C784',
        buttonBackground: 'rgba(30, 32, 34, 0.9)',
        buttonText: '#ECEDEE',
        sourceTagBackground: 'rgba(0, 255, 65, 0.2)',
        sourceTagText: '#ECEDEE',
      },
      electricBlue: {
        overlayBackground: 'rgba(21, 23, 24, 0.95)',
        glowColor: '#00D4FF',
        statusBadgeBackground: '#0D47A1',
        statusBadgeText: '#64B5F6',
        buttonBackground: 'rgba(30, 32, 34, 0.9)',
        buttonText: '#ECEDEE',
        sourceTagBackground: 'rgba(0, 212, 255, 0.2)',
        sourceTagText: '#ECEDEE',
      },
      warningRed: {
        overlayBackground: 'rgba(21, 23, 24, 0.95)',
        glowColor: '#FF4444',
        statusBadgeBackground: '#B71C1C',
        statusBadgeText: '#EF5350',
        buttonBackground: 'rgba(30, 32, 34, 0.9)',
        buttonText: '#ECEDEE',
        sourceTagBackground: 'rgba(255, 68, 68, 0.2)',
        sourceTagText: '#ECEDEE',
      },
    },
  };

  /**
   * Initialize dark mode service with device preference
   */
  initialize(deviceColorScheme: ColorScheme): void {
    if (this.config.autoDetect) {
      this.config.scheme = deviceColorScheme;
    }
  }

  /**
   * Get current color scheme
   */
  getColorScheme(): ColorScheme {
    return this.config.scheme;
  }

  /**
   * Set color scheme manually
   */
  setColorScheme(scheme: ColorScheme): void {
    this.config.scheme = scheme;
  }

  /**
   * Toggle between light and dark mode
   */
  toggleDarkMode(): void {
    this.config.scheme = this.config.scheme === 'light' ? 'dark' : 'light';
  }

  /**
   * Enable/disable dark mode
   */
  setDarkModeEnabled(enabled: boolean): void {
    this.config.enabled = enabled;
  }

  /**
   * Check if dark mode is enabled
   */
  isDarkModeEnabled(): boolean {
    return this.config.enabled;
  }

  /**
   * Set auto-detect preference
   */
  setAutoDetect(autoDetect: boolean): void {
    this.config.autoDetect = autoDetect;
  }

  /**
   * Get colors for current scheme and glow color
   */
  getColors(glowColor: 'default' | 'cyberGreen' | 'electricBlue' | 'warningRed' = 'default'): DarkModeColors {
    const scheme = this.config.enabled ? this.config.scheme : 'light';
    return this.colorSchemes[scheme][glowColor];
  }

  /**
   * Get all available color schemes
   */
  getAvailableSchemes(): ColorScheme[] {
    return ['light', 'dark'];
  }

  /**
   * Get all available glow colors
   */
  getAvailableGlowColors(): Array<'default' | 'cyberGreen' | 'electricBlue' | 'warningRed'> {
    return ['default', 'cyberGreen', 'electricBlue', 'warningRed'];
  }

  /**
   * Get current configuration
   */
  getConfig(): DarkModeConfig {
    return { ...this.config };
  }

  /**
   * Export configuration as JSON
   */
  exportConfig(): string {
    return JSON.stringify(this.config, null, 2);
  }

  /**
   * Import configuration from JSON
   */
  importConfig(json: string): void {
    try {
      const config = JSON.parse(json) as DarkModeConfig;
      if (config.enabled !== undefined && config.scheme !== undefined && config.autoDetect !== undefined) {
        this.config = config;
      }
    } catch (error) {
      console.error('Failed to import dark mode config:', error);
    }
  }

  /**
   * Reset to defaults
   */
  resetToDefaults(): void {
    this.config = {
      enabled: true,
      scheme: 'light',
      autoDetect: true,
    };
  }
}

// Export singleton instance
export const darkModeService = new DarkModeService();
