package com.eyelinecom.whoisd.sads2.apiai.test;

import com.eyelinecom.whoisd.sads2.apiai.model.types.Request;
import com.eyelinecom.whoisd.sads2.apiai.model.types.Response;
import com.eyelinecom.whoisd.sads2.apiai.utils.MarshalUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jeck on 13/04/16.
 */
public class Test {
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
      convertSound();
    }

  public static void convertSound() throws IOException, UnsupportedAudioFileException {

  }

  public static void marshall() throws JsonProcessingException {
    Request request = new Request();
    request.setLang("RU");
    request.setSessionId(UUID.randomUUID().toString());
    request.setTimezone("Asia/Novosibirsk");
    System.out.println(MarshalUtils.marshal(request));
  }

  public static void unmarshall() throws IOException {
    String json = "{\n" +
            "  \"id\": \"cfcbd337-6b66-4393-a6a3-74fc5487cedb\",\n" +
            "  \"timestamp\": \"2016-02-16T00:30:13.529Z\",\n" +
            "  \"result\": {\n" +
            "    \"source\": \"agent\",\n" +
            "    \"resolvedQuery\": \"hi my name is Sam\",\n" +
            "    \"action\": \"greetings\",\n" +
            "    \"actionIncomplete\": false,\n" +
            "    \"parameters\": {\n" +
            "      \"name\": \"Sam\"\n" +
            "    },\n" +
            "    \"contexts\": [\n" +
            "      {\n" +
            "        \"name\": \"user_name\",\n" +
            "        \"parameters\": {\n" +
            "          \"name\": \"Sam\"\n" +
            "        },\n" +
            "        \"lifespan\": 5\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"greetings\",\n" +
            "        \"parameters\": {\n" +
            "          \"name\": \"Sam\"\n" +
            "        },\n" +
            "        \"lifespan\": 5\n" +
            "      }\n" +
            "    ],\n" +
            "    \"metadata\": {\n" +
            "      \"intentId\": \"c251ef97-0c43-404d-bf75-98e806f942be\",\n" +
            "      \"intentName\": \"Greetings\"\n" +
            "    },\n" +
            "    \"fulfillment\": {\n" +
            "      \"speech\": \"Hi Sam! How can I help you?\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"status\": {\n" +
            "    \"code\": 200,\n" +
            "    \"errorType\": \"success\"\n" +
            "  }\n" +
            "}";

    Response response = MarshalUtils.unmarshal(MarshalUtils.parse(json), Response.class);
    System.out.println(response);
  }
}
