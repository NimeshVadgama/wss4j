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
package org.apache.wss4j.common;

/**
 * This class defines Configuration Constants that are shared between the DOM + StAX code. This
 * allows a user to configure both layers in the same way (e.g. via a Map).
 */
public final class ConfigurationConstants {  

    private ConfigurationConstants() {
        // complete
    }
    
    //
    // Action configuration tags
    //
    
    /**
     * The action parameter. It is a blank separated list of actions to perform.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
     * </pre>
     */
    public static final String ACTION = "action";

    /**
     * Perform a UsernameToken action.
     */
    public static final String USERNAME_TOKEN = "UsernameToken";
    
    /**
     * Perform a UsernameTokenSignature action.
     */
    public static final String USERNAME_TOKEN_SIGNATURE = "UsernameTokenSignature";
    
    /**
     * Perform a UsernameToken action with no password.
     */
    public static final String USERNAME_TOKEN_NO_PASSWORD = "UsernameTokenNoPassword";

    /**
     * Perform an unsigned SAML Token action.
     */
    public static final String SAML_TOKEN_UNSIGNED = "SAMLTokenUnsigned";
    
    /**
     * Perform a signed SAML Token action.
     */
    public static final String SAML_TOKEN_SIGNED = "SAMLTokenSigned";

    /**
     * Perform a Signature action. The signature specific parameters define how
     * to sign, which keys to use, and so on.
     */
    public static final String SIGNATURE = "Signature";

    /**
     * Perform an Encryption action. The encryption specific parameters define how 
     * to encrypt, which keys to use, and so on.
     */
    public static final String ENCRYPT = "Encrypt";

    /**
     * Add a timestamp to the security header.
     */
    public static final String TIMESTAMP = "Timestamp";
    
    /**
     * Perform a Signature action with derived keys. The signature specific parameters define how
     * to sign, which keys to use, and so on.
     */
    public static final String SIGNATURE_DERIVED = "SignatureDerived";

    /**
     * Perform an Encryption action with derived keys. The encryption specific parameters define how 
     * to encrypt, which keys to use, and so on.
     */
    public static final String ENCRYPT_DERIVED = "EncryptDerived";
    
    /**
     * Perform a Signature action with a kerberos token. The signature specific parameters define how
     * to sign, which keys to use, and so on.
     */
    public static final String SIGNATURE_WITH_KERBEROS_TOKEN = "SignatureWithKerberosToken";
    
    /**
     * Perform a Encryption action with a kerberos token. The signature specific parameters define how
     * to encrypt, which keys to use, and so on.
     */
    public static final String ENCRYPT_WITH_KERBEROS_TOKEN = "EncryptWithKerberosToken";
    
    /**
     * Add a kerberos token. 
     */
    public static final String KERBEROS_TOKEN = "KerberosToken";
    
    //
    // User properties
    //

    /**
     * The actor or role name of the <code>wsse:Security</code> header. If this parameter 
     * is omitted, the actor name is not set.
     * <p/>
     * The value of the actor or role has to match the receiver's setting
     * or may contain standard values.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ACTOR, "ActorName");
     * </pre>
     */
    public static final String ACTOR = "actor";

    /**
     * The user's name. It is used differently by each of the WS-Security functions.
     * <ul>
     * <li>The <i>UsernameToken</i> function sets this name in the
     * <code>UsernameToken</code>.
     * </li>
     * <li>The <i>Signing</i> function uses this name as the alias name
     * in the keystore to get user's certificate and private key to
     * perform signing if {@link #SIGNATURE_USER} is not used.
     * </li>
     * <li>The <i>encryption</i>
     * functions uses this parameter as fallback if {@link #ENCRYPTION_USER}
     * is not used.
     * </li>
     * </ul>
     */
    public static final String USER = "user";
    
