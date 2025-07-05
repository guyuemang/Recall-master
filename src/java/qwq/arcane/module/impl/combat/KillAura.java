package qwq.arcane.module.impl.combat;

import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.MultiBooleanValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.Arrays;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:53
 */
public class KillAura extends Module {
    public KillAura() {
        super("KillAura",Category.Combat);
    }
    private final ModeValue mode = new ModeValue("AttackMode","Single",new String[]{"Single","Switch"});
    public final NumberValue switchDelayValue = new NumberValue("SwitchDelay", () -> mode.is("Switch"),15, 0, 20, 1);
    private final NumberValue maxCPS = new NumberValue("Max CPS", 18, 1, 20, 1);
    private final NumberValue minCPS = new NumberValue("Min CPS", 12, 1, 20, 1);
    public static NumberValue range = new NumberValue("Range", 3.0,  0.0, 5.0, 0.1);
    private final ModeValue priority = new ModeValue("Priority", "Health", new String[]{"Range", "Armor", "Health", "HurtTime"});
    private final BooleanValue raycase = new BooleanValue("RayCase",true);
    private final BooleanValue rotations = new BooleanValue("rotations",true);
    public ModeValue rotMode = new ModeValue("Rotation Mode",rotations::get, "Normal", new String[]{"Normal", "HvH", "CNM","New"});
    private final NumberValue RotationsSpeed = new NumberValue("RotationsSpeed", rotations::get,8, 1, 10, 1);
    private final ModeValue moveFix = new ModeValue("MovementFix","Silent",new String[]{ "Silent", "Strict", "None", "BackSprint"});
    public static BooleanValue autoblock = new BooleanValue("AutoBlock",true);
    public static ModeValue autoblockmode = new ModeValue("AutoBlockMode", autoblock::getValue,"Grim",new String[]{"Grim","Watchdog","Off"});
    private final MultiBooleanValue targetOption = new MultiBooleanValue("Targets", Arrays.asList(new BooleanValue("Players", true), new BooleanValue("Mobs", false),
            new BooleanValue("Animals", false), new BooleanValue("Invisible", true), new BooleanValue("Dead", false)));
    public final MultiBooleanValue filter = new MultiBooleanValue("Filter", Arrays.asList(new BooleanValue("Teams", true), new BooleanValue("Friends", true)));
    private final MultiBooleanValue auraESP = new MultiBooleanValue("TargetHUD ESP", Arrays.asList(
            new BooleanValue("Circle", true),
            new BooleanValue("Tracer", false),
            new BooleanValue("Box", false),
            new BooleanValue("Custom Color", false)));
}
