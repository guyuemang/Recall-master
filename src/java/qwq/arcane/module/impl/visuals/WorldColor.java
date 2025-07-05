package qwq.arcane.module.impl.visuals;

import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.ColorValue;

import java.awt.*;

public final class WorldColor extends Module {

    public final ColorValue lightMapColorProperty = new ColorValue("Light Map", new Color(100, 100, 100, 255));

    public WorldColor() {
        super("WorldColor",Category.Visuals);
    }
}
