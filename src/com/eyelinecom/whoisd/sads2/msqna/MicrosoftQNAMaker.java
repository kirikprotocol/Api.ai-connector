package com.eyelinecom.whoisd.sads2.msqna;


import com.eyelinecom.whoisd.sads2.ai.utils.MarshalUtils;
import com.eyelinecom.whoisd.sads2.common.Loader;
import com.eyelinecom.whoisd.sads2.msqna.model.Request;
import com.eyelinecom.whoisd.sads2.msqna.model.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeck on 23/03/17.
 */
public class MicrosoftQNAMaker {
    private String url = "https://westus.api.cognitive.microsoft.com/qnamaker/v2.0/knowledgebases/";
    private String knowledgeBaseID;
    private String subscriptionKey;
    private Loader<Loader.Entity> loader;


    public MicrosoftQNAMaker(Loader<Loader.Entity> loader, String knowledgeBaseID, String subscriptionKey) {
        this.knowledgeBaseID = knowledgeBaseID;
        this.subscriptionKey = subscriptionKey;
        this.loader = loader;
    }

    public Response query(String question, int top) throws Exception {
        final String url = this.url +knowledgeBaseID+"/generateAnswer";
        final Request request = new Request();
        request.setQuestion(question);
        request.setTop(top);
        String jsonRequest = MarshalUtils.marshal(request);
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Ocp-Apim-Subscription-Key", subscriptionKey);
        Loader.Entity response = loader.load(url, jsonRequest, "application/json", "utf-8", headers, "post");
        if (response.getBuffer()!=null) {
            String data = new String(response.getBuffer(), "utf-8");
            Response res = MarshalUtils.unmarshal(MarshalUtils.parse(data), Response.class);
            res.setRaw(data);
            return res;
        }
        return null;
    }
}
