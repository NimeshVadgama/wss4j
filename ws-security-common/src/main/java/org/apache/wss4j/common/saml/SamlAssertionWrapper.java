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

package org.apache.wss4j.common.saml;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.builder.SAML1ComponentBuilder;
import org.apache.wss4j.common.saml.builder.SAML2ComponentBuilder;
import org.apache.wss4j.common.util.DOM2Writer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.stax.impl.util.IDGenerator;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml1.core.AttributeStatement;
import org.opensaml.saml1.core.AuthenticationStatement;
import org.opensaml.saml1.core.AuthorizationDecisionStatement;
import org.opensaml.saml1.core.ConfirmationMethod;
import org.opensaml.saml1.core.Subject;
import org.opensaml.saml1.core.SubjectConfirmation;
import org.opensaml.saml1.core.SubjectStatement;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.ValidatorSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class SamlAssertionWrapper can generate, sign, and validate both SAML v1.1
 * and SAML v2.0 assertions.
 */
public class SamlAssertionWrapper {
    /**
     * Field log
     */
    private static final org.slf4j.Logger LOG = 
        org.slf4j.LoggerFactory.getLogger(SamlAssertionWrapper.class);

    /**
     * Raw SAML assertion data
     */
    private XMLObject xmlObject = null;

    /**
     * Typed SAML v1.1 assertion
     */
    private org.opensaml.saml1.core.Assertion saml1 = null;

    /**
     * Typed SAML v2.0 assertion
     */
    private org.opensaml.saml2.core.Assertion saml2 = null;

    /**
     * Which SAML specification to use (currently, only v1.1 and v2.0 are supported)
     */
    private SAMLVersion samlVersion;

    /**
     * The Assertion as a DOM element
     */
    private Element assertionElement;
    
    /**
     * The SAMLKeyInfo object associated with the Subject KeyInfo
     */
    private SAMLKeyInfo subjectKeyInfo;
    
    /**
     * The SAMLKeyInfo object associated with the Signature on the Assertion
     */
    private SAMLKeyInfo signatureKeyInfo;

    /**
     * Default Canonicalization algorithm used for signing.
     */
    private final String defaultCanonicalizationAlgorithm = SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;

    /**
     * Default RSA Signature algorithm used for signing.
     */
    private final String defaultRSASignatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1;

    /**
     * Default DSA Signature algorithm used for signing.
     */
    private final String defaultDSASignatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_DSA;
    
    /**
     * Whether this object was instantiated with a DOM Element or an XMLObject initially
     */
    private final boolean fromDOM;
    
    /**
     * Constructor SamlAssertionWrapper creates a new SamlAssertionWrapper instance.
     *
     * @param element of type Element
     * @throws WSSecurityException
     */
    public SamlAssertionWrapper(Element element) throws WSSecurityException {
        OpenSAMLUtil.initSamlEngine();
        
        parseElement(element);
        fromDOM = true;
    }

    /**
     * Constructor SamlAssertionWrapper creates a new SamlAssertionWrapper instance.
     *
     * @param saml2 of type Assertion
     */
    public SamlAssertionWrapper(org.opensaml.saml2.core.Assertion saml2) {
        this((XMLObject)saml2);
    }

    /**
     * Constructor SamlAssertionWrapper creates a new SamlAssertionWrapper instance.
     *
     * @param saml1 of type Assertion
     */
    public SamlAssertionWrapper(org.opensaml.saml1.core.Assertion saml1) {
        this((XMLObject)saml1);
    }

    /**
     * Constructor SamlAssertionWrapper creates a new SamlAssertionWrapper instance.
     * This is the primary constructor.  All other constructor calls should
     * be routed to this method to ensure that the wrapper is initialized
     * correctly.
     *
     * @param xmlObject of type XMLObject
     */
    public SamlAssertionWrapper(XMLObject xmlObject) {
        OpenSAMLUtil.initSamlEngine();
        
        this.xmlObject = xmlObject;
        if (xmlObject instanceof org.opensaml.saml1.core.Assertion) {
            this.saml1 = (org.opensaml.saml1.core.Assertion) xmlObject;
            samlVersion = SAMLVersion.VERSION_11;
        } else if (xmlObject instanceof org.opensaml.saml2.core.Assertion) {
            this.saml2 = (org.opensaml.saml2.core.Assertion) xmlObject;
            samlVersion = SAMLVersion.VERSION_20;
        } else {
            LOG.error(
                "SamlAssertionWrapper: found unexpected type "
                + (xmlObject != null ? xmlObject.getClass().getName() : null)
            );
        }
        fromDOM = false;
    }
    
