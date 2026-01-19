package name.modid;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static name.modid.SugarcaneClientClient.MOD_ID;

public class RenderingUtils {
    public interface RenderBuffers {
        BufferBuilder getFillBuffer();
        void setFillBuffer(BufferBuilder buffer);
        BufferBuilder getOutlineBuffer();
        void setOutlineBuffer(BufferBuilder buffer);

        MappableRingBuffer getVertexFillBuffer();
        void setVertexFillBuffer(MappableRingBuffer buffer);
        MappableRingBuffer getVertexOutlineFillBuffer();
        void setVertexOutlineFillBuffer(MappableRingBuffer buffer);

        ByteBufferBuilder getFillAllocator();
        ByteBufferBuilder getOutlineAllocator();
    }

    public record Colour(float r, float g, float b, float a){}

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    enum VertexBufferMode {
        FillMode,
        OutlineMode
    }

    //Draw vertices onto the screen
    public static void draw(Minecraft client, RenderPipeline pipeline, ByteBufferBuilder allocator,
                             MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
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
                .createRenderPass(() -> MOD_ID + " ESP Pass", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
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

    //Upload the buffer to the GPU to draw it
    private static GpuBuffer upload(MeshData.DrawState drawParameters, RenderBuffers esp, VertexFormat format, MeshData builtBuffer, VertexBufferMode mode) {
        //Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        //Initialize or resize the vertex buffer as needed
        if (mode == VertexBufferMode.FillMode) {
            if (esp.getVertexFillBuffer() == null || esp.getVertexFillBuffer().size() < vertexBufferSize) {
                esp.setVertexFillBuffer(new MappableRingBuffer(() -> MOD_ID + " ESP", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize));
            }
        } else {
            if (esp.getVertexOutlineFillBuffer() == null || esp.getVertexOutlineFillBuffer().size() < vertexBufferSize) {
                esp.setVertexOutlineFillBuffer(new MappableRingBuffer(() -> MOD_ID + " ESP", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize));
            }
        }

        //Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        ByteBuffer vertexData = builtBuffer.vertexBuffer();
        vertexData.rewind();
        int length = vertexData.remaining();

        MappableRingBuffer ringBuffer = mode == VertexBufferMode.FillMode
                ? esp.getVertexFillBuffer()
                : esp.getVertexOutlineFillBuffer();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
                ringBuffer.currentBuffer().slice(0, length), false, true)) {
            MemoryUtil.memCopy(MemoryUtil.memAddress(vertexData), MemoryUtil.memAddress(mappedView.data()), length);
        }

        return ringBuffer.currentBuffer();
    }

    //Draw through walls
    public static void drawFilledThroughWalls(Minecraft client, RenderBuffers esp, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        // Build the buffer
        MeshData builtBuffer = esp.getFillBuffer().buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, esp, format, builtBuffer, VertexBufferMode.FillMode);

        RenderingUtils.draw(client, pipeline, esp.getFillAllocator(), builtBuffer, drawParameters, vertices, format);

