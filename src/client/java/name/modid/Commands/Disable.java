package name.modid.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import name.modid.Presets.Modules;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class Disable {
    public static int RunCommand(CommandContext<CommandSourceStack> context){
        String module = StringArgumentType.getString(context, "module");
        Modules.setModule(module, new Modules.ModuleOptions(false));
        context.getSource().sendSuccess(() -> Component.literal("Disabled %s".formatted(module)), false);
        return 1;
    }
}
