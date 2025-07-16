package qwq.arcane.value.impl;

import qwq.arcane.value.Value;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 00:47
 */
public class BoolValue extends Value<Boolean> {
    public BoolValue(String name, Dependency dependency, boolean defaultValue) {
        super(name, dependency);
        this.value = defaultValue;
    }

    public BoolValue(String name, boolean defaultValue) {
        this(name, () -> true, defaultValue);
    }

    public void toggle() {
        value = !value;
    }
}