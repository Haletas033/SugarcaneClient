package name.modid;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;

import static name.modid.Pipelines.ESP_LINES;
import static name.modid.Pipelines.FILLED_THROUGH_WALLS;
import static name.modid.SugarcaneClientClient.LOGGER;

public class MobESP implements ClientModInitializer, RenderingUtils.RenderBuffers{

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

        WorldRenderEvents.AFTER_ENTITIES.register(this::ExtractAndDrawMobs);
    }

    private void ExtractAndDrawMobs(WorldRenderContext context){
        RenderMobs(context);

        RenderingUtils.drawLinesThroughWalls(Minecraft.getInstance(), this, Pipelines.ESP_LINES);
    }

    private void RenderMobs(WorldRenderContext context){
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (fillBuffer == null) { fillBuffer = new BufferBuilder(fillAllocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat()); }
        if (outlineBuffer == null) { outlineBuffer = new BufferBuilder(outlineAllocator, ESP_LINES.getVertexFormatMode(), ESP_LINES.getVertexFormat()); }

        List<LivingEntity> mobs = EntityUtils.GetLivingEntities().toList();
        for (LivingEntity mob : mobs){
            Vec3 startPos = new Vec3(mob.getBoundingBox().minX, mob.getBoundingBox().minY, mob.getBoundingBox().minZ);
            Vec3 endPos = new Vec3(mob.getBoundingBox().maxX, mob.getBoundingBox().maxY, mob.getBoundingBox().maxZ);
            RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), startPos, endPos, getColor(mob));
        }

        RenderingUtils.drawCuboidOutline(outlineBuffer, matrices.last().pose(), new Vec3(0,0,0), new Vec3(0,0,0), new RenderingUtils.Colour(1f,0f,0f,1f));

        matrices.popPose();
    }

    private RenderingUtils.Colour getColor(LivingEntity e) {
        float f = Minecraft.getInstance().player.distanceTo(e) / 20F;
        float r = Mth.clamp(2 - f, 0, 1);
        float g = Mth.clamp(f, 0, 1);
        return new RenderingUtils.Colour(r, g, 0, 1);
    }
}
