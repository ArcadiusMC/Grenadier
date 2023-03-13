package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.kyori.adventure.util.Ticks;

public interface TimeArgument extends ArgumentType<Duration> {

  @Override
  Duration parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  @Getter
  enum Unit {
    YEARS (
        TimeUnit.DAYS.toMillis(365),
        "yr", "year", "years", "yrs"
    ),

    MONTHS (
        TimeUnit.DAYS.toMillis(7 * 4),
        "mo", "month", "months"
    ),

    WEEKS (
        TimeUnit.DAYS.toMillis(7),
        "w", "week", "weeks"
    ),

    DAYS (
        TimeUnit.DAYS.toMillis(1),
        "d", "day", "days"
    ),

    HOURS (
        TimeUnit.HOURS.toMillis(1),
        "h", "hour", "hours"
    ),

    MINUTES (
        TimeUnit.MINUTES.toMillis(1),
        "m", "min", "mins", "minute", "minutes"
    ),

    SECONDS (
        TimeUnit.SECONDS.toMillis(1),
        "s", "sec", "secs", "second", "seconds"
    ),

    TICKS (
        Ticks.SINGLE_TICK_DURATION_MS,
        "t", "tick", "ticks"
    ),

    MILLIS (
        1,
        "mil", "millis", "millisecond", "milliseconds"
    );

    public static final Map<String, Unit> LOOKUP;

    static {
      Map<String, Unit> map = new HashMap<>();

      for (var v: values()) {
        v.strings.forEach(s -> map.put(s, v));
      }

      LOOKUP = Collections.unmodifiableMap(map);
    }

    private final long millis;
    private final List<String> strings;

    Unit(long millis, String... strings) {
      this.millis = millis;
      this.strings = List.of(strings);
    }
  }
}