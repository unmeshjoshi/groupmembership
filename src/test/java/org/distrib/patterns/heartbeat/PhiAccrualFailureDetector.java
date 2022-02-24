package org.distrib.patterns.heartbeat;

import org.apache.logging.log4j.Logger;
import org.distrib.patterns.common.Logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PhiAccrualFailureDetector<T> extends AbstractFailureDetector<T> implements Logging {
    public static final long INITIAL_VALUE_NANOS = TimeUnit.NANOSECONDS.convert(getInitialValue(), TimeUnit.MILLISECONDS);
    ;
    private Logger logger = getLogger();

    private final double PHI_FACTOR = 1.0 / Math.log(10.0); // 0.434...

    private final ConcurrentHashMap<T, ArrivalWindow> arrivalSamples = new ConcurrentHashMap<>();
    private int SAMPLE_SIZE = 1000;
    private int phiSuspectThreshold_ = 5;
    private int phiConvictThreshold_ = 8;
    /* The Failure Detector has to have been up for atleast 1 min. */
    private int uptimeThreshold_ = 60000;
    private long lastPause = 0L;

    private long lastInterpret = nanoTime();
    private long DEFAULT_MAX_PAUSE = 5000L * 1000000L; // 5 seconds

    private long MAX_LOCAL_PAUSE_IN_NANOS = DEFAULT_MAX_PAUSE;
    private int DEBUG_PERCENTAGE = 80; // if the phi is larger than this percentage of the max, log a debug message


    public void report(T ep)
    {
        long now = nanoTime();
        ArrivalWindow heartbeatWindow = arrivalSamples.get(ep);
        if (heartbeatWindow == null)
        {
            // avoid adding an empty ArrivalWindow to the Map
            heartbeatWindow = new ArrivalWindow(SAMPLE_SIZE);
            heartbeatWindow.add(now, ep);
            heartbeatWindow = arrivalSamples.putIfAbsent(ep, heartbeatWindow);
            if (heartbeatWindow != null)
                heartbeatWindow.add(now, ep);
        }
        else
        {
            heartbeatWindow.add(now, ep);
        }

        if (logger.isTraceEnabled() && heartbeatWindow != null)
            logger.trace("Average for {} is {}ns", ep, heartbeatWindow.mean());
    }

    public void interpret(T ep)
    {
        ArrivalWindow hbWnd = arrivalSamples.get(ep);
        if (hbWnd == null)
        {
            return;
        }
        long now = nanoTime();
        long diff = now - lastInterpret;
        lastInterpret = now;
        if (diff > MAX_LOCAL_PAUSE_IN_NANOS)
        {
            logger.warn("Not marking nodes down due to local pause of {}ns > {}ns", diff, MAX_LOCAL_PAUSE_IN_NANOS);
            lastPause = now;
            return;
        }
        if (nanoTime() - lastPause < MAX_LOCAL_PAUSE_IN_NANOS)
        {
            logger.debug("Still not marking nodes down due to local pause");
            return;
        }
        double phi = hbWnd.phi(now);
        if (logger.isTraceEnabled())
            logger.trace("PHI for {} : {}", ep, phi);

        if (PHI_FACTOR * phi > getPhiConvictThreshold())
        {
            logger.info("Node {} phi {} > {}; intervals: {} mean: {}ns", new Object[]{ep, PHI_FACTOR * phi, getPhiConvictThreshold(), hbWnd, hbWnd.mean()});

            markDown(ep);
        }
        else if (logger.isDebugEnabled() && (PHI_FACTOR * phi * DEBUG_PERCENTAGE / 100.0 > getPhiConvictThreshold()))
        {
            logger.debug("PHI for {} : {}", ep, phi);
        }
        else if (logger.isTraceEnabled())
        {
            logger.trace("PHI for {} : {}", ep, phi);
            logger.trace("mean for {} : {}ns", ep, hbWnd.mean());
        }
    }

    private long nanoTime() {
        return System.nanoTime();
    }

    private static long getInitialValue() {
        var intervalInMillis = 1000;
        return intervalInMillis * 2;
    }


    private double getPhiConvictThreshold() {
            // return DatabaseDescriptor.getPhiConvictThreshold//
            return 8.0;
    }

    @Override
    void heartBeatCheck() {
        var keys = arrivalSamples.keySet();
        keys.forEach(key -> interpret(key));
    }

    @Override
    void heartBeatReceived(T serverId) {
        report(serverId);
        super.markUp(serverId);
    }
}
