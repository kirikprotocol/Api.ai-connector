<%@ page contentType="text/html;charset=UTF-8" language="java"
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.XMLUtils"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.Loader"
%><%@ page import="com.eyelinecom.whoisd.sads2.common.HttpDataLoader"
%><%@ page import="java.util.List"
%><%@ page import="org.apache.log4j.Logger"
%><%@ page import="com.eyelinecom.whoisd.sads2.msqna.MicrosoftQNAMaker"
%><%@ page import="com.eyelinecom.whoisd.sads2.msqna.model.Response"
%><%@ page import="com.eyelinecom.whoisd.sads2.msqna.model.Answer"
%><%!
    private static Logger log = Logger.getLogger("helper.ms.qna");
    Loader<Loader.Entity> loader = new HttpDataLoader();

%><%
    String subscriber = request.getParameter("user_id");
    String apiToken = request.getParameter("token");
    String query = request.getParameter("query");
    if (StringUtils.isEmpty(query)) {
        query = request.getParameter("event.text");
    }
    String backPage = XMLUtils.forXML(request.getParameter("back_url"));
    String lang = request.getParameter("lang");
    if (StringUtils.isBlank(lang)) {
        lang = "en";
    }
    String answer = "";
    boolean iAmUseless = false;
    if (StringUtils.isNotBlank(apiToken) && StringUtils.isNotBlank(query)) {
        String[] authParams = apiToken.split(":");
        MicrosoftQNAMaker api = new MicrosoftQNAMaker(loader, authParams[0], authParams[1]);
        try {
            Response aiResponse = api.query(query, 1);
            if (log.isInfoEnabled()) {
                log.info(subscriber+" got api.ai raw response: "+aiResponse.getRaw());
            }
            List<Answer> answers = aiResponse.getAnswers();
            if (answers != null && answers.size()>0) {
                Answer a = answers.get(0);
                if (a.getScore() == 0.0) {
                    iAmUseless = true;
                }
                answer = a.getAnswer();
            }
        } catch (Exception e) {
            log.warn(subscriber,e);
        }
    }
    if (StringUtils.isBlank(answer)) {
        if (lang.equals("ru")) {
            answer = "Попробуйте еще раз";
        } else {
            answer = "Please try again";
        }
    }
%>
<page version="2.0">
    <% if (iAmUseless) { %>
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
</page>