    /**
     * The user's name for encryption. The encryption functions use the public key of 
     * this user's certificate to encrypt the generated symmetric key.
     * <p/>
     * If this parameter is not set, then the encryption
     * function falls back to the {@link #USER} parameter to get the
     * certificate.
     * <p/>
     * If <b>only</b> encryption of the SOAP body data is requested,
     * it is recommended to use this parameter to define the username.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ENCRYPTION_USER, "encryptionUser");
     * </pre>
     */
    public static final String ENCRYPTION_USER = "encryptionUser";
    
    /**
     * The user's name for signature. This name is used as the alias name in the keystore 
     * to get user's certificate and private key to perform signing.
     * <p/>
     * If this parameter is not set, then the signature
     * function falls back to the {@link #USER} parameter.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.SIGNATURE_USER, "signatureUser");
     * </pre>
     */
    public static final String SIGNATURE_USER = "signatureUser";

    /**
     * Specifying this name as {@link #ENCRYPTION_USER}
     * triggers a special action to get the public key to use for encryption.
     * <p/>
     * The handler uses the public key of the sender's certificate. Using this
     * way to define an encryption key simplifies certificate management to
     * a large extend.
     */
    public static final String USE_REQ_SIG_CERT = "useReqSigCert";
    
    //
    // Callback class and property file properties
    //

    /**
     * This tag refers to the CallbackHandler implementation class used to obtain passwords. 
     * The value of this tag must be the class name of a 
     * {@link javax.security.auth.callback.CallbackHandler} instance.
     * </p>
     * The callback function
     * {@link javax.security.auth.callback.CallbackHandler#handle(
     * javax.security.auth.callback.Callback[])} gets an array of 
     * {@link org.apache.wss4j.common.ext.WSPasswordCallback} objects. Only the first entry of the
     * array is used. This object contains the username/keyname as identifier. The callback
     * handler must set the password or key associated with this identifier before it returns.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.PW_CALLBACK_CLASS, "PWCallbackClass");
     * </pre>
     */
    public static final String PW_CALLBACK_CLASS = "passwordCallbackClass";
    
    /**
     * This tag refers to the CallbackHandler implementation object used to obtain
     * passwords. The value of this tag must be a
     * {@link javax.security.auth.callback.CallbackHandler} instance.
     * </p>
     * Refer to {@link #PW_CALLBACK_CLASS} for further information about password callback 
     * handling.
     */
    public static final String PW_CALLBACK_REF = "passwordCallbackRef";
    
    /**
     * This tag refers to the SAML CallbackHandler implementation class used to construct
     * SAML Assertions. The value of this tag must be the class name of a 
     * {@link javax.security.auth.callback.CallbackHandler} instance.
     */
    public static final String SAML_CALLBACK_CLASS = "samlCallbackClass";
    
    /**
     * This tag refers to the SAML CallbackHandler implementation object used to construct
     * SAML Assertions. The value of this tag must be a
     * {@link javax.security.auth.callback.CallbackHandler} instance.
     */
    public static final String SAML_CALLBACK_REF = "samlCallbackRef";

    /**
     * This tag refers to the CallbackHandler implementation class used to get the key
     * associated with a key name. The value of this tag must be the class name of a 
     * {@link javax.security.auth.callback.CallbackHandler} instance.
     */
    public static final String ENC_CALLBACK_CLASS = "embeddedKeyCallbackClass";

    /**
     * This tag refers to the  CallbackHandler implementation object used to get the key
     * associated with a key name. The value of this tag must be a
     * {@link javax.security.auth.callback.CallbackHandler} instance.
     */
    public static final String ENC_CALLBACK_REF = "embeddedKeyCallbackRef";
    
    /**
     * The path of the crypto property file to use for Signature creation. The classloader 
     * loads this file. Therefore it must be accessible via the classpath.
     * <p/>
     * To locate the implementation of the
     * {@link org.apache.wss4j.common.crypto.Crypto Crypto}
     * interface implementation the property file must contain the property
     * <code>org.apache.wss4j.crypto.provider</code>. The value of
     * this property is the classname of the implementation class.
     * <p/>
     * The following line defines the standard implementation:
     * <pre>
     * org.apache.wss4j.crypto.provider=org.apache.wss4j.common.crypto.Merlin
     * </pre>
     * The other contents of the property file depend on the implementation
     * of the {@link org.apache.wss4j.common.crypto.Crypto Crypto}
     * interface. Please see the WSS4J website for more information on the Merlin property 
     * tags and values.
     * </p>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.SIG_PROP_FILE, "myCrypto.properties");
     * </pre>
     */
    public static final String SIG_PROP_FILE = "signaturePropFile";

