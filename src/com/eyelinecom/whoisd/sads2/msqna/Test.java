package com.eyelinecom.whoisd.sads2.msqna;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.Loader;
import com.eyelinecom.whoisd.sads2.msqna.model.Response;

/**
 * Created by jeck on 23/03/17.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        Loader<Loader.Entity> loader = new HttpDataLoader();

        MicrosoftQNAMaker api = new MicrosoftQNAMaker(loader, "TOKEN", "TOKEN");

        Response response = api.query("спасибо", 1);
        System.out.println(response.getRaw());

    }
}
