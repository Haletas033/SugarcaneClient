package name.modid.Presets;

import java.util.HashMap;
import java.util.Map;



public class Modules {
    public record ModuleOptions(boolean enabled){}

    private static HashMap<String, ModuleOptions> modules = new HashMap<>(Map.of(
            "BlockEntityESP", new ModuleOptions(true),
            "MobESP", new ModuleOptions(true),
            "PlayerESP", new ModuleOptions(true)
    ));

    public static ModuleOptions getModule(String module) {
        return modules.get(module);
    }

    public static void setModule(String module, ModuleOptions moduleOptions) {
        modules.put(module, moduleOptions);
    }
}
