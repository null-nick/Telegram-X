package org.thunderdog.challegram.unsorted

import me.vkryl.annotation.MadeByLLM
import tgx.bridge.DeviceTokenRetriever
import tgx.bridge.PushManagerBridge

object DeviceTokenRetrieverInstance {
  private var deviceTokenRetriever: DeviceTokenRetriever? = null

  @JvmStatic
  @Synchronized
  @Suppress("USELESS_ELVIS")
  fun initialize(): Boolean {
    return deviceTokenRetriever.let {
      if (it != null) {
        it
      } else {
        val retriever = PushManagerBridge.onCreateNewTokenRetriever(AppContext.get())
          ?: return false
        deviceTokenRetriever = retriever
        retriever
      }
    }.initialize(AppContext.get())
  }

  @JvmStatic
  fun get(): DeviceTokenRetriever {
    if (deviceTokenRetriever == null) {
      initialize()
    }
    return deviceTokenRetriever!!
  }

  @JvmStatic
  @Synchronized
  @MadeByLLM("claude-opus-5-5", id = "merge-upstream-1833")
  fun invalidate() {
    deviceTokenRetriever = null
  }
}