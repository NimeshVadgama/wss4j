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

import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.policy.WSSPolicyException;
import org.apache.wss4j.policy.model.AbstractSecurityAssertion;
import org.apache.wss4j.policy.model.AbstractToken;
import org.apache.wss4j.policy.model.SamlToken;
import org.apache.wss4j.stax.securityToken.WSSecurityTokenConstants;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.opensaml.common.SAMLVersion;
import org.apache.wss4j.stax.securityEvent.SamlTokenSecurityEvent;
import org.apache.wss4j.stax.securityEvent.WSSecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.TokenSecurityEvent;
import org.apache.xml.security.stax.securityToken.SecurityToken;

/**
 * WSP1.3, 5.4.8 SamlToken Assertion
 */

public class SamlTokenAssertionState extends TokenAssertionState {

    public SamlTokenAssertionState(AbstractSecurityAssertion assertion, boolean asserted, boolean initiator) {
        super(assertion, asserted, initiator);
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                WSSecurityEventConstants.SamlToken
        };
    }

    @Override
    public boolean assertToken(TokenSecurityEvent<? extends SecurityToken> tokenSecurityEvent,
                               AbstractToken abstractToken) throws WSSPolicyException, XMLSecurityException {
        if (!(tokenSecurityEvent instanceof SamlTokenSecurityEvent)) {
            throw new WSSPolicyException("Expected a SamlTokenSecurityEvent but got " + tokenSecurityEvent.getClass().getName());
        }
        SamlTokenSecurityEvent samlTokenSecurityEvent = (SamlTokenSecurityEvent) tokenSecurityEvent;
        SamlToken samlToken = (SamlToken) abstractToken;

        if (samlToken.getIssuerName() != null && !samlToken.getIssuerName().equals(samlTokenSecurityEvent.getIssuerName())) {
            setErrorMessage("IssuerName in Policy (" + samlToken.getIssuerName() + ") didn't match with the one in the SamlToken (" + samlTokenSecurityEvent.getIssuerName() + ")");
            return false;
        }
        if (samlToken.isRequireKeyIdentifierReference() &&
                !WSSecurityTokenConstants.KeyIdentifier_X509KeyIdentifier.equals(samlTokenSecurityEvent.getSecurityToken().getKeyIdentifier())) {
            setErrorMessage("Policy enforces KeyIdentifierReference but we got " + samlTokenSecurityEvent.getSecurityToken().getTokenType());
            return false;
        }
        if (samlToken.getSamlTokenType() != null) {
            final SamlAssertionWrapper samlAssertionWrapper = samlTokenSecurityEvent.getSamlAssertionWrapper();
            switch (samlToken.getSamlTokenType()) {
                case WssSamlV11Token10:
                    if (samlAssertionWrapper.getSamlVersion() != SAMLVersion.VERSION_10) {
                        setErrorMessage("Policy enforces SamlVersion11Profile10 but we got " + samlAssertionWrapper.getSamlVersion());
                        return false;
                    }
                    break;
                case WssSamlV11Token11:
                    if (samlAssertionWrapper.getSamlVersion() != SAMLVersion.VERSION_11) {
                        setErrorMessage("Policy enforces SamlVersion11Profile11 but we got " + samlAssertionWrapper.getSamlVersion());
                        return false;
                    }
                    break;
                case WssSamlV20Token11:
                    if (samlAssertionWrapper.getSamlVersion() != SAMLVersion.VERSION_20) {
                        setErrorMessage("Policy enforces SamlVersion20Profile11 but we got " + samlAssertionWrapper.getSamlVersion());
                        return false;
                    }
                    break;
                case WssSamlV10Token10:
                case WssSamlV10Token11:
                    setErrorMessage("Unsupported token type: " + samlToken.getSamlTokenType());
                    return false;
            }
        }
        //always return true to prevent false alarm in case additional tokens with the same usage
        //appears in the message but do not fulfill the policy and are also not needed to fulfil the policy.
        return true;
    }
}
