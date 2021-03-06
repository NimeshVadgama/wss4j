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

package org.apache.wss4j.dom.action;

import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSEncryptionPart;
import org.apache.wss4j.dom.WSSecurityEngineResult;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandler;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.apache.wss4j.dom.message.WSSecSignatureConfirmation;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class SignatureConfirmationAction implements Action {
    private static final org.slf4j.Logger log = 
        org.slf4j.LoggerFactory.getLogger(SignatureConfirmationAction.class);

    @SuppressWarnings("unchecked")
    public void execute(WSHandler handler, int actionToDo, Document doc, RequestData reqData)
            throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Perform Signature confirmation");
        }

        List<WSHandlerResult> results = 
            (List<WSHandlerResult>) handler.getProperty(
                reqData.getMsgContext(), WSHandlerConstants.RECV_RESULTS
            );
        if (results == null) {
            return;
        }
        //
        // Loop over all the (signature) results gathered by all the processors, and store
        // them in a list.
        //
        final List<Integer> actions = new ArrayList<Integer>(3);
        actions.add(WSConstants.SIGN);
        actions.add(WSConstants.ST_SIGNED);
        actions.add(WSConstants.UT_SIGN);
        List<WSSecurityEngineResult> signatureActions = new ArrayList<WSSecurityEngineResult>();
        for (WSHandlerResult wshResult : results) {
            List<WSSecurityEngineResult> resultList = wshResult.getResults();

            signatureActions.addAll(
                WSSecurityUtil.fetchAllActionResults(resultList, actions)
            );
        }
        //
        // prepare a SignatureConfirmation token
        //
        WSSecSignatureConfirmation wsc = new WSSecSignatureConfirmation(reqData.getWssConfig());
        List<WSEncryptionPart> signatureParts = reqData.getSignatureParts();
        if (signatureActions.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Signature Confirmation: number of Signature results: "
                        + signatureActions.size());
            }
            for (int i = 0; i < signatureActions.size(); i++) {
                WSSecurityEngineResult wsr = signatureActions.get(i);
                byte[] sigVal = (byte[]) wsr.get(WSSecurityEngineResult.TAG_SIGNATURE_VALUE);
                wsc.build(doc, sigVal, reqData.getSecHeader());
                signatureParts.add(new WSEncryptionPart(wsc.getId()));
            }
        } else {
            wsc.build(doc, null, reqData.getSecHeader());
            signatureParts.add(new WSEncryptionPart(wsc.getId()));
        }
        handler.setProperty(
            reqData.getMsgContext(), WSHandlerConstants.SIG_CONF_DONE, ""
        );
    }
    
}
