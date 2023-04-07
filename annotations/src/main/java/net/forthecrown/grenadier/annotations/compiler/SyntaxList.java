package net.forthecrown.grenadier.annotations.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.SyntaxConsumer;
import net.kyori.adventure.text.Component;

class SyntaxList {

  private final List<SyntaxElement> elements = new ArrayList<>();

  public void add(String arguments,
                  Component info,
                  Predicate<CommandSource> predicate
  ) {
    SyntaxElement element = new SyntaxElement(arguments, info, predicate);
    elements.add(element);
  }

  public void consume(String commandName, SyntaxConsumer consumer) {
    for (SyntaxElement syntaxElement : elements) {
      consumer.accept(commandName,
          syntaxElement.arguments,
          syntaxElement.info,
          syntaxElement.predicate
      );
    }
  }

  @RequiredArgsConstructor
  class SyntaxElement {
    private final String arguments;
    private final Component info;
    private final Predicate<CommandSource> predicate;
  }
}