    /**
     * The key that holds a reference to the object holding complete information about 
     * the signature Crypto implementation. This object can either be a Crypto instance or a
     * <code>java.util.Properties</code> file, which should contain all information that 
     * would contain in an equivalent properties file which includes the Crypto implementation
     * class name.
     * 
     * Refer to documentation of {@link #SIG_PROP_FILE}.
     */
    public static final String SIG_PROP_REF_ID = "signaturePropRefId";
    
    /**
     * The path of the crypto property file to use for Signature verification. The 
     * classloader loads this file. Therefore it must be accessible via the classpath.
     * <p/>
     * Refer to documentation of {@link #SIG_PROP_FILE}.
     */
    public static final String SIG_VER_PROP_FILE = "signatureVerificationPropFile";
    
    /**
     * The key that holds a reference to the object holding complete information about 
     * the signature verification Crypto implementation. This object can either be a Crypto
     * instance or a <code>java.util.Properties</code> file, which should contain all 
     * information that would contain in an equivalent properties file which includes the 
     * Crypto implementation class name.
     * 
     * Refer to documentation of {@link #SIG_VER_PROP_FILE}.
     */
    public static final String SIG_VER_PROP_REF_ID = "signatureVerificationPropRefId";
    
    /**
     * The path of the crypto property file to use for Decryption. The classloader loads this 
     * file. Therefore it must be accessible via the classpath. Refer to documentation of 
     * {@link #SIG_PROP_FILE} for more information about the contents of the Properties file.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.DEC_PROP_FILE, "myCrypto.properties");
     * </pre>
     */
    public static final String DEC_PROP_FILE = "decryptionPropFile";
    
    /**
     * The key that holds a reference to the object holding complete information about 
     * the decryption Crypto implementation. This object can either be a Crypto instance or a
     * <code>java.util.Properties</code> file, which should contain all information that 
     * would contain in an equivalent properties file which includes the Crypto implementation
     * class name.
     * 
     * Refer to documentation of {@link #DEC_PROP_FILE}.
     */
    public static final String DEC_PROP_REF_ID = "decryptionPropRefId";
    
    /**
     * The path of the crypto property file to use for Encryption. The classloader loads this 
     * file. Therefore it must be accessible via the classpath. Refer to documentation of 
     * {@link #SIG_PROP_FILE} for more information about the contents of the Properties file.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ENC_PROP_FILE, "myCrypto.properties");
     * </pre>
     */
    public static final String ENC_PROP_FILE = "encryptionPropFile";
    
    /**
     * The key that holds a reference to the object holding complete information about 
     * the encryption Crypto implementation. This object can either be a Crypto instance or a
     * <code>java.util.Properties</code> file, which should contain all information that 
     * would contain in an equivalent properties file which includes the Crypto implementation
     * class name.
     * 
     * Refer to documentation of {@link #ENC_PROP_FILE}.
     */
    public static final String ENC_PROP_REF_ID = "encryptionPropRefId";
    
    //
    // Boolean configuration tags, e.g. the value should be "true" or "false".
    //
    
    /**
     * Whether to enable signatureConfirmation or not. The default value is "false".
     */
    public static final String ENABLE_SIGNATURE_CONFIRMATION = "enableSignatureConfirmation";
    
    /**
     * Whether to set the mustUnderstand flag on an outbound message or not. The default 
     * setting is "true".
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.MUST_UNDERSTAND, "false");
     * </pre>
     */
    public static final String MUST_UNDERSTAND = "mustUnderstand";
    
    /**
     * Whether to ensure compliance with the Basic Security Profile (BSP) 1.1 or not. The
     * default value is "true".
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.IS_BSP_COMPLIANT, "false");
     * </pre>
     */
    public static final String IS_BSP_COMPLIANT = "isBSPCompliant";
    
