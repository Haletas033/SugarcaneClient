package name.modid;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


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
}
