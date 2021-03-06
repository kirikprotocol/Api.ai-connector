<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.UrlUtils"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.XMLUtils"
%><%@ page import="java.util.Map"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.Loader"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.HttpDataLoader"
%><%@ page import="com.eyelinecom.whoisd.sads2.apiai.AiApi"
%><%@ page import="java.net.URLEncoder"
%><%@ page import="java.util.List"
%><%@ page import="org.apache.log4j.Logger"
%><%@ page import="com.eyelinecom.whoisd.sads2.apiai.model.*"
%><%!
        private static Logger log = Logger.getLogger("helper.api.ai");
        private final static String MINIAPPS_HOST = "api.miniapps.run";
        private final static String PUSH_API = "https://"+MINIAPPS_HOST+"/push";
        private final static Boolean RETURN_BACK_ON_FAIL = Boolean.TRUE;
        private final static Boolean READ_SESSION_AI_RESPONSE = Boolean.FALSE;

        Loader<Loader.Entity> loader = new HttpDataLoader();

    private void sendMessage(String userId, String service, String protocol, String message) throws Exception {
        String pushUrl = UrlUtils.addParameter(PUSH_API, "user_id", userId);
        pushUrl = UrlUtils.addParameter(pushUrl, "subscriber", userId);
        pushUrl = UrlUtils.addParameter(pushUrl, "service", service);
        pushUrl = UrlUtils.addParameter(pushUrl, "scenario", "push");
        pushUrl = UrlUtils.addParameter(pushUrl, "protocol", protocol);
        pushUrl = UrlUtils.addParameter(pushUrl, "message", message);
        loader.load(pushUrl);
    }

    %><%
        String subscriber = request.getParameter("user_id");
        String service = request.getParameter("service");
        String protocol = request.getParameter("protocol");
        String apiToken = request.getParameter("token");
        String query = request.getParameter("query");
        if (StringUtils.isEmpty(query)) {
            query = request.getParameter("event.text");
        }
        String backPageRaw = request.getParameter("back_url");
        String backPage = XMLUtils.forXML(backPageRaw);
        String lang = request.getParameter("lang");
        if (StringUtils.isBlank(lang)) {
            lang = "en";
        }
        String answer = "";
        Entity selectEntity = null;
        boolean iAmUseless = false;

        if (StringUtils.isNotBlank(apiToken) && StringUtils.isNotBlank(query)) {
            AiApi api = new AiApi(loader, apiToken);
            try {
                Response aiResponse;
                if (!READ_SESSION_AI_RESPONSE) {
                    aiResponse = api.query(subscriber, query);
                    session.setAttribute("ai.response", aiResponse);
                } else {
                    aiResponse = (Response) session.getAttribute("ai.response");
                }
                Result result = aiResponse.getResult();
                if (log.isInfoEnabled()) {
                    log.info(subscriber+" got api.ai raw response: "+aiResponse.getRaw());
                }
                if (result!=null) {
                    boolean complete = result.getActionIncomplete() != null && !result.getActionIncomplete();
                    if (log.isInfoEnabled()) {
                        log.info(subscriber + " action completed state: " + complete + ", action: " + result.getAction());
                    }
                    Fulfillment full = result.getFulfillment();
                    if (full!=null) {
                        answer = full.getSpeech();
                    }
                    if (complete) {
                        String action = result.getAction();
                        if (StringUtils.isNotBlank(action) && UrlUtils.isAbsoluteUrl(action)) {
                            String redirectPage = action;
                            if (result.getParameters() != null) {
                                for (Map.Entry<String, Object> aiParam : result.getParameters().entrySet()) {
                                    if (aiParam.getValue() instanceof String && StringUtils.isNotBlank((String) aiParam.getValue())) {
                                        redirectPage = UrlUtils.addParameter(redirectPage, aiParam.getKey(), (String) aiParam.getValue());
                                    }
                                }
                            }
                            if (StringUtils.isNotBlank(answer)) {
                                this.sendMessage(subscriber, service, protocol, answer);
                            }
                            response.sendRedirect(redirectPage);
                            return;
                        }
                        String intentName = result.getMetadata().getIntentName();
                        log.info("intent name: "+intentName);
                        if ("Default Fallback Intent".equals(intentName)) {
                            iAmUseless = true;
                            if (RETURN_BACK_ON_FAIL) {
                                if (StringUtils.isNotBlank(answer)) {
                                    this.sendMessage(subscriber, service, protocol, answer);
                                }
                                response.sendRedirect(backPageRaw);
                                return;
                            }
                        }
                    } else {
                        iAmUseless = false;
                        Map<String,Object> params = result.getParameters();
                        if (params!=null) {
                            for (Map.Entry<String,Object> param: params.entrySet()) {
                                if (StringUtils.isEmpty((String)param.getValue())) {
                                    List<Context> ctxs = result.getContexts();
                                    for(Context ctx: ctxs) {
                                        //we trying to find context with name ends with parameter
                                        //it is need to check what exactly parameters we asked
                                        if (ctx.getName().endsWith(param.getKey())) {
                                            try {
                                                selectEntity = api.getEntity(param.getKey());
                                            } catch (Exception e) {
                                                selectEntity = null;
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn(subscriber,e);
            }
        }
        if (StringUtils.isBlank(answer)) {
            iAmUseless = true;
            if (lang.equals("ru")) {
                answer = "Попробуйте еще раз";
            } else {
                answer = "Please try again";
            }
        }
    %>
<page version="2.0">
    <% if (iAmUseless && !RETURN_BACK_ON_FAIL) { %>
    <attributes>
        <attribute name="i-am-useless" value="<%=XMLUtils.forXML(query)%>"/>
    </attributes>
    <% } %>
    <div>
        <%=answer%>
    </div>
    <% if (lang.equals("ru")) { %>
    <navigation>
        <link accesskey="0" pageId="<%=backPage%>">Назад</link>
    </navigation>
    <% } else { %>
    <navigation>
        <link accesskey="0" pageId="<%=backPage%>">Back</link>
    </navigation>
    <% } %>

    <%if (selectEntity!=null) { %>
    <navigation>
        <% for(Entity.Entry entry: selectEntity.getEntries()) {%>
        <link pageId="?ai.entity.select=<%=URLEncoder.encode(entry.getValue(),"utf-8")%>"><%=entry.getValue()%></link>
        <%}%>
    </navigation>
    <%}%>
</page>
