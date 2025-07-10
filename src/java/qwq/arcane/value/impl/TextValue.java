
package qwq.arcane.value.impl;

import qwq.arcane.value.Value;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextValue extends Value {
    private String text;
    private boolean onlyNumber;

    public TextValue(String name, String text, Dependency dependency) {
        super(name, dependency);
        this.text = text;
        this.onlyNumber = false;
    }

    public TextValue(String name, String text) {
        super(name, () -> true);
        this.text = text;
    }

    public TextValue(String name, String text, boolean onlyNumber, Dependency dependency) {
        super(name, dependency);
        this.text = text;
        this.onlyNumber = onlyNumber;
    }

    public TextValue(String name, String text, boolean onlyNumber) {
        super(name, () -> true);
        this.text = text;
        this.onlyNumber = onlyNumber;
    }

    public String get() {
        return text;
    }
}
