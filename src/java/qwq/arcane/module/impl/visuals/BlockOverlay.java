
package qwq.arcane.module.impl.visuals;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ColorValue;

import java.awt.*;

public class BlockOverlay extends Module {

    public final BoolValue outline = new BoolValue("Outline", true);
    public final BoolValue filled = new BoolValue("Filled", false);
    public final BoolValue syncColor = new BoolValue("Sync Color", false);
    public final ColorValue color = new ColorValue("Color",() -> !syncColor.get(),new Color(255,255,255));

    public BlockOverlay() {
        super("BlockOverlay", Category.Visuals);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if(getBlock(mc.objectMouseOver.getBlockPos()) instanceof BlockAir)
            return;
        if (syncColor.get()) {
            RenderUtil.renderBlock(mc.objectMouseOver.getBlockPos(), Client.Instance.getModuleManager().getModule(InterFace.class).color(0).getRGB(), outline.get(), filled.get());
        } else {
            RenderUtil.renderBlock(mc.objectMouseOver.getBlockPos(), color.get().getRGB(), outline.get(), filled.get());
        }
    }

    public static Block getBlock(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }
}