    /**
     * Whether to add an InclusiveNamespaces PrefixList as a CanonicalizationMethod
     * child when generating Signatures using WSConstants.C14N_EXCL_OMIT_COMMENTS.
     * The default is true.
     */
    public static final String ADD_INCLUSIVE_PREFIXES = "addInclusivePrefixes";
    
    /**
     * Whether to add a Nonce Element to a UsernameToken. This only applies when the
     * password type is of type "text". A Nonce is automatically added for the "digest"
     * case. The default is false.
     */
    public static final String ADD_USERNAMETOKEN_NONCE = "addUsernameTokenNonce";
    
    /**
     * Whether to add a Created Element to a UsernameToken. This only applies when the
     * password type is of type "text". A Created is automatically added for the "digest"
     * case. The default is false.
     */
    public static final String ADD_USERNAMETOKEN_CREATED = "addUsernameTokenCreated";
    
    /**
     * This variable controls whether types other than PasswordDigest or PasswordText
     * are allowed when processing UsernameTokens. The default value is "false".
     */
    public static final String HANDLE_CUSTOM_PASSWORD_TYPES = "handleCustomPasswordTypes";
    
    /**
     * This variable controls whether a UsernameToken with no password element is allowed. 
     * The default value is "false". Set it to "true" to allow deriving keys from UsernameTokens 
     * or to support UsernameTokens for purposes other than authentication.
     */
    public static final String ALLOW_USERNAMETOKEN_NOPASSWORD = "allowUsernameTokenNoPassword";
    
    /**
     * This variable controls whether (wsse) namespace qualified password types are
     * accepted when processing UsernameTokens. The default value is "false".
     */
    public static final String ALLOW_NAMESPACE_QUALIFIED_PASSWORD_TYPES 
        = "allowNamespaceQualifiedPasswordTypes";
    
    /**
     * This variable controls whether to enable Certificate Revocation List (CRL) checking
     * or not when verifying trust in a certificate. The default value is "false".
     */
    public static final String ENABLE_REVOCATION = "enableRevocation";
    
    /**
     * This parameter sets whether to use a single certificate or a whole certificate
     * chain when constructing a BinarySecurityToken used for direct reference in
     * signature. The default is "true", meaning that only a single certificate is used.
     */
    public static final String USE_SINGLE_CERTIFICATE = "useSingleCertificate";
    
    /**
     * This parameter sets whether to use the Username Token derived key for a MAC
     * or not. The default is "true".
     */
    public static final String USE_DERIVED_KEY_FOR_MAC = "useDerivedKeyForMAC";
    
    /**
     * Set whether Timestamps have precision in milliseconds. This applies to the
     * creation of Timestamps only. The default value is "true".
     */
    public static final String TIMESTAMP_PRECISION = "precisionInMilliseconds";
    
    /**
     * Set the value of this parameter to true to enable strict timestamp
     * handling. The default value is "true".
     * 
     * Strict Timestamp handling: throw an exception if a Timestamp contains
     * an <code>Expires</code> element and the semantics of the request are
     * expired, i.e. the current time at the receiver is past the expires time.
     */
    public static final String TIMESTAMP_STRICT = "timestampStrict";
    
    /**
     * Defines whether to encrypt the symmetric encryption key or not. If true
     * (the default), the symmetric key used for encryption is encrypted in turn,
     * and inserted into the security header in an "EncryptedKey" structure. If
     * set to false, no EncryptedKey structure is constructed.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ENC_SYM_ENC_KEY, "false");
     * </pre>
     */
    public static final String ENC_SYM_ENC_KEY = "encryptSymmetricEncryptionKey";
    
    /**
     * Whether the engine needs to enforce EncryptedData elements are
     * in a signed subtree of the document. This can be used to prevent
     * some wrapping based attacks when encrypt-before-sign token
     * protection is selected.
     */
    public static final String REQUIRE_SIGNED_ENCRYPTED_DATA_ELEMENTS = "requireSignedEncryptedDataElements";
    
