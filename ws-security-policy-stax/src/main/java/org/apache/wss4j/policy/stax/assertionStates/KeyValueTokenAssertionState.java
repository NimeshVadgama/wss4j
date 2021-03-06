/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wss4j.policy.stax.assertionStates;

import org.apache.wss4j.policy.WSSPolicyException;
import org.apache.wss4j.policy.model.AbstractSecurityAssertion;
import org.apache.wss4j.policy.model.AbstractToken;
import org.apache.wss4j.policy.model.KeyValueToken;
import org.apache.wss4j.stax.securityToken.RsaKeyValueSecurityToken;
import org.apache.wss4j.stax.securityEvent.KeyValueTokenSecurityEvent;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.TokenSecurityEvent;
import org.apache.xml.security.stax.securityToken.SecurityToken;
import org.apache.wss4j.stax.securityEvent.WSSecurityEventConstants;

/**
 * WSP1.3, 5.4.11 KeyValueToken Assertion
 */

public class KeyValueTokenAssertionState extends TokenAssertionState {

    public KeyValueTokenAssertionState(AbstractSecurityAssertion assertion, boolean asserted, boolean initiator) {
        super(assertion, asserted, initiator);
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                WSSecurityEventConstants.KeyValueToken
        };
    }

    @Override
    public boolean assertToken(TokenSecurityEvent<? extends SecurityToken> tokenSecurityEvent,
                               AbstractToken abstractToken) throws WSSPolicyException {
        if (!(tokenSecurityEvent instanceof KeyValueTokenSecurityEvent)) {
            throw new WSSPolicyException("Expected a KeyValueTokenSecurityEvent but got " + tokenSecurityEvent.getClass().getName());
        }

        KeyValueTokenSecurityEvent keyValueTokenSecurityEvent = (KeyValueTokenSecurityEvent) tokenSecurityEvent;
        KeyValueToken keyValueToken = (KeyValueToken) abstractToken;
        if (keyValueToken.isRsaKeyValue() && !(keyValueTokenSecurityEvent.getSecurityToken() instanceof RsaKeyValueSecurityToken)) {
            setErrorMessage("Policy enforces that a RsaKeyValue must be present in the KeyValueToken but we got a " +
                    keyValueTokenSecurityEvent.getSecurityToken().getClass().getSimpleName());
            return false;
        }
        //always return true to prevent false alarm in case additional tokens with the same usage
        //appears in the message but do not fulfill the policy and are also not needed to fulfil the policy.
        return true;
    }
}
