package me.qamulex.erl;

import java.time.Clock;

public class DelayLimiter extends AbstractRateLimiter { // TODO: rename (does not reflect the purpose)

    public DelayLimiter(
            Clock clock,
            long affectedTimeRangeInMillis
    ) {
        super(clock, affectedTimeRangeInMillis);
    }

    @Override
    protected void requestIsAllowed() {
        // must be empty
    }

    @Override
    protected boolean calculateNextRequestTime() {
        setNextRequestTimeInMillis(getLastRequestTimeInMillis() + getAffectedTimeRangeInMillis());
        return false;
    }

}