    /**
     * Whether to allow the RSA v1.5 Key Transport Algorithm or not. Use of this algorithm
     * is discouraged, and so the default is "false".
     */
    public static final String ALLOW_RSA15_KEY_TRANSPORT_ALGORITHM = "allowRSA15KeyTransportAlgorithm";

    /**
     * Whether to validate the SubjectConfirmation requirements of a received SAML Token
     * (sender-vouches or holder-of-key). The default is true.
     */
    public static final String VALIDATE_SAML_SUBJECT_CONFIRMATION = 
        "validateSamlSubjectConfirmation";
    
    /**
     * Whether to include the Signature Token in the security header as well or not. This is only
     * applicable to the IssuerSerial and Thumbprint Key Identifier cases. The default is false.
     */
    public static final String INCLUDE_SIGNATURE_TOKEN = "includeSignatureToken";
    
    /**
     * Whether to cache UsernameToken nonces. The default value is "true".
     */
    public static final String ENABLE_NONCE_CACHE = "enableNonceCache";
    
    /**
     * Whether to cache Timestamp Created Strings (these are only cached in conjunction with a message 
     * Signature). The default value is "true".
     */
    public static final String ENABLE_TIMESTAMP_CACHE = "ws-security.enable.timestamp.cache";
    
    //
    // (Non-boolean) Configuration parameters for the actions/processors
    //
    
    /**
     * Specific parameter for UsernameTokens to define the encoding of the password. It can
     * be used on either the outbound or inbound side. The valid values are:
     * 
     * - {@link WSConstants#PW_DIGEST}
     * - {@link WSConstants#PW_TEXT}
     * - {@link WSConstants#PW_NONE}
     * 
     * On the Outbound side, the default value is PW_DIGEST. There is no default value on
     * the inbound side. If a value is specified on the inbound side, the password type of
     * the received UsernameToken must match the specified type, or an exception will be
     * thrown.
     */
    public static final String PASSWORD_TYPE = "passwordType";
    
    /**
     * Defines which key identifier type to use for signature. The WS-Security specifications
     * recommends to use the identifier type <code>IssuerSerial</code>. For possible signature 
     * key identifier types refer to {@link #getKeyIdentifier(String)}. 
     * For signature <code>IssuerSerial</code>, <code>DirectReference</code>,
     * <code>X509KeyIdentifier</code>, <code>Thumbprint</code>, <code>SKIKeyIdentifier</code>
     * and <code>KeyValue</code> are valid only. 
     * <p/>
     * The default is <code>IssuerSerial</code>.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.SIG_KEY_ID, "DirectReference");
     * </pre>
     */
    public static final String SIG_KEY_ID = "signatureKeyIdentifier";

    /**
     * Defines which signature algorithm to use. The default is set by the data in the 
     * certificate, i.e. one of the following:
     * 
     * "http://www.w3.org/2000/09/xmldsig#rsa-sha1"
     * "http://www.w3.org/2000/09/xmldsig#dsa-sha1"
     * 
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(
     *     ConfigurationConstants.SIG_ALGO, 
     *     "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
     * );
     * </pre>
     */
    public static final String SIG_ALGO = "signatureAlgorithm";
    
    /**
     * Defines which signature digest algorithm to use. The default is:
     * 
     * "http://www.w3.org/2000/09/xmldsig#sha1"
     * 
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(
     *    ConfigurationConstants.SIG_DIGEST_ALGO, "http://www.w3.org/2001/04/xmlenc#sha256"
     * );
     * </pre>
     */
    public static final String SIG_DIGEST_ALGO = "signatureDigestAlgorithm";

