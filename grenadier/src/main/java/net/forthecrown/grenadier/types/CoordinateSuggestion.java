package net.forthecrown.grenadier.types;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;

public interface CoordinateSuggestion {
  Component tooltip();

  String x();

  String y();

  String z();

  default boolean isTwoDimensional() {
    return y() == null || y().isBlank();
  }

  void applySuggestions(SuggestionsBuilder builder);
}