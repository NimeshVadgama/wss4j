/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package wssec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.utils.XMLUtils;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;


/**
 * WS-Security Test Case for UsernameTokens.
 * <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNew5 extends TestCase implements CallbackHandler {
    private static Log log = LogFactory.getLog(TestWSSecurityNew5.class);
    static final String NS = "http://www.w3.org/2000/09/xmldsig#";
    static final String soapMsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + "<SOAP-ENV:Body>" + "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" + "<value xmlns=\"\">15</value>" + "</add>" + "</SOAP-ENV:Body>\r\n       \r\n" + "</SOAP-ENV:Envelope>";
    static final WSSecurityEngine secEngine = new WSSecurityEngine();
    MessageContext msgContext;
    SOAPEnvelope unsignedEnvelope;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityNew5(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNew5.class);
    }

    /**
     * Main method
     * <p/>
     * 
     * @param args command line args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Setup method
     * <p/>
     * 
     * @throws java.lang.Exception Thrown when there is a problem in setup
     */
    protected void setUp() throws Exception {
        AxisClient tmpEngine = new AxisClient(new NullProvider());
        msgContext = new MessageContext(tmpEngine);
        unsignedEnvelope = getSOAPEnvelope();
    }

    /**
     * Constructs a soap envelope
     * <p/>
     * 
     * @return soap envelope
     * @throws java.lang.Exception if there is any problem constructing the soap envelope
     */
    protected SOAPEnvelope getSOAPEnvelope() throws Exception {
        InputStream in = new ByteArrayInputStream(soapMsg.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg.getSOAPEnvelope();
    }

    /**
     * Test that adds a UserNameToken with password Digest to a WS-Security envelope
     * <p/>
     */
    public void testUsernameTokenDigest() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("wernerd", "verySecret");
        log.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        /*
         * convert the resulting document into a message first. The toAxisMessage()
         * method performs the necessary c14n call to properly set up the signed
         * document and convert it into a SOAP message. After that we extract it
         * as a document again for further processing.
         */

        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Digest:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        log.info("After adding UsernameToken PW Digest....");
        verify(signedDoc);
    }
    
    /**
     * Test that a bad username with password digest does not leak whether the username
     * is valid or not - see WSS-141.
     */
    public void testUsernameTokenBadUsername() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("badusername", "verySecret");
        log.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        /*
         * convert the resulting document into a message first. The toAxisMessage()
         * method performs the necessary c14n call to properly set up the signed
         * document and convert it into a SOAP message. After that we extract it
         * as a document again for further processing.
         */

        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Digest:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        log.info("After adding UsernameToken PW Digest....");
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad username");
        } catch (WSSecurityException ex) {
            String message = ex.getMessage();
            assertTrue(message.indexOf("badusername") == -1);
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test that adds a UserNameToken with a bad password Digest to a WS-Security envelope
     * <p/>
     */
    public void testUsernameTokenBadDigest() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setUserInfo("wernerd", "verySecre");
        log.info("Before adding UsernameToken PW Digest....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        /*
         * convert the resulting document into a message first. The toAxisMessage()
         * method performs the necessary c14n call to properly set up the signed
         * document and convert it into a SOAP message. After that we extract it
         * as a document again for further processing.
         */

        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Digest:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        log.info("After adding UsernameToken PW Digest....");
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad password digest");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }

    /**
     * Test that adds a UserNameToken with password text to a WS-Security envelope
     * <p/>
     */
    public void testUsernameTokenText() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo("wernerd", "verySecret");
        log.info("Before adding UsernameToken PW Text....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Text:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        log.info("After adding UsernameToken PW Text....");
        verify(signedDoc);
    }
    
    /**
     * Test that adds a UserNameToken with (bad) password text to a WS-Security envelope
     * <p/>
     */
    public void testUsernameTokenBadText() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_TEXT);
        builder.setUserInfo("wernerd", "verySecre");
        log.info("Before adding UsernameToken PW Text....");
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Text:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        log.info("After adding UsernameToken PW Text....");
        
        try {
            verify(signedDoc);
            throw new Exception("Failure expected on a bad password text");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test with a null token type. This will fail as the default is to reject custom
     * token types.
     * <p/>
     */
    public void testUsernameTokenCustomFail() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(null);
        builder.setUserInfo("wernerd", null);
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Text:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        try {
            verify(signedDoc);
            throw new Exception("Custom token types are not permitted");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Test with a null password type. This will pass as the WSSConfig is configured to 
     * handle custom token types.
     * <p/>
     */
    public void testUsernameTokenCustomPass() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(null);
        builder.setUserInfo("customUser", null);
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);
        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        
        if (log.isDebugEnabled()) {
            log.debug("Message with UserNameToken PW Text:");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        
        //
        // Configure so that custom token types are accepted
        //
        WSSConfig cfg = WSSConfig.getNewInstance();
        cfg.setHandleCustomPasswordTypes(true);
        secEngine.setWssConfig(cfg);
        verify(signedDoc);
        
        //
        // Go back to default for other tests
        //
        cfg.setHandleCustomPasswordTypes(false);
        secEngine.setWssConfig(cfg);
    }
    
    
    /**
     * A test for WSS-66 - the nonce string is null
     * http://issues.apache.org/jira/browse/WSS-66
     * "Possible security hole when PasswordDigest is used by client."
     */
    public void testNullNonce() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_DIGEST);
        builder.setUserInfo("wernerd", "BAD_PASSWORD");
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document utDoc = builder.build(doc, secHeader);
        
        //
        // Manually find the Nonce node and set the content to null
        //
        org.w3c.dom.Element elem = builder.getUsernameTokenElement();
        org.w3c.dom.NodeList list = elem.getElementsByTagName("wsse:Nonce");
        org.w3c.dom.Node nonceNode = list.item(0);
        org.w3c.dom.Node childNode = nonceNode.getFirstChild();
        childNode.setNodeValue("");
        
        if (log.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(utDoc);
            log.debug(outputString);
        }
        
        try {
            //
            // Verification should fail as the password is bad
            //
            verify(utDoc);
            throw new Exception("Expected failure due to a bad password");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * A test for WSS-66 - the created string is null
     * http://issues.apache.org/jira/browse/WSS-66
     * "Possible security hole when PasswordDigest is used by client."
     */
    public void testNullCreated() throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordType(WSConstants.PASSWORD_DIGEST);
        builder.setUserInfo("wernerd", "BAD_PASSWORD");
        
        Document doc = unsignedEnvelope.getAsDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document utDoc = builder.build(doc, secHeader);
        
        //
        // Manually find the Created node and set the content to null
        //
        org.w3c.dom.Element elem = builder.getUsernameTokenElement();
        org.w3c.dom.NodeList list = elem.getElementsByTagName("wsu:Created");
        org.w3c.dom.Node nonceNode = list.item(0);
        org.w3c.dom.Node childNode = nonceNode.getFirstChild();
        childNode.setNodeValue("");
        
        if (log.isDebugEnabled()) {
            String outputString = 
                org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(utDoc);
            log.debug(outputString);
        }
        
        try {
            //
            // Verification should fail as the password is bad
            //
            verify(utDoc);
            throw new Exception("Expected failure due to a bad password");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getErrorCode() == WSSecurityException.FAILED_AUTHENTICATION);
            // expected
        }
    }
    
    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param env soap envelope
     * @throws java.lang.Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        log.info("Before verifying UsernameToken....");
        secEngine.processSecurityHeader(doc, null, this, null);
        log.info("After verifying UsernameToken....");
    }

    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN
                    && "wernerd".equals(pc.getIdentifier())) {
                    pc.setPassword("verySecret");
                } else if (
                    pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN
                ) {
                    if ("wernerd".equals(pc.getIdentifier())
                        && "verySecret".equals(pc.getPassword())) {
                        return;
                    } else if ("customUser".equals(pc.getIdentifier())) {
                        return;
                    } else {
                        throw new IOException("Authentication failed");
                    }
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
}