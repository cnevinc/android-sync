/* Any copyright is dedicated to the Public Domain.
   http://creativecommons.org/publicdomain/zero/1.0/ */

package org.mozilla.android.sync.test.helpers;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.mozilla.gecko.sync.GlobalSession;
import org.mozilla.gecko.sync.NonObjectJSONException;
import org.mozilla.gecko.sync.SyncConfiguration;
import org.mozilla.gecko.sync.SyncConfigurationException;
import org.mozilla.gecko.sync.crypto.KeyBundle;
import org.mozilla.gecko.sync.delegates.GlobalSessionCallback;
import org.mozilla.gecko.sync.stage.GlobalSyncStage.Stage;

import android.content.Context;
import android.content.SharedPreferences;
import org.mozilla.android.sync.test.helpers.MockSharedPreferences;

public class MockGlobalSession extends GlobalSession {

  public MockSharedPreferences prefs;
  private int numClients;

  public MockGlobalSession(String clusterURL, String username, String password,
				  KeyBundle syncKeyBundle, GlobalSessionCallback callback)
    throws SyncConfigurationException, IllegalArgumentException, IOException, ParseException, NonObjectJSONException {
    super(SyncConfiguration.DEFAULT_USER_API, clusterURL, username, password, null, syncKeyBundle, callback, /* context */ null, null, null);
  }

  /*
   * PrefsSource methods.
   */
  @Override
  public SharedPreferences getPrefs(String name, int mode) {
    if (prefs == null) {
      prefs = new MockSharedPreferences();
    }
    return prefs;
  }

  @Override
  public Context getContext() {
    return null;
  }

  @Override
  synchronized public void setNumClients(int numClients) {
    this.numClients = numClients;
  }

  @Override
  synchronized public int getNumClients() {
    return this.numClients;
  }

  @Override
  protected void prepareStages() {
    super.prepareStages();
    // Fake whatever stages we don't want to run.
    stages.put(Stage.syncBookmarks,           new MockServerSyncStage());
    stages.put(Stage.syncHistory,             new MockServerSyncStage());
    stages.put(Stage.fetchInfoCollections,    new MockServerSyncStage());
    stages.put(Stage.fetchMetaGlobal,         new MockServerSyncStage());
    stages.put(Stage.ensureKeysStage,         new MockServerSyncStage());
    stages.put(Stage.ensureClusterURL,        new MockServerSyncStage());
    stages.put(Stage.syncClientsEngine,       new MockServerSyncStage());
  }
}
