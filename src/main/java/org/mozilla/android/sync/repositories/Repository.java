/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Android Sync Client.
 *
 * The Initial Developer of the Original Code is
 * the Mozilla Foundation.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Jason Voll <jvoll@mozilla.com>
 * Richard Newman <rnewman@mozilla.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.android.sync.repositories;

import org.mozilla.android.sync.repositories.delegates.RepositorySessionCreationDelegate;

import android.content.Context;

public abstract class Repository {
  
  protected RepositorySessionCreationDelegate delegate;
  
  public void createSession(Context context, RepositorySessionCreationDelegate delegate, long lastSyncTimestamp) {
    this.delegate = delegate;
    CreateSessionThread thread = new CreateSessionThread(context, lastSyncTimestamp);
    thread.start();
  }
  
  protected abstract void sessionCreator(Context context, long lastSyncTimestamp); 
  
  class CreateSessionThread extends Thread {

    private Context context;
    private long lastSyncTimestamp;

    public CreateSessionThread(Context context, long lastSyncTimestamp) {
      if (context == null) {
        throw new IllegalArgumentException("context is null.");
      }
      this.context = context;
      this.lastSyncTimestamp = lastSyncTimestamp;
    }

    public void run() {
      sessionCreator(context, lastSyncTimestamp);
    }
  }
}
