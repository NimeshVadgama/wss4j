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
package org.apache.wss4j.policy;

import org.apache.wss4j.policy.model.SupportingTokenType;

import javax.xml.namespace.QName;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public abstract class SPConstants {

    public static final String ERR_INVALID_POLICY = "Invalid Policy";
    public static final QName EMPTY_QNAME = new QName("", "", "");

    public enum SPVersion {

        SP11(SP11Constants.SP_NS, SP11Constants.SP_PREFIX, SP11Constants.getInstance()),
        //this is not an error. We use SP13 for SP12NS because SP13 is an extension of SP12:
        SP12(SP12Constants.SP_NS, SP12Constants.SP_PREFIX, SP13Constants.getInstance()),
        SP13(SP13Constants.SP_NS, SP13Constants.SP_PREFIX, SP13Constants.getInstance());

        private final String prefix;
        private final String namespace;
        private final SPConstants spConstants;

        SPVersion(String namespace, String prefix, SPConstants spConstants) {
            this.namespace = namespace;
            this.spConstants = spConstants;
            this.prefix = prefix;
        }

        private static final Map<String, SPVersion> lookup = new HashMap<String, SPVersion>();

        static {
            for (SPVersion spVersion : EnumSet.allOf(SPVersion.class))
                lookup.put(spVersion.namespace, spVersion);
        }

        public static SPVersion getSPVersion(String namespace) {
            return lookup.get(namespace);
        }

        public SPConstants getSPConstants() {
            return spConstants;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static final String P_LOCALNAME = "Policy";


    public final static String LAYOUT = "Layout";

    /**
     * Security Header Layout : Strict
     */
    public final static String LAYOUT_STRICT = "Strict";

    /**
     * Security Header Layout : Lax
     */
    public final static String LAYOUT_LAX = "Lax";

    /**
     * Security Header Layout : LaxTsFirst
     */
    public final static String LAYOUT_LAX_TIMESTAMP_FIRST = "LaxTsFirst";

    /**
     * Security Header Layout : LaxTsLast
     */
    public final static String LAYOUT_LAX_TIMESTAMP_LAST = "LaxTsLast";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Protection Order : EncryptBeforeSigning
     */
    public final static String ENCRYPT_BEFORE_SIGNING = "EncryptBeforeSigning";

    /**
     * Protection Order : SignBeforeEncrypting
     */
    public final static String SIGN_BEFORE_ENCRYPTING = "SignBeforeEncrypting";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String ENCRYPT_SIGNATURE = "EncryptSignature";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String PROTECT_TOKENS = "ProtectTokens";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String ONLY_SIGN_ENTIRE_HEADERS_AND_BODY = "OnlySignEntireHeadersAndBody";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String INCLUDE_TIMESTAMP = "IncludeTimestamp";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String SIGNED_PARTS = "SignedParts";

    public final static String ENCRYPTED_PARTS = "EncryptedParts";

    public final static String SIGNED_ELEMENTS = "SignedElements";

    public final static String ENCRYPTED_ELEMENTS = "EncryptedElements";

    public final static String REQUIRED_ELEMENTS = "RequiredElements";

    public final static String CONTENT_ENCRYPTED_ELEMENTS = "ContentEncryptedElements";

    public final static String REQUIRED_PARTS = "RequiredParts";

    public final static String XPATH_VERSION = "XPathVersion";

    public final static String XPATH_EXPR = "XPath";

    public final static String XPATH2_EXPR = "Xpath2";

    public final static String FILTER = "Filter";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // X509 Token types 

    public final static String X509_TOKEN = "X509Token";

    public final static String WSS_X509_V1_TOKEN10 = "WssX509V1Token10";

    public final static String WSS_X509_V3_TOKEN10 = "WssX509V3Token10";

    public final static String WSS_X509_PKCS7_TOKEN10 = "WssX509Pkcs7Token10";

    public final static String WSS_X509_PKI_PATH_V1_TOKEN10 = "WssX509PkiPathV1Token10";

    public final static String WSS_X509_V1_TOKEN11 = "WssX509V1Token11";

    public final static String WSS_X509_V3_TOKEN11 = "WssX509V3Token11";

    public final static String WSS_X509_PKCS7_TOKEN11 = "WssX509Pkcs7Token11";

    public final static String WSS_X509_PKI_PATH_V1_TOKEN11 = "WssX509PkiPathV1Token11";


    public final static String USERNAME_TOKEN = "UsernameToken";

    public final static String USERNAME_TOKEN10 = "WssUsernameToken10";

    public final static String USERNAME_TOKEN11 = "WssUsernameToken11";


    public final static String TRANSPORT_TOKEN = "TransportToken";

    public final static String HTTPS_TOKEN = "HttpsToken";

    public final static String KERBEROS_TOKEN = "KerberosToken";

    public final static String SPNEGO_CONTEXT_TOKEN = "SpnegoContextToken";

    public final static String REQUIRE_CLIENT_CERTIFICATE = "RequireClientCertificate";

    public final static String HTTP_BASIC_AUTHENTICATION = "HttpBasicAuthentication";

    public final static String HTTP_DIGEST_AUTHENTICATION = "HttpDigestAuthentication";

    public final static String SECURITY_CONTEXT_TOKEN = "SecurityContextToken";

    public final static String SECURE_CONVERSATION_TOKEN = "SecureConversationToken";

    public final static String SAML_TOKEN = "SamlToken";

    public final static String REL_TOKEN = "RelToken";

    public final static String KEY_VALUE_TOKEN = "KeyValueToken";

    public final static String RSA_KEY_VALUE = "RsaKeyValue";

    public final static String ISSUED_TOKEN = "IssuedToken";


    public final static String SIGNATURE_TOKEN = "SignatureToken";

    public final static String ENCRYPTION_TOKEN = "EncryptionToken";

    public final static String PROTECTION_TOKEN = "ProtectionToken";

    public final static String INITIATOR_TOKEN = "InitiatorToken";

    public final static String INITIATOR_SIGNATURE_TOKEN = "InitiatorSignatureToken";

    public final static String INITIATOR_ENCRYPTION_TOKEN = "InitiatorEncryptionToken";

    public final static String RECIPIENT_TOKEN = "RecipientToken";

    public final static String RECIPIENT_SIGNATURE_TOKEN = "RecipientSignatureToken";

    public final static String RECIPIENT_ENCRYPTION_TOKEN = "RecipientEncryptionToken";


    public final static String SUPPORTING_TOKENS = "SupportingTokens";

    public final static String SIGNED_SUPPORTING_TOKENS = "SignedSupportingTokens";

    public final static String ENDORSING_SUPPORTING_TOKENS = "EndorsingSupportingTokens";

    public final static String SIGNED_ENDORSING_SUPPORTING_TOKENS = "SignedEndorsingSupportingTokens";

    public final static String ENCRYPTED_SUPPORTING_TOKENS = "EncryptedSupportingTokens";

    public final static String SIGNED_ENCRYPTED_SUPPORTING_TOKENS = "SignedEncryptedSupportingTokens";

    public final static String ENDORSING_ENCRYPTED_SUPPORTING_TOKENS = "EndorsingEncryptedSupportingTokens";

    public final static String SIGNED_ENDORSING_ENCRYPTED_SUPPORTING_TOKENS = "SignedEndorsingEncryptedSupportingTokens";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String ALGORITHM_SUITE = "AlgorithmSuite";

    public final static String ALGO_SUITE_BASIC256 = "Basic256";

    public final static String ALGO_SUITE_BASIC192 = "Basic192";

    public final static String ALGO_SUITE_BASIC128 = "Basic128";

    public final static String ALGO_SUITE_TRIPLE_DES = "TripleDes";

    public final static String ALGO_SUITE_BASIC256_RSA15 = "Basic256Rsa15";

    public final static String ALGO_SUITE_BASIC192_RSA15 = "Basic192Rsa15";

    public final static String ALGO_SUITE_BASIC128_RSA15 = "Basic128Rsa15";

    public final static String ALGO_SUITE_TRIPLE_DES_RSA15 = "TripleDesRsa15";

    public final static String ALGO_SUITE_BASIC256_SHA256 = "Basic256Sha256";

    public final static String ALGO_SUITE_BASIC192_SHA256 = "Basic192Sha256";

    public final static String ALGO_SUITE_BASIC128_SHA256 = "Basic128Sha256";

    public final static String ALGO_SUITE_TRIPLE_DES_SHA256 = "TripleDesSha256";

    public final static String ALGO_SUITE_BASIC256_SHA256_RSA15 = "Basic256Sha256Rsa15";

    public final static String ALGO_SUITE_BASIC192_SHA256_RSA15 = "Basic192Sha256Rsa15";

    public final static String ALGO_SUITE_BASIC128_SHA256_RSA15 = "Basic128Sha256Rsa15";

    public final static String ALGO_SUITE_TRIPLE_DES_SHA256_RSA15 = "TripleDesSha256Rsa15";

    // /
    // /Algorithms
    // /
    public final static String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";

    public final static String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    public final static String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    public final static String RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";

    public final static String RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";

    public final static String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    public final static String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

    public final static String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";

    public final static String AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";

    public final static String AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";

    public final static String AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";

    public final static String TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";

    public final static String KW_AES128 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";

    public final static String KW_AES192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";

    public final static String KW_AES256 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";

    public final static String KW_TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";

    public final static String KW_RSA_OAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";

    public final static String KW_RSA15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";

    public final static String P_SHA1 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L128 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L192 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L256 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public final static String XPATH20 = "http://www.w3.org/2002/06/xmldsig-filter2";

    public final static String C14N = "http://www.w3.org/2001/10/xml-c14n#";

    public final static String C14N11 = "http://www.w3.org/2006/12/xml-c14n11";

    public final static String EX_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";

    public final static String SNT = "http://www.w3.org/TR/soap12-n11n";

    public final static String STRT10 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform";

    // /////////////////////////////////////////////////////////////////////////////////////////////

    public static final String INCLUSIVE_C14N = "InclusiveC14N";

    public static final String SOAP_NORMALIZATION_10 = "SoapNormalization10";

    public static final String STR_TRANSFORM_10 = "STRTransform10";

    public static final String XPATH10 = "XPath10";

    public static final String XPATH_FILTER20 = "XPathFilter20";

    public static final String ABS_XPATH = "http://docs.oasis-open.org/...TBD.../AbsXPath";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static String ATTR_INCLUDE_TOKEN = "IncludeToken";

    public static final String INCLUDE_TOKEN_NEVER_SUFFIX = "/IncludeToken/Never";

    public static final String INCLUDE_TOKEN_ONCE_SUFFIX = "/IncludeToken/Once";

    public static final String INCLUDE_TOEKN_ALWAYS_TO_RECIPIENT_SUFFIX = "/IncludeToken/AlwaysToRecipient";

    public static final String INCLUDE_TOEKN_ALWAYS_TO_INITIATOR_SUFFIX = "/IncludeToken/AlwaysToInitiator";

    public static final String INCLUDE_TOEKN_ALWAYS_SUFFIX = "/IncludeToken/Always";

    public static final int INCLUDE_TOKEN_NEVER = 1;

    public static final int INCLUDE_TOKEN_ONCE = 2;

    public static final int INCLUDE_TOEKN_ALWAYS_TO_RECIPIENT = 3;

    public static final int INCLUDE_TOEKN_ALWAYS_TO_INITIATOR = 4;

    public static final int INCLUDE_TOEKN_ALWAYS = 5;


    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String TRANSPORT_BINDING = "TransportBinding";

    public static final String ASYMMETRIC_BINDING = "AsymmetricBinding";

    public static final String SYMMETRIC_BINDING = "SymmetricBinding";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String REQUIRE_KEY_IDENTIFIER_REFERENCE = "RequireKeyIdentifierReference";

    public static final String REQUIRE_ISSUER_SERIAL_REFERENCE = "RequireIssuerSerialReference";

    public static final String REQUIRE_EMBEDDED_TOKEN_REFERENCE = "RequireEmbeddedTokenReference";

    public static final String REQUIRE_THUMBPRINT_REFERENCE = "RequireThumbprintReference";

    public static final String REQUIRE_SIGNATURE_CONFIRMATION = "RequireSignatureConfirmation";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String MUST_SUPPORT_REF_KEY_IDENTIFIER = "MustSupportRefKeyIdentifier";

    public static final String MUST_SUPPORT_REF_ISSUER_SERIAL = "MustSupportRefIssuerSerial";

    public static final String MUST_SUPPORT_REF_EXTERNAL_URI = "MustSupportRefExternalURI";

    public static final String MUST_SUPPORT_REF_EMBEDDED_TOKEN = "MustSupportRefEmbeddedToken";

    public static final String MUST_SUPPORT_REF_THUMBPRINT = "MustSupportRefThumbprint";

    public static final String MUST_SUPPORT_REF_ENCRYPTED_KEY = "MustSupportRefEncryptedKey";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String WSS10 = "Wss10";

    public static final String WSS11 = "Wss11";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String TRUST_10 = "Trust10";

    public static final String TRUST_13 = "Trust13";

    public static final String MUST_SUPPORT_CLIENT_CHALLENGE = "MustSupportClientChallenge";

    public static final String MUST_SUPPORT_SERVER_CHALLENGE = "MustSupportServerChallenge";

    public static final String REQUIRE_CLIENT_ENTROPY = "RequireClientEntropy";

    public static final String REQUIRE_SERVER_ENTROPY = "RequireServerEntropy";

    public static final String MUST_SUPPORT_ISSUED_TOKENS = "MustSupportIssuedTokens";

    public static final String REQUIRE_REQUEST_SECURITY_TOKEN_COLLECTION = "RequireRequestSecurityTokenCollection";

    public static final String REQUIRE_APPLIES_TO = "RequireAppliesTo";

    public static final String SCOPE_POLICY_15 = "ScopePolicy15";
    public static final String MUST_SUPPORT_INTERACTIVE_CHALLENGE = "MustSupportInteractiveChallenge";

    public static final String ISSUER = "Issuer";

    public static final String ISSUER_NAME = "IssuerName";

    public static final String CLAIMS = "Claims";

    public static final String REQUIRE_DERIVED_KEYS = "RequireDerivedKeys";

    public static final String REQUIRE_IMPLIED_DERIVED_KEYS = "RequireImpliedDerivedKeys";

    public static final String REQUIRE_EXPLICIT_DERIVED_KEYS = "RequireExplicitDerivedKeys";

    public static final String REQUIRE_EXTERNAL_URI_REFERENCE = "RequireExternalUriReference";

    public static final String REQUIRE_EXTERNAL_REFERENCE = "RequireExternalReference";

    public static final String REQUIRE_INTERNAL_REFERENCE = "RequireInternalReference";

    public static final String REQUEST_SECURITY_TOKEN_TEMPLATE = "RequestSecurityTokenTemplate";

    public static final String SC10_SECURITY_CONTEXT_TOKEN = "SC10SecurityContextToken";

    public static final String SC13_SECURITY_CONTEXT_TOKEN = "SC13SecurityContextToken";

    public static final String BOOTSTRAP_POLICY = "BootstrapPolicy";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String HEADER = "Header";

    public static final String BODY = "Body";

    public static final String ATTACHMENTS = "Attachments";
    public static final String CONTENT_SIGNATURE_TRANSFORM = "ContentSignatureTransform";
    public static final String ATTACHMENT_COMPLETE_SIGNATURE_TRANSFORM = "AttachmentCompleteSignatureTransform";

    public static final String NAME = "Name";
    public static final String NAMESPACE = "Namespace";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String NO_PASSWORD = "NoPassword";
    public static final String HASH_PASSWORD = "HashPassword";

    public static final String CREATED = "Created";
    public static final String NONCE = "Nonce";

    public static final String MUST_NOT_SEND_CANCEL = "MustNotSendCancel";
    public static final String MUST_NOT_SEND_AMEND = "MustNotSendAmend";
    public static final String MUST_NOT_SEND_RENEW = "MustNotSendRenew";

    public static final String ATT_OPTIONAL = "Optional";

    public static final String ATT_IGNORABLE = "Ignorable";

    public enum IncludeTokenType {
        INCLUDE_TOKEN_NEVER,
        INCLUDE_TOKEN_ONCE,
        INCLUDE_TOKEN_ALWAYS_TO_RECIPIENT,
        INCLUDE_TOKEN_ALWAYS_TO_INITIATOR,
        INCLUDE_TOKEN_ALWAYS;

        private static final Map<String, IncludeTokenType> lookup = new HashMap<String, IncludeTokenType>();

        static {
            for (IncludeTokenType u : EnumSet.allOf(IncludeTokenType.class))
                lookup.put(u.name(), u);
        }

        public static IncludeTokenType lookUp(String name) {
            return lookup.get(name);
        }
    }

    public abstract IncludeTokenType getInclusionFromAttributeValue(String value) throws IllegalArgumentException;

    public abstract String getAttributeValueFromInclusion(IncludeTokenType value) throws IllegalArgumentException;

    public abstract QName getIncludeToken();

    public abstract QName getIssuer();

    public abstract QName getIssuerName();

    public abstract QName getClaims();

    public abstract QName getX509Token();

    public abstract QName getRequireIssuerSerialReference();

    public abstract QName getRequireEmbeddedTokenReference();

    public abstract QName getRequireThumbprintReference();

    public abstract QName getHttpsToken();

    public abstract QName getUsernameToken();

    public abstract QName getCreated();

    public abstract QName getNonce();

    public abstract QName getIssuedToken();

    public abstract QName getRequireExternalReference();

    public abstract QName getRequireInternalReference();

    public abstract QName getRequestSecurityTokenTemplate();

    public abstract QName getKerberosToken();

    public abstract QName getSpnegoContextToken();

    public abstract QName getSecurityContextToken();

    public abstract QName getRequireExternalUriReference();

    public abstract QName getSc13SecurityContextToken();

    public abstract QName getSc10SecurityContextToken();

    public abstract QName getSecureConversationToken();

    public abstract QName getMustNotSendCancel();

    public abstract QName getMustNotSendAmend();

    public abstract QName getMustNotSendRenew();

    public abstract QName getBootstrapPolicy();

    public abstract QName getSamlToken();

    public abstract QName getRelToken();

    public abstract QName getRequireKeyIdentifierReference();

    public abstract QName getKeyValueToken();

    public abstract QName getRsaKeyValue();

    public abstract QName getSignedParts();

    public abstract QName getSignedElements();

    public abstract QName getXPathExpression();

    public abstract QName getXPath2Expression();

    public abstract QName getEncryptedParts();

    public abstract QName getEncryptedElements();

    public abstract QName getContentEncryptedElements();

    public abstract QName getRequiredElements();

    public abstract QName getRequiredParts();

    public abstract QName getAlgorithmSuite();

    public abstract QName getLayout();

    public abstract QName getBody();

    public abstract QName getAttachments();

    public abstract QName getContentSignatureTransform();

    public abstract QName getAttachmentCompleteSignatureTransform();

    public abstract QName getHeader();

    public abstract QName getEncryptSignature();

    public abstract QName getProtectTokens();

    public abstract QName getOnlySignEntireHeadersAndBody();

    public abstract QName getTransportBinding();

    public abstract QName getSymmetricBinding();

    public abstract QName getAsymmetricBinding();

    public abstract QName getEncryptionToken();

    public abstract QName getSignatureToken();

    public abstract QName getProtectionToken();

    public abstract QName getTransportToken();

    public abstract QName getInitiatorToken();

    public abstract QName getInitiatorSignatureToken();

    public abstract QName getInitiatorEncryptionToken();

    public abstract QName getRecipientToken();

    public abstract QName getRecipientSignatureToken();

    public abstract QName getRecipientEncryptionToken();

    public abstract SupportingTokenType getSupportingTokenType(QName name);

    public abstract QName getTrust10();

    public abstract QName getTrust13();

    public abstract QName getScopePolicy15();

    public abstract QName getMustSupportClientChallenge();

    public abstract QName getMustSupportServerChallenge();

    public abstract QName getRequireClientEntropy();

    public abstract QName getRequireServerEntropy();

    public abstract QName getMustSupportIssuedTokens();

    public abstract QName getRequireRequestSecurityTokenCollection();

    public abstract QName getRequireAppliesTo();

    public abstract QName getMustSupportInteractiveChallenge();

    public abstract QName getWss10();

    public abstract QName getMustSupportRefKeyIdentifier();

    public abstract QName getMustSupportRefIssuerSerial();

    public abstract QName getMustSupportRefExternalUri();

    public abstract QName getMustSupportRefEmbeddedToken();

    public abstract QName getWss11();

    public abstract QName getMustSupportRefThumbprint();

    public abstract QName getMustSupportRefEncryptedKey();

    public abstract QName getRequireSignatureConfirmation();
}
