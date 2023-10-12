package me.qamulex.erl;

import java.time.Clock;
import java.util.List;

public class BandwidthLimiter extends AbstractRateLimiter {

    private final int        maximumBandwidth;
    private final List<Long> capturedTimestamps;

    public BandwidthLimiter(
            Clock clock,
            long affectedTimeRangeInMillis,
            int maximumBandwidth,
            List<Long> capturedTimestampsStorage
    ) {
        super(clock, affectedTimeRangeInMillis);

        this.maximumBandwidth = maximumBandwidth;
        this.capturedTimestamps = capturedTimestampsStorage;
    }

    @Override
    public void reset() {
        super.reset();
        capturedTimestamps.clear();
    }

    @Override
    protected void requestIsAllowed() {
        eliminateOldTimestamps();
    }

    protected void eliminateOldTimestamps() {
        long leftTimeRangeBorderInMillis = currentTimeInMillis() - getAffectedTimeRangeInMillis();
        capturedTimestamps.removeIf(
                capturedTimestamp -> capturedTimestamp <= leftTimeRangeBorderInMillis
        );
    }

    @Override
    protected boolean calculateNextRequestTime() {
        if (capturedTimestamps.size() >= maximumBandwidth) {
            long oldestTimestamp = capturedTimestamps.get(0);
            setNextRequestTimeInMillis(oldestTimestamp + getAffectedTimeRangeInMillis());
            return true;
        }

        setNextRequestTimeInMillis(currentTimeInMillis());
        return false;
    }

    @Override
    protected void captureTimestamp() {
        super.captureTimestamp();
        capturedTimestamps.add(getLastRequestTimeInMillis());
    }

}