    /**
     * Parameter to define which parts of the request shall be signed.
     * <p/>
     * Refer to {@link #ENCRYPTION_PARTS} for a detailed description of
     * the format of the value string.
     * <p/>
     * If this parameter is not specified the handler signs the SOAP Body
     * by default, i.e.:
     * <pre>
     * &lt;parameter name="signatureParts"
     *   value="{}{http://schemas.xmlsoap.org/soap/envelope/}Body;" />
     * </pre>
     * To specify an element without a namespace use the string
     * <code>Null</code> as the namespace name (this is a case sensitive
     * string)
     * <p/>
     * If there is no other element in the request with a local name of
     * <code>Body</code> then the SOAP namespace identifier can be empty
     * (<code>{}</code>).
     */
    public static final String SIGNATURE_PARTS = "signatureParts";
    
    /**
     * This parameter sets the number of iterations to use when deriving a key
     * from a Username Token. The default is 1000. 
     */
    public static final String DERIVED_KEY_ITERATIONS = "derivedKeyIterations";

    /**
     * Defines which key identifier type to use for encryption. The WS-Security specifications
     * recommends to use the identifier type <code>IssuerSerial</code>. For
     * possible encryption key identifier types refer to
     * {@link #getKeyIdentifier(String)}. For encryption <code>IssuerSerial</code>,
     * <code>DirectReference</code>, <code>X509KeyIdentifier</code>, 
     * <code>Thumbprint</code>, <code>SKIKeyIdentifier</code>, <code>EncryptedKeySHA1</code>
     * and <code>EmbeddedKeyName</code> are valid only.
     * <p/>
     * The default is <code>IssuerSerial</code>.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ENC_KEY_ID, "X509KeyIdentifier");
     * </pre>
     */
    public static final String ENC_KEY_ID = "encryptionKeyIdentifier";

    /**
     * Defines which symmetric encryption algorithm to use. WSS4J supports the
     * following algorithms: {@link WSConstants#TRIPLE_DES},
     * {@link WSConstants#AES_128}, {@link WSConstants#AES_256},
     * and {@link WSConstants#AES_192}. Except for AES 192 all of these
     * algorithms are required by the XML Encryption specification.
     * The default algorithm is:
     * 
     * "http://www.w3.org/2001/04/xmlenc#aes128-cbc"
     * 
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ENC_SYM_ALGO, WSConstants.AES_256);
     * </pre>
     */
    public static final String ENC_SYM_ALGO = "encryptionSymAlgorithm";

    /**
     * Defines which algorithm to use to encrypt the generated symmetric key.
     * The default algorithm is:
     * 
     * "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p"
     * 
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(ConfigurationConstants.ENC_KEY_TRANSPORT, WSConstants.KEYTRANSPORT_RSA15);
     * </pre>
     */
    public static final String ENC_KEY_TRANSPORT = "encryptionKeyTransportAlgorithm";
    
    /**
     * Parameter to define which parts of the request shall be encrypted.
     * <p/>
     * The value of this parameter is a list of semi-colon separated
     * element names that identify the elements to encrypt. An encryption mode
     * specifier and a namespace identification, each inside a pair of curly
     * brackets, may preceed each element name.
     * <p/>
     * The encryption mode specifier is either <code>{Content}</code> or
     * <code>{Element}</code>. Please refer to the W3C XML Encryption
     * specification about the differences between Element and Content
     * encryption. The encryption mode defaults to <code>Content</code>
     * if it is omitted. Example of a list:
     * <pre>
     * &lt;parameter name="encryptionParts"
     *   value="{Content}{http://example.org/paymentv2}CreditCard;
     *             {Element}{}UserName" />
     * </pre>
     * The the first entry of the list identifies the element
     * <code>CreditCard</code> in the namespace
     * <code>http://example.org/paymentv2</code>, and will encrypt its content.
     * Be aware that the element name, the namespace identifier, and the
     * encryption modifier are case sensitive.
     * <p/>
     * The encryption modifier and the namespace identifier can be ommited.
     * In this case the encryption mode defaults to <code>Content</code> and
     * the namespace is set to the SOAP namespace.
     * <p/>
     * An empty encryption mode defaults to <code>Content</code>, an empty
     * namespace identifier defaults to the SOAP namespace.
     * The second line of the example defines <code>Element</code> as
     * encryption mode for an <code>UserName</code> element in the SOAP
     * namespace.
     * <p/>
     * To specify an element without a namespace use the string
     * <code>Null</code> as the namespace name (this is a case sensitive
     * string)
     * <p/>
     * If no list is specified, the handler encrypts the SOAP Body in
     * <code>Content</code> mode by default.
     */
    public static final String ENCRYPTION_PARTS = "encryptionParts";
    
