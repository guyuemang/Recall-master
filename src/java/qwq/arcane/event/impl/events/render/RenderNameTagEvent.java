package qwq.arcane.event.impl.events.render;

import net.minecraft.entity.EntityLivingBase;
import qwq.arcane.event.impl.CancellableEvent;

public final class RenderNameTagEvent extends CancellableEvent {

    private final EntityLivingBase entityLivingBase;

    public RenderNameTagEvent(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    public EntityLivingBase getEntityLivingBase() {
        return entityLivingBase;
    }

}
