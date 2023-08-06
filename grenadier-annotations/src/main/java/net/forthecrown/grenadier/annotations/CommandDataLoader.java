package net.forthecrown.grenadier.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads a command data input string from a 'file = file/path.txt' value in the
 * {@link CommandData} annotation
 */
public interface CommandDataLoader {

  /**
   * Gets a string input from the specified {@code path}
   *
   * @param path String path to command data resource
   * @return Command data string, or {@code null}, if the resource the path
   *         leads to doesn't exist
   * @throws IOException If an error occurred during input loading
   */
  String getString(String path) throws IOException;

  /**
   * Creates a loader that will load input from the specified {@code loader}'s
   * resources
   *
   * @param loader Class loader
   * @return Created loader
   */
  static CommandDataLoader resources(ClassLoader loader) {
    return path -> {
      InputStream stream = loader.getResourceAsStream(path);
      if (stream == null) {
        return null;
      }

      InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
      StringWriter writer = new StringWriter();

      reader.transferTo(writer);
      reader.close();

      return writer.toString();
    };
  }

  /**
   * Creates a data loader that loads data from the specified {@code dir}
   * @param dir Directory to load data from
   * @return Created loader
   */
  static CommandDataLoader directory(Path dir) {
    return path -> {
      Path filePath = dir.resolve(path);

      if (!Files.exists(filePath)) {
        return null;
      }

      return Files.readString(filePath, StandardCharsets.UTF_8);
    };
  }
}