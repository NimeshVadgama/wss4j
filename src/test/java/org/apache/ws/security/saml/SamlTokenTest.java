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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.common.SAML1CallbackHandler;
import org.apache.ws.security.common.SAML2CallbackHandler;
import org.apache.ws.security.common.SOAPUtil;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSAMLToken;
import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.SAMLIssuer;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.util.WSSecurityUtil;

import org.w3c.dom.Document;

import java.util.List;

/**
 * Test-case for sending and processing an unsigned (sender vouches) SAML Assertion.
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class SamlTokenTest extends org.junit.Assert {
    private static final Log LOG = LogFactory.getLog(SamlTokenTest.class);
    private WSSecurityEngine secEngine = new WSSecurityEngine();

    /**
     * Test that creates, sends and processes an unsigned SAML 1.1 authentication assertion.
     */
    @org.junit.Test
    public void testSAML1AuthnAssertion() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.AUTHN);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Authn Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 1 authentication assertion, where
     * the configuration is loaded from a properties file
     */
    @org.junit.Test
    public void testSAML1AuthnAssertionFromProperties() throws Exception {
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml_sv.properties");
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Authn Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 1.1 attribute assertion.
     */
    @org.junit.Test
    public void testSAML1AttrAssertion() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.ATTR);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Attr Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 1.1 authorization assertion.
     */
    @org.junit.Test
    public void testSAML1AuthzAssertion() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.AUTHZ);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setIssuerName("www.example.com");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Authz Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion.
     */
    @org.junit.Test
    public void testSAML2AuthnAssertion() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setSamlVersion("2.0");
        saml.setIssuerName("www.example.com");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Authn Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 attribute assertion.
     */
    @org.junit.Test
    public void testSAML2AttrAssertion() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.ATTR);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setSamlVersion("2.0");
        saml.setIssuerName("www.example.com");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Attr Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authorization assertion.
     */
    @org.junit.Test
    public void testSAML2AuthzAssertion() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHZ);
        SAMLIssuer saml = new SAMLIssuerImpl();
        saml.setSamlVersion("2.0");
        saml.setIssuerName("www.example.com");
        saml.setCallbackHandler(callbackHandler);
        AssertionWrapper assertion = saml.newAssertion();

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, assertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Authz Assertion (sender vouches):");
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper receivedAssertion = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedAssertion != null);
        assert !receivedAssertion.isSigned();
    }

    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param envelope 
     * @throws Exception Thrown when there is a problem in verification
     */
    private List<WSSecurityEngineResult> verify(Document doc) throws Exception {
        List<WSSecurityEngineResult> results = 
            secEngine.processSecurityHeader(doc, null, null, null);
        String outputString = 
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("counter_port_type") > 0 ? true : false);
        return results;
    }

}