    /**
     * Constructor SamlAssertionWrapper creates a new SamlAssertionWrapper instance.
     * This constructor is primarily called on the client side to initialize
     * the wrapper from a configuration file. <br>
     *
     * @param samlCallback of type SAMLCallback
     */
    public SamlAssertionWrapper(SAMLCallback samlCallback) throws WSSecurityException {
        OpenSAMLUtil.initSamlEngine();

        if (samlCallback.getAssertionElement() != null) {
            parseElement(samlCallback.getAssertionElement());
            fromDOM = true;
        } else {
            // If not then parse the SAMLCallback object
            parseCallback(samlCallback);
            fromDOM = false;
        }
    }

    /**
     * Method getSaml1 returns the saml1 of this SamlAssertionWrapper object.
     *
     * @return the saml1 (type Assertion) of this SamlAssertionWrapper object.
     */
    public org.opensaml.saml1.core.Assertion getSaml1() {
        return saml1;
    }

    /**
     * Method getSaml2 returns the saml2 of this SamlAssertionWrapper object.
     *
     * @return the saml2 (type Assertion) of this SamlAssertionWrapper object.
     */
    public org.opensaml.saml2.core.Assertion getSaml2() {
        return saml2;
    }

    /**
     * Method getXmlObject returns the xmlObject of this SamlAssertionWrapper object.
     *
     * @return the xmlObject (type XMLObject) of this SamlAssertionWrapper object.
     */
    public XMLObject getXmlObject() {
        return xmlObject;
    }

    /**
     * Method isCreated returns the created of this SamlAssertionWrapper object.
     *
     * @return the created (type boolean) of this SamlAssertionWrapper object.
     */
    public boolean isCreated() {
        return saml1 != null || saml2 != null;
    }


    /**
     * Create a DOM from the current XMLObject content. If the user-supplied doc is not null,
     * reparent the returned Element so that it is compatible with the user-supplied document.
     *
     * @param doc of type Document
     * @return Element
     */
    public Element toDOM(Document doc) throws WSSecurityException {
        if (fromDOM && assertionElement != null) {
            parseElement(assertionElement);
            if (doc != null) {
                return (Element)doc.importNode(assertionElement, true);
            }
            return assertionElement;
        }
        assertionElement = OpenSAMLUtil.toDom(xmlObject, doc);
        return assertionElement;
    }

    /**
     * Method assertionToString ...
     *
     * @return String
     */
    public String assertionToString() throws WSSecurityException {
        if (assertionElement == null) {
            Element element = toDOM(null);
            return DOM2Writer.nodeToString(element);
        }
        return DOM2Writer.nodeToString(assertionElement);
    }

    /**
     * Method getId returns the id of this SamlAssertionWrapper object.
     *
     * @return the id (type String) of this SamlAssertionWrapper object.
     */
    public String getId() {
        String id = null;
        if (saml2 != null) {
            id = saml2.getID();
        } else if (saml1 != null) {
            id = saml1.getID();
        } else {
            LOG.error("SamlAssertionWrapper: unable to return ID - no saml assertion object");
        }
        if (id == null || id.length() == 0) {
            LOG.error("SamlAssertionWrapper: ID was null, seeting a new ID value");
            id = IDGenerator.generateID("_");
            if (saml2 != null) {
                saml2.setID(id);
            } else if (saml1 != null) {
                saml1.setID(id);
            }
        }
        return id;
    }

    /**
     * Method getIssuerString returns the issuerString of this SamlAssertionWrapper object.
     *
     * @return the issuerString (type String) of this SamlAssertionWrapper object.
     */
    public String getIssuerString() {
        if (saml2 != null && saml2.getIssuer() != null) {
            return saml2.getIssuer().getValue();
        } else if (saml1 != null) {
            return saml1.getIssuer();
        }
        LOG.error(
            "SamlAssertionWrapper: unable to return Issuer string - no saml assertion "
            + "object or issuer is null"
        );
        return null;
    }

