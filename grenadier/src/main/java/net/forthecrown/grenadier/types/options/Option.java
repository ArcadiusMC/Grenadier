package net.forthecrown.grenadier.types.options;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public sealed interface Option
    extends Predicate<CommandSource>
    permits ArgumentOption, FlagOption
{
  List<String> getLabels();

  Predicate<CommandSource> getCondition();

  Component getTooltip();

  @Override
  default boolean test(CommandSource source) {
    return getCondition().test(source);
  }

  static boolean isValidLabel(String s) {
    return Readers.WORD_PATTERN.matcher(s).matches();
  }

  static void validateLabels(Collection<String> labels) {
    Preconditions.checkArgument(
        !labels.isEmpty(),
        "Empty labels, expected at least 1 label"
    );

    labels.forEach(s -> {
      Preconditions.checkArgument(
          isValidLabel(s),
          "Invalid label '%s', must match pattern %s",
          s, Readers.WORD_PATTERN.pattern()
      );
    });
  }

  interface OptionBuilder<T extends OptionBuilder<T>> {

    List<String> getLabels();

    Predicate<CommandSource> getCondition();

    T setCondition(@NotNull Predicate<CommandSource> predicate);

    Component getTooltip();

    T setTooltip(Component component);

    default T setLabels(String... labels) {
      Preconditions.checkArgument(labels.length > 1,
          "Expected 1 or more labels, found 0"
      );

      getLabels().clear();
      getLabels().addAll(Arrays.asList(labels));

      return (T) this;
    }

    default T addLabel(String label) {
      getLabels().add(label);
      return (T) this;
    }

    default T setPermission(Permission permission) {
      return setCondition(source -> source.hasPermission(permission));
    }

    default T setPermission(String permission) {
      return setCondition(source -> source.hasPermission(permission));
    }
  }
}