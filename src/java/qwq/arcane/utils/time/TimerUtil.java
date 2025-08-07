package qwq.arcane.utils.time;

import static org.apache.commons.lang3.RandomUtils.nextInt;

public class TimerUtil {
    public long lastMS = System.currentTimeMillis();

    public final long getDifference() {
        return getCurrentMS() - lastMS;
    }


    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }
    public long hasTimeLeft(long ms) {
        return ms + lastMS - System.currentTimeMillis();
    }
    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }
    public boolean hasReached(double milliseconds) {
        return (double) (this.getCurrentMS() - this.lastMS) >= milliseconds;
    }
    public static long randomDelay(final int minDelay, final int maxDelay) {
        return nextInt(minDelay, maxDelay);
    }
    public boolean reached(long currentTime) {
        return Math.max(0L, System.currentTimeMillis() - this.lastMS) >= currentTime;
    }
    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }

        return false;
    }
    private long currentMs;

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - this.lastMS > time;
    }
    public boolean hasElapsed(long milliseconds) {
        return elapsed() > milliseconds;
    }
    public long elapsed() {
        return System.currentTimeMillis() - currentMs;
    }
    public boolean delay(float time) {
        return System.currentTimeMillis() - this.lastMS >= time;

    }
    public void reset2() {
        currentMs = System.currentTimeMillis();
    }
    public boolean hasTimeElapsed(double time) {
        return hasTimeElapsed((long) time);
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public void setTime(long time) {
        this.lastMS = time;
    }
}