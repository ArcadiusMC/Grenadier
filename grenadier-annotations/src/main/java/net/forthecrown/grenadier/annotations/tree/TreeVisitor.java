package net.forthecrown.grenadier.annotations.tree;

import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.ResultMemberMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.MemberMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.VariableMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeTree.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeTree.VariableTypeReference;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.ArrayDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.LiteralDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.TranslatableDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.VariableDescription;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.MemberExecutes;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.FieldReferenceName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.ConstantRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.MemberRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.MemberSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;
import net.forthecrown.grenadier.annotations.tree.TransformTree.MemberTransform;
import net.forthecrown.grenadier.annotations.tree.TransformTree.VariableTransform;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface TreeVisitor<R, C> {

  /* ------------------------------- NODES -------------------------------- */

  R visitLiteral(LiteralTree tree, C c);

  R visitArgument(ArgumentTree tree, C c);

  R visitRoot(RootTree tree, C c);

  /* ---------------------------- SUGGESTIONS ----------------------------- */

  R visitStringSuggestions(StringListSuggestions tree, C c);

  R visitMemberSuggestions(MemberSuggestions tree, C c);

  R visitVariableSuggests(VariableSuggestions tree, C c);

  /* --------------------------- REQUIREMENTS ----------------------------- */

  R visitPermissionRequirement(PermissionRequires tree, C c);

  R visitMemberRequirement(MemberRequires tree, C c);

  R visitVariableRequires(VariableRequires tree, C c);

  R visitConstantRequires(ConstantRequires tree, C c);

  /* ------------------------- ARGUMENT TYPE INFO ------------------------- */

  R visitArgumentTypeTree(TypeInfoTree tree, C c);

  R visitVariableArgumentType(VariableTypeReference tree, C c);

  /* ----------------------------- EXECUTIONS ----------------------------- */

  R visitVariableExecutes(VariableExecutes tree, C c);

  R visitMemberExecutes(MemberExecutes tree, C c);

  /* ------------------------------- NAMES -------------------------------- */

  R visitVariableName(VariableName tree, C c);

  R visitDirectName(DirectName tree, C c);

  R visitFieldName(FieldReferenceName tree, C c);

  /* --------------------------- RESULT MAPPERS --------------------------- */

  R visitVariableMapper(VariableMapper tree, C c);

  R visitMemberMapper(MemberMapper tree, C c);

  R visitResultMemberMapper(ResultMemberMapper tree, C c);

  /* ---------------------------- DESCRIPTION ----------------------------- */

  R visitLiteralDescription(LiteralDescription tree, C c);

  R visitVariableDescription(VariableDescription tree, C c);

  R visitTranslatableDescription(TranslatableDescription tree, C c);

  R visitArrayDescription(ArrayDescription tree, C c);

  R visitMemberTransform(MemberTransform tree, C c);

  R visitVariableTransform(VariableTransform tree, C c);
}