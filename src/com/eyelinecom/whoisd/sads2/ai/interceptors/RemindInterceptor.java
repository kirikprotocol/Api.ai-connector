package com.eyelinecom.whoisd.sads2.ai.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.*;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import org.apache.commons.logging.Log;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeck on 10/04/17.
 */
public class RemindInterceptor extends BlankInterceptor  implements Initable {
    private static final String ATTR_NOTIFICATION =  "notification.future";
    private ScheduledExecutorService executor;
    private Loader loader;
    private String text;
    private String url;
    private long delay;

    private String prohibitedScenario;
    private String prohibitedParameter;

    @Override
    public void onRequest(final SADSRequest request, RequestDispatcher dispatcher) throws InterceptionException {
        final Log log = SADSLogger.getLogger(request.getServiceId(), request.getAbonent(), this.getClass());
        if (org.apache.commons.lang.StringUtils.isNotBlank(prohibitedScenario)) {
            if (prohibitedScenario.equals(request.getScenarioId())) {
                log.debug("Do not create notification by scenario: "+request.getScenarioId());
                return;
            }
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(prohibitedParameter)) {
            String param = request.getParameters().get(prohibitedParameter);
            if (org.apache.commons.lang.StringUtils.isNotBlank(param)) {
                log.debug("Do not create notification by parameter: "+prohibitedParameter);
                return;
            }
        }
        Session session = request.getSession();
        ScheduledFuture oldNotification = (ScheduledFuture) session.getAttribute(ATTR_NOTIFICATION);
        if (oldNotification!=null) {
            if (log.isDebugEnabled()) {
                log.debug("got old notification from session: in "+oldNotification.getDelay(TimeUnit.MILLISECONDS)+" ms, Done: "+oldNotification.isDone()+", Cancelled: "+oldNotification.isCancelled());
            }
            if (!oldNotification.isDone()) {
                oldNotification.cancel(false);
                if (log.isDebugEnabled()) {
                    log.debug("cancelled old notification from session: in "+oldNotification.getDelay(TimeUnit.MILLISECONDS)+" ms, Done: "+oldNotification.isDone()+", Cancelled: "+oldNotification.isCancelled());
                }

            }
        }
        ScheduledFuture future = executor.schedule(
                new NotificationRunnable(
                        url,
                        request.getProfile().getWnumber(),
                        text,
                        request.getProtocol().getProtocolName(),
                        log,
                        loader,
                        request.getServiceId()
                ), delay, TimeUnit.MILLISECONDS);
        session.setAttribute(ATTR_NOTIFICATION, future);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(Properties config) throws Exception {
        executor = SADSInitUtils.getResource("executor", config);
        loader = SADSInitUtils.getResource("loader", config);
        text = InitUtils.getString("text", config);
        url = InitUtils.getString("url", config);
        delay = InitUtils.getLong("delay", config);
        prohibitedScenario = InitUtils.getString("disable.scenario", null, config);
        prohibitedParameter = InitUtils.getString("disable.parameter", null, config);
    }

    private static class NotificationRunnable implements Runnable {
        String url;
        String wnumber;
        String text;
        String protocol;
        Log log;
        Loader loader;
        String service;

        public NotificationRunnable(String url, String wnumber, String text, String protocol, Log log, Loader loader, String service) {
            this.url = url;
            this.wnumber = wnumber;
            this.text = text;
            this.protocol = protocol;
            this.log = log;
            this.loader = loader;
            this.service = service;
        }

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Executing notification for "+wnumber);
            }
            String pushUrl = UrlUtils.addParameter(url, "user_id", wnumber);
            pushUrl = UrlUtils.addParameter(pushUrl, "subscriber", wnumber);
            pushUrl = UrlUtils.addParameter(pushUrl, "message", text);
            pushUrl = UrlUtils.addParameter(pushUrl, "protocol", protocol);
            pushUrl = UrlUtils.addParameter(pushUrl, "service", service);
            try {
                loader.load(pushUrl);
                if (log.isDebugEnabled()) {
                    log.debug("Executing notification for "+wnumber+" executed");
                }
            } catch (Exception e) {
                log.warn("",e);
            }

        }
    }
}
