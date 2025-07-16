package qwq.arcane.value.impl;

import qwq.arcane.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 00:47
 */
public class MultiBooleanValue extends Value {
    public List<BoolValue> options;
    public int index;

    public MultiBooleanValue(String name, Dependency dependency, List<BoolValue> options) {
        super(name,dependency);
        this.options = options;
        index = options.size();
    }

    public MultiBooleanValue(String name, List<BoolValue> options) {
        super(name);
        this.options = options;
        index = options.size();
    }

    public boolean isEnabled(String name) {
        return Objects.requireNonNull(this.options.stream().filter((option) -> option.getName().equalsIgnoreCase(name)).findFirst().orElse(null)).get();
    }

    public void set(String name, boolean value) {
        Objects.requireNonNull(this.options.stream().filter((option) -> option.getName().equalsIgnoreCase(name)).findFirst().orElse(null)).set(value);
    }

    public List<BoolValue> getToggled() {
        return this.options.stream().filter(BoolValue::get).collect(Collectors.toList());
    }

    public String isEnabled() {
        List<String> includedOptions = new ArrayList<>();
        for (BoolValue option : options) {
            if (option.get()) {
                includedOptions.add(option.getName());
            }
        }
        return String.join(", ", includedOptions);
    }

    public void set(int index, boolean value) {
        this.options.get(index).set(value);
    }

    public boolean isEnabled(int index) {
        return this.options.get(index).get();
    }

    public List<BoolValue> getValues() {
        return this.options;
    }
}