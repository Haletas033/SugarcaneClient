package name.modid;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;

public class MobESP extends ESP{
    public static HashMap<EntityType<?>, Presets.ESPOptions> mobOptions = Presets.defaultMobESPPreset;

    @Override
    void onInitialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::ExtractAndDraw);
    }

    @Override
    void ExtractAndDraw(WorldRenderContext context){
        RenderESP(context);
        RenderingUtils.drawLinesThroughWalls(Minecraft.getInstance(), this, Pipelines.ESP_LINES);
    }

    @Override
    void RenderFunctionality(WorldRenderContext context, PoseStack matrices, BufferBuilder fillBuffer, BufferBuilder outlineBuffer){
        List<LivingEntity> mobs = EntityUtils.GetLivingEntities().toList();

        for (LivingEntity mob : mobs){
            Presets.ESPOptions options = mobOptions.get(mob.getType());

            //If options aren't explicitly set but enable all is true use that instead.
            if (options == null && Presets.getEnableAll(Presets.EnableAllType.MOBS).enabled()){
                options = Presets.getEnableAll(Presets.EnableAllType.MOBS).options();
            }

            if (options != null) {
                if (Presets.getEnableAll(Presets.EnableAllType.MOBS).enabled()) {
                    //Fill in ColourOption information
                    options.colourOptions().setEntity(mob);

                    AABB lerpedBoundingBox = EntityUtils.getLerpedBox(mob, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
                    Vec3 startPos = new Vec3(lerpedBoundingBox.minX, lerpedBoundingBox.minY, lerpedBoundingBox.minZ);
                    Vec3 endPos = new Vec3(lerpedBoundingBox.maxX, lerpedBoundingBox.maxY, lerpedBoundingBox.maxZ);
                    RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), startPos, endPos, Presets.mobESPColourFunc.apply(options.colourOptions()));
                }
            }
        }

        RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), new Vec3(0,0,0), new Vec3(0,0,0), new RenderingUtils.Colour(0f,0f,0f));
    }
}
