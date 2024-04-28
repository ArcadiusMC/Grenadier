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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.kyori.adventure.util.Ticks;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Parses a duration
 * <p>
 * Input examples: <pre>
 * 1.02s
 * 1.45seconds
 * 2.52days
 * 12months
 * 1000 (no time unit given, treated as milliseconds)
 * 3days+1h+20m+20s (Values are all added together)
 * 5h-20min
 * 4h;20min (';' == '+')
 * </pre>
 */
public interface TimeArgument extends ArgumentType<Duration> {

  @Override
  Duration parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  /**
   * Converts a duration into a parse-able string
   * @param duration Duration to convert
   * @return Parse-able time string
   */
  static String toString(@NotNull Duration duration) {
    Objects.requireNonNull(duration, "Null duration");

    if (duration.isZero()) {
      return "0";
    }

    StringBuilder builder = new StringBuilder();
    boolean negative = duration.isNegative();
    long millis = Math.abs(duration.toMillis());

    for (Unit unit : Unit.values()) {
      long unitValue = unit.getMillis();

      if (unitValue > millis) {
        continue;
      }

      long value = millis / unitValue;
      long remainder = millis % unitValue;

      if (!builder.isEmpty()) {
        builder.append(negative ? '-' : '+');
      } else if (negative) {
        builder.append("-");
      }

      builder.append(value);
      builder.append(unit.getStrings().iterator().next());

      millis = remainder;
    }

    return builder.toString();
  }

  /**
   * All supported time units that can be parsed
   */
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

    private static final Unit[] REVERSE_VALUES;
    public static final Map<String, Unit> LOOKUP;

    static {
      Map<String, Unit> map = new HashMap<>();

      for (var v: values()) {
        v.strings.forEach(s -> map.put(s, v));
      }

      LOOKUP = Collections.unmodifiableMap(map);

      REVERSE_VALUES = values();
      ArrayUtils.reverse(REVERSE_VALUES);
    }

    private final long millis;
    private final List<String> strings;

    Unit(long millis, String... strings) {
      this.millis = millis;
      this.strings = List.of(strings);
    }
  }
}