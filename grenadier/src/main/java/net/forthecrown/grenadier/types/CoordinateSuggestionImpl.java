package net.forthecrown.grenadier.types;

import static net.forthecrown.grenadier.types.PositionArgumentImpl.FLAG_2D;

import com.google.common.base.Strings;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.kyori.adventure.text.Component;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
class CoordinateSuggestionImpl implements CoordinateSuggestion {

  private static final Predicate<String> VALIDATOR_3 = coordinateValidator(3);
  private static final Predicate<String> VALIDATOR_2 = coordinateValidator(2);

  private final Component tooltip;
  private final String x, y, z;

  private static Predicate<String> coordinateValidator(final int coordinates) {
    return InternalUtil.createPredicate(reader -> {
      byte flags = coordinates == 2 ? FLAG_2D : 0;

      PositionParser parser = new PositionParser(reader, flags);
      parser.parse();

      return true;
    });
  }

  @Override
  public void applySuggestions(SuggestionsBuilder builder) {
    var input = builder.getRemainingLowerCase();

    Message tooltip = this.tooltip != null
        ? Grenadier.toMessage(this.tooltip)
        : null;

    if (isTwoDimensional()) {
      apply2(builder, tooltip, input);
    } else {
      apply3(builder, tooltip ,input);
    }
  }

  private void apply3(SuggestionsBuilder builder,
                      Message tooltip,
                      String remaining
  ) {
    if (Strings.isNullOrEmpty(remaining)) {
      String str = x + " " + y + " " + z;

      if (VALIDATOR_3.test(str)) {
        builder.suggest(str, tooltip);
      }

      return;
    }

    String[] split = remaining.split("\\s+");

    if (split.length == 1) {
      String first = inputMatchingArgument(split[0], x);
      String str = first + " " + y + " " + z;

      if (VALIDATOR_3.test(str)) {
        builder.suggest(first, tooltip);
        builder.suggest(first + " " + y, tooltip);
        builder.suggest(str, tooltip);
      }

      return;
    }

    if (split.length == 2) {
      String first = inputMatchingArgument(split[0], x);
      String second = inputMatchingArgument(split[1], y);
      String str = first + " " + second + " " + z;

      if (VALIDATOR_3.test(str)) {
        builder.suggest(first + " " + second, tooltip);
        builder.suggest(str, tooltip);
      }

      return;
    }

    String first = inputMatchingArgument(split[0], x);
    String second = inputMatchingArgument(split[1], y);
    String third = inputMatchingArgument(split[2], z);

    String str = first + " " + second + " " + third;

    if (VALIDATOR_3.test(str)) {
      builder.suggest(str, tooltip);
    }
  }

  private void apply2(SuggestionsBuilder builder,
                      Message tooltip,
                      String remaining
  ) {
    if (Strings.isNullOrEmpty(remaining)) {
      String str = x + " " + z;

      if (VALIDATOR_2.test(str)) {
        builder.suggest(str, tooltip);
      }

      return;
    }

    String[] split = remaining.split("\\s+");

    if (split.length == 1) {
      String first = inputMatchingArgument(split[0], x);
      String str = first + " " + y;

      if (VALIDATOR_2.test(str)) {
        builder.suggest(first, tooltip);
        builder.suggest(str, tooltip);
      }

      return;
    }

    String first = inputMatchingArgument(split[0], x);
    String second = inputMatchingArgument(split[1], y);
    String str = first + " " + second;

    if (VALIDATOR_2.test(str)) {
      builder.suggest(str, tooltip);
    }
  }

  private String inputMatchingArgument(String remain, String coordinate) {
    return Completions.matches(remain, coordinate) ? coordinate : remain;
  }

  @Override
  public String toString() {
    return isTwoDimensional()
        ? x + " " + z
        : x + " " + y + " " + z;
  }
}