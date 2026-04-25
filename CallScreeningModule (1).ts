import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const { CallScreeningModule: NativeCallScreeningModule } = NativeModules;

export interface CallScreeningResult {
  action: 'ALLOW' | 'BLOCK';
  reason: string;
}

export interface IncomingCall {
  phoneNumber: string;
  displayName?: string;
  timestamp: number;
  isIncoming: boolean;
}

/**
 * CallScreeningModule - TypeScript bridge to native call screening
 * 
 * This module:
 * 1. Registers/unregisters the CallScreeningService
 * 2. Listens for incoming calls
 * 3. Sends decisions back to native service
 */
export class CallScreeningModule {
  private static instance: CallScreeningModule;
  private eventEmitter: NativeEventEmitter | null = null;
  private incomingCallListener: ((call: IncomingCall) => void) | null = null;

  private constructor() {
    if (Platform.OS === 'android' && NativeCallScreeningModule) {
      this.eventEmitter = new NativeEventEmitter(NativeCallScreeningModule);
    }
  }

  static getInstance(): CallScreeningModule {
    if (!CallScreeningModule.instance) {
      CallScreeningModule.instance = new CallScreeningModule();
    }
    return CallScreeningModule.instance;
  }

  /**
   * Register the call screening service
   */
  async registerCallScreeningService(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      console.warn('CallScreeningModule not available on this platform');
      return false;
    }

    try {
      const result = await NativeCallScreeningModule.registerCallScreeningService();
      console.log('CallScreeningService registered:', result);
      return result;
    } catch (error) {
      console.error('Failed to register CallScreeningService:', error);
      return false;
    }
  }

  /**
   * Unregister the call screening service
   */
  async unregisterCallScreeningService(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      const result = await NativeCallScreeningModule.unregisterCallScreeningService();
      console.log('CallScreeningService unregistered:', result);
      return result;
    } catch (error) {
      console.error('Failed to unregister CallScreeningService:', error);
      return false;
    }
  }

  /**
   * Check if app is set as the default screening app
   */
  async isDefaultScreeningApp(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.isDefaultScreeningApp();
    } catch (error) {
      console.error('Failed to check default screening app:', error);
      return false;
    }
  }

  /**
   * Request the user to set the app as the default screening app
   */
  async requestDefaultScreeningApp(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.requestDefaultScreeningApp();
    } catch (error) {
      console.error('Failed to request default screening app:', error);
      return false;
    }
  }

  /**
   * Block a call
   */
  async blockCall(phoneNumber: string): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.blockCall(phoneNumber);
    } catch (error) {
      console.error('Failed to block call:', error);
      return false;
    }
  }

  /**
   * Allow a call
   */
  async allowCall(phoneNumber: string): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.allowCall(phoneNumber);
    } catch (error) {
      console.error('Failed to allow call:', error);
      return false;
    }
  }

  /**
   * Respond to a call with a decision
   */
  async respondToCall(phoneNumber: string, decision: 'BLOCK' | 'ALLOW'): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.respondToCall(phoneNumber, decision);
    } catch (error) {
      console.error('Failed to respond to call:', error);
      return false;
    }
  }

  /**
   * Listen for incoming calls
   */
  onIncomingCall(callback: (call: IncomingCall) => void): () => void {
    this.incomingCallListener = callback;

    if (!this.eventEmitter) {
      console.warn('Event emitter not available');
      return () => {};
    }

    const subscription = this.eventEmitter.addListener('onIncomingCall', (call: IncomingCall) => {
      callback(call);
    });

    // Return unsubscribe function
    return () => {
      subscription.remove();
      this.incomingCallListener = null;
    };
  }

  /**
   * Get screening status
   */
  async getScreeningStatus(): Promise<any> {
    if (!NativeCallScreeningModule) {
      return null;
    }

    try {
      return await NativeCallScreeningModule.getScreeningStatus();
    } catch (error) {
      console.error('Failed to get screening status:', error);
      return null;
    }
  }
}

// Export singleton instance
export const callScreeningModule = CallScreeningModule.getInstance();
