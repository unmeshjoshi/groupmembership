package org.distrib.patterns.heartbeat;

import org.apache.logging.log4j.Logger;
import org.distrib.patterns.common.Logging;

import java.util.Arrays;

/*
 This class is not thread safe.
 */
class ArrayBackedBoundedStats {
    private final long[] arrivalIntervals;
    private long sum = 0;
    private int index = 0;
    private boolean isFilled = false;
    private volatile double mean = 0;

    public ArrayBackedBoundedStats(final int size)
    {
        arrivalIntervals = new long[size];
    }

    public void add(long interval)
    {
        if(index == arrivalIntervals.length)
        {
            isFilled = true;
            index = 0;
        }

        if(isFilled)
            sum = sum - arrivalIntervals[index];

        arrivalIntervals[index++] = interval;
        sum += interval;
        mean = (double)sum / size();
    }

    private int size()
    {
        return isFilled ? arrivalIntervals.length : index;
    }

    public double mean()
    {
        return mean;
    }

    public long[] getArrivalIntervals()
    {
        return arrivalIntervals;
    }

}
public class ArrivalWindow<T>  implements Logging {
    private Logger logger = getLogger();
    private long tLast = 0L;
    private final ArrayBackedBoundedStats arrivalIntervals;
    private double lastReportedPhi = Double.MIN_VALUE;

    // in the event of a long partition, never record an interval longer than the rpc timeout,
    // since if a host is regularly experiencing connectivity problems lasting this long we'd
    // rather mark it down quickly instead of adapting
    // this value defaults to the same initial value the FD is seeded with
    private final long MAX_INTERVAL_IN_NANO = getMaxInterval();

    ArrivalWindow(int size)
    {
        arrivalIntervals = new ArrayBackedBoundedStats(size);
    }

    synchronized void add(long value, T ep)
    {
        assert tLast >= 0;
        if (tLast > 0L)
        {
            long interArrivalTime = (value - tLast);
            if (interArrivalTime <= MAX_INTERVAL_IN_NANO)
            {
                arrivalIntervals.add(interArrivalTime);
                logger.trace("Reporting interval time of {}ns for {}", interArrivalTime, ep);
            }
            else
            {
                logger.trace("Ignoring interval time of {}ns for {}", interArrivalTime, ep);
            }
        }
        else
        {
            // We use a very large initial interval since the "right" average depends on the cluster size
            // and it's better to err high (false negatives, which will be corrected by waiting a bit longer)
            // than low (false positives, which cause "flapping").
            arrivalIntervals.add(PhiAccrualFailureDetector.INITIAL_VALUE_NANOS);
        }
        tLast = value;
    }

    double mean()
    {
        return arrivalIntervals.mean();
    }

    // see CASSANDRA-2597 for an explanation of the math at work here.
    double phi(long tnow)
    {
        assert arrivalIntervals.mean() > 0 && tLast > 0; // should not be called before any samples arrive
        long t = tnow - tLast;
        lastReportedPhi = t / mean();
        return lastReportedPhi;
    }

    double getLastReportedPhi()
    {
        return lastReportedPhi;
    }

    public String toString()
    {
        return Arrays.toString(arrivalIntervals.getArrivalIntervals());
    }

    private static long getMaxInterval()
    {
        return PhiAccrualFailureDetector.INITIAL_VALUE_NANOS;
    }
}
