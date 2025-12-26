package org.thunderdog.challegram.push

import android.content.Context
import org.drinkless.tdlib.TdApi
import org.thunderdog.challegram.Log
import org.thunderdog.challegram.U
import org.thunderdog.challegram.tool.UI
import org.thunderdog.challegram.unsorted.Settings
import org.unifiedpush.android.connector.UnifiedPush
import tgx.bridge.PushManagerBridge
import tgx.bridge.TokenRetrieverListener
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList

object UnifiedPushHelper {
  enum class Status {
    UNSUPPORTED,
    OFF,
    MISSING_DISTRIBUTOR,
    READY
  }

  data class UnifiedPushState(
    val status: Status,
    val distributorName: String? = null
  )

  private const val INSTANCE_ID = "default"
  private val pendingListeners = CopyOnWriteArrayList<TokenRetrieverListener>()

  fun isSupported(context: Context): Boolean {
    if (U.isGooglePlayServicesAvailable(context)) {
      Log.d("UnifiedPush available alongside Google Play Services")
      // Log.d("UnifiedPush ignored because Google Play Services are available")
      // return false
    }
    return true
  }

  fun state(context: Context): UnifiedPushState {
    if (!isSupported(context)) {
      return UnifiedPushState(Status.UNSUPPORTED)
    }
    if (!Settings.instance().isUnifiedPushEnabled) {
      return UnifiedPushState(Status.OFF)
    }
    val distributor = resolveDistributor(context)
      ?: return UnifiedPushState(Status.MISSING_DISTRIBUTOR)
    return UnifiedPushState(Status.READY, resolveDistributorName(context, distributor))
  }

  fun requestRegistration(context: Context, listener: TokenRetrieverListener? = null) {
    if (!isSupported(context)) {
      listener?.onTokenRetrievalError("UNSUPPORTED", null)
      return
    }
    if (!hasDistributor(context)) {
      listener?.onTokenRetrievalError("NO_DISTRIBUTOR", null)
      return
    }
    listener?.let { pendingListeners.add(it) }
    try {
      UnifiedPush.registerApp(context.applicationContext, INSTANCE_ID, ArrayList(), "Telegram X")
      Settings.instance().isUnifiedPushEnabled = true
    } catch (t: Throwable) {
      Log.e("UnifiedPush registerApp failed", t)
      notifyError("REGISTER_FAILED", t)
    }
  }

  fun unregister(context: Context) {
    try {
      UnifiedPush.unregisterApp(context.applicationContext, INSTANCE_ID)
    } catch (t: Throwable) {
      Log.e("UnifiedPush unregister failed", t)
    }
    Settings.instance().isUnifiedPushEnabled = false
  }

  fun handleNewEndpoint(context: Context, endpoint: String) {
    UI.initApp(context.applicationContext)
    val deviceToken = TdApi.DeviceTokenSimplePush(endpoint)
    Settings.instance().isUnifiedPushEnabled = true
    PushManagerBridge.onNewToken(context.applicationContext, deviceToken)
    notifySuccess(deviceToken)
  }

  fun handleRegistrationFailed(error: String?, throwable: Throwable? = null) {
    notifyError(error ?: "REGISTRATION_FAILED", throwable)
  }

  fun handleUnregistered(error: String?) {
    notifyError(error ?: "UNREGISTERED", null)
  }

  fun handleMessage(context: Context, message: ByteArray, sentTime: Long, ttl: Int) {
    UI.initApp(context.applicationContext)
    val payload = try {
      String(message, StandardCharsets.UTF_8)
    } catch (t: Throwable) {
      Log.e("UnifiedPush payload decode failed", t)
      ""
    }
    val payloadMap = hashMapOf<String, Any>("p" to payload)
    PushManagerBridge.onMessageReceived(context.applicationContext, payloadMap, sentTime, ttl)
  }

  fun resolveDistributorName(context: Context, packageName: String?): String? {
    if (packageName.isNullOrEmpty()) {
      return null
    }
    return try {
      val info = context.packageManager.getApplicationInfo(packageName, 0)
      context.packageManager.getApplicationLabel(info).toString()
    } catch (_: Throwable) {
      packageName
    }
  }

  fun selectDistributor(context: Context, packageName: String) {
    try {
      UnifiedPush.saveDistributor(context.applicationContext, packageName)
      requestRegistration(context, null)
    } catch (t: Throwable) {
      Log.e("UnifiedPush saveDistributor failed", t)
    }
  }

  private fun notifySuccess(token: TdApi.DeviceToken) {
    pendingListeners.forEach {
      it.onTokenRetrievalSuccess(token)
    }
    pendingListeners.clear()
  }

  private fun notifyError(errorKey: String, throwable: Throwable?) {
    pendingListeners.forEach {
      it.onTokenRetrievalError(errorKey, throwable)
    }
    pendingListeners.clear()
  }

  private fun hasDistributor(context: Context): Boolean =
    availableDistributors(context).isNotEmpty()

  private fun resolveDistributor(context: Context): String? {
    val appContext = context.applicationContext
    var distributor = UnifiedPush.getAckDistributor(appContext)
    if (distributor.isNullOrEmpty()) {
      distributor = UnifiedPush.getSavedDistributor(appContext)
    }
    if (distributor.isNullOrEmpty()) {
      distributor = availableDistributors(appContext).firstOrNull()
      if (!distributor.isNullOrEmpty()) {
        UnifiedPush.saveDistributor(appContext, distributor)
      }
    }
    return distributor
  }

  fun availableDistributors(context: Context): List<String> = try {
    UnifiedPush.getDistributors(context.applicationContext, ArrayList())
  } catch (t: Throwable) {
    Log.e("UnifiedPush getDistributors failed", t)
    emptyList()
  }
}
