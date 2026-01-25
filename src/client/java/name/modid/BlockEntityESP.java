package name.modid;

import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import static name.modid.Pipelines.ESP_LINES;
import static name.modid.Pipelines.FILLED_THROUGH_WALLS;

public class BlockEntityESP extends ESP {
    public record BlockEntityOptions(boolean enabled, RenderingUtils.Colour col){}

    public static HashMap<BlockEntityType<?>, BlockEntityOptions> blockEntitiesOptions = Presets.defaultBlockEntityESPPreset;

    //Entry point
    @Override
    void onInitialize() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::ExtractAndDraw);
    }

    @Override
    //Draw block entities onto the screen
    void ExtractAndDraw(WorldRenderContext context) {
        RenderESP(context);

        RenderingUtils.drawFilledThroughWalls(Minecraft.getInstance(), this, FILLED_THROUGH_WALLS);
        RenderingUtils.drawLinesThroughWalls(Minecraft.getInstance(), this, ESP_LINES);
    }

    @Override
    //Render blockEntities to the GPU
    void RenderFunctionality(WorldRenderContext context, PoseStack matrices, BufferBuilder fillBuffer, BufferBuilder outlineBuffer) {
        List<BlockEntity> blockEntities = ChunkUtils.getLoadedBlockEntities().toList();
        for (BlockEntity be : blockEntities) {
            BlockEntityOptions options = blockEntitiesOptions.get(be.getType());
            if (options != null && options.enabled) {
                BlockPos BPendPos = new BlockPos(be.getBlockPos().getX() + 1, be.getBlockPos().getY() + 1, be.getBlockPos().getZ() + 1);
                Vec3 endPos = RenderingUtils.BPtoVec3(BPendPos);
                RenderingUtils.drawCuboid(fillBuffer, matrices.last().pose(), RenderingUtils.BPtoVec3(be.getBlockPos()), endPos, options.col);
                RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), RenderingUtils.BPtoVec3(be.getBlockPos()), endPos, options.col);

            }
        }

        //Draw an invisible box to prevent crashes
        RenderingUtils.drawCuboid(fillBuffer, matrices.last().pose(), new Vec3(0,0,0), new Vec3(0,0,0), new RenderingUtils.Colour(0f, 1f, 0f, 0.5f));
        RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), new Vec3(0,0,0), new Vec3(0,0,0), new RenderingUtils.Colour(0f, 1f, 0f, 0.5f));
    }
}