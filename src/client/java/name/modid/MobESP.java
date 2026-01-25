package name.modid;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MobESP extends ESP{
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
            AABB lerpedBoundingBox = EntityUtils.getLerpedBox(mob, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            Vec3 startPos = new Vec3(lerpedBoundingBox.minX, lerpedBoundingBox.minY, lerpedBoundingBox.minZ);
            Vec3 endPos = new Vec3(lerpedBoundingBox.maxX, lerpedBoundingBox.maxY, lerpedBoundingBox.maxZ);
            RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), startPos, endPos, getColourByHealth(mob));
        }

        RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), new Vec3(0,0,0), new Vec3(0,0,0), new RenderingUtils.Colour(1f,0f,0f,1f));
    }

    private RenderingUtils.Colour getColourByDistance(LivingEntity e) {
        float f = Minecraft.getInstance().player.distanceTo(e) / 20F;
        float r = Mth.clamp(2 - f, 0, 1);
        float g = Mth.clamp(f, 0, 1);
        return new RenderingUtils.Colour(r, g, 0, 1);
    }

    private RenderingUtils.Colour getColourByHealth(LivingEntity e) {
        float normalizedH = e.getHealth() / e.getMaxHealth();
        float r = normalizedH < 0.5f ? 1f : 2f - 2f * normalizedH;
        float g = normalizedH > 0.5f ? 1f : 2f * normalizedH;
        return new RenderingUtils.Colour(r, g, 0, 1);
    }
}
