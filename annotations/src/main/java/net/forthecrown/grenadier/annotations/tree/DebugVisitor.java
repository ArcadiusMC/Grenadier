package net.forthecrown.grenadier.annotations.tree;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.InvokeResultMethod;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.RefMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.VariableMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.VariableTypeRef;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.RefExecution;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.FieldRefName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.ConstantRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.RequiresRef;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.ComponentRefSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Getter
@RequiredArgsConstructor
public class DebugVisitor implements TreeVisitor<Void, Void> {
  static final char NL = '\n';

  final StringBuilder builder;

  int indent = 0;

  private void appendRef(MemberChainTree ref) {
    ref.appendTo(builder);
  }

  @Override
  public Void visitStringSuggestions(StringListSuggestions tree, Void unused) {
    builder.append("suggest('");
    builder.append(Joiner.on("', '").join(tree.suggestions()));
    builder.append("')");

    return null;
  }

  @Override
  public Void visitRefSuggestions(ComponentRefSuggestions tree, Void unused) {
    appendRef(tree.ref());
    return null;
  }

  @Override
  public Void visitVariableSuggests(VariableSuggestions tree, Void unused) {
    appendVar(tree);
    return null;
  }

  private void appendVar(VariableHolder holder) {
    builder.append("@").append(holder.variable());
  }

  @Override
  public Void visitPermissionRequirement(PermissionRequires tree, Void unused) {
    builder.append("permission(");
    tree.name().accept(this, unused);
    builder.append(")");
    return null;
  }

  @Override
  public Void visitRefRequires(RequiresRef tree, Void unused) {
    appendRef(tree.ref());
    return null;
  }

  @Override
  public Void visitVariableRequires(VariableRequires tree, Void unused) {
    appendVar(tree);
    return null;
  }

  @Override
  public Void visitConstantRequires(ConstantRequires tree, Void unused) {
    builder.append(tree.value());
    return null;
  }

  @Override
  public Void visitLiteral(LiteralTree tree, Void unused) {
    appendIndent();

    builder.append("literal(");
    tree.getName().accept(this, unused);
    builder.append(") {");

    builder.append(NL);

    indent++;
    genericVisit(tree);
    indent--;

    appendIndent();
    builder.append('}');

    return null;
  }

  @Override
  public Void visitTypeInfo(TypeInfoTree tree, Void unused) {
    builder.append(tree.name());

    if (tree.options() != null) {
      builder.append("(");
      var it = tree.options().entrySet().iterator();

      while (it.hasNext()) {
        var e = it.next();

        builder.append(e.getKey());
        builder.append(" = ");
        builder.append(e.getValue());

        if (it.hasNext()) {
          builder.append(", ");
        }
      }

      builder.append(")");
    }

    return null;
  }

  @Override
  public Void visitVariableType(VariableTypeRef tree, Void unused) {
    builder.append('@').append(tree.variable());
    return null;
  }

  @Override
  public Void visitVarExec(VariableExecutes tree, Void unused) {
    appendVar(tree);
    return null;
  }

  @Override
  public Void visitRefExec(RefExecution tree, Void unused) {
    appendRef(tree.ref());
    return null;
  }

  @Override
  public Void visitArgument(ArgumentTree tree, Void unused) {
    appendIndent();

    builder.append("argument(");

    tree.getName().accept(this, unused);

    builder.append(", ");

    tree.getTypeInfo().accept(this, unused);

    builder.append(") {").append(NL);
    ++indent;

    if (tree.getSuggests() != null) {
      appendIndent();
      builder.append("suggests = ");
      tree.getSuggests().accept(this, unused);
      builder.append(NL);
    }

    genericVisit(tree);

    --indent;
    appendIndent();
    builder.append("}");

    return null;
  }

  @Override
  public Void visitRoot(RootTree tree, Void unused) {
    builder.append("name = ");
    tree.getName().accept(this, unused);
    builder.append(NL);

    if (tree.getPermission() != null) {
      builder.append("permission = ");
      tree.getPermission().accept(this, unused);
      builder.append(NL);
    }

    if (tree.getDescription() != null) {
      builder.append("description = '")
          .append(tree.getDescription())
          .append("'")
          .append(NL);
    }

    if (tree.getAliases() != null) {
      builder.append("aliases = ");

      var it = tree.getAliases().iterator();

      while (it.hasNext()) {
        it.next().accept(this, unused);

        if (it.hasNext()) {
          builder.append(" | ");
        }
      }

      builder.append(NL);
    }

    genericVisit(tree);
    return null;
  }

  private void genericVisit(AbstractCmdTree tree) {
    if (tree.getExecutes() != null) {
      appendIndent();
      builder.append("executes = ");
      tree.getExecutes().accept(this, null);

      builder.append(NL);
    }

    if (tree.getRequires() != null) {
      appendIndent();

      builder.append("requires = ");
      tree.getRequires().accept(this, null);

      builder.append(NL);
    }

    visitChildren(tree);
  }

  private void visitChildren(AbstractCmdTree tree) {
    var children = tree.getChildren();

    for (var c: children) {
      builder.append(NL);
      c.accept(this, null);
      builder.append(NL);
    }
  }


  @Override
  public Void visitDirectName(DirectName tree, Void unused) {
    builder
        .append("'")
        .append(tree.value())
        .append("'");

    return null;
  }

  @Override
  public Void visitVariableName(VariableName tree, Void unused) {
    appendVar(tree);
    return null;
  }

  @Override
  public Void visitFieldName(FieldRefName tree, Void unused) {
    builder.append(tree.fieldName());
    return null;
  }

  @Override
  public Void visitVarModifier(VariableMapper tree, Void unused) {
    appendModifierPrefix(tree);
    appendVar(tree);

    return null;
  }

  @Override
  public Void visitRefModifier(RefMapper tree, Void unused) {
    appendModifierPrefix(tree);
    tree.ref().appendTo(builder);
    return null;
  }

  @Override
  public Void visitResultInvokeModifier(InvokeResultMethod tree, Void unused) {
    appendModifierPrefix(tree);
    builder.append("result.");
    tree.ref().appendTo(builder);
    return null;
  }

  private void appendModifierPrefix(ArgumentMapperTree tree) {
    builder.append("map_type");

    if (tree.argumentName() != null) {
      builder.append("(");
      tree.argumentName().accept(this, null);
      builder.append(")");
    }

    builder.append(" = ");
  }

  private void appendIndent() {
    builder.append("  ".repeat(indent));
  }
}