package net.forthecrown.grenadier.internal.types.pos;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.position.CoordinateSuggestion;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider.TextCoordinates;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class CoordinateSuggestionImpl implements CoordinateSuggestion {

  private final Component tooltip;
  private final String x, y, z;

  private TextCoordinates toVanilla() {
    return isTwoDimensional()
        ? new TextCoordinates(x, "", y)
        : new TextCoordinates(x,  y, z);
  }

  @Override
  public void applySuggestions(SuggestionsBuilder builder) {
    SuggestionsBuilder tempBuilder
        = new SuggestionsBuilder(builder.getInput(), builder.getStart());

    var vanilla = toVanilla();
    var input = builder.getRemainingLowerCase();

    if (isTwoDimensional()) {
      SharedSuggestionProvider.suggest2DCoordinates(
          input, Collections.singleton(vanilla), tempBuilder, s -> true
      );
    } else {
      SharedSuggestionProvider.suggestCoordinates(
          input, Collections.singleton(vanilla), tempBuilder, s -> true
      );
    }

    var suggestions = tempBuilder.build().getList();
    suggestions.forEach(suggestion -> {
      builder.suggest(
          suggestion.getText(),
          tooltip == null
              ? null
              : Grenadier.toMessage(tooltip)
      );
    });
  }
}