package name.modid.Commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class Enable {
    public static int RunCommand(CommandContext<CommandSourceStack> context){
        int ping = IntegerArgumentType.getInteger(context, "ping");
        context.getSource().sendSuccess(() -> Component.literal("I wish my ping was %s".formatted(ping)), false);
        return 1;
    }
}
