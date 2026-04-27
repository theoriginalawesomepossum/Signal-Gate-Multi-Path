import { describe, it, expect, beforeEach } from 'vitest';
import { PermissionManager } from '../../src/lib/services/permission-manager';

describe('PermissionManager', () => {
  let manager: PermissionManager;

  beforeEach(() => {
    manager = PermissionManager.getInstance();
    manager.resetPermissions();
  });

  describe('requestCallScreeningPermission', () => {
    it('should request call screening permission', async () => {
      const result = await manager.requestCallScreeningPermission();
      expect(result).toBe(true);
    });

    it('should return true if already granted', async () => {
      await manager.requestCallScreeningPermission();
      const result = await manager.requestCallScreeningPermission();
      expect(result).toBe(true);
    });
  });

  describe('verifyPermissions', () => {
    it('should return all required permissions', async () => {
      const permissions = await manager.verifyPermissions();
      expect(permissions.length).toBeGreaterThan(0);
    });

    it('should include READ_CALL_LOG permission', async () => {
      const permissions = await manager.verifyPermissions();
      const readCallLog = permissions.find((p) => p.name === 'READ_CALL_LOG');
      expect(readCallLog).toBeDefined();
    });

    it('should include READ_PHONE_STATE permission', async () => {
      const permissions = await manager.verifyPermissions();
      const readPhoneState = permissions.find((p) => p.name === 'READ_PHONE_STATE');
      expect(readPhoneState).toBeDefined();
    });

    it('should have descriptions for all permissions', async () => {
      const permissions = await manager.verifyPermissions();
      permissions.forEach((p) => {
        expect(p.description).toBeDefined();
        expect(p.description.length).toBeGreaterThan(0);
      });
    });
  });

  describe('requestPermission', () => {
    it('should request specific permission', async () => {
      const result = await manager.requestPermission('android.permission.READ_CALL_LOG');
      expect(result).toBe(true);
    });

    it('should return true if already granted', async () => {
      await manager.requestPermission('android.permission.READ_CALL_LOG');
      const result = await manager.requestPermission('android.permission.READ_CALL_LOG');
      expect(result).toBe(true);
    });
  });

  describe('getPermissionStatus', () => {
    it('should return permission status', async () => {
      const status = manager.getPermissionStatus('android.permission.READ_CALL_LOG');
      expect(['granted', 'denied', 'pending']).toContain(status);
    });

    it('should return granted after request', async () => {
      await manager.requestPermission('android.permission.READ_CALL_LOG');
      const status = manager.getPermissionStatus('android.permission.READ_CALL_LOG');
      expect(status).toBe('granted');
    });
  });

  describe('areAllPermissionsGranted', () => {
    it('should return false initially', () => {
      const result = manager.areAllPermissionsGranted();
      expect(result).toBe(false);
    });

    it('should return true after granting all', async () => {
      await manager.requestCallScreeningPermission();
      await manager.requestPermission('android.permission.SYSTEM_ALERT_WINDOW');
      await manager.requestPermission('android.permission.ANSWER_PHONE_CALLS');
      await manager.requestPermission('android.permission.POST_NOTIFICATIONS');

      const result = manager.areAllPermissionsGranted();
      expect(result).toBe(true);
    });
  });

  describe('areCriticalPermissionsGranted', () => {
    it('should return false initially', () => {
      const result = manager.areCriticalPermissionsGranted();
      expect(result).toBe(false);
    });

    it('should return true after granting critical permissions', async () => {
      await manager.requestCallScreeningPermission();
      const result = manager.areCriticalPermissionsGranted();
      expect(result).toBe(true);
    });
  });

  describe('handlePermissionRevocation', () => {
    it('should handle permission revocation', async () => {
      await manager.requestPermission('android.permission.READ_CALL_LOG');
      expect(manager.getPermissionStatus('android.permission.READ_CALL_LOG')).toBe('granted');

      manager.handlePermissionRevocation('android.permission.READ_CALL_LOG');
      expect(manager.getPermissionStatus('android.permission.READ_CALL_LOG')).toBe('denied');
    });
  });

  describe('getAllPermissionInfo', () => {
    it('should return all permission info', () => {
      const infos = manager.getAllPermissionInfo();
      expect(infos.length).toBeGreaterThan(0);
    });

    it('should include all required permissions', () => {
      const infos = manager.getAllPermissionInfo();
      const names = infos.map((i) => i.name);

      expect(names).toContain('READ_CALL_LOG');
      expect(names).toContain('READ_PHONE_STATE');
      expect(names).toContain('SYSTEM_ALERT_WINDOW');
      expect(names).toContain('ANSWER_PHONE_CALLS');
      expect(names).toContain('POST_NOTIFICATIONS');
    });

    it('should mark all as required', () => {
      const infos = manager.getAllPermissionInfo();
      infos.forEach((info) => {
        expect(info.required).toBe(true);
      });
    });
  });

  describe('resetPermissions', () => {
    it('should reset all permissions', async () => {
      await manager.requestCallScreeningPermission();
      expect(manager.areAllPermissionsGranted()).toBe(false);

      manager.resetPermissions();

      const infos = manager.getAllPermissionInfo();
      infos.forEach((info) => {
        expect(info.status).toBe('pending');
      });
    });
  });
});
