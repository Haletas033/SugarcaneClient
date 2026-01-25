package name.modid;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;

import static name.modid.Pipelines.ESP_LINES;
import static name.modid.Pipelines.FILLED_THROUGH_WALLS;

public abstract class ESP implements ClientModInitializer, RenderingUtils.RenderBuffers {

    private BufferBuilder fillBuffer;
    private BufferBuilder outlineBuffer;


    private MappableRingBuffer vertexFillBuffer;
    private MappableRingBuffer vertexOutlineBuffer;

    private static final ByteBufferBuilder fillAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final ByteBufferBuilder outlineAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

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

    @Override
    public ByteBufferBuilder getOutlineAllocator() { return outlineAllocator; }

    @Override
    public void onInitializeClient() {
        onInitialize();
    }

    public void RenderESP(WorldRenderContext context){
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (fillBuffer == null) { fillBuffer = new BufferBuilder(fillAllocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat()); }
        if (outlineBuffer == null) { outlineBuffer = new BufferBuilder(outlineAllocator, ESP_LINES.getVertexFormatMode(), ESP_LINES.getVertexFormat()); }

        RenderFunctionality(context, matrices, fillBuffer, outlineBuffer);

        matrices.popPose();
    }

    abstract void onInitialize();
    abstract void ExtractAndDraw(WorldRenderContext context);
    abstract void RenderFunctionality(WorldRenderContext context, PoseStack matrices, BufferBuilder fillBuffer, BufferBuilder outlineBuffer);
}