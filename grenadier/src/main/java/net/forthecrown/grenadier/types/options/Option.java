package net.forthecrown.grenadier.types.options;

import java.util.Objects;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An option within an {@link OptionsArgument}
 */
public interface Option extends Predicate<CommandSource> {

  /**
   * Gets the option's label.
   * <p>
   * This label will be used to parse the option
   *
   * @return Option's label
   */
  @NotNull
  String getLabel();

  /**
   * Gets the condition that must be passed in order to use this option and to
   * later access it in {@link ParsedOptions}
   * @return Use condition
   * @see ParsedOptions#getValue(ArgumentOption, CommandSource)
   */
  @NotNull
  Predicate<CommandSource> getCondition();

  /**
   * Gets the tooltip shown when hovering over the option's labels in command
   * suggestions
   *
   * @return Suggestion tooltip
   */
  Component getTooltip();

  /**
   * Tests if the {@code source} passes the {@link #getCondition()} test
   *
   * @param source the input argument
   * @return {@code true} if {@link #getCondition()} returns true, {@code false}
   *         otherwise
   */
  @Override
  default boolean test(CommandSource source) {
    return getCondition().test(source);
  }

  /**
   * Option builder
   * @param <T> Options builder type
   */
  interface OptionBuilder<T extends OptionBuilder<T>> {

    /**
     * Sets this option's labels
     * @param labels Option labels
     * @return this
     * @throws IllegalArgumentException If any of the labels failed the
     *                                  {@link Options#validateLabel(String)}
     *                                  check
     */
    T setLabel(String label) throws IllegalArgumentException;

    /**
     * Sets the option's use condition
     * @param condition Use condition
     * @return this
     */
    T setCondition(@NotNull Predicate<CommandSource> condition);

    /**
     * Sets the option's use condition to be a permission check for the
     * specified {@code permission}
     *
     * @param permission Permission to check for
     * @return this
     */
    default T setPermission(@NotNull String permission) {
      Objects.requireNonNull(permission, "Null Permission");
      return setCondition(source -> source.hasPermission(permission));
    }

    /**
     * Sets the option's use condition to be a permission check for the
     * specified {@code permission}
     *
     * @param permission Permission to check for
     * @return this
     */
    default T setPermission(@NotNull Permission permission) {
      Objects.requireNonNull(permission, "Null Permission");
      return setCondition(source -> source.hasPermission(permission));
    }

    /**
     * Sets the tooltip shown when hovering over the option's name in
     * suggestions
     *
     * @param tooltip Suggestion tooltip
     * @return this
     */
    T setTooltip(@Nullable Component tooltip);
  }
}