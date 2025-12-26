package org.thunderdog.challegram.push

import android.content.Context
import org.unifiedpush.android.connector.MessagingReceiver

class UnifiedPushReceiver : MessagingReceiver() {
  override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
    UnifiedPushHelper.handleNewEndpoint(context, endpoint)
  }

  override fun onRegistrationFailed(context: Context, instance: String) {
    UnifiedPushHelper.handleRegistrationFailed("REGISTRATION_FAILED", null)
  }

  override fun onUnregistered(context: Context, instance: String) {
    UnifiedPushHelper.handleUnregistered("UNREGISTERED")
  }

  override fun onMessage(context: Context, message: ByteArray, instance: String) {
    UnifiedPushHelper.handleMessage(context, message, System.currentTimeMillis(), 0)
  }
}
