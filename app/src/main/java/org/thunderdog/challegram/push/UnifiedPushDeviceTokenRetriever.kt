package org.thunderdog.challegram.push

import android.content.Context
import tgx.bridge.DeviceTokenRetriever
import tgx.bridge.TokenRetrieverListener

class UnifiedPushDeviceTokenRetriever : DeviceTokenRetriever("unifiedpush") {
  override fun isAvailable(context: Context): Boolean =
    UnifiedPushHelper.isSupported(context)

  override fun performInitialization(context: Context): Boolean =
    isAvailable(context)

  override fun fetchDeviceToken(context: Context, listener: TokenRetrieverListener) {
    val state = UnifiedPushHelper.state(context)
    when (state.status) {
      UnifiedPushHelper.Status.UNSUPPORTED -> {
        listener.onTokenRetrievalError("UNSUPPORTED", null)
      }
      UnifiedPushHelper.Status.MISSING_DISTRIBUTOR -> {
        listener.onTokenRetrievalError("NO_DISTRIBUTOR", null)
      }
      UnifiedPushHelper.Status.OFF -> {
        listener.onTokenRetrievalError("DISABLED", null)
      }
      UnifiedPushHelper.Status.READY -> UnifiedPushHelper.requestRegistration(context, listener)
    }
  }
}