    /**
     * Method getSubjectName returns the Subject name value
     * @return the subjectName of this SamlAssertionWrapper object
     */
    public String getSubjectName() {
        if (saml2 != null) {
            org.opensaml.saml2.core.Subject subject = saml2.getSubject();
            if (subject != null && subject.getNameID() != null) {
                return subject.getNameID().getValue();
            }
        } else if (saml1 != null) {
            org.opensaml.saml1.core.Subject samlSubject = null;
            for (org.opensaml.saml1.core.Statement stmt : saml1.getStatements()) {
                if (stmt instanceof org.opensaml.saml1.core.AttributeStatement) {
                    org.opensaml.saml1.core.AttributeStatement attrStmt =
                            (org.opensaml.saml1.core.AttributeStatement) stmt;
                    samlSubject = attrStmt.getSubject();
                } else if (stmt instanceof org.opensaml.saml1.core.AuthenticationStatement) {
                    org.opensaml.saml1.core.AuthenticationStatement authStmt =
                            (org.opensaml.saml1.core.AuthenticationStatement) stmt;
                    samlSubject = authStmt.getSubject();
                } else {
                    org.opensaml.saml1.core.AuthorizationDecisionStatement authzStmt =
                            (org.opensaml.saml1.core.AuthorizationDecisionStatement)stmt;
                    samlSubject = authzStmt.getSubject();
                }
                if (samlSubject != null) {
                    break;
                }
            }
            if (samlSubject != null && samlSubject.getNameIdentifier() != null) {
                return samlSubject.getNameIdentifier().getNameIdentifier();
            }
        }
        LOG.error(
                "SamlAssertionWrapper: unable to return SubjectName - no saml assertion "
                        + "object or subject is null"
        );
        return null;
    }

    /**
     * Method getConfirmationMethods returns the confirmationMethods of this 
     * SamlAssertionWrapper object.
     *
     * @return the confirmationMethods of this SamlAssertionWrapper object.
     */
    public List<String> getConfirmationMethods() {
        List<String> methods = new ArrayList<String>();
        if (saml2 != null) {
            org.opensaml.saml2.core.Subject subject = saml2.getSubject();
            List<org.opensaml.saml2.core.SubjectConfirmation> confirmations = 
                subject.getSubjectConfirmations();
            for (org.opensaml.saml2.core.SubjectConfirmation confirmation : confirmations) {
                methods.add(confirmation.getMethod());
            }
        } else if (saml1 != null) {
            List<SubjectStatement> subjectStatements = new ArrayList<SubjectStatement>();
            subjectStatements.addAll(saml1.getSubjectStatements());
            subjectStatements.addAll(saml1.getAuthenticationStatements());
            subjectStatements.addAll(saml1.getAttributeStatements());
            subjectStatements.addAll(saml1.getAuthorizationDecisionStatements());
            for (SubjectStatement subjectStatement : subjectStatements) {
                Subject subject = subjectStatement.getSubject();
                if (subject != null) {
                    SubjectConfirmation confirmation = subject.getSubjectConfirmation();
                    if (confirmation != null) {
                        XMLObject data = confirmation.getSubjectConfirmationData();
                        if (data instanceof ConfirmationMethod) {
                            ConfirmationMethod method = (ConfirmationMethod) data;
                            methods.add(method.getConfirmationMethod());
                        }
                        List<ConfirmationMethod> confirmationMethods = 
                            confirmation.getConfirmationMethods();
                        for (ConfirmationMethod confirmationMethod : confirmationMethods) {
                            methods.add(confirmationMethod.getConfirmationMethod());
                        }
                    }
                }
            }
        }
        return methods;
    }

    /**
     * Method isSigned returns the signed of this SamlAssertionWrapper object.
     *
     * @return the signed (type boolean) of this SamlAssertionWrapper object.
     */
    public boolean isSigned() {
        if (saml2 != null) {
            return saml2.isSigned() || saml2.getSignature() != null;
        } else if (saml1 != null) {
            return saml1.isSigned() || saml1.getSignature() != null;
        }
        return false;
    }

    /**
     * Method setSignature sets the signature of this SamlAssertionWrapper object.
     *
     * @param signature the signature of this SamlAssertionWrapper object.
     */
    public void setSignature(Signature signature) {
        if (xmlObject instanceof SignableSAMLObject) {
            SignableSAMLObject signableObject = (SignableSAMLObject) xmlObject;
            signableObject.setSignature(signature);
            signableObject.releaseDOM();
            signableObject.releaseChildrenDOM(true);
        } else {
            LOG.error("Attempt to sign an unsignable object " + xmlObject.getClass().getName());
        }
    }
    
