package net.forthecrown.grenadier.types;

import net.forthecrown.grenadier.internal.CoordinateSuggestionImpl;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public final class CoordinateSuggestions {
  private CoordinateSuggestions() {}

  public static CoordinateSuggestion create(String x, String y, String z) {
    return create(null, x, y, z);
  }

  public static CoordinateSuggestion create(String x, String z) {
    return create(null, x, null, z);
  }

  public static CoordinateSuggestion create(@Nullable Component tooltip,
                                            String x,
                                            String z
  ) {
    return create(tooltip, x, null, z);
  }

  public static CoordinateSuggestion create(@Nullable Component tooltip,
                                            String x,
                                            String y,
                                            String z
  ) {
    return new CoordinateSuggestionImpl(tooltip, x, y, z);
  }


  public static CoordinateSuggestion create(double x, double y, double z) {
    return create(null, toString(x), toString(y), toString(z));
  }

  public static CoordinateSuggestion create(double x, double z) {
    return create(null, toString(x), null, toString(z));
  }

  public static CoordinateSuggestion create(@Nullable Component tooltip,
                                            double x,
                                            double z
  ) {
    return create(tooltip, toString(x), null, toString(z));
  }

  public static CoordinateSuggestion create(@Nullable Component tooltip,
                                            double x,
                                            double y,
                                            double z
  ) {
    return create(tooltip, toString(x), toString(y), toString(z));
  }

  private static String toString(double x) {
    return String.format("%.2f", x);
  }
}