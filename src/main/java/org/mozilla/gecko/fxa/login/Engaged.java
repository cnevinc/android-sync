/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.fxa.login;

import java.security.NoSuchAlgorithmException;

import org.mozilla.gecko.background.fxa.FxAccountClient10.TwoKeys;
import org.mozilla.gecko.background.fxa.FxAccountUtils;
import org.mozilla.gecko.browserid.BrowserIDKeyPair;
import org.mozilla.gecko.fxa.FxAccountConstants;
import org.mozilla.gecko.fxa.login.FxAccountLoginStateMachine.ExecuteDelegate;
import org.mozilla.gecko.fxa.login.FxAccountLoginTransition.LocalError;
import org.mozilla.gecko.fxa.login.FxAccountLoginTransition.LogMessage;
import org.mozilla.gecko.fxa.login.FxAccountLoginTransition.RemoteError;
import org.mozilla.gecko.sync.ExtendedJSONObject;
import org.mozilla.gecko.sync.Utils;

public class Engaged extends State {
  private static final String LOG_TAG = Engaged.class.getSimpleName();

  protected final byte[] sessionToken;
  protected final byte[] keyFetchToken;
  protected final byte[] unwrapkB;

  public Engaged(String email, String uid, boolean verified, byte[] unwrapkB, byte[] sessionToken, byte[] keyFetchToken) {
    super(StateLabel.Engaged, email, uid, verified);
    Utils.throwIfNull(unwrapkB, sessionToken, keyFetchToken);
    this.unwrapkB = unwrapkB;
    this.sessionToken = sessionToken;
    this.keyFetchToken = keyFetchToken;
  }

  @Override
  public ExtendedJSONObject toJSONObject() {
    ExtendedJSONObject o = super.toJSONObject();
    // Fields are non-null by constructor.
    o.put("unwrapkB", Utils.byte2Hex(unwrapkB));
    o.put("sessionToken", Utils.byte2Hex(sessionToken));
    o.put("keyFetchToken", Utils.byte2Hex(keyFetchToken));
    return o;
  }

  @Override
  public void execute(final ExecuteDelegate delegate) {
    BrowserIDKeyPair theKeyPair;
    try {
      theKeyPair = delegate.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      delegate.handleTransition(new LocalError(e), new Doghouse(email, uid, verified));
      return;
    }
    final BrowserIDKeyPair keyPair = theKeyPair;

    delegate.getClient().keys(keyFetchToken, new BaseRequestDelegate<TwoKeys>(this, delegate) {
      @Override
      public void handleSuccess(TwoKeys result) {
        byte[] kB;
        try {
          kB = FxAccountUtils.unwrapkB(unwrapkB, result.wrapkB);
          if (FxAccountConstants.LOG_PERSONAL_INFORMATION) {
            FxAccountConstants.pii(LOG_TAG, "Fetched kA: " + Utils.byte2Hex(result.kA));
            FxAccountConstants.pii(LOG_TAG, "And wrapkB: " + Utils.byte2Hex(result.wrapkB));
            FxAccountConstants.pii(LOG_TAG, "Giving kB : " + Utils.byte2Hex(kB));
          }
        } catch (Exception e) {
          delegate.handleTransition(new RemoteError(e), new Separated(email, uid, verified));
          return;
        }
        delegate.handleTransition(new LogMessage("keys succeeded"), new Cohabiting(email, uid, sessionToken, result.kA, kB, keyPair));
      }
    });
  }

  @Override
  public Action getNeededAction() {
    if (!verified) {
      return Action.NeedsVerification;
    }
    return Action.None;
  }

  public byte[] getSessionToken() {
    return sessionToken;
  }
}
