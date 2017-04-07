package com.eyelinecom.whoisd.sads2.ai.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.*;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

/**
 * Created by jeck on 22/03/17.
 */
public abstract class AbstractAiInterceptor extends BlankInterceptor {

    protected static final String ATTR_TOKEN = "process.ai.token";
    protected static final String ATTR_BACKPAGE = "process.ai.page";
    protected static final String PARAM_AI_DISABLED = "_ai_disabled";

    protected abstract boolean isPluginCall(SADSRequest request);
    protected abstract String buildPluginUrl(String query, SADSRequest request) throws Exception;

    protected abstract String getAiUrl(SADSRequest request) throws Exception;
    protected abstract String getToken(SADSRequest request) throws Exception;

    protected abstract boolean fireOnAnyPromt(SADSRequest request);

    protected boolean isContinueDialog(SADSRequest request) {
        try {
            return request.getResourceURI().startsWith(getAiUrl(request));
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isAiEnabled(SADSRequest request) {
        try{

            String disableAi = request.getParameters().get(PARAM_AI_DISABLED);
            if (StringUtils.isBlank(disableAi)) {
                disableAi = UrlUtils.getParameter(request.getResourceURI(), PARAM_AI_DISABLED);
                if (StringUtils.isNotBlank(disableAi)) {
                    try {
                        return Boolean.parseBoolean(disableAi);
                    } catch (Exception e) {
                        //skip
                    }
                }
            }
            String url = getAiUrl(request);
            String token = getToken(request);
            return StringUtils.isNotBlank(url) && StringUtils.isNotBlank(token);
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isNeedToProcessPromt(SADSRequest request) {
        if (!isCurrentAiFiredAttribute(request)) {
            if (fireOnAnyPromt(request)) {
                if ("message".equals(request.getParameters().get("event"))) {
                    String eventText = request.getParameters().get("event.text");
                    return StringUtils.isNotBlank(eventText) && !eventText.startsWith("/");
                }
            } else {
                return request.getParameters().containsKey("bad_command");
            }
        }
        return false;
    }

    protected void process(String query, String backPage, String token, SADSRequest request, Log log) {
        try {
            String aiUrl = this.getAiUrl(request);
            request.getParameters().put("query", query);
            request.getParameters().put("token", token);
            request.getParameters().put("back_url", backPage);
            request.setResourceURI(aiUrl);
            setAiFiredAttribute(request);
            log.info("... move dialog to "+aiUrl+" by query: \""+query+"\", with backpage: "+backPage);
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    @Override
    public void onRequest(SADSRequest request, RequestDispatcher dispatcher) throws InterceptionException {
        Log log = SADSLogger.getLogger(request.getServiceId(), request.getAbonent(), this.getClass());
        if (isAiEnabled(request) && !isCurrentAiUseless(request)) {
            if (isPluginCall(request)) {
                log.info("... it is start AI dialog by plugin calling");
                String backUrl = request.getParameters().get("back_url");
                if (StringUtils.isBlank(backUrl)){
                    backUrl = UrlUtils.getParameter("back_url", request.getResourceURI());
                }
                String startPage;
                try {
                    startPage = InitUtils.getString("start-page", request.getServiceScenario().getAttributes());
                } catch (Exception e) {
                    throw new InterceptionException(e);
                }
                try {
                    backUrl = UrlUtils.merge(startPage, backUrl);
                } catch (Exception e) {
                    backUrl = startPage;
                    log.warn("",e);
                }
                request.getSession().setAttribute(ATTR_BACKPAGE, backUrl);
                String query = request.getParameters().get("query");
                try {
                    String token = this.getToken(request);
                    request.getSession().setAttribute(ATTR_TOKEN, token);
                    this.process(query, backUrl, token, request, log);
                } catch (Exception e) {
                    log.warn("",e);
                }
            } else if (
                    !isAiFiredAttribute(request) ||       //No one of AI processors is not active OR
                    isCurrentAiFiredAttribute(request)   //at the moment CURRENT processor is active
                    ) {
                log.info("AI is enabled trying to resolve dialog stage...");
                if (isContinueDialog(request)) {
                    log.info("... it is AI dialog continue dialog");
                    String query = request.getParameters().get("event.text");
                    String backPage = (String) request.getSession().getAttribute(ATTR_BACKPAGE);
                    String token = (String) request.getSession().getAttribute(ATTR_TOKEN);
                    this.process(query, backPage, token, request, log);
                } else if (isNeedToProcessPromt(request)) {
                    log.info("... it is start AI dialog by text input (fire on any text = "+this.fireOnAnyPromt(request)+")");
                    String currentPage = request.getResourceURI();
                    currentPage = UrlUtils.removeParameter(currentPage,"bad_command");
                    currentPage = UrlUtils.removeParameter(currentPage,"subscriber");
                    currentPage = UrlUtils.removeParameter(currentPage,"user_id");
                    currentPage = UrlUtils.removeParameter(currentPage,"service");
                    currentPage = UrlUtils.removeParameter(currentPage,"serviceId");
                    currentPage = UrlUtils.removeParameter(currentPage,"protocol");
                    currentPage = UrlUtils.removeParameter(currentPage,"abonent");
                    request.getSession().setAttribute(ATTR_BACKPAGE, currentPage);
                    try {
                        String token = this.getToken(request);
                        request.getSession().setAttribute(ATTR_TOKEN, token);
                        String query = request.getParameters().get("event.text");
                        this.process(query, currentPage, token, request, log);
                    } catch (Exception e) {
                        log.warn("",e);
                    }
                } else {
                    log.info("... exit from AI. Clearing AI attributes");
                    request.getSession().removeAttribute(ATTR_BACKPAGE);
                    request.getSession().removeAttribute(ATTR_TOKEN);
                    clearAiFiredAttribute(request);
                }
            }
        }
    }

    @Override
    public void afterContentResponse(SADSRequest request, ContentRequest contentRequest, ContentResponse content, RequestDispatcher dispatcher) throws InterceptionException {
        Log log = SADSLogger.getLogger(request.getServiceId(), request.getAbonent(), this.getClass());
        String heIsUseless = (String) content.getAttributes().get("i-am-useless");
        if (heIsUseless!=null && StringUtils.isNotBlank(heIsUseless)) { //if AI failed
            if (isCurrentAiFiredAttribute(request)) { //is current AI failed
                setAiUseless(request); //mark current AI as useless with current query
            } else if (!isCurrentAiUseless(request)) {
                try {
                    request.getParameters().remove("token"); //remove token from previous AI module
                    if (isAiEnabled(request)){ //check if is set in config
                        clearAiFiredAttribute(request);
                        setAiFiredAttribute(request);
                        String nextPlugin = this.buildPluginUrl(heIsUseless, request);
                        request.setResourceURI(nextPlugin);
                        request.getParameters().put("token", this.getToken(request));
                        log.info("quality is terrible! I swear, I will be better with query: \""+ heIsUseless +"\"! Moving to "+nextPlugin);
                        dispatcher.processRequest(request);
                    }
                } catch (Exception e) {
                    log.warn("",e);
                }
            }
        }
    }

    protected void setAiUseless(SADSRequest request) {
        request.getAttributes().put("ai.module.useless-"+this.hashCode(), true);
    }

    protected boolean isCurrentAiUseless(SADSRequest request) {
        Object attr = request.getAttributes().get("ai.module.useless-"+this.hashCode());
        return attr!=null && attr instanceof Boolean && (Boolean)attr;
    }

    protected void setAiFiredAttribute(SADSRequest request) {
        request.getSession().setAttribute("ai.module.fire-"+this.hashCode(), true);
        request.getSession().setAttribute("ai.module.fire", true);
    }
    protected boolean isAiFiredAttribute(SADSRequest request) {
        Object attr = request.getSession().getAttribute("ai.module.fire");
        return attr!=null && attr instanceof Boolean && (Boolean)attr;
    }
    protected boolean isCurrentAiFiredAttribute(SADSRequest request) {
        Object attr = request.getSession().getAttribute("ai.module.fire-"+this.hashCode());
        return attr!=null && attr instanceof Boolean && (Boolean)attr;
    }

    protected void clearAiFiredAttribute(SADSRequest request) {
        Session session = request.getSession();
        for (String attributeName: session.getAttributesNames()) {
            if (attributeName.startsWith("ai.module.fire-")) {
                session.removeAttribute(attributeName);
            }
        }
        request.getSession().removeAttribute("ai.module.fire");
    }
}
