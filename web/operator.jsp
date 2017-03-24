<%@ page contentType="text/html;charset=UTF-8" language="java"%><?xml version="1.0" encoding="UTF-8"?>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.eyelinecom.whoisd.sads2.ai.utils.MarshalUtils" %>
<%@ page import="com.eyelinecom.whoisd.sads2.common.UrlUtils" %>
<%@ page import="com.eyelinecom.whoisd.sads2.common.XMLUtils" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.eyelinecom.whoisd.sads2.common.Loader" %>
<%@ page import="com.eyelinecom.whoisd.sads2.common.HttpDataLoader" %>
<%@ page import="com.eyelinecom.whoisd.sads2.apiai.AiApi" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.List" %>
<%@ page import="com.eyelinecom.whoisd.sads2.apiai.model.*" %>
<page version="2.0">
    <%!
        Loader<Loader.Entity> loader = new HttpDataLoader();
    %>
    <%
        String apiAijson = request.getParameter("api.ai");
        String apiToken = request.getParameter("api.ai.token");
        String backPage = request.getParameter("back_url");
        backPage = UrlUtils.addParameter(backPage, "mini_exit", "true");
        backPage = XMLUtils.forXML(backPage);

        boolean needEntityList = false;
	String lang = request.getParameter("lang");
        if (StringUtils.isBlank(lang)) {
		lang = "en";
	}
        Entity selectEntity = null;

        String answer = "";
        Response resp = MarshalUtils.unmarshal(MarshalUtils.parse(apiAijson), Response.class);
        if (resp.getStatus().getCode()==200) {
            Result result = resp.getResult();
            if (result!=null) {
                Fulfillment full = resp.getResult().getFulfillment();
                if (full!=null) {
                    answer = full.getSpeech();
                }
                boolean complete = result.getActionIncomplete() == null || !result.getActionIncomplete();
                if (!complete) {
                    Map<String,Object> params = result.getParameters();
                    if (params!=null) {
                        for (Map.Entry<String,Object> param: params.entrySet()) {
                            if ("".equals(param.getValue())) {
                                List<Context> ctxs = result.getContexts();
                                for(Context ctx: ctxs) {
                                    //we trying to find context with name ends with parameter
                                    //it is need to check what exactly parameters we asked
                                    if (ctx.getName().endsWith(param.getKey())) {
                                        AiApi api = new AiApi(loader, apiToken);
                                        try {
                                            selectEntity = api.getEntity(param.getKey());
                                        } catch (Exception e) {
                                            selectEntity = null;
                                        }
                                        if (selectEntity!=null) {
                                            needEntityList = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
	    if (lang.equals("ru")) {
            	answer = "Попробуйте еще раз";
            } else {
            	answer = "Please try again";
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
      <div>
        <%=answer%>
      </div>
      <!-- div>
        <input navigationId="submit" name="promt"/>
      </div>
      <navigation id="submit">
        <link accesskey="1" pageId="resp.jsp">Ok</link>
      </navigation -->
      <% if (lang.equals("ru")) { %>
      <navigation>
        <link accesskey="0" pageId="<%=backPage%>">Назад</link>
      </navigation>
      <% } else { %>
      <navigation>
        <link accesskey="0" pageId="<%=backPage%>">Back</link>
      </navigation>
      <% } %>

    <%if (needEntityList && selectEntity!=null) { %>
    <navigation>
        <% for(Entity.Entry entry: selectEntity.getEntries()) {%>
            <link pageId="?ai.entity.select=<%=URLEncoder.encode(entry.getValue(),"utf-8")%>"><%=entry.getValue()%></link>
        <%}%>
    </navigation>
    <%}%>
</page>
