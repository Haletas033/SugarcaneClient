package name.modid;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
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

import static name.modid.SugarcaneClientClient.LOGGER;
import static name.modid.SugarcaneClientClient.MOD_ID;

public class ChestESP implements ClientModInitializer {
    private static ChestESP instance;
    // :::custom-pipelines:define-pipeline
    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/debug_filled_box_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    );

    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private BufferBuilder buffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private MappableRingBuffer vertexBuffer;

    public static ChestESP getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::extractAndDrawChests);
    }

    private void extractAndDrawChests(WorldRenderContext context) {
        renderChests(context);
        drawFilledThroughWalls(Minecraft.getInstance(), FILLED_THROUGH_WALLS);
    }

    private void renderChests(WorldRenderContext context) {

        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(allocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
        }

        List<BlockEntity> blockEntities = ChunkUtils.getLoadedBlockEntities().toList();
        for (BlockEntity be : blockEntities) {
            renderFilledBox(matrices.last().pose(), buffer, be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ(),
                    be.getBlockPos().getX() + 1, be.getBlockPos().getY() + 1, be.getBlockPos().getZ() + 1, 0f, 1f, 0f, 0.5f);
        }

        //Draw an invisible box to prevent crashes
        renderFilledBox(matrices.last().pose(), buffer, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f, 0f, 0f);

        matrices.popPose();
    }

    private void renderFilledBox(Matrix4fc positionMatrix, BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        // Front Face
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Back face
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        // Left face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Right face
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Top face
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Bottom face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    private void drawFilledThroughWalls(Minecraft client, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        // Build the buffer
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        vertexBuffer.rotate();
        buffer = null;
    }

    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        //Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        //Initialize or resize the vertex buffer as needed
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            vertexBuffer = new MappableRingBuffer(() -> MOD_ID + " ChestESP", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        //Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            //Sort the quads if there is translucency
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            //Upload the index buffer
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            //Use the general shape index buffer for non-quad draw modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        //Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> MOD_ID + " example render pipeline rendering", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            //Bind texture if applicable:
            //Sampler0 is used for texture inputs in vertices
            //renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            //The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        allocator.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}