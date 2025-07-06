package qwq.arcane.utils.player;

import net.minecraft.util.MovementInput;
import org.lwjgl.input.Keyboard;
import qwq.arcane.utils.Instance;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:53 AM
 */
public class MovementInputKeyboard extends MovementInput implements Instance {
    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            ++this.moveForward;
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            --this.moveForward;
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
            ++this.moveStrafe;
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
            --this.moveStrafe;
        }

        this.jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        this.sneak = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());

        if (this.sneak) {
            this.moveStrafe = (float) ((double) this.moveStrafe * 0.3D);
            this.moveForward = (float) ((double) this.moveForward * 0.3D);
        }
    }
}
