package qwq.arcane.utils.animations;

import lombok.Getter;
import lombok.Setter;
import qwq.arcane.module.Mine;

public class AnimationUtils {
    @Getter
    @Setter
    private static int delta;
    public static long deltaTime = 500L;
    private static float debugFPS() {
        return Mine.getDebugFPS() <= 5 ? 60f : (float) Mine.getDebugFPS();
    }
    public static double base(double current, double target, double speed) {
        return Double.isNaN(current + (target - current) * speed / (debugFPS() / 60)) ? 0.0 : current + (target - current) * speed / (debugFPS() / 60);
    }
    public static double easeOutQuint(long elapsedTime, double start, double rest, double duration) {
        double percent = 1.0 - Math.pow(1.0 - elapsedTime / duration, 5);
        return start + (rest * percent);
    }

    public static double easeInBack(double t, double b, double c, double d) {
        double s = 1.70158;
        t /= d;
        return c * t * t * ((s + 1) * t - s) + b;
    }

    public static double easeOutQuad(long startTime, long duration, double start, double end) {
        float x = (System.currentTimeMillis() - startTime) * 1.0f / duration;
        float y = -x * x + 2 * x;
        return start + (end - start) * y;
    }

    public static double easeOutBack(double t, double b, double c, double d) {
        double s = 1.70158;
        t = t / d - 1;
        return c * (t * t * ((s + 1) * t + s) + 1) + b;
    }

    public static double easeInOutQuad(long startTime, long duration, double start, double end) {
        float t = (System.currentTimeMillis() - startTime) * 1.0f / duration;
        t *= 2f;
        if (t < 1) {
            return (end - start) / 2 * t * t + start;
        } else {
            t--;
            return -(end - start) / 2 * (t * (t - 2) - 1) + start;
        }
    }

    public static double easeInElastic(double t, double b, double c, double d) {
        double s;
        double p;
        double a = c;

        if (t == 0.0) return b;
        t /= d;
        if (t == 1.0) return b + c;

        p = d * 0.3;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4.0;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        t--;
        return -(a * Math.pow(2.0, 10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
    }

    public static double easeOutElastic(double t, double b, double c, double d) {
        double s;
        double p;
        double a = c;

        if (t == 0.0) return b;
        t /= d;
        if (t == 1.0) return b + c;

        p = d * 0.3;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4.0;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        return a * Math.pow(2.0, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
    }
    public static double linear(long startTime, long duration, double start, double end) {
        return (end - start) * ((System.currentTimeMillis() - startTime) * 1.0 / duration) + start;
    }

    public static double easeInQuad(long startTime, long duration, double start, double end) {
        return (end - start) * Math.pow((System.currentTimeMillis() - startTime) * 1.0 / duration, 2.0) + start;
    }
    public static double easeInOutElastic(double t, double b, double c, double d) {
        double s;
        double p;
        double a = c;

        if (t == 0.0) return b;
        t /= d / 2;
        if (t == 2.0) return b + c;

        p = d * (0.3 * 1.5);
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4.0;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        if (t < 1) {
            t--;
            return -0.5 * (a * Math.pow(2.0, 10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        } else {
            t--;
            return a * Math.pow(2.0, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) * 0.5 + c + b;
        }
    }
    public static float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
        boolean larger = end > current;
        if (smoothSpeed < 0.0f) {
            smoothSpeed = 0.0f;
        } else if (smoothSpeed > 1.0f) {
            smoothSpeed = 1.0f;
        }
        if (minSpeed < 0.0f) {
            minSpeed = 0.0f;
        } else if (minSpeed > 1.0f) {
            minSpeed = 1.0f;
        }
        float movement = (end - current) * smoothSpeed;
        if (movement > 0) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
        } else if (movement < 0) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
        }
        if (larger){
            if (end <= current + movement){
                return end;
            }
        }else {
            if (end >= current + movement){
                return end;
            }
        }
        return current + movement;
    }
    public static float clamp(float number, float min, float max) {
        return number < min ? min : Math.min(number, max);
    }
    public static float calculateCompensation(final float target, float current, long delta, final int speed) {
        final float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }
        double v = (speed * delta / 16L < 0.25) ? 0.5 : (speed * delta / 16L);
        if (diff > speed) {
            current -= (float) v;
            if (current < target) {
                current = target;
            }
        } else if (diff < -speed) {
            current += (float) v;
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }
    public static float animateIDK(double target, double current, double speed) {
        boolean larger = (target > current);
        if (speed < 0.0F) speed = 0.0F;
        else if (speed > 1.0F) speed = 1.0F;
        double dif = Math.abs(current - target);
        double factor = dif * speed;
//        if (factor < 0.1f) factor = 0.1F;
        if (larger) current += factor;
        else current -= factor;
        return (float) current;
    }
    public static double animate(double target, double current, double speed) {
        if (current == target) return current;

        boolean larger = target > current;
        if (speed < 0.0D) {
            speed = 0.0D;
        } else if (speed > 1.0D) {
            speed = 1.0D;
        }

        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1D) {
            factor = 0.1D;
        }

        if (larger) {
            current += factor;
            if (current >= target) current = target;
        } else {
            current -= factor;
            if (current <= target) current = target;
        }

        return current;
    }

    public static float animateSmooth(float current, float target, float speed) {
        return purse(target, current, getDelta(), Math.abs(target - current) * speed);
    }

    public static float purse(float target, float current, long delta, float speed) {

        if (delta < 1L) delta = 1L;

        final float difference = current - target;

        final float smoothing = Math.max(speed * (delta / 16F), .15F);

        if (difference > speed)
            current = Math.max(current - smoothing, target);
        else if (difference < -speed)
            current = Math.min(current + smoothing, target);
        else current = target;

        return current;
    }

    public static float animate(float target, float current, float speed) {
        if (current == target) return current;

        boolean larger = target > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }

        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1D) {
            factor = 0.1D;
        }

        if (larger) {
            current += factor;
            if (current >= target) current = target;
        } else {
            current -= factor;
            if (current <= target) current = target;
        }

        return current;
    }
}
