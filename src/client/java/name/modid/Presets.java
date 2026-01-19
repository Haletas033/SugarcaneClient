package name.modid;

import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;

public class Presets {
    public static HashMap<BlockEntityType<?>, BlockEntityESP.BlockEntityOptions> defaultBlockEntityESPPreset = new HashMap<>(Map.of(
            BlockEntityType.CHEST, new BlockEntityESP.BlockEntityOptions(true, new RenderingUtils.Colour(0f, 1f, 0f, 0.5f)),
            BlockEntityType.BARREL, new BlockEntityESP.BlockEntityOptions(true, new RenderingUtils.Colour(0f, 1f, 0f, 0.5f)),
            BlockEntityType.TRAPPED_CHEST, new BlockEntityESP.BlockEntityOptions(true, new RenderingUtils.Colour(1f, 0f, 0f, 0.5f)),
            BlockEntityType.ENDER_CHEST, new BlockEntityESP.BlockEntityOptions(true, new RenderingUtils.Colour(0f, 1f, 1f, 0.5f)),
            BlockEntityType.SHULKER_BOX, new BlockEntityESP.BlockEntityOptions(true, new RenderingUtils.Colour(1f, 0f, 1f, 0.5f)),
            BlockEntityType.MOB_SPAWNER, new BlockEntityESP.BlockEntityOptions(true, new RenderingUtils.Colour(0f, 0f, 1f, 0.5f))
    ));
}
