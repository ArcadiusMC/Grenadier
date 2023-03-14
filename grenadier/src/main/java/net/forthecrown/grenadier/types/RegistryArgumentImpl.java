package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

@Getter
class RegistryArgumentImpl<T extends Keyed>
    implements RegistryArgument<T>, VanillaMappedArgument
{

  private final UnknownFactory exceptionFactory;
  private final Registry<T> registry;

  public RegistryArgumentImpl(Registry<T> registry,
                              UnknownFactory exceptionFactory
  ) {
    this.exceptionFactory = Objects.requireNonNull(exceptionFactory);
    this.registry = Objects.requireNonNull(registry);
  }

  @Override
  public T parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();

    NamespacedKey key = ArgumentTypes.key().parse(reader);
    T value = registry.get(key);

    if (value == null) {
      reader.setCursor(start);
      throw getExceptionFactory().create(reader, key);
    }

    return value;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggestKeyed(builder, registry);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ResourceLocationArgument.id();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return false;
  }
}