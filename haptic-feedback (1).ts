/**
 * Haptic Feedback Service
 * Manages haptic feedback for call blocking actions
 * Respects silent mode and device capabilities
 */

export type HapticIntensity = 'light' | 'medium' | 'strong';

export interface HapticFeedbackConfig {
  enabled: boolean;
  intensity: HapticIntensity;
  respectSilentMode: boolean;
}

/**
 * Haptic Feedback Service
 */
export class HapticFeedbackService {
  private config: HapticFeedbackConfig = {
    enabled: true,
    intensity: 'medium',
    respectSilentMode: true,
  };

  private isSilentMode: boolean = false;
  private isDeviceCapable: boolean = true;

  // Haptic patterns (duration in ms)
  private hapticPatterns: Record<HapticIntensity, { duration: number; strength: number }> = {
    light: { duration: 50, strength: 0.3 },
    medium: { duration: 100, strength: 0.6 },
    strong: { duration: 150, strength: 1.0 },
  };

  /**
   * Initialize haptic feedback service
   */
  initialize(isSilentMode: boolean, isDeviceCapable: boolean): void {
    this.isSilentMode = isSilentMode;
    this.isDeviceCapable = isDeviceCapable;
  }

  /**
   * Trigger haptic feedback for block action
   */
  triggerBlockHaptic(): boolean {
    if (!this.shouldTriggerHaptic()) {
      return false;
    }

    const pattern = this.hapticPatterns[this.config.intensity];
    return this.executeHaptic(pattern.duration, pattern.strength);
  }

  /**
   * Trigger haptic feedback for allow action
   */
  triggerAllowHaptic(): boolean {
    if (!this.shouldTriggerHaptic()) {
      return false;
    }

    // Allow haptic is slightly lighter than block
    const pattern = this.hapticPatterns['light'];
    return this.executeHaptic(pattern.duration, pattern.strength);
  }

  /**
   * Trigger haptic feedback for manual action
   */
  triggerManualActionHaptic(): boolean {
    if (!this.shouldTriggerHaptic()) {
      return false;
    }

    const pattern = this.hapticPatterns[this.config.intensity];
    return this.executeHaptic(pattern.duration, pattern.strength);
  }

  /**
   * Trigger double-tap haptic pattern
   */
  triggerDoubleTapHaptic(): boolean {
    if (!this.shouldTriggerHaptic()) {
      return false;
    }

    const pattern = this.hapticPatterns['light'];
    this.executeHaptic(pattern.duration, pattern.strength);
    setTimeout(() => {
      this.executeHaptic(pattern.duration, pattern.strength);
    }, 100);

    return true;
  }

  /**
   * Trigger success haptic pattern
   */
  triggerSuccessHaptic(): boolean {
    if (!this.shouldTriggerHaptic()) {
      return false;
    }

    // Success pattern: light-medium-light
    const lightPattern = this.hapticPatterns['light'];
    const mediumPattern = this.hapticPatterns['medium'];

    this.executeHaptic(lightPattern.duration, lightPattern.strength);
    setTimeout(() => {
      this.executeHaptic(mediumPattern.duration, mediumPattern.strength);
    }, 100);
    setTimeout(() => {
      this.executeHaptic(lightPattern.duration, lightPattern.strength);
    }, 200);

    return true;
  }

  /**
   * Trigger error haptic pattern
   */
  triggerErrorHaptic(): boolean {
    if (!this.shouldTriggerHaptic()) {
      return false;
    }

    // Error pattern: strong-pause-strong
    const strongPattern = this.hapticPatterns['strong'];

    this.executeHaptic(strongPattern.duration, strongPattern.strength);
    setTimeout(() => {
      this.executeHaptic(strongPattern.duration, strongPattern.strength);
    }, 150);

    return true;
  }

  /**
   * Check if haptic should be triggered
   */
  private shouldTriggerHaptic(): boolean {
    if (!this.config.enabled) {
      return false;
    }

    if (!this.isDeviceCapable) {
      return false;
    }

    if (this.config.respectSilentMode && this.isSilentMode) {
      return false;
    }

    return true;
  }

  /**
   * Execute haptic feedback
   */
  private executeHaptic(duration: number, strength: number): boolean {
    // In a real implementation, this would call native Android haptic APIs
    // For testing, we just log and return success
    console.log(`Haptic: ${duration}ms @ ${Math.round(strength * 100)}%`);
    return true;
  }

  /**
   * Set haptic intensity
   */
  setIntensity(intensity: HapticIntensity): void {
    this.config.intensity = intensity;
  }

  /**
   * Get current intensity
   */
  getIntensity(): HapticIntensity {
    return this.config.intensity;
  }

  /**
   * Enable/disable haptic feedback
   */
  setEnabled(enabled: boolean): void {
    this.config.enabled = enabled;
  }

  /**
   * Check if haptic feedback is enabled
   */
  isEnabled(): boolean {
    return this.config.enabled;
  }

  /**
   * Set silent mode respect
   */
  setRespectSilentMode(respect: boolean): void {
    this.config.respectSilentMode = respect;
  }

  /**
   * Check if silent mode is respected
   */
  doesRespectSilentMode(): boolean {
    return this.config.respectSilentMode;
  }

  /**
   * Update silent mode status
   */
  setSilentMode(isSilent: boolean): void {
    this.isSilentMode = isSilent;
  }

  /**
   * Check if device is in silent mode
   */
  getSilentMode(): boolean {
    return this.isSilentMode;
  }

  /**
   * Update device capability
   */
  setDeviceCapability(capable: boolean): void {
    this.isDeviceCapable = capable;
  }

  /**
   * Check if device is capable of haptic feedback
   */
  getDeviceCapability(): boolean {
    return this.isDeviceCapable;
  }

  /**
   * Get current configuration
   */
  getConfig(): HapticFeedbackConfig {
    return { ...this.config };
  }

  /**
   * Get available intensities
   */
  getAvailableIntensities(): HapticIntensity[] {
    return ['light', 'medium', 'strong'];
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
      const config = JSON.parse(json) as HapticFeedbackConfig;
      if (config.enabled !== undefined && config.intensity !== undefined && config.respectSilentMode !== undefined) {
        this.config = config;
      }
    } catch (error) {
      console.error('Failed to import haptic feedback config:', error);
    }
  }

  /**
   * Reset to defaults
   */
  resetToDefaults(): void {
    this.config = {
      enabled: true,
      intensity: 'medium',
      respectSilentMode: true,
    };
  }
}

// Export singleton instance
export const hapticFeedbackService = new HapticFeedbackService();
