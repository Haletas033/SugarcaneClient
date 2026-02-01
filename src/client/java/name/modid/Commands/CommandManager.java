package name.modid.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.util.Tuple;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandManager {

    @SafeVarargs
    public static <T> void register(String name, Command<CommandSourceStack> func, @Nullable Tuple<String, ArgumentType<T>>... args){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(name);

            RequiredArgumentBuilder<CommandSourceStack, ?> arg =
                    Commands.argument(
                            args[args.length - 1].getA(),
                            args[args.length - 1].getB()
                    ).executes(func);

            for (int i = args.length-2; i >= 0 ; i--){
                assert args[i] != null;
                arg = Commands.argument(args[i].getA(), args[i].getB()).then(arg);
            }
            command.then(arg);


            //Register command
            dispatcher.register(command);
        });
    }
}
