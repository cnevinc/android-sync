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
 *   Jason Voll <jvoll@mozilla.com>
 *   Richard Newman <rnewman@mozilla.com>
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

package org.mozilla.gecko.sync.repositories.android;

import org.mozilla.gecko.sync.Logger;
import org.mozilla.gecko.sync.repositories.NullCursorException;
import org.mozilla.gecko.sync.repositories.domain.Record;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;

public class AndroidBrowserMergeDataAccessor extends AndroidBrowserRepositoryDataAccessor {
  protected static String LOG_TAG = "MergeDataAccessor";

  protected AndroidBrowserRepositoryDataAccessor regularAccessor;
  protected AndroidBrowserRepositoryDataAccessor deletedAccessor;

  public AndroidBrowserMergeDataAccessor(Context context, AndroidBrowserRepositoryDataAccessor regularAccessor, AndroidBrowserRepositoryDataAccessor deletedAccessor) {
    super(context);
    this.regularAccessor = regularAccessor;
    this.deletedAccessor = deletedAccessor;
  }

  @Override
  protected String[] getAllColumns() {
    throw new IllegalArgumentException("Can't call getAllColumns");
  }

  @Override
  protected ContentValues getContentValuesForInsert(Record record) {
    if (record.deleted) {
      return deletedAccessor.getContentValuesForInsert(record);
    }
    return regularAccessor.getContentValuesForInsert(record);
  }

  @Override
  protected ContentValues getContentValuesForUpdate(Record record) {
    if (record.deleted) {
      return deletedAccessor.getContentValuesForUpdate(record);
    }
    return regularAccessor.getContentValuesForUpdate(record);
  }

  @Override
  protected Uri getUri() {
    // Have to return something valid
    return BrowserContractHelpers.DELETED_FORM_HISTORY_CONTENT_URI;
  }

  /**
   * Dump all the records in raw format.
   */
  @Override
  public void dumpDB() {
    regularAccessor.dumpDB();
    deletedAccessor.dumpDB();
  }

  @Override
  public void wipe() {
    Logger.debug(LOG_TAG, "Wiping...");
    regularAccessor.wipe();
    deletedAccessor.wipe();
    Logger.debug(LOG_TAG, "Wiping... DONE");
  }

  @Override
  public void purgeDeleted() {
    Logger.debug(LOG_TAG, "Purging deleted...");
    deletedAccessor.wipe();
    Logger.debug(LOG_TAG, "Purging deleted... DONE");
  }

  @Override
  public int purgeGuid(String guid) {
    int regular = regularAccessor.purgeGuid(guid);
    int deleted = deletedAccessor.purgeGuid(guid);
    return regular + deleted;
  }

  // guid exists, assume not changing deleted state
  public void update(String guid, Record newRecord) {
    if (newRecord.deleted) {
      deletedAccessor.update(guid,  newRecord);
      return;
    }
    regularAccessor.update(guid,  newRecord);
  }

  // This is only called with ContentValues that are <b>not</b> deleted.
  @Override
  public void updateByGuid(String guid, ContentValues cv) {
    deletedAccessor.purgeGuid(guid);
    updateByGuid(guid, cv);
  }

  /**
   * Insert a <b>new</b> record.
   * <p>
   * Since <code>record</code> is not present in either database, we insert
   * based on <code>record.deleted</code> and do not need to consider deleting
   * from either database.
   */
  public Uri insert(Record record) {
    if (record.deleted) {
      return deletedAccessor.insert(record);
    }
    return regularAccessor.insert(record);
  }

  public Cursor fetchAll() throws NullCursorException {
    Cursor regular = regularAccessor.fetchAll();
    Cursor deleted = deletedAccessor.fetchAll();
    Cursor merge = new MergeCursor(new Cursor[] { regular, deleted });
    Logger.debug(LOG_TAG, "fetchAll... DONE ("
        + merge.getCount() + " total, "
        + regular.getCount() + " regular, "
        + deleted.getCount() + " deleted)");
    return merge;
  }

  public Cursor getGUIDsSince(long timestamp) throws NullCursorException {
    Cursor regular = regularAccessor.getGUIDsSince(timestamp);
    Cursor deleted = deletedAccessor.getGUIDsSince(timestamp);
    Cursor merge = new MergeCursor(new Cursor[] { regular, deleted });
    Logger.debug(LOG_TAG, "getGUIDsSince... DONE ("
        + merge.getCount() + " total, "
        + regular.getCount() + " regular, "
        + deleted.getCount() + " deleted)");
    return merge;
  }

  public Cursor fetchSince(long timestamp) throws NullCursorException {
    Cursor regular = regularAccessor.fetchSince(timestamp);
    Cursor deleted = deletedAccessor.fetchSince(timestamp);
    return new MergeCursor(new Cursor[] { regular, deleted });
  }

  public Cursor fetch(String guids[]) throws NullCursorException {
    Cursor regular = regularAccessor.fetch(guids);
    Cursor deleted = deletedAccessor.fetch(guids);
    return new MergeCursor(new Cursor[] { regular, deleted });
  }
}