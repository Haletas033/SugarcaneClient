package name.modid;

import name.modid.utils.RenderingUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ColourOptions {
    private RenderingUtils.Colour staticColour = new RenderingUtils.Colour(0f, 1f, 0f); //Default colour
    //Colours used in animated gradient
    private RenderingUtils.Colour[] animatedColours = {
            new RenderingUtils.Colour(1f,0f,1f) ,
            new RenderingUtils.Colour(0f,0f,1f)
    };
    private RenderingUtils.Colour highColour = new RenderingUtils.Colour(0f, 1f, 0f); //Colour used for high health or far away distance
    private RenderingUtils.Colour lowColour = new RenderingUtils.Colour(1f, 0f, 0f); //Colour used for low health or close up distance
    //Given to ColourOptions by other methods
    private LivingEntity entity;
    private Vec3 blockPos;

    public RenderingUtils.Colour getStaticColour() { return staticColour; }

    public ColourOptions setStaticColour(RenderingUtils.Colour staticColour) { this.staticColour = staticColour; return this; }

    public ColourOptions setStaticColour(float r, float g, float b) { this.staticColour = new RenderingUtils.Colour(r,g,b); return this; }

    public RenderingUtils.Colour[] getAnimatedColours() { return animatedColours; }

    public ColourOptions setAnimatedColours(RenderingUtils.Colour[] animatedColours) { this.animatedColours = animatedColours; return this; }

    public RenderingUtils.Colour getHighColour() { return highColour; }

    public ColourOptions setHighColour(RenderingUtils.Colour highColour) { this.highColour = highColour; return this; }

    public RenderingUtils.Colour getLowColour() { return lowColour; }

    public ColourOptions setLowColour(RenderingUtils.Colour lowColour) { this.lowColour = lowColour; return this; }

    public LivingEntity getEntity() { return entity; }

    public ColourOptions setEntity(LivingEntity entity) { this.entity = entity; return this; }

    public Vec3 getBlockPos() { return blockPos; }

    public ColourOptions setBlockPos(Vec3 blockPos) { this.blockPos = blockPos; return this; }
}
