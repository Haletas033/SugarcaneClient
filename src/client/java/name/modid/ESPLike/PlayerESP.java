package name.modid.ESPLike;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import name.modid.Presets.ColourOptions;
import name.modid.Pipelines;
import name.modid.Presets.Modules;
import name.modid.Presets.Presets;
import name.modid.Utils.EntityUtils;
import name.modid.Utils.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;

public class PlayerESP extends ESP {
    @Override
    void onInitialize() {
            WorldRenderEvents.AFTER_ENTITIES.register(this::ExtractAndDraw);
    }

    @Override
    void ExtractAndDraw(WorldRenderContext context){
        if (Modules.getModule("PlayerESP").enabled()) {
            RenderESP(context);
            RenderingUtils.drawLinesThroughWalls(Minecraft.getInstance(), this, Pipelines.ESP_LINES);
        }
    }

    @Override
    void RenderFunctionality(WorldRenderContext context, PoseStack matrices, BufferBuilder fillBuffer, BufferBuilder outlineBuffer){
        //Get all players
        assert Minecraft.getInstance().level != null;
        Stream<AbstractClientPlayer> stream = Minecraft.getInstance().level.players()
                .parallelStream().filter(e -> !e.isRemoved() && e.getHealth() > 0)
                .filter(e -> e != Minecraft.getInstance().player)
                .filter(e -> Math.abs(e.getY() - Minecraft.getInstance().player.getY()) <= 1e6);

        List<AbstractClientPlayer> players = stream.toList();

        for (AbstractClientPlayer player : players){
            //Fill in colourOption information
            ColourOptions colourOptions = new ColourOptions().setEntity(player).setStaticColour(1f,0f,1f);

            AABB lerpedBoundingBox = EntityUtils.getLerpedBox(player, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            Vec3 startPos = new Vec3(lerpedBoundingBox.minX, lerpedBoundingBox.minY, lerpedBoundingBox.minZ);
            Vec3 endPos = new Vec3(lerpedBoundingBox.maxX, lerpedBoundingBox.maxY, lerpedBoundingBox.maxZ);
            RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), startPos, endPos, Presets.playerESPColourFunc.apply(colourOptions));
        }

        RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), new Vec3(0,0,0), new Vec3(0,0,0), new RenderingUtils.Colour(0f,0f,0f));
    }


}
