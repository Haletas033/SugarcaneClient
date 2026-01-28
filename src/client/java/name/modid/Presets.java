package name.modid;

import net.minecraft.references.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;



public class Presets {
    public record EnableAll(Presets.ESPOptions options, boolean enabled){}

    public record ESPOptions(boolean enabled, ColourOptions colourOptions){}

    public static ESPOptions defaultESPOptions = new ESPOptions(true, new ColourOptions());

    public enum EnableAllType{
        BLOCK_ENTITIES,
        MOBS
    }

    private static HashMap<EnableAllType, EnableAll> enableAlls = new HashMap<>(Map.of(
            EnableAllType.BLOCK_ENTITIES, new EnableAll(defaultESPOptions, false),
            EnableAllType.MOBS, new EnableAll(defaultESPOptions, true)
    ));

    public static void enableAll(EnableAllType type, boolean enabled){
        enableAlls.put(EnableAllType.BLOCK_ENTITIES, new EnableAll(enableAlls.get(type).options(), enabled));
    }

    public static EnableAll getEnableAll(EnableAllType type){
        return enableAlls.get(type);
    }

    public static HashMap<BlockEntityType<?>, ESPOptions> defaultBlockEntityESPPreset = new HashMap<>(Map.of(
            BlockEntityType.CHEST, new ESPOptions(true, new ColourOptions().setStaticColour(new RenderingUtils.Colour(0f,1f,0f))),
            BlockEntityType.BARREL, new ESPOptions(true, new ColourOptions().setStaticColour(new RenderingUtils.Colour(0f,1f,0f))),
            BlockEntityType.TRAPPED_CHEST, new ESPOptions(true, new ColourOptions().setStaticColour(new RenderingUtils.Colour(1f,0f,0f))),
            BlockEntityType.ENDER_CHEST, new ESPOptions(true, new ColourOptions().setStaticColour(new RenderingUtils.Colour(0f,1f,1f))),
            BlockEntityType.SHULKER_BOX, new ESPOptions(true, new ColourOptions().setStaticColour(new RenderingUtils.Colour(1f,0f,1f))),
            BlockEntityType.MOB_SPAWNER, new ESPOptions(true, new ColourOptions().setStaticColour(new RenderingUtils.Colour(0f,0f,1f)))
    ));

    public static HashMap<EntityType<?>, ESPOptions> defaultMobESPPreset = new HashMap<>(Map.of());

    public static Function<ColourOptions, RenderingUtils.Colour> blockEntityESPColourFunc = ColourRenderFunctions::getStaticColour;
    public static Function<ColourOptions, RenderingUtils.Colour> mobESPColourFunc = ColourRenderFunctions::getColourByHealth;
}
