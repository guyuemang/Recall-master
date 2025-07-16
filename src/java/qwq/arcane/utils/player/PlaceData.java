package qwq.arcane.utils.player;

import lombok.AllArgsConstructor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@AllArgsConstructor
public class PlaceData {
    public BlockPos blockPos;
    public EnumFacing facing;

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }
}
