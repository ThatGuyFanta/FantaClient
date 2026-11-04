package net.fanta.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<FantaModule> MODULES = new ArrayList<>();

    static {
        register(FreecamModule.INSTANCE);
        register(AutoSprintModule.INSTANCE);
        register(FullBrightModule.INSTANCE);
        register(BlockEspModule.INSTANCE);
        register(StorageEspModule.INSTANCE);
        register(PlayerEspModule.INSTANCE);
        register(NameTagsModule.INSTANCE);
        register(ZoomModule.INSTANCE);
        register(HudModule.INSTANCE);
        register(NoWeatherModule.INSTANCE);
        register(CustomCrosshairModule.INSTANCE);
        register(AntiVanishModule.INSTANCE);
        register(ChunkAnalyzerModule.INSTANCE);
        register(SchematicBuilderModule.INSTANCE);
        register(TwerkModule.INSTANCE);
        register(DerpModule.INSTANCE);
        register(SpinModule.INSTANCE);
        register(RainbowModule.INSTANCE);
        register(PanicModule.INSTANCE);

        // Accessories
        register(SparkleTrailModule.INSTANCE);
        register(HeartAuraModule.INSTANCE);
        register(FlameHaloModule.INSTANCE);
        register(NoteTrailModule.INSTANCE);
        register(WingsModule.INSTANCE);
    }

    private static void register(FantaModule module) {
        MODULES.add(module);
    }

    public static List<FantaModule> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<FantaModule> getByCategory(String category) {
        List<FantaModule> result = new ArrayList<>();
        for (FantaModule m : MODULES) {
            if (m.getCategory().equalsIgnoreCase(category)) {
                result.add(m);
            }
        }
        return result;
    }

    public static FantaModule getByName(String name) {
        for (FantaModule m : MODULES) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}
