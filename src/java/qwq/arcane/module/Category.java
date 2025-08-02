package qwq.arcane.module;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 00:50
 */
@Rename
@FlowObfuscate
@InvokeDynamic
public enum Category {
    Combat("A"),
    Movement("B"),
    Misc("C"),
    Player("D"),
    World("E"),
    Visuals("F"),
    Display("G");
    public String icon;
    Category(String icon){
        this.icon = icon;
    }
}
