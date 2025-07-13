package qwq.arcane.module.impl.visuals;

import lombok.Getter;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:30
 */
@Getter
public class Animations extends Module {
    private final BooleanValue old = new BooleanValue("Old", false);
    private final ModeValue type = new ModeValue("Block Anim", () -> !old.get(), "Sigma", new String[]{"Swank", "Swing", "Swang", "Swong", "Swaing", "Punch", "Virtue", "Push", "Stella", "Styles", "Slide", "Interia", "Ethereal", "1.7", "Sigma", "Exhibition", "Old Exhibition", "Smooth", "Moon", "Leaked", "Astolfo", "Small"});
    private final BooleanValue blockWhenSwing = new BooleanValue("Block Swing", false);
    private final ModeValue hit = new ModeValue("Hit", ()-> !old.get(),"Vanilla", new String[]{"Vanilla", "Smooth"});
    private final NumberValue slowdown = new NumberValue("Slow Down", 0.0, -5.0, 15.0, 1.0);
    private final NumberValue downscaleFactor = new NumberValue("Scale", 0.0, 0.0, 0.5, .1);
    private final BooleanValue rotating = new BooleanValue("Rotating", ()-> !old.get(),false);
    private final BooleanValue swingWhileUsingItem = new BooleanValue("Swing Using Item", false);
    private final NumberValue x = new NumberValue("Item-X", 0.0, -1.0, 1.0, .05);
    private final NumberValue y = new NumberValue("Item-Y", 0.0, -1.0, 1.0, .05);
    private final NumberValue z = new NumberValue("Item-Z", 0.0, -1.0, 1.0, .05);
    private final NumberValue bx = new NumberValue("Block-X", 0.0, -1.0, 1.0, .05);
    private final NumberValue by = new NumberValue("Block-Y", 0.0, -1.0, 1.0, .05);
    private final NumberValue bz = new NumberValue("Block-Z", 0.0, -1.0, 1.0, .05);
    public Animations() {
        super("Animations", Category.Visuals);
    }
    @EventTarget
    private void onUpdate(MotionEvent event) {
        setsuffix(type.get());
    }
}
