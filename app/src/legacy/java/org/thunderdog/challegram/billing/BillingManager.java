/*
 * This file is a part of Telegram X
 * Copyright © 2014 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.thunderdog.challegram.billing;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.thunderdog.challegram.telegram.Tdlib;

/**
 * No-op billing stub for the legacy flavor, whose minSdk is below the level
 * required by the Google Play Billing Library. Mirrors the public surface used
 * by callers so the legacy flavor builds without the billing dependency.
 */
public final class BillingManager {
  private static final BillingManager INSTANCE = new BillingManager();

  public static BillingManager getInstance () {
    return INSTANCE;
  }

  private BillingManager () { }

  public void initialize () { }

  public boolean isBillingAvailable () {
    return false;
  }

  public boolean isReady () {
    return false;
  }

  public void launchPremiumPurchase (@NonNull Activity activity, @NonNull Tdlib tdlib, @Nullable Runnable onCanceled) {
    if (onCanceled != null) {
      onCanceled.run();
    }
  }

  public void restorePurchases (@NonNull Tdlib tdlib) { }

  public void queryExistingPurchases () { }
}
