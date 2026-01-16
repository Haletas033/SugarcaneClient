package name.modid;

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

import static name.modid.SugarcaneClientClient.MOD_ID;

public class BlockEntityESP implements ClientModInitializer, RenderingUtils.RenderBuffers {
    private static BlockEntityESP instance;

    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/debug_filled_box_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build()
    );

    private BufferBuilder buffer;


    private MappableRingBuffer vertexBuffer;

    public static BlockEntityESP getInstance() {
        return instance;
    }

    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    //Assign variables for RenderingUtils
    @Override
    public BufferBuilder getBuffer() { return buffer; }
    @Override
    public void setBuffer(BufferBuilder buffer) { this.buffer = buffer; }

    @Override
    public MappableRingBuffer getVertexBuffer() { return vertexBuffer; }
    @Override
    public void setVertexBuffer(MappableRingBuffer buffer) { this.vertexBuffer = buffer; }

    @Override
    public ByteBufferBuilder getAllocator() { return allocator; }

    @Override
    //Entry point
    public void onInitializeClient() {
        instance = this;
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::extractAndDrawBlockEntities);
    }

    //Draw block entities onto the screen
    private void extractAndDrawBlockEntities(WorldRenderContext context) {
        renderBlockEntities(context);
        RenderingUtils.drawFilledThroughWalls(Minecraft.getInstance(), this, FILLED_THROUGH_WALLS);
    }

    //Render blockEntities to the GPU
    private void renderBlockEntities(WorldRenderContext context) {
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(allocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
        }

        List<BlockEntity> blockEntities = ChunkUtils.getLoadedBlockEntities().toList();
        for (BlockEntity be : blockEntities) {
            BlockPos endPos = new BlockPos(be.getBlockPos().getX()+1, be.getBlockPos().getY()+1, be.getBlockPos().getZ()+1);
            RenderingUtils.drawCuboid(buffer, matrices.last().pose(), be.getBlockPos(), endPos, new RenderingUtils.Colour(0f, 1f, 0f, 0.5f));
        }

        //Draw an invisible box to prevent crashes
        RenderingUtils.drawCuboid(buffer, matrices.last().pose(), new BlockPos(0,0,0), new BlockPos(0,0,0), new RenderingUtils.Colour(0f, 1f, 0f, 0.5f));

        matrices.popPose();
    }

    //Cleanup everything
    public void close() {
        allocator.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}