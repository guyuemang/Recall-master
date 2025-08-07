package qwq.arcane.module;



/**
 * @Author：Guyuemang
 * @Date：2025/6/29 00:50
 */

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
