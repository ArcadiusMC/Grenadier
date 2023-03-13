package net.forthecrown.grenadier;

import com.mojang.brigadier.StringReader;

/**
 * An interface that handles exceptions when they're thrown during command
 * execution or during suggestion creation
 */
public interface CommandExceptionHandler {

  /**
   * Handles an exception thrown during command execution
   *
   * @param input input
   * @param throwable thrown exception
   * @param source source executing the command
   */
  void onCommandException(
      StringReader input,
      Throwable throwable,
      CommandSource source
  );

  /**
   * Handles an exception thrown during command suggestion creation
   *
   * @param input current input
   * @param throwable thrown exception
   * @param source source executing the command
   */
  void onSuggestionException(
      String input,
      Throwable throwable,
      CommandSource source
  );
}