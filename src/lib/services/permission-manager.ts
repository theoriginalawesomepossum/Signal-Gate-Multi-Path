/**
 * PermissionManager - Handle Android permissions
 * 
 * Manages all required permissions for call screening:
 * - READ_CALL_LOG
 * - READ_PHONE_STATE
 * - SYSTEM_ALERT_WINDOW
 * - ANSWER_PHONE_CALLS
 * - POST_NOTIFICATIONS
 */

export type PermissionStatus = 'granted' | 'denied' | 'pending';

export interface PermissionInfo {
  name: string;
  status: PermissionStatus;
  description: string;
  required: boolean;
}

export class PermissionManager {
  private static instance: PermissionManager;
  private permissions: Map<string, PermissionStatus> = new Map();

  private requiredPermissions = [
    'android.permission.READ_CALL_LOG',
    'android.permission.READ_PHONE_STATE',
    'android.permission.SYSTEM_ALERT_WINDOW',
    'android.permission.ANSWER_PHONE_CALLS',
    'android.permission.POST_NOTIFICATIONS',
  ];

  private permissionDescriptions: Record<string, string> = {
    'android.permission.READ_CALL_LOG': 'Access your call history to screen calls',
    'android.permission.READ_PHONE_STATE': 'Monitor incoming calls for screening',
    'android.permission.SYSTEM_ALERT_WINDOW': 'Show call screening notifications',
    'android.permission.ANSWER_PHONE_CALLS': 'Automatically handle calls',
    'android.permission.POST_NOTIFICATIONS': 'Send you notifications about blocked calls',
  };

  private constructor() {
    this.initializePermissions();
  }

  static getInstance(): PermissionManager {
    if (!PermissionManager.instance) {
      PermissionManager.instance = new PermissionManager();
    }
    return PermissionManager.instance;
  }

  /**
   * Initialize all permissions as pending
   */
  private initializePermissions(): void {
    this.requiredPermissions.forEach((permission) => {
      this.permissions.set(permission, 'pending');
    });
  }

  /**
   * Request call screening permission
   * 
   * This is the main permission needed for the app to function.
   */
  async requestCallScreeningPermission(): Promise<boolean> {
    try {
      console.log('[PermissionManager] Requesting call screening permission');

      // Check if already granted
      const status = this.permissions.get('android.permission.READ_CALL_LOG');
      if (status === 'granted') {
        console.log('[PermissionManager] Call screening permission already granted');
        return true;
      }

      // In production, this would use native permission request
      // For now, simulate permission grant
      this.permissions.set('android.permission.READ_CALL_LOG', 'granted');
      this.permissions.set('android.permission.READ_PHONE_STATE', 'granted');

      console.log('[PermissionManager] Call screening permission granted');
      return true;
    } catch (error) {
      console.error('[PermissionManager] Failed to request call screening permission:', error);
      this.permissions.set('android.permission.READ_CALL_LOG', 'denied');
      return false;
    }
  }

  /**
   * Verify all required permissions are granted
   */
  async verifyPermissions(): Promise<PermissionInfo[]> {
    try {
      console.log('[PermissionManager] Verifying permissions');

      const permissionInfos: PermissionInfo[] = this.requiredPermissions.map(
        (permission) => ({
          name: permission.split('.').pop() || permission,
          status: this.permissions.get(permission) || 'pending',
          description: this.permissionDescriptions[permission] || 'Unknown permission',
          required: true,
        })
      );

      return permissionInfos;
    } catch (error) {
      console.error('[PermissionManager] Failed to verify permissions:', error);
      throw error;
    }
  }

  /**
   * Request specific permission
   */
  async requestPermission(permission: string): Promise<boolean> {
    try {
      console.log(`[PermissionManager] Requesting permission: ${permission}`);

      // Check if already granted
      const status = this.permissions.get(permission);
      if (status === 'granted') {
        return true;
      }

      // In production, this would use native permission request
      // For now, simulate permission grant
      this.permissions.set(permission, 'granted');

      console.log(`[PermissionManager] Permission granted: ${permission}`);
      return true;
    } catch (error) {
      console.error(`[PermissionManager] Failed to request permission ${permission}:`, error);
      this.permissions.set(permission, 'denied');
      return false;
    }
  }

  /**
   * Get permission status
   */
  getPermissionStatus(permission: string): PermissionStatus {
    return this.permissions.get(permission) || 'pending';
  }

  /**
   * Check if all permissions are granted
   */
  areAllPermissionsGranted(): boolean {
    return this.requiredPermissions.every(
      (permission) => this.permissions.get(permission) === 'granted'
    );
  }

  /**
   * Check if critical permissions are granted
   */
  areCriticalPermissionsGranted(): boolean {
    const criticalPermissions = [
      'android.permission.READ_CALL_LOG',
      'android.permission.READ_PHONE_STATE',
    ];

    return criticalPermissions.every(
      (permission) => this.permissions.get(permission) === 'granted'
    );
  }

  /**
   * Handle permission revocation
   */
  handlePermissionRevocation(permission: string): void {
    try {
      console.warn(`[PermissionManager] Permission revoked: ${permission}`);
      this.permissions.set(permission, 'denied');

      // If critical permission revoked, disable call screening
      if (permission === 'android.permission.READ_CALL_LOG') {
        console.error('[PermissionManager] Critical permission revoked, disabling call screening');
      }
    } catch (error) {
      console.error('[PermissionManager] Failed to handle permission revocation:', error);
    }
  }

  /**
   * Get all permission info
   */
  getAllPermissionInfo(): PermissionInfo[] {
    return this.requiredPermissions.map((permission) => ({
      name: permission.split('.').pop() || permission,
      status: this.permissions.get(permission) || 'pending',
      description: this.permissionDescriptions[permission] || 'Unknown permission',
      required: true,
    }));
  }

  /**
   * Reset all permissions
   */
  resetPermissions(): void {
    this.permissions.clear();
    this.initializePermissions();
    console.log('[PermissionManager] Permissions reset');
  }
}
