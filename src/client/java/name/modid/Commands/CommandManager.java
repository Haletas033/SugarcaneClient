package name.modid.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandManager {
    public record ArgData<T>(String name, ArgumentType<T> argType, Suggestions suggestions){}

    @SafeVarargs
    public static <T> void register(String name, Command<CommandSourceStack> func, ArgData<T>... args){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(name);

            RequiredArgumentBuilder<CommandSourceStack, ?> arg =
                    Commands.argument(
                            args[args.length - 1].name(),
                            args[args.length - 1].argType()
                    ).suggests(args[args.length - 1].suggestions()).executes(func);

            for (int i = args.length-2; i >= 0 ; i--){
                assert args[i] != null;
                arg = AddArg(args[i], arg);
            }
            command.then(arg);


            //Register command
            dispatcher.register(command);
        });
    }

    private static <T> RequiredArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> AddArg(ArgData<T> arg,
    RequiredArgumentBuilder<CommandSourceStack, ?> lastArg) {
        if (arg.suggestions() != null)
            return Commands.argument(arg.name(), arg.argType()).suggests(arg.suggestions()).then(lastArg);
        return Commands.argument(arg.name(), arg.argType()).then(lastArg);
    }

    //Load all commands
    public static void Initialize() {
        CommandManager.register("enable", Enable::RunCommand,
                new CommandManager.ArgData<>("module", StringArgumentType.string(), new Suggestions().setSuggestions(Suggestions.moduleNames)));

        CommandManager.register("disable", Disable::RunCommand,
            new CommandManager.ArgData<>("module", StringArgumentType.string(), new Suggestions().setSuggestions(Suggestions.moduleNames))
        );

        CommandManager.register("SetColourFunc", Disable::RunCommand,
            new CommandManager.ArgData<>("ESPLike", StringArgumentType.string(), new Suggestions().setSuggestions(Suggestions.ESPLikes)),
            new CommandManager.ArgData<>("ColourFunc", StringArgumentType.string(), new Suggestions().setSuggestions(Suggestions.colourFuncs))
        );
    }
}
