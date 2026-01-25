package name.modid;

import net.fabricmc.fabric.impl.object.builder.FabricEntityTypeImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.DeltaTracker;


/**Class for anything to do with entities**/
public class EntityUtils {
    public static final Minecraft MC = Minecraft.getInstance();
    //Get all living non-player entities
    public static Stream<LivingEntity> GetLivingEntities(){
        assert MC.level != null;

        return StreamSupport
                .stream(MC.level.entitiesForRendering().spliterator(), false)
                .filter(LivingEntity.class::isInstance).map(e -> (LivingEntity)e)
                .filter(e -> !(e instanceof Player))
                .filter(e -> !e.isRemoved() && e.getHealth() > 0);
    }

    public static AABB getLerpedBox(LivingEntity e, float tickDelta){
            double x = Mth.lerp(tickDelta, e.xOld, e.getX());
            double y = Mth.lerp(tickDelta, e.yOld, e.getY());
            double z = Mth.lerp(tickDelta, e.zOld, e.getZ());
            Vec3 lerpedPos = new Vec3(x, y, z);
            Vec3 offset = lerpedPos.subtract(e.position());

            return e.getBoundingBox().move(offset);
    }
}
