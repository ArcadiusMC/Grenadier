package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.StringReader;
import java.io.Reader;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@Internal
public class StringReaderWrapper extends Reader {

  private final StringReader reader;
  private int mark = 0;

  public StringReaderWrapper(StringReader reader) {
    this.reader = Objects.requireNonNull(reader);
  }

  public StringReader getReader() {
    return reader;
  }

  @Override
  public int read(@NotNull char[] cbuf, int off, int len) {
    if (!reader.canRead()) {
      return -1;
    }

    int res = 0;
    for (int i = 0; i < len; i++) {
      if (!reader.canRead()) {
        break;
      }

      cbuf[i + off] = reader.read();
      res++;
    }

    return res;
  }

  @Override
  public int read() {
    return reader.canRead() ? reader.read() : -1;
  }

  @Override
  public void mark(int readAheadLimit) {
    this.mark = reader.getCursor();
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public void reset() {
    reader.setCursor(mark);
  }

  @Override
  public void close() {
  }
}