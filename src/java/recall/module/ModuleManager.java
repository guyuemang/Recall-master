package recall.module;

import recall.event.annotations.EventTarget;
import recall.event.impl.events.misc.KeyPressEvent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 13:06
 */
public class ModuleManager {
    private final Map<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();

    public void Init() {
    }
    public void registerModule(Module module) {
        for (final Field field : module.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                final Object obj = field.get(module);
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
}
