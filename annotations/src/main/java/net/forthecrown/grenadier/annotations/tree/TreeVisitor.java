package net.forthecrown.grenadier.annotations.tree;

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
public interface TreeVisitor<R, C> {

  R visitLiteral(LiteralTree tree, C c);

  R visitArgument(ArgumentTree tree, C c);

  R visitRoot(RootTree tree, C c);

  R visitStringSuggestions(StringListSuggestions tree, C c);

  R visitRefSuggestions(ComponentRefSuggestions tree, C c);

  R visitVariableSuggests(VariableSuggestions tree, C c);

  R visitPermissionRequirement(PermissionRequires tree, C c);

  R visitRefRequires(RequiresRef tree, C c);

  R visitVariableRequires(VariableRequires tree, C c);

  R visitConstantRequires(ConstantRequires tree, C c);

  R visitTypeInfo(TypeInfoTree tree, C c);

  R visitVariableType(VariableTypeRef tree, C c);

  R visitVarExec(VariableExecutes tree, C c);

  R visitRefExec(RefExecution tree, C c);

  R visitVariableName(VariableName tree, C c);

  R visitDirectName(DirectName tree, C c);

  R visitFieldName(FieldRefName tree, C c);

  R visitVarModifier(VariableMapper tree, C c);

  R visitRefModifier(RefMapper tree, C c);

  R visitResultInvokeModifier(InvokeResultMethod tree, C c);
}