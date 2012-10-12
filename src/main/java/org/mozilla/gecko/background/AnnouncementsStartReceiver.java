/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.background;

import org.mozilla.gecko.sync.Logger;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Start the announcements service when instructed by the {@link AlarmManager}.
 */
public class AnnouncementsStartReceiver extends BroadcastReceiver {

  private static final String LOG_TAG = "GeckoSnippets";

  @Override
  public void onReceive(Context context, Intent intent) {
    Logger.debug(LOG_TAG, "AnnouncementsStartReceiver.onReceive().");
    Intent service = new Intent(context, AnnouncementsService.class);
    context.startService(service);
  }
}