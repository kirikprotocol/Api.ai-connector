package com.eyelinecom.whoisd.sads2.ai.utils;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Loader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Created by jeck on 15/04/16.
 */
public class SoundUtils {
  static Loader<Loader.Entity> loader = new HttpDataLoader();

  public static byte[] convert2wav(String url) throws Exception {
    File inputFile = null;
    File outputFile = null;
    try {
      byte[] inputData = loader.load(url).getBuffer();
      inputFile = new File("/tmp/"+UUID.randomUUID()+".oga");
      FileUtils.writeByteArrayToFile(inputFile, inputData);
      outputFile = new File("/tmp/"+UUID.randomUUID()+".wav");
      Process p = Runtime.getRuntime().exec("/opt/apps/sads/ffmpeg/ffmpeg -i "+inputFile.getAbsolutePath()+" -ar 16000 "+outputFile.getAbsolutePath());
      p.waitFor();
      return FileUtils.readFileToByteArray(outputFile);
    } finally {
      FileUtils.deleteQuietly(inputFile);
      FileUtils.deleteQuietly(outputFile);
    }
  }
}


