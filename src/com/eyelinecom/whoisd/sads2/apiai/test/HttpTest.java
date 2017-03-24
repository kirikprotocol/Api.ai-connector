package com.eyelinecom.whoisd.sads2.apiai.test;

import com.eyelinecom.whoisd.sads2.apiai.AiApi;
import com.eyelinecom.whoisd.sads2.apiai.model.Entity;
import com.eyelinecom.whoisd.sads2.apiai.model.Response;
import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Loader;

import java.util.*;

/**
 * Created by jeck on 14/04/16.
 */
public class HttpTest {
    public static void main(String[] args) throws Exception {
      text();
    }

    public static void entities() throws Exception {
      //SoundUtils.convert2wav("https://api.telegram.org/file/bot190874795:AAE6y2W9KL1kF_5X_JPcuW_JiavRE1bRzx4/voice/file_12.oga");
      //SoundUtils.convert2wav("https://api.telegram.org/file/bot190874795:AAE6y2W9KL1kF_5X_JPcuW_JiavRE1bRzx4/voice/file_12.oga");
      Loader<Loader.Entity> loader = new HttpDataLoader();
      AiApi api = new AiApi(loader, "7aac4e0183cf4ed9ad77fdc32e3e39e2");
      //byte[] bytes = SoundUtils.convert2wav("https://api.telegram.org/file/bot190874795:AAE6y2W9KL1kF_5X_JPcuW_JiavRE1bRzx4/voice/file_15.oga");
      //Response response = api.query(UUID.randomUUID().toString(), bytes);
      Entity entity = api.getEntity("service-balance-control");
      System.out.println(entity);

    }

  public static void text() throws Exception {
    Loader<Loader.Entity> loader = new HttpDataLoader();
    AiApi api = new AiApi(loader, "7aac4e0183cf4ed9ad77fdc32e3e39e2");
    Response response = api.query(UUID.randomUUID().toString(), "пополни баланс 7XXXXXXXXXX");
    System.out.println(response);
  }

/*  public static void voice() throws Exception {
    Loader<Loader.Entity> loader = new HttpDataLoader();
    AiApi api = new AiApi(loader, "edc38bca466547e08ec17c531dfd2c38");
    byte[] bytes = FileUtils.readFileToByteArray(new File("/Users/jeck/Downloads/file_4.wav"));
    Response response = api.query(UUID.randomUUID().toString(), bytes);
    System.out.println(response);
  } */

}
