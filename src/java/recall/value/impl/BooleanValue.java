package recall.value.impl;

import recall.value.Value;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 00:47
 */
public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, Dependency dependency, boolean defaultValue) {
        super(name, dependency);
        this.value = defaultValue;
    }

    public BooleanValue(String name, boolean defaultValue) {
        this(name, () -> true, defaultValue);
    }

    public void toggle() {
        value = !value;
    }
}