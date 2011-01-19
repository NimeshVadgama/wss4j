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

package org.apache.ws.security.saml;

import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.SAMLIssuer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDataRef;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.common.KeystoreCallbackHandler;
import org.apache.ws.security.common.SAML1CallbackHandler;
import org.apache.ws.security.common.SAML2CallbackHandler;
import org.apache.ws.security.common.SOAPUtil;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean.CERT_IDENTIFIER;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
import org.apache.ws.security.saml.ext.builder.SAML2Constants;
import org.apache.ws.security.util.Loader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;

/**
 * Test-case for sending and processing a signed (holder-of-key) SAML Assertion. These tests
 * also cover the case of using the credential information in the SAML Subject to sign the
 * SOAP body.
 */
public class SignedSamlTokenHOKTest extends org.junit.Assert {
    private static final Log LOG = LogFactory.getLog(SignedSamlTokenHOKTest.class);
    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private CallbackHandler callbackHandler = new KeystoreCallbackHandler();
    private Crypto trustCrypto = null;
    private Crypto issuerCrypto = null;
    private Crypto userCrypto = CryptoFactory.getInstance("wss40.properties");
    
    public SignedSamlTokenHOKTest() throws Exception {
        // Load the issuer keystore
        issuerCrypto = new Merlin();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        ClassLoader loader = Loader.getClassLoader(SignedSamlTokenHOKTest.class);
        InputStream input = AbstractCrypto.loadInputStream(loader, "keys/wss40_server.jks");
        keyStore.load(input, "security".toCharArray());
        issuerCrypto.setKeyStore(keyStore);
        
        // Load the server truststore
        trustCrypto = new Merlin();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        input = AbstractCrypto.loadInputStream(loader, "keys/wss40CA.jks");
        trustStore.load(input, "security".toCharArray());
        trustCrypto.setTrustStore(trustStore);
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 1.1 authentication assertion.
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testSAML1AuthnAssertion() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.AUTHN);
        callbackHandler.setConfirmationMethod(SAML1Constants.CONF_HOLDER_KEY);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML 1.1 Authn Assertion (key holder):");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("http://www.w3.org/2001/04/xmlenc#sha256") != -1);
        assertTrue(outputString.indexOf("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256") != -1);
        
        List<WSSecurityEngineResult> results = verify(signedDoc, trustCrypto);
        
