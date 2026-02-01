package name.modid.Commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import name.modid.Presets.Modules;
import name.modid.Presets.Presets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class Enable {
    public static int RunCommand(CommandContext<CommandSourceStack> context){
        String module = StringArgumentType.getString(context, "module");
        Modules.setModule(module, new Modules.ModuleOptions(true));
        context.getSource().sendSuccess(() -> Component.literal("Enabled %s".formatted(module)), false);
        return 1;
    }
}
