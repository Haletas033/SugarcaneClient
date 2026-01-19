package name.modid;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static name.modid.SugarcaneClientClient.MOD_ID;

/**Class of different pipelines**/
public class Pipelines {
    //Line snippet
    public static final RenderPipeline.Snippet LINES_THROUGH_WALLS = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
		.withVertexShader(Identifier.parse("sugarcaneclient:lines"))
        .withFragmentShader(Identifier.parse("sugarcaneclient:lines"))
        .withBlend(BlendFunction.TRANSLUCENT).withCull(false)
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
		.buildSnippet();

    //Standard esp
    public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/debug_filled_box_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build()
    );

    //line esp
    public static final RenderPipeline ESP_LINES =
            RenderPipelines.register(RenderPipeline.builder(LINES_THROUGH_WALLS)
                    .withLocation(Identifier.parse("sugarcaneclient:pipeline/esp_lines"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());
}
