/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.extensions.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTUtils;

import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * In synapse configuration you have to specify full qualified class name as the name for the class mediator as follows.
 * <handler class="org.wso2.carbon.apimgt.extensions.auth.URLTokenInjector"/>
 */
public class URLTokenInjector extends AbstractHandler {

    private static final Log log = LogFactory.getLog(URLTokenInjector.class);

    private static final String OAUTH_HEADER = "Authorization";
    private static final String OAUTH_BEARER = "Bearer";

    private static final String CUSTOM_URL_TOKEN_PARAM = System.getProperty("urlTokenParam");

    private static String urlTokenParam = CUSTOM_URL_TOKEN_PARAM != null ? CUSTOM_URL_TOKEN_PARAM : "access_token";


    public boolean handleRequest(MessageContext synCtx) {
        String path = RESTUtils.getFullRequestPath(synCtx);
        String token = getUrlToken(path);
        if (token != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting Authorization header retrieved from the URL");
                log.debug("Path : " + path);
                log.debug("Token : " + token);
            }
            Map headers = getHeaders(synCtx);
            headers.put(OAUTH_HEADER, getAuthHeader(token));
        }
        return true;
    }

    public boolean handleResponse(MessageContext synCtx) {
        return true;
    }

    private static String getUrlToken(String path) {
        int index = path.indexOf("?");
        if (index <= 0) {
            return null;
        }
        String query = path.substring(index + 1);
        Hashtable params = parseQuery(query);
        Object tokens = params.get(urlTokenParam);
        if (tokens == null) {
            return null;
        }
        return ((String[]) tokens)[0];
    }

    private static String getAuthHeader(String token) {
        return OAUTH_BEARER + " " + token;
    }

    private static Map getHeaders(MessageContext synCtx) {
        return (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    private static Hashtable parseQuery(String query) {

        Hashtable ht = new Hashtable();
        if (query == null) {
            return ht;
        }
        String[] valArray;
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(query, "&");
        while (st.hasMoreTokens()) {
            String pair = (String) st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // should give more detail about the illegal argument
                return ht;
            }
            String key = parseName(pair.substring(0, pos), sb);
            String val = parseName(pair.substring(pos + 1, pair.length()), sb);
            if (ht.containsKey(key)) {
                String oldVals[] = (String[]) ht.get(key);
                valArray = new String[oldVals.length + 1];
                System.arraycopy(oldVals, 0, valArray, 0, oldVals.length);
                valArray[oldVals.length] = val;
            } else {
                valArray = new String[1];
                valArray[0] = val;
            }
            ht.put(key, valArray);
        }
        return ht;
    }

    private static String parseName(String s, StringBuffer sb) {
        sb.setLength(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 3),
                                16));
                        i += 2;
                    } catch (NumberFormatException e) {
                        // XXX
                        // need to be more specific about illegal arg
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2)
                            i++;
                    }
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
