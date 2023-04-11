package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.annotations.util.ExpandedCommandContext;
import org.jetbrains.annotations.Nullable;

public interface ParamFiller {

  ParamFiller CONTEXT = (context, builder) -> context;
  ParamFiller SOURCE = (context, builder)  -> context.getSource();
  ParamFiller BUILDER = (context, builder) -> builder;

  Object getValue(
      ExpandedCommandContext context,
      @Nullable SuggestionsBuilder builder
  );

  record ArgumentFiller(String name, boolean optional, Class<?> type)
      implements ParamFiller
  {

    @Override
    public Object getValue(
        ExpandedCommandContext context,
        @Nullable SuggestionsBuilder builder
    ) {
      return context.getValue(name, type, optional);
    }
  }
}