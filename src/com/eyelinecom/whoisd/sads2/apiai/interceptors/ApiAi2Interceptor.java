package com.eyelinecom.whoisd.sads2.apiai.interceptors;

import com.eyelinecom.whoisd.sads2.ai.interceptors.AbstractAiInterceptor;
import com.eyelinecom.whoisd.sads2.ai.utils.SoundUtils;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import org.apache.commons.lang.StringUtils;

/**
 * Created by jeck on 22/03/17.
 */
public class ApiAi2Interceptor extends AbstractAiInterceptor {
    private static final String CONF_API_AI_TOKEN = "api-ai.token";
    private static final String CONF_API_AI_PAGE = "api-ai.page";

    @Override
    protected boolean isPluginCall(SADSRequest request) {
        String url = request.getResourceURI();
        return url.startsWith("https://miniapps.run/api.ai") || url.startsWith("http://miniapps.run/api.ai") ;
    }

    @Override
    protected String buildPluginUrl(String query, SADSRequest request) throws Exception {
        String url = UrlUtils.addParameter("http://miniapps.run/api.ai", "query", query);
        return UrlUtils.addParameter(url, "token", getToken(request));
    }

    @Override
    protected String getAiUrl(SADSRequest request) throws Exception {
        return InitUtils.getString(CONF_API_AI_PAGE, request.getServiceScenario().getAttributes());
    }

    @Override
    protected String getToken(SADSRequest request) throws Exception  {
        String token = request.getParameters().get("token");
        if (StringUtils.isNotBlank(token)) {
            return token;
        } else {
            return InitUtils.getString(CONF_API_AI_TOKEN, request.getServiceScenario().getAttributes());
        }
    }
}