        // Test we processed a SAML assertion
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 1.1 authentication assertion, where
     * the configuration is loaded from a properties file
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testSAML1AuthnAssertionFromProperties() throws Exception {
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml_hok.properties");
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML 1.1 Authn Assertion (key holder):");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("http://www.w3.org/2001/04/xmlenc#sha256") != -1);
        assertTrue(outputString.indexOf("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256") != -1);
        
        List<WSSecurityEngineResult> results = verify(signedDoc, trustCrypto);
        
        // Test we processed a SAML assertion
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 1.1 attribute assertion.
     */
    @org.junit.Test
    public void testSAML1AttrAssertion() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.ATTR);
        callbackHandler.setConfirmationMethod(SAML1Constants.CONF_HOLDER_KEY);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();
        byte[] ephemeralKey = callbackHandler.getEphemeralKey();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm(WSConstants.HMAC_SHA256);
        wsSign.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
        wsSign.setSecretKey(ephemeralKey);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML 1.1 Attr Assertion (key holder):");
            LOG.debug(outputString);
        }
        
        /*TODO - Re-enable this when WSS-265 is fixed:
         * https://issues.apache.org/jira/browse/WSS-265
        List<WSSecurityEngineResult> results = verify(signedDoc, trustCrypto);
        
        // Test we processed a SAML assertion
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
        */
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 2 authentication assertion.
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testSAML2AuthnAssertion() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setConfirmationMethod(SAML2Constants.CONF_HOLDER_KEY);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        saml.setSamlVersion("2.0");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML 2 Authn Assertion (key holder):");
            LOG.debug(outputString);
        }
        assertTrue(outputString.indexOf("http://www.w3.org/2001/04/xmlenc#sha256") != -1);
        assertTrue(outputString.indexOf("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256") != -1);
        
        List<WSSecurityEngineResult> results = verify(signedDoc, trustCrypto);
        
        // Test we processed a SAML assertion
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 2 attribute assertion.
     */
    @org.junit.Test
    public void testSAML2AttrAssertion() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.ATTR);
        callbackHandler.setConfirmationMethod(SAML2Constants.CONF_HOLDER_KEY);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        saml.setSamlVersion("2.0");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();
        byte[] ephemeralKey = callbackHandler.getEphemeralKey();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm(WSConstants.HMAC_SHA256);
        wsSign.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
        wsSign.setSecretKey(ephemeralKey);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signed SAML 2 Attr Assertion (key holder):");
            LOG.debug(outputString);
        }
        
        /*
         * TODO - Re-enable this when WSS-265 is fixed:
         * https://issues.apache.org/jira/browse/WSS-265
        List<WSSecurityEngineResult> results = verify(signedDoc, trustCrypto);
        
        // Test we processed a SAML assertion
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
        */
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 1.1 authentication assertion,
     * where the subject cert is referenced using IssuerSerial
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testSAML1AuthnAssertionIssuerSerial() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.AUTHN);
        callbackHandler.setConfirmationMethod(SAML1Constants.CONF_HOLDER_KEY);
        callbackHandler.setCertIdentifier(CERT_IDENTIFIER.X509_ISSUER_SERIAL);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Authn Assertion Issuer Serial (holder-of-key):");
            LOG.debug(outputString);
        }
        assert outputString.contains("X509IssuerSerial");
        
        List<WSSecurityEngineResult> results = verify(signedDoc, userCrypto);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 1.1 authentication assertion,
     * where the subject cert is referenced using a Key Value
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testSAML1AuthnAssertionKeyValue() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.AUTHN);
        callbackHandler.setConfirmationMethod(SAML1Constants.CONF_HOLDER_KEY);
        callbackHandler.setCertIdentifier(CERT_IDENTIFIER.KEY_VALUE);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        // saml.setSamlVersion("2.0");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Authn Assertion Key Value (holder-of-key):");
            LOG.debug(outputString);
        }
        assert outputString.contains("KeyValue");
        
        List<WSSecurityEngineResult> results = verify(signedDoc, userCrypto);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
    }
    
    /**
     * Test that creates, sends and processes a signed SAML 2 authentication assertion,
     * where the subject cert is referenced using a Key Value
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testSAML2AuthnAssertionKeyValue() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setConfirmationMethod(SAML2Constants.CONF_HOLDER_KEY);
        callbackHandler.setCertIdentifier(CERT_IDENTIFIER.KEY_VALUE);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setIssuerCrypto(issuerCrypto);
        saml.setIssuerKeyName("wss40_server");
        saml.setIssuerKeyPassword("security");
        saml.setSignAssertion(true);
        saml.setSamlVersion("2.0");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setUserInfo("wss40", "security");
        wsSign.setDigestAlgo("http://www.w3.org/2001/04/xmlenc#sha256");
        wsSign.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Document signedDoc = 
            wsSign.build(doc, userCrypto, assertion, null, null, null, secHeader);

        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Authn Assertion Key Value (holder-of-key):");
            LOG.debug(outputString);
        }
        assert outputString.contains("KeyValue");
        
        List<WSSecurityEngineResult> results = verify(signedDoc, userCrypto);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert receivedAssertion.isSigned();
        
        // Test we processed a signature (SOAP body)
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
        assertTrue(actionResult != null);
        assertFalse(actionResult.isEmpty());
        final List<WSDataRef> refs =
            (List<WSDataRef>) actionResult.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
        assertTrue(refs.size() == 1);
        
        WSDataRef wsDataRef = (WSDataRef)refs.get(0);
        String xpath = wsDataRef.getXpath();
        assertEquals("/SOAP-ENV:Envelope/SOAP-ENV:Body", xpath);
    }
    
    /**
     * Verifies the soap envelope
     * 
     * @param doc
     * @throws Exception Thrown when there is a problem in verification
     */
    private List<WSSecurityEngineResult> verify(Document doc, Crypto sigCrypto) throws Exception {
        List<WSSecurityEngineResult> results = 
            secEngine.processSecurityHeader(
                doc, null, callbackHandler, sigCrypto, userCrypto
            );
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("counter_port_type") > 0 ? true : false);
        return results;
    }

}
