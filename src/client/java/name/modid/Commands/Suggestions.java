package name.modid.Commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**Clas containing a bunch of suggestion lists for commands**/
public class Suggestions implements SuggestionProvider<CommandSourceStack> {
    private String[] suggestions;
    @Override
    public CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        for (String suggestion : suggestions)
            suggestionsBuilder.suggest(suggestion);

        return suggestionsBuilder.buildFuture();
    }

    //Every module
    public static final String[] moduleNames = {
      "BlockEntityESP",
      "MobESP",
      "PlayerESP"
    };

    //Every module that behaves like an ESP
    public static final String[] ESPLikes = {
            "BlockEntityESP",
            "MobESP",
            "PlayerESP"
    };

    //Every colourFunc
    public static final String[] colourFuncs = {
            "StaticColour",
            "AnimatedColour",
            "ColourByDistance",
            "ColourByHealth"
    };

    public String[] getSuggestions(){
        return this.suggestions;
    }

    public Suggestions setSuggestions(String[] suggestions) {
        this.suggestions = suggestions;
        return this;
    }
}
