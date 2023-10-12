package me.qamulex.erl;

import java.time.Clock;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class AbstractRateLimiter implements RateLimiter {

    private final Clock clock;
    private final long  affectedTimeRangeInMillis;

    private long lastRequestTimeInMillis = 0L;
    private long nextRequestTimeInMillis = 0L;

    @Override
    public void reset() {
        lastRequestTimeInMillis = 0L;
        nextRequestTimeInMillis = 0L;
    }

    @Override
    public long remainingTimeInMillis() {
        return Math.max(0L, nextRequestTimeInMillis - clock.millis());
    }

    @Override
    public boolean canRequest() {
        return nextRequestTimeInMillis <= clock.millis();
    }

    @Override
    public boolean request() {
        if (!canRequest())
            return false;
        requestIsAllowed();
        captureTimestamp();
        calculateNextRequestTime();
        return true;
    }

    protected abstract void requestIsAllowed();

    /**
     * @return <b>true</b> if next request time is not current time
     */
    protected abstract boolean calculateNextRequestTime();

    protected long currentTimeInMillis() {
        return clock.millis();
    }

    protected void captureTimestamp() {
        lastRequestTimeInMillis = clock.millis();
    }

}
