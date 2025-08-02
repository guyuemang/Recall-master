package qwq.arcane.value;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 00:47
 */
@Rename
@FlowObfuscate
@InvokeDynamic
@Getter
@Setter
public abstract class Value<T> {
    protected final Dependency dependency;
    protected T value;
    protected final String name;

    public Value(String name, Dependency dependency) {
        this.name = name;
        this.dependency = dependency;
    }

    public Value(String name, String description) {
        this(name, () -> true);
    }

    public Value(String name) {
        this(name, () -> true);
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    public boolean isAvailable() {
        return dependency != null && this.dependency.check();
    }

    @FunctionalInterface
    public interface Dependency {
        boolean check();
    }
}
