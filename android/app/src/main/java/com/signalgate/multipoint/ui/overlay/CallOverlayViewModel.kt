package com.signalgate.multipoint.ui.overlay

import androidx.lifecycle.ViewModel
import com.signalgate.multipoint.CallInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Call Overlay UI.
 * Manages the state of the incoming call information and handles user actions.
 */
class CallOverlayViewModel : ViewModel() {

    private val _callInfo = MutableStateFlow<CallInfo?>(null)
    val callInfo: StateFlow<CallInfo?> = _callInfo.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    private val _dismissOverlay = MutableStateFlow(false)
    val dismissOverlay: StateFlow<Boolean> = _dismissOverlay.asStateFlow()

    /**
     * Sets the call information to be displayed.
     */
    fun setCallInfo(info: CallInfo) {
        _callInfo.value = info
    }

    /**
     * Toggles the expanded state for "More Details".
     */
    fun toggleExpanded() {
        _isExpanded.value = !_isExpanded.value
    }

    /**
     * Handles the Allow action.
     */
    fun onAllowClicked() {
        // Business logic to allow call would go here
        dismiss()
    }

    /**
     * Handles the Screen action.
     */
    fun onScreenClicked() {
        // Business logic to screen call would go here
        dismiss()
    }

    /**
     * Handles the Block action.
     */
    fun onBlockClicked() {
        // Business logic to block call would go here
        dismiss()
    }

    /**
     * Signals that the overlay should be dismissed.
     */
    private fun dismiss() {
        _dismissOverlay.value = true
    }

    /**
     * Resets the dismissal flag.
     */
    fun onDismissed() {
        _dismissOverlay.value = false
    }
}
