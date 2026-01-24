package name.modid;

import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import static name.modid.Pipelines.ESP_LINES;
import static name.modid.SugarcaneClientClient.LOGGER;
import static name.modid.SugarcaneClientClient.MOD_ID;
import static name.modid.Pipelines.FILLED_THROUGH_WALLS;

public class BlockEntityESP implements ClientModInitializer, RenderingUtils.RenderBuffers {
    public record BlockEntityOptions(boolean enabled, RenderingUtils.Colour col){}

    public static HashMap<BlockEntityType<?>, BlockEntityOptions> blockEntitiesOptions = Presets.defaultBlockEntityESPPreset;

    private BufferBuilder fillBuffer;
    private BufferBuilder outlineBuffer;


    private MappableRingBuffer vertexFillBuffer;
    private MappableRingBuffer vertexOutlineBuffer;

    private static final ByteBufferBuilder fillAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final ByteBufferBuilder outlineAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    //Assign variables for RenderingUtils
    @Override
    public BufferBuilder getFillBuffer() { return fillBuffer; }
    @Override
    public void setFillBuffer(BufferBuilder buffer) { this.fillBuffer = buffer; }
    @Override
    public BufferBuilder getOutlineBuffer() { return outlineBuffer; }
    @Override
    public void setOutlineBuffer(BufferBuilder buffer) { this.outlineBuffer = buffer; }

    @Override
    public MappableRingBuffer getVertexFillBuffer() { return vertexFillBuffer; }
    @Override
    public void setVertexFillBuffer(MappableRingBuffer buffer) { this.vertexFillBuffer = buffer; }
    @Override
    public MappableRingBuffer getVertexOutlineFillBuffer() { return vertexOutlineBuffer; }
    @Override
    public void setVertexOutlineFillBuffer(MappableRingBuffer buffer) { this.vertexOutlineBuffer = buffer; }

    @Override
    public ByteBufferBuilder getFillAllocator() { return fillAllocator; }
    public ByteBufferBuilder getOutlineAllocator() { return outlineAllocator; }

    @Override

    //Entry point
    public void onInitializeClient() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::extractAndDrawBlockEntities);
    }

    //Draw block entities onto the screen
    private void extractAndDrawBlockEntities(WorldRenderContext context) {
        renderBlockEntities(context);

        RenderingUtils.drawFilledThroughWalls(Minecraft.getInstance(), this, FILLED_THROUGH_WALLS);
        RenderingUtils.drawLinesThroughWalls(Minecraft.getInstance(), this, ESP_LINES);
    }

    //Render blockEntities to the GPU
    private void renderBlockEntities(WorldRenderContext context) {
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (fillBuffer == null) { fillBuffer = new BufferBuilder(fillAllocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat()); }
        if (outlineBuffer == null) { outlineBuffer = new BufferBuilder(outlineAllocator, ESP_LINES.getVertexFormatMode(), ESP_LINES.getVertexFormat()); }

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

        matrices.popPose();
    }

    //Cleanup everything
    public void close() {
        fillAllocator.close();
        outlineAllocator.close();

        if (vertexFillBuffer != null) {
            vertexFillBuffer.close();
            vertexFillBuffer = null;
        }
        if (vertexOutlineBuffer != null) {
            vertexOutlineBuffer.close();
            vertexOutlineBuffer = null;
        }
    }
}