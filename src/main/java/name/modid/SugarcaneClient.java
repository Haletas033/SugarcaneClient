package name.modid;

import net.fabricmc.api.ModInitializer;

import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugarcaneClient implements ModInitializer {
	public static final String MOD_ID = "sugarcaneclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



	@Override
	public void onInitialize() {
		LOGGER.info("Main initialized!");
	}
}