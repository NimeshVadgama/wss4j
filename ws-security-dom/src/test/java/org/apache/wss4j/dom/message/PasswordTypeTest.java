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

package org.apache.wss4j.dom.message;

import org.apache.wss4j.dom.WSSecurityEngine;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSSConfig;
import org.apache.wss4j.dom.common.CustomHandler;
import org.apache.wss4j.dom.common.SOAPUtil;
import org.apache.wss4j.dom.common.UsernamePasswordCallbackHandler;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;

/**
 * This is a test for processing a Username Token to enforce either a plaintext or digest
 * password type. See WSS-255.
 */
public class PasswordTypeTest extends org.junit.Assert {
    private static final org.slf4j.Logger LOG = 
        org.slf4j.LoggerFactory.getLogger(PasswordTypeTest.class);
    private CallbackHandler callbackHandler = new UsernamePasswordCallbackHandler();

    /**
     * Test that adds a UserNameToken with password Digest to a WS-Security envelope
     */
    @org.junit.Test
    public void testPasswordDigest() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("wernerd", "verySecret");
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Digest:");
            String outputString = 
                XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        WSSecurityEngine secEngine = new WSSecurityEngine();
        WSSConfig wssConfig = WSSConfig.getNewInstance();
        
        //
        // It should pass with PASSWORD_DIGEST
        //
        wssConfig.setRequiredPasswordType(WSConstants.PASSWORD_DIGEST);
        secEngine.setWssConfig(wssConfig);
        secEngine.processSecurityHeader(doc, null, callbackHandler, null);
        
        //
        // It should pass with null
        //
        wssConfig.setRequiredPasswordType(null);
        secEngine.setWssConfig(wssConfig);
        secEngine.processSecurityHeader(doc, null, callbackHandler, null);
        
        //
        // It should fail with PASSWORD_TEXT
        //
        try {
            wssConfig.setRequiredPasswordType(WSConstants.PASSWORD_TEXT);
            secEngine.setWssConfig(wssConfig);
            secEngine.processSecurityHeader(doc, null, callbackHandler, null);
            fail("Expected failure on the wrong password type");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test that adds a UserNameToken with password text to a WS-Security envelope
     */
    @org.junit.Test
    public void testUsernameTokenText() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo("wernerd", "verySecret");
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message with UserNameToken PW Text:");
            String outputString = 
                XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        WSSecurityEngine secEngine = new WSSecurityEngine();
        WSSConfig wssConfig = WSSConfig.getNewInstance();
        
        //
        // It should pass with PASSWORD_TEXT
        //
        wssConfig.setRequiredPasswordType(WSConstants.PASSWORD_TEXT);
        secEngine.setWssConfig(wssConfig);
        secEngine.processSecurityHeader(doc, null, callbackHandler, null);
        
        //
        // It should pass with null
        //
        wssConfig.setRequiredPasswordType(null);
        secEngine.setWssConfig(wssConfig);
        secEngine.processSecurityHeader(doc, null, callbackHandler, null);
        
        //
        // It should fail with PASSWORD_DIGEST
        //
        try {
            wssConfig.setRequiredPasswordType(WSConstants.PASSWORD_DIGEST);
            secEngine.setWssConfig(wssConfig);
            secEngine.processSecurityHeader(doc, null, callbackHandler, null);
            fail("Expected failure on the wrong password type");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
            // expected
        }
        
    }
    
    /**
     * Test that adds a UserNameToken via WSHandler
     */
    @org.junit.Test
    public void testUsernameTokenWSHandler() throws Exception {
        CustomHandler handler = new CustomHandler();
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        
        RequestData reqData = new RequestData();
        java.util.Map<String, Object> config = new java.util.TreeMap<String, Object>();
        config.put("password", "verySecret");
        config.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        reqData.setUsername("wernerd");
        reqData.setMsgContext(config);
        
        java.util.List<Integer> actions = new java.util.ArrayList<Integer>();
        actions.add(WSConstants.UT);
        
        handler.send(WSConstants.UT, doc, reqData, actions, true);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Username Token via WSHandler");
            String outputString = 
                XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
        
        //
        // It should fail on a different password type
        //
        config.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
        reqData.setMsgContext(config);
        handler.receive(WSConstants.UT, reqData);
        WSSecurityEngine secEngine = new WSSecurityEngine();
        secEngine.setWssConfig(reqData.getWssConfig());
        
        try {
            secEngine.processSecurityHeader(doc, null, callbackHandler, null);
            fail("Expected failure on the wrong password type");
        } catch (WSSecurityException ex) {
            // expected
        }
    }

}