    /**
     * Create an enveloped signature on the assertion that has been created.
     * 
     * @param issuerKeyName the Issuer KeyName to use with the issuerCrypto argument
     * @param issuerKeyPassword the Issuer Password to use with the issuerCrypto argument
     * @param issuerCrypto the Issuer Crypto instance
     * @param sendKeyValue whether to send the key value or not
     * @throws WSSecurityException
     */
    public void signAssertion(String issuerKeyName, String issuerKeyPassword,
            Crypto issuerCrypto, boolean sendKeyValue)
            throws WSSecurityException {

        signAssertion(issuerKeyName, issuerKeyPassword, issuerCrypto,
                sendKeyValue, defaultCanonicalizationAlgorithm,
                defaultRSASignatureAlgorithm);
    }
    
    /**
     * Create an enveloped signature on the assertion that has been created.
     * 
     * @param issuerKeyName the Issuer KeyName to use with the issuerCrypto argument
     * @param issuerKeyPassword the Issuer Password to use with the issuerCrypto argument
     * @param issuerCrypto the Issuer Crypto instance
     * @param sendKeyValue whether to send the key value or not
     * @param canonicalizationAlgorithm the canonicalization algorithm to be used for signing
     * @param signatureAlgorithm the signature algorithm to be used for signing
     * @throws WSSecurityException
     */
    public void signAssertion(String issuerKeyName, String issuerKeyPassword,
            Crypto issuerCrypto, boolean sendKeyValue,
            String canonicalizationAlgorithm, String signatureAlgorithm)
            throws WSSecurityException {
        //
        // Create the signature
        //
        Signature signature = OpenSAMLUtil.buildSignature();
        String c14nAlgo = canonicalizationAlgorithm;
        if (c14nAlgo == null) {
            c14nAlgo = defaultCanonicalizationAlgorithm;
        }
        signature.setCanonicalizationAlgorithm(c14nAlgo);
        LOG.debug("Using Canonicalization algorithm " + c14nAlgo);
        // prepare to sign the SAML token
        CryptoType cryptoType = new CryptoType(CryptoType.TYPE.ALIAS);
        cryptoType.setAlias(issuerKeyName);
        X509Certificate[] issuerCerts = issuerCrypto.getX509Certificates(cryptoType);
        if (issuerCerts == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "empty",
                    "No issuer certs were found to sign the SAML Assertion using issuer name: "
                            + issuerKeyName);
        }

