package name.modid.Presets;

import name.modid.Utils.RenderingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**Class containing different colour visualization methods e.g., byDistance, byHealth, solid**/
public class ColourRenderFunctions {
    public static RenderingUtils.Colour getStaticColour(ColourOptions colourOptions){
        return colourOptions.getStaticColour();
    }

    private static float time = 0f;
    public static RenderingUtils.Colour getAnimatedColour(ColourOptions colourOptions){
        time += Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false) / colourOptions.getAnimationSpeed();
        return RenderingUtils.Colour.lerp(colourOptions.getAnimatedColours().getA(), colourOptions.getAnimatedColours().getB(), (float) (((Math.sin(time) * 0.5) + 0.5)));
    }

    public static RenderingUtils.Colour getColourByDistance(float distance){
        float r = Mth.clamp(2 - distance, 0, 1);
        float g = Mth.clamp(distance, 0, 1);
        return new RenderingUtils.Colour(r, g, 0);
    }

    public static RenderingUtils.Colour getColourByHealth(ColourOptions colourOptions) {
        float normalizedH = colourOptions.getEntity().getHealth() / colourOptions.getEntity().getMaxHealth();
        float r = normalizedH < 0.5f ? 1f : 2f - 2f * normalizedH;
        float g = normalizedH > 0.5f ? 1f : 2f * normalizedH;
        return new RenderingUtils.Colour(r, g, 0);
    }

    public static RenderingUtils.Colour getColourByDistanceToLivingEntity(ColourOptions colourOptions) {
        return getColourByDistance(Minecraft.getInstance().player.distanceTo(colourOptions.getEntity()) / 20F);
    }
    public static RenderingUtils.Colour getColourByDistanceToBlock(ColourOptions colourOptions) {
        return getColourByDistance((float)Minecraft.getInstance().player.distanceToSqr(colourOptions.getBlockPos()) / 100F);
    }
}
