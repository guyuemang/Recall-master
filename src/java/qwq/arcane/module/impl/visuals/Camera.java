package qwq.arcane.module.impl.visuals;

import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.NumberValue;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
public class Camera extends Module {
    public final BooleanValue noFovValue = new BooleanValue("NoFov", false);
    public final NumberValue fovValue = new NumberValue("Fov", 1.0, 0.0, 4.0, 0.1);
    public final BooleanValue motionCamera = new BooleanValue("Motion Camera", true);
    public final NumberValue interpolation = new NumberValue("Interpolation", 0.01, 0.01, 0.4, 0.01);
    public Camera() {
        super("Camera", Category.Visuals);
    }
}
