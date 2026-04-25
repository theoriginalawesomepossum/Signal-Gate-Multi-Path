import { callScreeningModule, IncomingCall } from '../native-modules/call-screening/CallScreeningModule';
import { CallScreeningService, CallScreeningResult } from './services/call-screening-integration';
import {
  CallLogService,
  PatternRulesService,
  ManualListService,
  SmartAllowListService,
  ContactGroupWhitelistService,
} from './services';

/**
 * Bridge between native CallScreeningService and TypeScript services
 * 
 * This connects the native Android service to your existing business logic
 */
export class CallScreeningBridge {
  private screeningService: CallScreeningService;
  private unsubscribe: (() => void) | null = null;

  constructor(
    callLog: CallLogService,
    patternRules: PatternRulesService,
    manualList: ManualListService,
    smartAllowList: SmartAllowListService,
    contactGroupWhitelist: ContactGroupWhitelistService
  ) {
    this.screeningService = new CallScreeningService(
      callLog,
      patternRules,
      manualList,
      smartAllowList,
      contactGroupWhitelist
    );
  }

  /**
   * Initialize the bridge
   * Registers the native service and sets up listeners
   */
  async initialize(): Promise<void> {
    try {
      // Register the native service
      await callScreeningModule.registerCallScreeningService();

      // Listen for incoming calls
      this.unsubscribe = callScreeningModule.onIncomingCall(
        (call: IncomingCall) => this.handleIncomingCall(call)
      );

      console.log('CallScreeningBridge initialized');
    } catch (error) {
      console.error('Failed to initialize CallScreeningBridge:', error);
    }
  }

  /**
   * Handle incoming call
   * Screens the call using your business logic
   */
  private async handleIncomingCall(call: IncomingCall): Promise<void> {
    try {
      // Use your existing screening logic
      const result = await this.screeningService.screenCall(call);

      // Send decision back to native service
      const decision = result.action === 'BLOCK' ? 'BLOCK' : 'ALLOW';
      await callScreeningModule.respondToCall(call.phoneNumber, decision);

      console.log(`Call screened: ${call.phoneNumber} -> ${decision}`);
    } catch (error) {
      console.error('Failed to screen call:', error);
      // Allow the call on error
      await callScreeningModule.allowCall(call.phoneNumber);
    }
  }

  /**
   * Cleanup
   */
  destroy(): void {
    if (this.unsubscribe) {
      this.unsubscribe();
    }
  }
}
