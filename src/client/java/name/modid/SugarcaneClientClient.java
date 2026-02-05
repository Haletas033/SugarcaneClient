package name.modid;

import name.modid.Commands.CommandManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SugarcaneClientClient implements ClientModInitializer {
	public static final String MOD_ID = "sugarcaneclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Client initialized!!");
		CommandManager.Initialize();
	}
}