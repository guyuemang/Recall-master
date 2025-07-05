/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & opZywl & lucas]
 */
package qwq.arcane.event.impl.events.render;

import lombok.Getter;
import lombok.Setter;
import qwq.arcane.event.impl.CancellableEvent;

@Getter
@Setter
public class Shader2DEvent extends CancellableEvent {

    private ShaderType shaderType;

    public Shader2DEvent(ShaderType shaderType) {
        this.shaderType = shaderType;
    }

    public enum ShaderType {
        BLUR, SHADOW, GLOW
    }
}
