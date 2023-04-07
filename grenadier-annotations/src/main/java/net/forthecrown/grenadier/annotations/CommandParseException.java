package net.forthecrown.grenadier.annotations;

/**
 * Exception thrown when an error occurs during command data parsing
 */
public class CommandParseException extends RuntimeException {

  public CommandParseException(String message) {
    super(message);
  }
}