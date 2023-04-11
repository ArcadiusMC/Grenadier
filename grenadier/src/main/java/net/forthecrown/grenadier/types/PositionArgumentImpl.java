package net.forthecrown.grenadier.types;

import static net.forthecrown.grenadier.types.CoordinateSuggestions.DEFAULT_LOCAL_2;
import static net.forthecrown.grenadier.types.CoordinateSuggestions.DEFAULT_LOCAL_3;
import static net.forthecrown.grenadier.types.CoordinateSuggestions.DEFAULT_WORLD_2;
import static net.forthecrown.grenadier.types.CoordinateSuggestions.DEFAULT_WORLD_3;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

class PositionArgumentImpl implements PositionArgument, VanillaMappedArgument {

  static final byte FLAG_2D = 0x1;
  static final byte FLAG_BLOCK = 0x2;
  static final byte NO_FLAGS = 0;

  static final PositionArgument BLOCK_POSITION
      = new PositionArgumentImpl(FLAG_BLOCK);

  static final PositionArgument POSITION
      = new PositionArgumentImpl(NO_FLAGS);

  static final PositionArgument BLOCK_POSITION_2D
      = new PositionArgumentImpl((byte) (FLAG_BLOCK | FLAG_2D));

  static final PositionArgument POSITION_2D
      = new PositionArgumentImpl(FLAG_2D);

  final byte flags;
  final ArgumentType<?> vanillaType;

  public PositionArgumentImpl(byte flags) {
    this.flags = flags;

    this.vanillaType = switch (flags) {
      case FLAG_2D | FLAG_BLOCK -> ColumnPosArgument.columnPos();
      case FLAG_2D -> Vec2Argument.vec2();
      case FLAG_BLOCK -> BlockPosArgument.blockPos();
      default -> Vec3Argument.vec3();
    };
  }

  boolean has(int flags) {
    return (this.flags & flags) == flags;
  }

  @Override
  public ParsedPosition parse(StringReader reader)
      throws CommandSyntaxException
  {
    return new PositionParser(reader, flags).parse();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource source)) {
      return Suggestions.empty();
    }

    boolean twoDimensional = has(FLAG_2D);
    List<CoordinateSuggestion> suggestions = new ArrayList<>();

    var relevant = twoDimensional
        ? source.getRelevant2DCords()
        : source.getRelevant3DCords();

    if (relevant != null) {
      suggestions.add(relevant);
    }

    boolean local = builder.getRemainingLowerCase().contains("^");
    CoordinateSuggestion defaultSuggestion;

    if (local) {
      defaultSuggestion = twoDimensional
          ? DEFAULT_LOCAL_2
          : DEFAULT_LOCAL_3;
    } else {
      defaultSuggestion = twoDimensional
          ? DEFAULT_WORLD_2
          : DEFAULT_WORLD_3;
    }

    suggestions.add(defaultSuggestion);

    return Completions.suggestCoordinates(builder, suggestions);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return vanillaType;
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}