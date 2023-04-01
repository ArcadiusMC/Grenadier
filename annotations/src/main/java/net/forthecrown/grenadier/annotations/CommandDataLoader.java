package net.forthecrown.grenadier.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.forthecrown.grenadier.annotations.util.Utils;

public interface CommandDataLoader {
  String getString(String path)
      throws IOException;

  static CommandDataLoader resources(ClassLoader loader) {
    return path -> {
      InputStream stream = loader.getResourceAsStream(path);
      return Utils.readStream(stream);
    };
  }

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