        esp.getVertexFillBuffer().rotate();
        esp.setFillBuffer(null);
    }

    //Draw outline through walls
    public static void drawLinesThroughWalls(Minecraft client, RenderBuffers esp, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        // Build the buffer
        MeshData builtBuffer = esp.getOutlineBuffer().buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, esp, format, builtBuffer, VertexBufferMode.OutlineMode);

        RenderingUtils.draw(client, pipeline, esp.getOutlineAllocator(), builtBuffer, drawParameters, vertices, format);

        esp.getVertexOutlineFillBuffer().rotate();
        esp.setOutlineBuffer(null);
    }

    //Interface to the AddVertex lambda
    @FunctionalInterface
    interface VertexAdder {
        void add(int index, Vector3f n);
    }

    //Render any mesh made of vertices and indices onto the screen
    public static void drawMesh(BufferBuilder buffer, Matrix4fc positionMatrix, float[] vertices, int[] indices, Colour col){
        //Lambda to simply the vertex adding process
        VertexAdder AddVertex = (int index, Vector3f n) ->
                buffer.addVertex(positionMatrix,
                vertices[index],
                vertices[index+1],
                vertices[index+2]
        ).setColor(col.r, col.g, col.b, col.a);

        for (int i = 0; i < indices.length; i+=3){
            int i0 = indices[i]*3;
            int i1 = indices[i+1]*3;
            int i2 = indices[i+2]*3;

            Vector3f nullVec = new Vector3f(0);

            AddVertex.add(i0, nullVec);
            AddVertex.add(i1, nullVec);
            AddVertex.add(i2, nullVec);
        }
    }

    //Render any made of vertices and line indices onto the screen
    public static void drawLineMesh(BufferBuilder buffer, Matrix4fc positionMatrix, float[] vertices, int[] indices, Colour col) {
        //Lambda to simply the vertex adding process
        VertexAdder AddVertex = (int index, Vector3f n) ->
                buffer.addVertex(positionMatrix,
                        vertices[index],
                        vertices[index+1],
                        vertices[index+2]
                ).setNormal(n.x, n.y, n.z).setLineWidth(2).setColor(col.r, col.g, col.b, 1f); //Fully opaque

        for (int i = 0; i < indices.length; i+=2){
            int i0 = indices[i]*3;
            int i1 = indices[i+1]*3;

            //Get normals (technically 0,0,0 would work but looks better this way)
            float dx = vertices[i1] - vertices[i0];
            float dy = vertices[i1 + 1] - vertices[i0 + 1];
            float dz = vertices[i1 + 2] - vertices[i0 + 2];

            float length = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (length == 0) length = 1f;
            float nx = dx / length;
            float ny = dy / length;
            float nz = dz / length;

            Vector3f normalVec = new Vector3f(nx, ny, nz);

            AddVertex.add(i0, normalVec);
            AddVertex.add(i1, normalVec);

        }
    }

    //Render a cuboid onto the screen from 2 points
    public static void drawCuboid(BufferBuilder buffer, Matrix4fc positionMatrix, BlockPos startPos, BlockPos endPos, Colour col){
        float[] vertices = getCuboidVertices(startPos, endPos);
        int[] indices = getCuboidIndices();
        drawMesh(buffer, positionMatrix, vertices, indices, col);
    }

    public static void drawCuboidOutline(BufferBuilder buffer, Matrix4fc positionMatrix, BlockPos startPos, BlockPos endPos, Colour col){
        float[] vertices = getCuboidVertices(startPos, endPos);
        int[] indices = getCuboidLineIndices();
        drawLineMesh(buffer, positionMatrix, vertices, indices, col);
    }

    //Get vertices needed for a cuboid from 2 points
    private static float @NonNull [] getCuboidVertices(BlockPos startPos, BlockPos endPos) {
        float fspX = startPos.getX(); float fepX = endPos.getX();
        float fspY = startPos.getY(); float fepY = endPos.getY();
        float fspZ = startPos.getZ(); float fepZ = endPos.getZ();

        //Cuboid vertices
        return new float[]{
                //Back
                fspX, fspY, fspZ, //Bottom left
                fepX, fspY, fspZ, //Bottom right
                fepX, fepY, fspZ, //Top right
                fspX, fepY, fspZ, //Top left
                //Front
                fspX, fspY, fepZ, //Bottom left
                fepX, fspY, fepZ, //Bottom right
                fepX, fepY, fepZ, //Top right
                fspX, fepY, fepZ, //Top left
        };
    }

    //Get indices for a cuboid
    private static int @NonNull [] getCuboidIndices() {
        //Cuboid indices
        return new int[]{
                //Back face
                0, 2, 1,
                0, 3, 2,
                //Front face
                4, 5, 6,
                4, 6, 7,
                //Bottom face
                0, 1, 5,
                0, 5, 4,
                //Top face
                3, 6, 2,
                3, 7, 6,
                //Left face
                0, 4, 7,
                0, 7, 3,
                //Right face
                1, 2, 6,
                1, 6, 5
        };
    }

    //Get line indices for a cuboid
    private static int @NonNull [] getCuboidLineIndices() {
        //Cuboid indices
        return new int[]{
                //Back face
                0, 1,
                1, 2,
                2, 3,
                3, 0,
                //Front face
                4, 5,
                5, 6,
                6, 7,
                7, 4,
                //Connecting
                0, 4,
                1, 5,
                2, 6,
                3, 7
        };
    }
}
