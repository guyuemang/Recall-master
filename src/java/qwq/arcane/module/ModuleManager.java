package qwq.arcane.module;

import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.KeyPressEvent;
import qwq.arcane.event.impl.events.render.ChatGUIEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.module.impl.combat.*;
import qwq.arcane.module.impl.display.ArrayList;
import qwq.arcane.module.impl.display.Notification;
import qwq.arcane.module.impl.misc.*;
import qwq.arcane.module.impl.misc.Timer;
import qwq.arcane.module.impl.movement.*;
import qwq.arcane.module.impl.player.*;
import qwq.arcane.module.impl.world.Disabler;
import qwq.arcane.module.impl.world.FastPlace;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.module.impl.visuals.*;
import qwq.arcane.value.Value;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 13:06
 */
public class ModuleManager {
    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();

    public void Init() {
        Client.Instance.getEventManager().register(this);
        registerModule(new InterFace());
        registerModule(new TickBase());
        registerModule(new BackTrack());
        registerModule(new ClickGui());
        registerModule(new Health());
        registerModule(new Chams());
        registerModule(new AutoWeapon());
        registerModule(new Projectile());
        registerModule(new MotionBlur());
        registerModule(new BlockOverlay());
        registerModule(new AutoClicker());
        registerModule(new FreeLook());
        registerModule(new Hat());
        registerModule(new Atmosphere());
        registerModule(new Breadcrumbs());
        registerModule(new Hitmarkers());
        registerModule(new ContainerESP());
        registerModule(new Noslow());
        registerModule(new Sprint());
        registerModule(new GuiMove());
        registerModule(new NoJumpDelay());
        registerModule(new Scaffold());
        registerModule(new FastPlace());
        registerModule(new Disabler());
        registerModule(new Stealer());
        registerModule(new InvManager());
        registerModule(new BedNuker());
        registerModule(new AutoTool());
        registerModule(new AntiVoid());
        registerModule(new NoFall());
        registerModule(new Freeze());
        registerModule(new LongJump());
        registerModule(new SafeWalk());
        registerModule(new Speed());
        registerModule(new NoWeb());
        registerModule(new AutoPlay());
        registerModule(new ClientSpoofer());
        registerModule(new FakeLag());
        registerModule(new NoRotate());
        registerModule(new Timer());
        registerModule(new AntiBot());
        registerModule(new AntiKB());
        registerModule(new KillEffect());
        registerModule(new ESP());
        registerModule(new KillAura());
        registerModule(new ItemESP());
        registerModule(new Animations());
        registerModule(new ArrayList());
        registerModule(new NoHurtCam());
        registerModule(new FullBright());
        registerModule(new Camera());
        registerModule(new Notification());
    }

    public void registerModule(Module module) {
        for (final Field field : module.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                final Object obj = field.get(module);
                if (obj instanceof Value<?>) module.getSettings().add((Value<?>) obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        modules.put(module.getClass(), module);
    }

    public Collection<Module> getAllModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public Module getModule(String name) {
        for (Module module : modules.values()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
    public <T extends Module> T getModule(Class<T> cls) {
        return cls.cast(modules.get(cls));
    }

    public <T extends ModuleWidget> T getModuleW(Class<T> cls) {
        return cls.cast(modules.get(cls));
    }

    public List<Module> getModsByCategory(Category m) {
        return modules.values().stream()
                .filter(module -> module.getCategory() == m)
                .collect(Collectors.toList());
    }

    @EventTarget
    public void onKey(KeyPressEvent e) {
        modules.values().stream().filter(module -> module.getKey() == e.getKey() && e.getKey() != -1)
                .forEach(Module::toggle);
    }

    public Collection<ModuleWidget> getAllWidgets() {
        return modules.values().stream()
                .filter(ModuleWidget.class::isInstance)
                .map(ModuleWidget.class::cast)
                .collect(Collectors.toList());
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        for (Module module : modules.values()) {
            if (module instanceof ModuleWidget && module.getState()) {
                ModuleWidget widget = (ModuleWidget) module;
                if (widget.shouldRender()) {
                    widget.updatePos();
                    widget.render();
                }
            }
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        for (Module module : modules.values()) {
            if (module instanceof ModuleWidget && module.getState()) {
                ModuleWidget widget = (ModuleWidget) module;
                if (widget.shouldRender()) {
                    widget.onShader(event);
                }
            }
        }
    }

    @EventTarget
    public void onChatGUI(ChatGUIEvent event) {
        ModuleWidget draggingWidget = null;
        for (Module module : modules.values()) {
            if (module instanceof ModuleWidget && module.getState()) {
                ModuleWidget widget = (ModuleWidget) module;
                if (widget.shouldRender() && widget.dragging) {
                    draggingWidget = widget;
                    break;
                }
            }
        }

        for (Module module : modules.values()) {
            if (module instanceof ModuleWidget && module.getState()) {
                ModuleWidget widget = (ModuleWidget) module;
                if (widget.shouldRender()) {
                    widget.onChatGUI(event.getMouseX(), event.getMouseY(),
                            (draggingWidget == null || draggingWidget == widget));
                    if (widget.dragging) draggingWidget = widget;
                }
            }
        }
    }
}
