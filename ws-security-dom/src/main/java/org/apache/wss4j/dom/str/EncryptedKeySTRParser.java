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

package org.apache.wss4j.dom.str;

import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.common.saml.SAMLKeyInfo;
import org.apache.wss4j.common.saml.SAMLUtil;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSDocInfo;
import org.apache.wss4j.dom.WSSecurityEngine;
import org.apache.wss4j.dom.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.BinarySecurity;
import org.apache.wss4j.dom.message.token.SecurityTokenReference;
import org.apache.wss4j.dom.message.token.X509Security;
import org.apache.wss4j.dom.saml.WSSSAMLKeyInfoProcessor;
import org.w3c.dom.Element;

/**
 * This implementation of STRParser is for parsing a SecurityTokenReference element, found in the
 * KeyInfo element associated with an EncryptedKey element
 */
public class EncryptedKeySTRParser implements STRParser {
    
    private static final org.slf4j.Logger LOG = 
        org.slf4j.LoggerFactory.getLogger(EncryptedKeySTRParser.class);
    
    private X509Certificate[] certs;
    
    private REFERENCE_TYPE referenceType;
    
    /**
     * Parse a SecurityTokenReference element and extract credentials.
     * 
     * @param strElement The SecurityTokenReference element
     * @param data the RequestData associated with the request
     * @param wsDocInfo The WSDocInfo object to access previous processing results
     * @param parameters A set of implementation-specific parameters
     * @throws WSSecurityException
     */
    public void parseSecurityTokenReference(
        Element strElement,
        RequestData data,
        WSDocInfo wsDocInfo,
        Map<String, Object> parameters
    ) throws WSSecurityException {
        Crypto crypto = data.getDecCrypto();
        SecurityTokenReference secRef = 
            new SecurityTokenReference(strElement, data.getBSPEnforcer());
        
        String uri = null;
        if (secRef.containsReference()) {
            uri = secRef.getReference().getURI();
            if (uri.charAt(0) == '#') {
                uri = uri.substring(1);
            }
            referenceType = REFERENCE_TYPE.DIRECT_REF;
        } else if (secRef.containsKeyIdentifier()) {
            uri = secRef.getKeyIdentifierValue();
            if (SecurityTokenReference.THUMB_URI.equals(secRef.getKeyIdentifierValueType())) {
                referenceType = REFERENCE_TYPE.THUMBPRINT_SHA1;
            } else {
                referenceType = REFERENCE_TYPE.KEY_IDENTIFIER;
            }
        }
        
        WSSecurityEngineResult result = wsDocInfo.getResult(uri);
        if (result != null) {
            processPreviousResult(result, secRef, data, wsDocInfo);
        } else if (secRef.containsKeyIdentifier()) {
            if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())
                || WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())) {
                SamlAssertionWrapper samlAssertion =
                    STRParserUtil.getAssertionFromKeyIdentifier(
                        secRef, strElement, data, wsDocInfo
                    );
                STRParserUtil.checkSamlTokenBSPCompliance(secRef, samlAssertion, data.getBSPEnforcer());
                
                SAMLKeyInfo samlKi = 
                    SAMLUtil.getCredentialFromSubject(samlAssertion,
                            new WSSSAMLKeyInfoProcessor(data, wsDocInfo), 
                            data.getSigVerCrypto(), data.getCallbackHandler());
                certs = samlKi.getCerts();
            } else {
                STRParserUtil.checkBinarySecurityBSPCompliance(secRef, null, data.getBSPEnforcer());
                certs = secRef.getKeyIdentifier(crypto);
            }
        } else if (secRef.containsX509Data() || secRef.containsX509IssuerSerial()) {
            referenceType = REFERENCE_TYPE.ISSUER_SERIAL;
            certs = secRef.getX509IssuerSerial(crypto);
        } else if (secRef.containsReference()) {
            Element bstElement = 
                secRef.getTokenElement(strElement.getOwnerDocument(), wsDocInfo, data.getCallbackHandler());

            // at this point ... check token type: Binary
            QName el = new QName(bstElement.getNamespaceURI(), bstElement.getLocalName());
            if (el.equals(WSSecurityEngine.BINARY_TOKEN)) {
                X509Security token = new X509Security(bstElement, data.getBSPEnforcer());
                STRParserUtil.checkBinarySecurityBSPCompliance(secRef, token, data.getBSPEnforcer());
                certs = new X509Certificate[]{token.getX509Certificate(crypto)};
            } else {
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.UNSUPPORTED_SECURITY_TOKEN,
                    "unsupportedBinaryTokenType"
                );
            }
        } 
        
        if (LOG.isDebugEnabled() && certs != null && certs[0] != null) {
            LOG.debug("cert: " + certs[0]);
        }
    }
    
    /**
     * Get the X509Certificates associated with this SecurityTokenReference
     * @return the X509Certificates associated with this SecurityTokenReference
     */
    public X509Certificate[] getCertificates() {
        return certs;
    }
    
    /**
     * Get the Principal associated with this SecurityTokenReference
     * @return the Principal associated with this SecurityTokenReference
     */
    public Principal getPrincipal() {
        return null;
    }
    
    /**
     * Get the PublicKey associated with this SecurityTokenReference
     * @return the PublicKey associated with this SecurityTokenReference
     */
    public PublicKey getPublicKey() {
        return null;
    }
    
    /**
     * Get the Secret Key associated with this SecurityTokenReference
     * @return the Secret Key associated with this SecurityTokenReference
     */
    public byte[] getSecretKey() {
        return null;
    }
    
    /**
     * Get whether the returned credential is already trusted or not. This is currently
     * applicable in the case of a credential extracted from a trusted HOK SAML Assertion,
     * and a BinarySecurityToken that has been processed by a Validator. In these cases,
     * the SignatureProcessor does not need to verify trust on the credential.
     * @return true if trust has already been verified on the returned Credential
     */
    public boolean isTrustedCredential() {
        return false;
    }
    
    /**
     * Get how the certificates were referenced
     * @return how the certificates were referenced
     */
    public REFERENCE_TYPE getCertificatesReferenceType() {
        return referenceType;
    }
    
    /**
     * Process a previous security result
     */
    private void processPreviousResult(
        WSSecurityEngineResult result,
        SecurityTokenReference secRef,
        RequestData data,
        WSDocInfo wsDocInfo
    ) throws WSSecurityException {
        int action = (Integer) result.get(WSSecurityEngineResult.TAG_ACTION);
        if (WSConstants.BST == action) {
            BinarySecurity token = 
                (BinarySecurity)result.get(
                    WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN
                );
            STRParserUtil.checkBinarySecurityBSPCompliance(secRef, token, data.getBSPEnforcer());
            certs = 
                (X509Certificate[])result.get(
                    WSSecurityEngineResult.TAG_X509_CERTIFICATES
                );
        } else if (WSConstants.ST_UNSIGNED == action || WSConstants.ST_SIGNED == action) {
            SamlAssertionWrapper samlAssertion =
                (SamlAssertionWrapper)result.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
            STRParserUtil.checkSamlTokenBSPCompliance(secRef, samlAssertion, data.getBSPEnforcer());
          
            SAMLKeyInfo keyInfo = 
                SAMLUtil.getCredentialFromSubject(samlAssertion,
                        new WSSSAMLKeyInfoProcessor(data, wsDocInfo), 
                        data.getSigVerCrypto(), data.getCallbackHandler());
            certs = keyInfo.getCerts();
        } else {
            throw new WSSecurityException(
                WSSecurityException.ErrorCode.UNSUPPORTED_SECURITY_TOKEN,
                "unsupportedBinaryTokenType"
            );
        }
    }
    
}