    /**
     * Defines which encryption digest algorithm to use with the RSA OAEP Key Transport 
     * algorithm for encryption. The default is SHA-1.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(
     *    ConfigurationConstants.ENC_DIGEST_ALGO, "http://www.w3.org/2001/04/xmlenc#sha256"
     * );
     * </pre>
     */
    public static final String ENC_DIGEST_ALGO = "encryptionDigestAlgorithm";

    /**
     * Defines which encryption mgf algorithm to use with the RSA OAEP Key Transport
     * algorithm for encryption. The default is mgfsha1.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(
     *    ConfigurationConstants.ENC_MGF_ALGO, "http://www.w3.org/2009/xmlenc11#mgf1sha256"
     * );
     * </pre>
     */
    public static final String ENC_MGF_ALGO = "encryptionMGFAlgorithm";

    /**
     * Time-To-Live is the time difference between creation and expiry time in
     * seconds of the UsernameToken Created value. After this time the SOAP request 
     * is invalid (at least the security data shall be treated this way).
     * <p/>
     * If this parameter is not defined, contains a value less or equal
     * zero, or an illegal format the handlers use a default TTL of
     * 300 seconds (5 minutes).
     */
    public static final String TTL_USERNAMETOKEN = "utTimeToLive";
    
    /**
     * This configuration tag specifies the time in seconds in the future within which
     * the Created time of an incoming UsernameToken is valid. The default value is "60",
     * to avoid problems where clocks are slightly askew. To reject all future-created
     * UsernameTokens, set this value to "0". 
     */
    public static final String TTL_FUTURE_USERNAMETOKEN = "utFutureTimeToLive";
    
    /**
     * This configuration tag is a comma separated String of regular expressions which
     * will be applied to the subject DN of the certificate used for signature
     * validation, after trust verification of the certificate chain associated with the 
     * certificate. These constraints are not used when the certificate is contained in
     * the keystore (direct trust).
     */
    public static final String SIG_SUBJECT_CERT_CONSTRAINTS = "sigSubjectCertConstraints";
    
    /**
     * Time-To-Live is the time difference between creation and expiry time in
     * seconds in the WSS Timestamp. After this time the SOAP request is
     * invalid (at least the security data shall be treated this way).
     * <p/>
     * If this parameter is not defined, contains a value less or equal
     * zero, or an illegal format the handlers use a default TTL of
     * 300 seconds (5 minutes).
     */
    public static final String TTL_TIMESTAMP = "timeToLive";
    
    /**
     * This configuration tag specifies the time in seconds in the future within which
     * the Created time of an incoming Timestamp is valid. The default value is "60",
     * to avoid problems where clocks are slightly askew. To reject all future-created
     * Timestamps, set this value to "0". 
     */
    public static final String TTL_FUTURE_TIMESTAMP = "futureTimeToLive";
    
    /**
     * This tag refers to a Map of QName, Object (Validator) instances to be used to 
     * validate tokens identified by their QName. For the DOM layer, the Object should
     * be a org.apache.wss4j.dom.validate.Validator instance. For the StAX layer, it
     * should be a org.apache.wss4j.stax.validate.Validator instance.
     */
    public static final String VALIDATOR_MAP = "validatorMap";
    
    /**
     * This holds a reference to a ReplayCache instance used to cache UsernameToken nonces. The
     * default instance that is used is the EHCacheReplayCache.
     */
    public static final String NONCE_CACHE_INSTANCE = "nonceCacheInstance";
    
    /**
     * This holds a reference to a ReplayCache instance used to cache Timestamp Created Strings. The
     * default instance that is used is the EHCacheReplayCache.
     */
    public static final String TIMESTAMP_CACHE_INSTANCE = "timestampCacheInstance";
    
}

