package me.qamulex.erl;

public interface RateLimiter {

    /**
     * Clears all captured timestamps
     */
    void reset();

    /**
     * @return estimated time in millis after which another request is possible
     */
    long remainingTimeInMillis(); // TODO: rename method

    /**
     * Checks whether another request is possible
     * 
     * @return <b>true</b> if the request is possible
     */
    boolean canRequest();

    /**
     * Checks whether the request is possible and captures request timestamp
     * 
     * @return <b>true</b> if the request was captured
     */
    boolean request();

}