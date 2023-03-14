package net.forthecrown.grenadier;

/**
 * A functional interface denoting an object that takes a literal argument
 * builder and builds a tree on that builder
 */
@FunctionalInterface
public interface CommandTreeBuilder {

  /**
   * Creates a command node tree for the specified {@code command}
   * @param command Command to create the node tree for
   */
  void createCommand(GrenadierCommand command);
}