package me.qamulex.erl;

import java.time.Clock;
import java.util.List;

public class BandwidthLimiterWithDelay extends BandwidthLimiter {

    private final DelayLimiter delayLimiter;

    public BandwidthLimiterWithDelay(
            Clock clock,
            long affectedTimeRangeInMillis,
            int maximumBandwidth,
            List<Long> capturedTimestampsStorage,
            long delayBetweenRequestsInMillis
    ) {
        super(clock, affectedTimeRangeInMillis, maximumBandwidth, capturedTimestampsStorage);

        this.delayLimiter = new DelayLimiter(clock, delayBetweenRequestsInMillis);
    }

    @Override
    public void reset() {
        super.reset();
        delayLimiter.reset();
    }

    @Override
    public boolean canRequest() {
        return super.canRequest() && delayLimiter.canRequest();
    }

    @Override
    protected void requestIsAllowed() {
        delayLimiter.request();
        super.requestIsAllowed();
    }

    @Override
    protected boolean calculateNextRequestTime() {
        if (super.calculateNextRequestTime())
            return true;

        if (!delayLimiter.canRequest()) {
            setNextRequestTimeInMillis(delayLimiter.getNextRequestTimeInMillis());
            return true;
        }

        return false;
    }

}
