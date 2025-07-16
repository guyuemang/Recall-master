package qwq.arcane.module.impl.visuals;

import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ColorValue;
import net.minecraft.tileentity.*;

import java.awt.*;

/**
 * @Author: Guyuemang
 * 2025/5/1
 */
public class ContainerESP extends Module {
    public final BoolValue outline = new BoolValue("Outline", false);
    public final BoolValue filled = new BoolValue("Filled", true);
    public final BoolValue syncColor = new BoolValue("SyncColor", false);
    public final ColorValue color = new ColorValue("Color",()-> !syncColor.get(),new Color(128, 244, 255));

    public final BoolValue chests = new BoolValue("Chests", true);
    public final BoolValue furnaces = new BoolValue("Furnaces", false);
    public final BoolValue brewingStands = new BoolValue("BrewingStands", false);

    public ContainerESP() {
        super("ContainerESP",Category.Visuals);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if ((chests.get() && (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest)) ||
                    (furnaces.get() && tileEntity instanceof TileEntityFurnace) ||
                    (brewingStands.get() && tileEntity instanceof TileEntityBrewingStand)) {
                if (!tileEntity.isInvalid() && mc.theWorld.getBlockState(tileEntity.getPos()) != null) {
                    if (syncColor.get()) {
                        RenderUtil.renderBlock(tileEntity.getPos(),getModule(InterFace.class).color(20).getRGB(),outline.get(),filled.get());
                    } else {
                        RenderUtil.renderBlock(tileEntity.getPos(),color.get().getRGB(),outline.get(),filled.get());
                    }
                }
            }
        }
    }
}