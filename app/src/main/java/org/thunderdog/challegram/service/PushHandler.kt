package org.thunderdog.challegram.service

import android.content.Context
import org.drinkless.tdlib.TdApi
import org.thunderdog.challegram.Log
import org.thunderdog.challegram.TDLib
import org.thunderdog.challegram.telegram.TdlibManager
import org.thunderdog.challegram.tool.UI
import org.thunderdog.challegram.unsorted.Settings
import tgx.bridge.PushManager
import tgx.td.stringify

class PushHandler : PushManager {
  override fun onNewToken(context: Context, token: TdApi.DeviceToken) {
    UI.initApp(context.applicationContext)
    log("onNewToken %s, sending to all accounts", token)
    TdlibManager.instance().runWithWakeLock { manager ->
      manager.setDeviceToken(token)
    }
  }

  override fun onMessageReceived(context: Context, message: Map<String, Any>, sentTime: Long, ttl: Int) {
    UI.initApp(context.applicationContext)
    val pushId = Settings.instance().newPushId()

    val payload = stringify(message)

    val pushProcessor = PushProcessor(context)
    pushProcessor.processPush(pushId, payload, sentTime, ttl)
  }

  override fun log(format: String, vararg args: Any) =
    TDLib.Tag.notifications(format, args)

  override fun error(message: String, error: Throwable?) {
    TDLib.Tag.notifications("$message: ${Log.toString(error)}")
  }
}
