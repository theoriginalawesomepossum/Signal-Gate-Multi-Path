import { describe, it, expect, beforeEach } from 'vitest';
import { EncryptionService } from '../../src/lib/services/encryption';

describe('EncryptionService', () => {
  let service: EncryptionService;

  beforeEach(() => {
    service = EncryptionService.getInstance();
  });

  describe('encryptSensitiveData', () => {
    it('should encrypt data successfully', async () => {
      const plaintext = 'sensitive data';
      const encrypted = await service.encryptSensitiveData(plaintext);

      expect(encrypted).toBeDefined();
      expect(encrypted.ciphertext).toBeDefined();
      expect(encrypted.iv).toBeDefined();
      expect(encrypted.authTag).toBeDefined();
      expect(encrypted.algorithm).toBe('AES-256-GCM');
    });

    it('should produce different ciphertexts for same plaintext', async () => {
      const plaintext = 'sensitive data';
      const encrypted1 = await service.encryptSensitiveData(plaintext);
      const encrypted2 = await service.encryptSensitiveData(plaintext);

      // IVs should be different (random)
      expect(encrypted1.iv).not.toBe(encrypted2.iv);
    });

    it('should handle empty strings', async () => {
      const encrypted = await service.encryptSensitiveData('');
      expect(encrypted).toBeDefined();
    });

    it('should handle long strings', async () => {
      const longString = 'a'.repeat(10000);
      const encrypted = await service.encryptSensitiveData(longString);
      expect(encrypted).toBeDefined();
    });
  });

  describe('decryptSensitiveData', () => {
    it('should decrypt encrypted data', async () => {
      const plaintext = 'sensitive data';
      const encrypted = await service.encryptSensitiveData(plaintext);
      const decrypted = await service.decryptSensitiveData(encrypted);

      expect(decrypted).toBe(plaintext);
    });

    it('should fail with invalid auth tag', async () => {
      const plaintext = 'sensitive data';
      const encrypted = await service.encryptSensitiveData(plaintext);

      // Corrupt the auth tag
      encrypted.authTag = '0'.repeat(32);

      await expect(service.decryptSensitiveData(encrypted)).rejects.toThrow();
    });

    it('should handle round-trip encryption', async () => {
      const testData = [
        'simple text',
        '123456789',
        'special!@#$%^&*()',
        'unicode: 你好世界',
      ];

      for (const data of testData) {
        const encrypted = await service.encryptSensitiveData(data);
        const decrypted = await service.decryptSensitiveData(encrypted);
        expect(decrypted).toBe(data);
      }
    });
  });

  describe('getOrCreateKey', () => {
    it('should return a key ID', async () => {
      const keyId = await service.getOrCreateKey();
      expect(keyId).toBeDefined();
      expect(typeof keyId).toBe('string');
    });

    it('should return same key on multiple calls', async () => {
      const keyId1 = await service.getOrCreateKey();
      const keyId2 = await service.getOrCreateKey();
      expect(keyId1).toBe(keyId2);
    });
  });

  describe('encryptObject', () => {
    it('should encrypt and decrypt objects', async () => {
      const obj = {
        phoneNumber: '+1-555-0123',
        reason: 'MANUAL_BLOCK',
        timestamp: Date.now(),
      };

      const encrypted = await service.encryptObject(obj);
      const decrypted = await service.decryptObject(encrypted);

      expect(decrypted).toEqual(obj);
    });

    it('should handle nested objects', async () => {
      const obj = {
        user: {
          name: 'John Doe',
          email: 'john@example.com',
          settings: {
            darkMode: true,
            notifications: false,
          },
        },
      };

      const encrypted = await service.encryptObject(obj);
      const decrypted = await service.decryptObject(encrypted);

      expect(decrypted).toEqual(obj);
    });

    it('should handle arrays', async () => {
      const obj = {
        numbers: ['+1-555-0001', '+1-555-0002', '+1-555-0003'],
      };

      const encrypted = await service.encryptObject(obj);
      const decrypted = await service.decryptObject(encrypted);

      expect(decrypted).toEqual(obj);
    });
  });

  describe('hashData', () => {
    it('should hash data', async () => {
      const data = 'test data';
      const hash = await service.hashData(data);

      expect(hash).toBeDefined();
      expect(typeof hash).toBe('string');
      expect(hash.length).toBeGreaterThan(0);
    });

    it('should produce same hash for same data', async () => {
      const data = 'test data';
      const hash1 = await service.hashData(data);
      const hash2 = await service.hashData(data);

      expect(hash1).toBe(hash2);
    });

    it('should produce different hashes for different data', async () => {
      const hash1 = await service.hashData('data1');
      const hash2 = await service.hashData('data2');

      expect(hash1).not.toBe(hash2);
    });

    it('should not be reversible', async () => {
      const data = 'sensitive data';
      const hash = await service.hashData(data);

      // Hash should not contain original data
      expect(hash).not.toContain(data);
    });
  });

  describe('security', () => {
    it('should use AES-256-GCM', async () => {
      const encrypted = await service.encryptSensitiveData('test');
      expect(encrypted.algorithm).toBe('AES-256-GCM');
    });

    it('should use random IVs', async () => {
      const encrypted1 = await service.encryptSensitiveData('test');
      const encrypted2 = await service.encryptSensitiveData('test');

      expect(encrypted1.iv).not.toBe(encrypted2.iv);
    });

    it('should have valid auth tags', async () => {
      const encrypted = await service.encryptSensitiveData('test');
      expect(encrypted.authTag.length).toBe(32); // 16 bytes = 32 hex chars
    });
  });
});
