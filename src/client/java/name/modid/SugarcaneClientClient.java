package name.modid;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import name.modid.Commands.CommandManager;
import name.modid.Commands.Disable;
import name.modid.Commands.Enable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SugarcaneClientClient implements ClientModInitializer {
	public static final String MOD_ID = "sugarcaneclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Client initialized!!");
		CommandManager.register("enable", Enable::RunCommand, new Tuple<>("module", StringArgumentType.string()));

		CommandManager.register("disable", Disable::RunCommand, new Tuple<>("module", StringArgumentType.string()));
	}
}