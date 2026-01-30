package name.modid.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.stream.Stream;

/**Class to help with chunk themed things**/
public class ChunkUtils {
    public static final Minecraft MC = Minecraft.getInstance();

    public static Stream<LevelChunk> getLoadedChunks() {

        int radius = Math.max(2, MC.options.getEffectiveRenderDistance()) + 3;
        int diameter = radius * 2 + 1;

        assert MC.player != null;
        ChunkPos center = MC.player.chunkPosition();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);

        //Get every chunk the player can see
        return Stream.iterate(min, pos -> {
                    int x = pos.x;
                    int z = pos.z;

                    x++;
                    if(x > max.x) { x = min.x; z++; }

                    if(z > max.z) throw new IllegalStateException("Stream limit didn't work.");

                    return new ChunkPos(x, z);

                }).limit((long) diameter * diameter).filter(c -> {
                    assert MC.level != null;
                    return MC.level.hasChunk(c.x, c.z);
                })
                .map(c -> MC.level.getChunk(c.x, c.z));
    }

    //Get every block entity (even unrendered ones)
    public static Stream<BlockEntity> getLoadedBlockEntities() {
        return getLoadedChunks()
                .flatMap(chunk -> chunk.getBlockEntities().values().stream());
    }
}
