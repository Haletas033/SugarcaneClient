package name.modid;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**Class containing different colour visualization methods e.g., byDistance, byHealth, solid**/
public class ColourRenderFunctions {
    public static RenderingUtils.Colour getColourByDistance(float distance){
        float f = distance / 20F;
        float r = Mth.clamp(2 - f, 0, 1);
        float g = Mth.clamp(f, 0, 1);
        return new RenderingUtils.Colour(r, g, 0, 1);
    }

    public static RenderingUtils.Colour getColourByHealth(LivingEntity e) {
        float normalizedH = e.getHealth() / e.getMaxHealth();
        float r = normalizedH < 0.5f ? 1f : 2f - 2f * normalizedH;
        float g = normalizedH > 0.5f ? 1f : 2f * normalizedH;
        return new RenderingUtils.Colour(r, g, 0, 1);
    }

    public static RenderingUtils.Colour getColourByDistanceToLivingEntity(LivingEntity e) { return getColourByDistance(Minecraft.getInstance().player.distanceTo(e)); }

}
