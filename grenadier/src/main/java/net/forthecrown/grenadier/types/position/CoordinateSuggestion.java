package net.forthecrown.grenadier.types.position;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;

public interface CoordinateSuggestion {
  Component tooltip();

  String x();

  String y();

  String z();

  default boolean isTwoDimensional() {
    return z() == null || z().isBlank();
  }

  void applySuggestions(SuggestionsBuilder builder);
}