        String sigAlgo = signatureAlgorithm;
        if (sigAlgo == null) {
            sigAlgo = defaultRSASignatureAlgorithm;
        }
        String pubKeyAlgo = issuerCerts[0].getPublicKey().getAlgorithm();
        if (LOG.isDebugEnabled()) {
            LOG.debug("automatic sig algo detection: " + pubKeyAlgo);
        }
        if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
            sigAlgo = defaultDSASignatureAlgorithm;
        }
        LOG.debug("Using Signature algorithm " + sigAlgo);
        PrivateKey privateKey = null;
        try {
            privateKey = issuerCrypto.getPrivateKey(issuerKeyName, issuerKeyPassword);
        } catch (Exception ex) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, ex);
        }

        signature.setSignatureAlgorithm(sigAlgo);

        BasicX509Credential signingCredential = new BasicX509Credential();
        signingCredential.setEntityCertificate(issuerCerts[0]);
        signingCredential.setPrivateKey(privateKey);

        signature.setSigningCredential(signingCredential);

        X509KeyInfoGeneratorFactory kiFactory = new X509KeyInfoGeneratorFactory();
        if (sendKeyValue) {
            kiFactory.setEmitPublicKeyValue(true);
        } else {
            kiFactory.setEmitEntityCertificate(true);
        }
        try {
            KeyInfo keyInfo = kiFactory.newInstance().generate(
                    signingCredential);
            signature.setKeyInfo(keyInfo);
        } catch (org.opensaml.xml.security.SecurityException ex) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "empty", ex,
                    "Error generating KeyInfo from signing credential");
        }

        // add the signature to the assertion
        setSignature(signature);
    }

    /**
     * Verify the signature of this assertion
     *
     * @throws ValidationException
     */
    public void verifySignature(
        SAMLKeyInfoProcessor keyInfoProcessor, Crypto sigCrypto
    ) throws WSSecurityException {
        Signature sig = getSignature();
        if (sig != null) {
            KeyInfo keyInfo = sig.getKeyInfo();
            SAMLKeyInfo samlKeyInfo = 
                SAMLUtil.getCredentialDirectlyFromKeyInfo(keyInfo.getDOM(), sigCrypto);
            verifySignature(samlKeyInfo);
        } else {
            LOG.debug("SamlAssertionWrapper: no signature to validate");
        }

    }
    
    /**
     * Verify the signature of this assertion
     *
     * @throws ValidationException
     */
    public void verifySignature(SAMLKeyInfo samlKeyInfo) throws WSSecurityException {
        Signature sig = getSignature();
        if (sig != null) {
            if (samlKeyInfo == null) {
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity",
                    "cannot get certificate or key"
                );
            }

            BasicX509Credential credential = new BasicX509Credential();
            if (samlKeyInfo.getCerts() != null) {
                credential.setEntityCertificate(samlKeyInfo.getCerts()[0]);
            } else if (samlKeyInfo.getPublicKey() != null) {
                credential.setPublicKey(samlKeyInfo.getPublicKey());
            } else {
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity",
                    "cannot get certificate or key"
                );
            }
            SignatureValidator sigValidator = new SignatureValidator(credential);
            try {
                sigValidator.validate(sig);
            } catch (ValidationException ex) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE,
                        "empty", ex, "SAML signature validation failed");
            }
            signatureKeyInfo = samlKeyInfo;
        } else {
            LOG.debug("SamlAssertionWrapper: no signature to validate");
        }
    }
    
    /**
     * Validate the signature of the Assertion against the Profile. This does not actually
     * verify the signature itself (see the verifySignature method for this)
     * @throws WSSecurityException
     */
    public void validateSignatureAgainstProfile() throws WSSecurityException {
        Signature sig = getSignature();
        if (sig != null) {
            SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();
            try {
                validator.validate(sig);
            } catch (ValidationException ex) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, 
                    "empty", ex, "SAML signature validation failed");
            }
        }
    }
    
    /**
     * This method parses the KeyInfo of the Subject for the holder-of-key confirmation
     * method, as required by the SAML Token spec. It then stores the SAMLKeyInfo object that
     * has been obtained for future processing.
     * @throws WSSecurityException
     */
    public void parseHOKSubject(
        SAMLKeyInfoProcessor keyInfoProcessor,
        Crypto sigCrypto,
        CallbackHandler callbackHandler
    ) throws WSSecurityException {
        String confirmMethod = null;
        List<String> methods = getConfirmationMethods();
        if (methods != null && methods.size() > 0) {
            confirmMethod = methods.get(0);
        }
        if (OpenSAMLUtil.isMethodHolderOfKey(confirmMethod)) {
            if (saml1 != null) {
                subjectKeyInfo = 
                    SAMLUtil.getCredentialFromSubject(
                        saml1, keyInfoProcessor, sigCrypto, callbackHandler
                    );
            } else if (saml2 != null) {
                subjectKeyInfo = 
                    SAMLUtil.getCredentialFromSubject(
                        saml2, keyInfoProcessor, sigCrypto, callbackHandler
                    );
            }
        }
    }
    

    /**
     * Method getSamlVersion returns the samlVersion of this SamlAssertionWrapper object.
     *
     * @return the samlVersion (type SAMLVersion) of this SamlAssertionWrapper object.
     */
    public SAMLVersion getSamlVersion() {
        if (samlVersion == null) {
            // Try to set the version.
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "The SAML version was null in getSamlVersion(). Recomputing SAML version..."
                );
            }
            if (saml1 != null && saml2 == null) {
                samlVersion = SAMLVersion.VERSION_11;
            } else if (saml1 == null && saml2 != null) {
                samlVersion = SAMLVersion.VERSION_20;
            } else {
                // We are only supporting SAML v1.1 or SAML v2.0 at this time.
                throw new IllegalStateException(
                    "Could not determine the SAML version number. Check your "
                    + "configuration and try again."
                );
            }
        }
        return samlVersion;
    }

    /**
     * Get the Assertion as a DOM Element.
     * @return the assertion as a DOM Element
     */
    public Element getElement() {
        return assertionElement;
    }
    
    /**
     * Get the SAMLKeyInfo associated with the signature of the assertion
     * @return the SAMLKeyInfo associated with the signature of the assertion
     */
    public SAMLKeyInfo getSignatureKeyInfo() {
        return signatureKeyInfo;
    }
    
    /**
     * Get the SAMLKeyInfo associated with the Subject KeyInfo
     * @return the SAMLKeyInfo associated with the Subject KeyInfo
     */
    public SAMLKeyInfo getSubjectKeyInfo() {
        return subjectKeyInfo;
    }
    
    /**
     * Get the SignatureValue bytes of the signed SAML Assertion 
     * @return the SignatureValue bytes of the signed SAML Assertion 
     * @throws WSSecurityException
     */
    public byte[] getSignatureValue() throws WSSecurityException {
        Signature sig = null;
        if (saml2 != null && saml2.getSignature() != null) {
            sig = saml2.getSignature();
        } else if (saml1 != null && saml1.getSignature() != null) {
            sig = saml1.getSignature();
        }
        if (sig != null) {
            Element signatureElement = sig.getDOM();
            
            try {
                // Use XML-Security class to obtain SignatureValue
                XMLSignature xmlSignature = new XMLSignature(signatureElement, "");
                return xmlSignature.getSignatureValue();
            } catch (XMLSignatureException e) {
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity", null, e
                );
            } catch (XMLSecurityException e) {
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity", null, e
                );
            }
        }
        return null;
    }

    public Signature getSignature() throws WSSecurityException {
        Signature sig = null;
        if (saml2 != null && saml2.getSignature() != null) {
            sig = saml2.getSignature();
        } else if (saml1 != null && saml1.getSignature() != null) {
            sig = saml1.getSignature();
        }
        return sig;
    }
    
    /**
     * Check the Conditions of the Assertion.
     */
    public void checkConditions(int futureTTL) throws WSSecurityException {
        DateTime validFrom = null;
        DateTime validTill = null;
        if (getSamlVersion().equals(SAMLVersion.VERSION_20)
            && getSaml2().getConditions() != null) {
            validFrom = getSaml2().getConditions().getNotBefore();
            validTill = getSaml2().getConditions().getNotOnOrAfter();
        } else if (getSamlVersion().equals(SAMLVersion.VERSION_11)
            && getSaml1().getConditions() != null) {
            validFrom = getSaml1().getConditions().getNotBefore();
            validTill = getSaml1().getConditions().getNotOnOrAfter();
        }
        
        if (validFrom != null) {
            DateTime currentTime = new DateTime();
            currentTime = currentTime.plusSeconds(futureTTL);
            if (validFrom.isAfter(currentTime)) {
                LOG.debug("SAML Token condition (Not Before) not met");
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
            }
        }

        if (validTill != null && validTill.isBeforeNow()) {
            LOG.debug("SAML Token condition (Not On Or After) not met");
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
        }
    }
    
    /**
     * Validate the samlAssertion against schemas/profiles
     */
    public void validateAssertion(boolean validateSignatureAgainstProfile) throws WSSecurityException {
        if (validateSignatureAgainstProfile) {
            validateSignatureAgainstProfile();
        }
        
        if (getSaml1() != null) {
            ValidatorSuite schemaValidators = 
                org.opensaml.Configuration.getValidatorSuite("saml1-schema-validator");
            ValidatorSuite specValidators = 
                org.opensaml.Configuration.getValidatorSuite("saml1-spec-validator");
            try {
                schemaValidators.validate(getSaml1());
                specValidators.validate(getSaml1());
            } catch (ValidationException e) {
                LOG.debug("Saml Validation error: " + e.getMessage(), e);
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity", e
                );
            }
        } else if (getSaml2() != null) {
            ValidatorSuite schemaValidators = 
                org.opensaml.Configuration.getValidatorSuite("saml2-core-schema-validator");
            ValidatorSuite specValidators = 
                org.opensaml.Configuration.getValidatorSuite("saml2-core-spec-validator");
            try {
                schemaValidators.validate(getSaml2());
                specValidators.validate(getSaml2());
            } catch (ValidationException e) {
                LOG.debug("Saml Validation error: " + e.getMessage(), e);
                throw new WSSecurityException(
                    WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity", e
                );
            }
        }
    }

    
    /**
     * Parse the DOM Element into Opensaml objects.
     */
    private void parseElement(Element element) throws WSSecurityException {
        this.xmlObject = OpenSAMLUtil.fromDom(element);
        if (xmlObject instanceof org.opensaml.saml1.core.Assertion) {
            this.saml1 = (org.opensaml.saml1.core.Assertion) xmlObject;
            samlVersion = SAMLVersion.VERSION_11;
        } else if (xmlObject instanceof org.opensaml.saml2.core.Assertion) {
            this.saml2 = (org.opensaml.saml2.core.Assertion) xmlObject;
            samlVersion = SAMLVersion.VERSION_20;
        } else {
            LOG.error(
                "SamlAssertionWrapper: found unexpected type "
                + (xmlObject != null ? xmlObject.getClass().getName() : xmlObject)
            );
        }
        
        assertionElement = element;
    }
    
    /**
     * Parse a SAMLCallback object to create a SAML Assertion
     */
    private void parseCallback(
        SAMLCallback samlCallback
    ) throws WSSecurityException {
        samlVersion = samlCallback.getSamlVersion();
        if (samlVersion == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "noSAMLVersion");
        }
        String issuer = samlCallback.getIssuer();
        
        if (samlVersion.equals(SAMLVersion.VERSION_11)) {
            // Build a SAML v1.1 assertion
            saml1 = SAML1ComponentBuilder.createSamlv1Assertion(issuer);

            try {
                // Process the SAML authentication statement(s)
                List<AuthenticationStatement> authenticationStatements = 
                    SAML1ComponentBuilder.createSamlv1AuthenticationStatement(
                        samlCallback.getAuthenticationStatementData()
                    );
                saml1.getAuthenticationStatements().addAll(authenticationStatements);
    
                // Process the SAML attribute statement(s)            
                List<AttributeStatement> attributeStatements =
                        SAML1ComponentBuilder.createSamlv1AttributeStatement(
                            samlCallback.getAttributeStatementData()
                        );
                saml1.getAttributeStatements().addAll(attributeStatements);
    
                // Process the SAML authorization decision statement(s)
                List<AuthorizationDecisionStatement> authDecisionStatements =
                        SAML1ComponentBuilder.createSamlv1AuthorizationDecisionStatement(
                            samlCallback.getAuthDecisionStatementData()
                        );
                saml1.getAuthorizationDecisionStatements().addAll(authDecisionStatements);
    
                // Build the complete assertion
                org.opensaml.saml1.core.Conditions conditions = 
                    SAML1ComponentBuilder.createSamlv1Conditions(samlCallback.getConditions());
                saml1.setConditions(conditions);
            } catch (org.opensaml.xml.security.SecurityException ex) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "empty", ex,
                        "Error generating KeyInfo from signing credential"
                );
            }

            // Set the OpenSaml2 XMLObject instance
            xmlObject = saml1;

        } else if (samlVersion.equals(SAMLVersion.VERSION_20)) {
            // Build a SAML v2.0 assertion
            saml2 = SAML2ComponentBuilder.createAssertion();
            Issuer samlIssuer = SAML2ComponentBuilder.createIssuer(issuer);

            // Authn Statement(s)
            List<AuthnStatement> authnStatements = 
                SAML2ComponentBuilder.createAuthnStatement(
                    samlCallback.getAuthenticationStatementData()
                );
            saml2.getAuthnStatements().addAll(authnStatements);

            // Attribute statement(s)
            List<org.opensaml.saml2.core.AttributeStatement> attributeStatements = 
                SAML2ComponentBuilder.createAttributeStatement(
                    samlCallback.getAttributeStatementData()
                );
            saml2.getAttributeStatements().addAll(attributeStatements);

            // AuthzDecisionStatement(s)
            List<AuthzDecisionStatement> authDecisionStatements =
                    SAML2ComponentBuilder.createAuthorizationDecisionStatement(
                        samlCallback.getAuthDecisionStatementData()
                    );
            saml2.getAuthzDecisionStatements().addAll(authDecisionStatements);

            // Build the SAML v2.0 assertion
            saml2.setIssuer(samlIssuer);
            
            try {
                org.opensaml.saml2.core.Subject subject = 
                    SAML2ComponentBuilder.createSaml2Subject(samlCallback.getSubject());
                saml2.setSubject(subject);
            } catch (org.opensaml.xml.security.SecurityException ex) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "empty", ex,
                        "Error generating KeyInfo from signing credential"
                );
            }
            
            org.opensaml.saml2.core.Conditions conditions = 
                SAML2ComponentBuilder.createConditions(samlCallback.getConditions());
            saml2.setConditions(conditions);

            // Set the OpenSaml2 XMLObject instance
            xmlObject = saml2;
        }
    }

}
