package net.forthecrown.grenadier.annotations.tree;

import java.util.Map;
import net.forthecrown.grenadier.annotations.ParseExceptions;
import net.forthecrown.grenadier.annotations.Token;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ArgumentTypeRef extends Tree {


  record TypeInfoTree(String name, Map<String, Token> options)
      implements ArgumentTypeRef
  {

    public Token getOrThrow(String optionName, ParseExceptions exceptions) {
      var token = options.get(optionName);

      if (token == null) {
        throw exceptions.create(
            "Missing '%s' option from '%s' argument type info",
            optionName, name
        );
      }

      return token;
    }

    @Override
      public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
        return visitor.visitTypeInfo(this, context);
      }
  }

  record VariableTypeRef(String variable) implements ArgumentTypeRef {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableType(this, context);
    }
  }
}