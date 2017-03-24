package com.eyelinecom.whoisd.sads2.apiai.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.apiai.AiApi;
import com.eyelinecom.whoisd.sads2.apiai.model.Response;
import com.eyelinecom.whoisd.sads2.apiai.model.Result;
import com.eyelinecom.whoisd.sads2.common.*;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.Properties;

/**
 * Created by jeck on 28/12/16.
 */
public class ApiAiInterceptor extends BlankInterceptor implements Initable{
    private static final String ATTR_API_AI = "process.api-ai";
    private static final String ATTR_API_AI_BACKPAGE = "process.api-ai.page";
    private static final String CONF_API_AI_TOKEN = "api-ai.token";
    private static final String CONF_API_AI_PAGE = "api-ai.page";

    public static final String PARAM_EXIT = "mini_exit";

    public static final String HTTP_PLUGIN_URL = "http://miniapps.run/api.ai";
    public static final String HTTPS_PLUGIN_URL = "https://miniapps.run/api.ai";

    private Loader<Loader.Entity> loader;

    private boolean isPluginCall(SADSRequest request) {
        return request.getResourceURI().startsWith(HTTP_PLUGIN_URL) || request.getResourceURI().startsWith(HTTPS_PLUGIN_URL);
    }

    @Override
    public void onRequest(SADSRequest request, RequestDispatcher dispatcher) throws InterceptionException {
        Log log = SADSLogger.getLogger(request.getServiceId(), request.getAbonent(), this.getClass());
        Session session = request.getSession();
        if ("true".equals(request.getParameters().get(PARAM_EXIT))) {
            log.info("exit from API.ai helper");
            request.getSession().removeAttribute(ATTR_API_AI);
            request.getSession().removeAttribute(ATTR_API_AI_BACKPAGE);
            request.getParameters().remove(PARAM_EXIT);
            request.setResourceURI(UrlUtils.removeParameter(request.getResourceURI(), PARAM_EXIT));
            return;
        }
        if (session!=null) {
            String attr = (String) session.getAttribute(ATTR_API_AI);
            String apiAipage = InitUtils.getString(CONF_API_AI_PAGE, null, request.getServiceScenario().getAttributes());
            String needToProcessAi  = request.getParameters().get("ai.entity.select");
            if (StringUtils.isNotBlank(apiAipage)) {
                if (StringUtils.isNotBlank(attr) || StringUtils.isNotBlank(needToProcessAi)) {
                    String query = request.getParameters().get("event.text");
                    this.process(query, request);
                } else if (request.getParameters().containsKey("bad_command")) {
                    if (!request.getResourceURI().startsWith(apiAipage)) {
                        String currentPage = request.getResourceURI();
                        currentPage = UrlUtils.removeParameter(currentPage,"bad_command");
                        currentPage = UrlUtils.removeParameter(currentPage,"subscriber");
                        currentPage = UrlUtils.removeParameter(currentPage,"user_id");
                        currentPage = UrlUtils.removeParameter(currentPage,"service");
                        currentPage = UrlUtils.removeParameter(currentPage,"serviceId");
                        currentPage = UrlUtils.removeParameter(currentPage,"protocol");
                        currentPage = UrlUtils.removeParameter(currentPage,"abonent");
                        request.getSession().setAttribute(ATTR_API_AI_BACKPAGE, currentPage);
                    }
                    String query = request.getParameters().get("event.text");
                    this.process(query, request);
                } else if (isPluginCall(request)){
                    String backUrl = request.getParameters().get("back_url");
                    if (StringUtils.isBlank(backUrl)){
                        backUrl = UrlUtils.getParameter("back_url", request.getResourceURI());
                    }
                    if (StringUtils.isBlank(backUrl)){
                        backUrl = request.getParameters().get("event.referer");
                    }
                    request.getSession().setAttribute(ATTR_API_AI_BACKPAGE, backUrl);
                    String query = request.getParameters().get("query");
                    this.process(query, request);
                }
            }
        }
    }

    private void process(String query, SADSRequest request) {
        Log log = SADSLogger.getLogger(request.getServiceId(), request.getAbonent(), this.getClass());
        String apiAiToken = InitUtils.getString(CONF_API_AI_TOKEN, null, request.getServiceScenario().getAttributes());
        if (log.isDebugEnabled()) {
            log.debug("User said: "+ query);
        }
        if (StringUtils.isNotBlank(apiAiToken) && StringUtils.isNotBlank(query)) {
            if (log.isInfoEnabled()) {
                log.info("User sentence: "+ query +" trying to send to api.ai");
            }
            AiApi api = new AiApi(loader, apiAiToken);
            try {
                Response response = api.query(request.getSession().getId(), query);
                Result result = response.getResult();
                if (log.isDebugEnabled()) {
                    log.debug("Got api.ai raw response: "+response.getRaw());
                }
                if (result!=null) {
                    boolean complete = result.getActionIncomplete() != null && !result.getActionIncomplete();
                    String apiAiResponse = response.getRaw();
                    request.getParameters().put("api.ai", apiAiResponse);
                    request.getParameters().put("api.ai.token", apiAiToken);
                    request.getParameters().put("action", result.getAction());
                    String backPage = (String) request.getSession().getAttribute(ATTR_API_AI_BACKPAGE);
                    if (StringUtils.isNotBlank(backPage)) {
                        request.getParameters().put("back_url", backPage);
                    }
                    if (log.isInfoEnabled()) {
                        log.info("Action completed state: " + complete + ", action: " + result.getAction());
                    }
                    if (result.getParameters() != null) {
                        for (Map.Entry<String, Object> aiParam : result.getParameters().entrySet()) {
                            if (aiParam.getValue() instanceof String && StringUtils.isNotBlank((String) aiParam.getValue())) {
                                request.getParameters().put(aiParam.getKey(), (String) aiParam.getValue());
                            }
                        }
                    }
                    if (complete) {
                        String action = result.getAction();
                        if (StringUtils.isNotBlank(action) && UrlUtils.isAbsoluteUrl(action)) {
                            request.getParameters().putAll(UrlUtils.getParametersMap(action));
                            request.setResourceURI(action);
                        } else {
                            request.setResourceURI(InitUtils.getString(CONF_API_AI_PAGE, request.getResourceURI(), request.getServiceScenario().getAttributes()));
                        }
                    } else {
                        request.getSession().setAttribute(ATTR_API_AI, "true");
                        String apiAipage = InitUtils.getString(CONF_API_AI_PAGE, null, request.getServiceScenario().getAttributes());
                        if (StringUtils.isNotBlank(apiAipage)) {
                            request.setResourceURI(apiAipage);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("",e);
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(Properties config) throws Exception {
        this.loader = SADSInitUtils.getResource("loader", config);
    }
}
