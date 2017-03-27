package com.eyelinecom.whoisd.sads2.ai.interceptors;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * Created by jeck on 23/03/17.
 */
public class UnviversalAiInterceptor extends AbstractAiInterceptor implements Initable {
    private String pluginName;
    private String aiPageConfigParameter;
    private String aiTokenConfigParameter;

    @Override
    public void init(Properties config) throws Exception {
        this.pluginName = InitUtils.getString("plugin", config);
        this.aiPageConfigParameter = InitUtils.getString("ai-page-parameter-name", config);
        this.aiTokenConfigParameter = InitUtils.getString("ai-token-parameter-name", config);
    }

    @Override
    protected boolean isPluginCall(SADSRequest request) {
        String url = request.getResourceURI();
        return url.startsWith("https://miniapps.run/"+pluginName) || url.startsWith("http://miniapps.run/"+pluginName) ;
    }

    @Override
    protected String buildPluginUrl(String query, SADSRequest request) throws Exception {
        return "http://miniapps.run/"+pluginName;
    }

    @Override
    protected String getAiUrl(SADSRequest request) throws Exception {
        return InitUtils.getString(aiPageConfigParameter, request.getServiceScenario().getAttributes());
    }

    @Override
    protected String getToken(SADSRequest request) throws Exception {
        String token = request.getParameters().get("token");
        if (StringUtils.isNotBlank(token)) {
            return token;
        } else {
            return InitUtils.getString(aiTokenConfigParameter, request.getServiceScenario().getAttributes());
        }
    }

    @Override
    public void destroy() {

    }

}
