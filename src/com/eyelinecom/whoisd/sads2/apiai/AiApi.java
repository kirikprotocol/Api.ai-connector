package com.eyelinecom.whoisd.sads2.apiai;

import com.eyelinecom.whoisd.sads2.apiai.model.Entities;
import com.eyelinecom.whoisd.sads2.apiai.model.Entity;
import com.eyelinecom.whoisd.sads2.apiai.model.Response;
import com.eyelinecom.whoisd.sads2.ai.utils.MarshalUtils;
import com.eyelinecom.whoisd.sads2.common.Loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeck on 14/04/16.
 */
public class AiApi {
  private Loader<Loader.Entity> loader;
  private String token;
  private String lang = "RU";
  private String apiUrl = "https://api.api.ai/v1/";

  public AiApi(Loader<Loader.Entity> loader, String token) {
    this.loader = loader;
    this.token = token;
  }

  public Response query(String session, String query) throws Exception {
    Map<String,String> parameters = new HashMap<String, String>();
    Map<String,String> headers = new HashMap<String, String>();
    headers.put("Authorization", "Bearer "+token);

    parameters.put("query", query);
    parameters.put("v", "20150910");
    parameters.put("sessionId", session);
    parameters.put("lang", lang);
    parameters.put("timezone", "Asia/Novosibirsk");

    Loader.Entity entity = loader.load(apiUrl+"query", parameters, headers, "get");
    String json = new String(entity.getBuffer(), "utf-8");
    Response response = MarshalUtils.unmarshal(MarshalUtils.parse(json), Response.class);
    response.setRaw(json);
    return response;
  }
/*
  public Response query(String session, byte[] wav) throws Exception {
    Request request = new Request();
    request.setLang("RU");
    request.setSessionId(session);
    request.setTimezone("Asia/Novosibirsk");
    StringPart jsonPart = new StringPart("request", MarshalUtils.marshal(request), "utf-8");

    PartSource bytesPart = new ByteArrayPartSource("sound.wav", wav);
    FilePart part = new FilePart("voiceData", bytesPart, "audio/wav", "UTF-8");
    PostMethod filePost = new PostMethod(apiUrl+"query?v=20150910");
    filePost.addRequestHeader("Authorization", "Bearer "+token);
    filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
    try {
      Part[] parts = new Part[]{jsonPart, part};
      filePost.setRequestEntity(
              new MultipartRequestEntity(parts, filePost.getParams())
      );
      HttpClient client = new HttpClient();
      client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
      int status = client.executeMethod(filePost);
      if (status == HttpStatus.SC_OK) {
        String json = filePost.getResponseBodyAsString();
        return MarshalUtils.unmarshal(MarshalUtils.parse(json), Response.class);
      } else {
        throw new Exception("Bad status: "+ HttpStatus.getStatusText(status));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      filePost.releaseConnection();
    }
    throw new Exception("error");
  }
*/
  private Entities.Entry fingEntry(String name) throws Exception {
    Map<String,String> parameters = new HashMap<String, String>();
    Map<String,String> headers = new HashMap<String, String>();
    headers.put("Authorization", "Bearer "+token);
    parameters.put("v", "20150910");

    Loader.Entity data = loader.load(apiUrl+"entities", parameters, headers, "get");
    String json = new String(data.getBuffer(), "utf-8");
    Entities.Entry[] entities = MarshalUtils.unmarshal(MarshalUtils.parse(json), Entities.Entry[].class);
    for (Entities.Entry entry: entities) {
      if (name.equals(entry.getName())) return entry;
    }
    return null;
  }

  public Entity getEntity(String name) throws Exception {
    Entities.Entry entry = this.fingEntry(name);
    if (entry == null) return null;

    Map<String,String> parameters = new HashMap<String, String>();
    Map<String,String> headers = new HashMap<String, String>();
    headers.put("Authorization", "Bearer "+token);
    parameters.put("v", "20150910");

    Loader.Entity entity = loader.load(apiUrl+"entities/"+entry.getId(), parameters, headers, "get");
    String json = new String(entity.getBuffer(), "utf-8");
    return MarshalUtils.unmarshal(MarshalUtils.parse(json), Entity.class);
  }
}
