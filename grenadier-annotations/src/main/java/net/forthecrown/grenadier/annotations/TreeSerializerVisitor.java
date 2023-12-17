package net.forthecrown.grenadier.annotations;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.forthecrown.grenadier.annotations.tree.AbstractCmdTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.MemberMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.ResultMemberMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.VariableMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeTree.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeTree.VariableTypeReference;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.ArrayDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.LiteralDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.TranslatableDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.VariableDescription;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.MemberExecutes;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.LiteralTree;
import net.forthecrown.grenadier.annotations.tree.Name;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.FieldReferenceName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.ConstantRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.MemberRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.MemberSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;
import net.forthecrown.grenadier.annotations.tree.TransformTree.MemberTransform;
import net.forthecrown.grenadier.annotations.tree.TransformTree.VariableTransform;
import net.forthecrown.grenadier.annotations.tree.Tree;
import net.forthecrown.grenadier.annotations.tree.TreeVisitor;

public class TreeSerializerVisitor implements TreeVisitor<JsonElement, Void> {

  @Override
  public JsonElement visitLiteral(LiteralTree tree, Void unused) {
    JsonObject obj = createTreeObject(tree, "literal");
    obj.add("name", fromTree(tree.getName()));
    genericVisit(obj, tree);
    return obj;
  }

  @Override
  public JsonElement visitArgument(ArgumentTree tree, Void unused) {
    JsonObject obj = createTreeObject(tree, "argument");
    obj.add("name", fromTree(tree.getName()));
    obj.add("argument_type", fromTree(tree.getTypeInfo()));

    if (tree.getSuggests() != null) {
      obj.add("suggests", fromTree(tree.getSuggests()));
    }

    genericVisit(obj, tree);
    return obj;
  }

  @Override
  public JsonElement visitRoot(RootTree tree, Void unused) {
    JsonObject obj = createTreeObject(tree, "root");
    obj.add("name", fromTree(tree.getName()));

    if (tree.getPermission() != null) {
      obj.add("permission", fromTree(tree.getPermission()));
    }

    if (tree.getAliases() != null && !tree.getAliases().isEmpty()) {
      JsonArray array = new JsonArray();
      tree.getAliases().forEach(name -> {
        array.add(fromTree(name));
      });
      obj.add("aliases", array);
    }

    genericVisit(obj, tree);
    return obj;
  }

  private void genericVisit(JsonObject obj, AbstractCmdTree tree) {
    if (tree.getSyntaxLabel() != null) {
      obj.add("syntax_label", fromName(tree.getSyntaxLabel()));
    }

    if (tree.getDescription() != null) {
      JsonElement desc = fromTree(tree.getDescription());
      obj.add("description", desc);
    }

    if (tree.getExecutes() != null) {
      obj.add("executes", fromTree(tree.getExecutes()));
    }

    if (tree.getRequires() != null) {
      obj.add("requires", fromTree(tree.getRequires()));
    }

    if (!tree.getMappers().isEmpty()) {
      JsonArray mappers = new JsonArray();

      tree.getMappers().forEach(tree1 -> {
        mappers.add(fromTree(tree1));
      });

      obj.add("mappers", mappers);
    }

    if (!tree.getChildren().isEmpty()) {
      JsonArray children = new JsonArray();

      tree.getChildren().forEach(tree1 -> {
        children.add(fromTree(tree1));
      });

      obj.add("children", children);
    }
  }

  @Override
  public JsonElement visitStringSuggestions(StringListSuggestions tree,
                                            Void unused
  ) {
    JsonArray array = new JsonArray();
    for (String suggestion : tree.suggestions()) {
      array.add(suggestion);
    }

    return createTreeObject(tree, "string_array", array);
  }

  @Override
  public JsonElement visitMemberSuggestions(MemberSuggestions tree, Void unused
  ) {
    return createTreeObject(tree, "member_chain", tree.ref().path());
  }

  @Override
  public JsonElement visitVariableSuggests(VariableSuggestions tree,
                                           Void unused
  ) {
    return createTreeObject(tree, "variable", tree.variable());
  }

  @Override
  public JsonElement visitPermissionRequirement(PermissionRequires tree,
                                                Void unused
  ) {
    return createTreeObject(tree, "permission", fromName(tree.name()));
  }

  @Override
  public JsonElement visitMemberRequirement(MemberRequires tree, Void unused) {
    return createTreeObject(tree, "member_chain", tree.ref().path());
  }

  @Override
  public JsonElement visitVariableRequires(VariableRequires tree, Void unused) {
    return createTreeObject(tree, "variable", tree.variable());
  }

  @Override
  public JsonElement visitConstantRequires(ConstantRequires tree, Void unused) {
    return createTreeObject(tree, "const", tree.value());
  }

  @Override
  public JsonElement visitArgumentTypeTree(TypeInfoTree tree, Void unused) {
    var obj = createTreeObject(tree, "argument_type");
    obj.addProperty("name", tree.name());

    if (tree.options() != null && !tree.options().isEmpty()) {
      JsonObject optionsJson = new JsonObject();

      tree.options().forEach((s, token) -> {
        JsonObject tokenJson = new JsonObject();
        tokenJson.addProperty("type", token.type().toString());
        tokenJson.addProperty("position", token.position());

        if (!token.value().isEmpty()) {
          tokenJson.addProperty("value", token.value());
        }

        optionsJson.add(s, tokenJson);
      });

      obj.add("options", optionsJson);
    }

    return obj;
  }

  @Override
  public JsonElement visitVariableArgumentType(VariableTypeReference tree,
                                               Void unused
  ) {
    return createTreeObject(tree, "variable", tree.variable());
  }

  @Override
  public JsonElement visitVariableExecutes(VariableExecutes tree, Void unused) {
    return createTreeObject(tree, "variable", tree.variable());
  }

  @Override
  public JsonElement visitMemberExecutes(MemberExecutes tree, Void unused) {
    return createTreeObject(tree, "member_chain", tree.ref().path());
  }

  @Override
  public JsonElement visitVariableName(VariableName tree, Void unused) {
    return createTreeObject(tree, "variable", tree.variable());
  }

  @Override
  public JsonElement visitDirectName(DirectName tree, Void unused) {
    return createTreeObject(tree, "literal", tree.value());
  }

  @Override
  public JsonElement visitFieldName(FieldReferenceName tree, Void unused) {
    return createTreeObject(tree, "field", tree.fieldName());
  }

  @Override
  public JsonElement visitVariableMapper(VariableMapper tree, Void unused) {
    JsonObject obj = createTreeObject(tree, "variable", tree.variable());
    if (tree.argumentName() != null) {
      obj.add("argument_name", fromName(tree.argumentName()));
    }
    return obj;
  }

  @Override
  public JsonElement visitMemberMapper(MemberMapper tree, Void unused) {
    JsonObject obj = createTreeObject(tree, "member_chain", tree.ref().path());

    if (tree.argumentName() != null) {
      obj.add("argument_name", fromName(tree.argumentName()));
    }

    return obj;
  }

  @Override
  public JsonElement visitResultMemberMapper(ResultMemberMapper tree,
                                             Void unused
  ) {
    JsonObject obj = createTreeObject(tree, "result_member_chain", tree.ref().path());
    if (tree.argumentName() != null) {
      obj.add("argument_name", fromName(tree.argumentName()));
    }
    return obj;
  }

  private JsonElement fromName(Name name) {
    return fromTree(name);
  }

  @Override
  public JsonElement visitLiteralDescription(LiteralDescription tree,
                                             Void unused
  ) {
    return createTreeObject(tree, "literal", tree.value());
  }

  @Override
  public JsonElement visitVariableDescription(VariableDescription tree,
                                              Void unused
  ) {
    return createTreeObject(tree, "variable", tree.variable());
  }

  @Override
  public JsonElement visitTranslatableDescription(TranslatableDescription tree,
                                                  Void unused
  ) {
    return createTreeObject(tree, "translatable", tree.translationKey());
  }

  @Override
  public JsonElement visitArrayDescription(ArrayDescription tree, Void unused) {
    JsonObject obj = createTreeObject(tree, "array");
    JsonArray array = new JsonArray();

    for (DescriptionTree element : tree.elements()) {
      JsonElement json = element.accept(this, unused);
      array.add(json);
    }

    obj.add("value", array);
    return obj;
  }

  @Override
  public JsonElement visitMemberTransform(MemberTransform tree, Void unused) {
    return createTreeObject(tree, "member_transform", tree.tree().path());
  }

  @Override
  public JsonElement visitVariableTransform(VariableTransform tree, Void unused) {
    return createTreeObject(tree, "variable_transform", tree.variable());
  }

  private JsonObject createTreeObject(Tree tree, String typeName, Object value) {
    var obj = createTreeObject(tree, typeName);

    if (value instanceof JsonElement element) {
      obj.add("value", element);
    } else if (value instanceof Number n) {
      obj.addProperty("value", n);
    } else if (value instanceof Boolean b) {
      obj.addProperty("value", b);
    } else if (value == null) {
      obj.add("value", JsonNull.INSTANCE);
    } else {
      obj.addProperty("value", String.valueOf(value));
    }

    return obj;
  }

  private JsonObject createTreeObject(Tree tree, String typeName) {
    JsonObject obj = createTreeObject(tree);
    obj.addProperty("type", typeName);
    return obj;
  }

  private JsonObject createTreeObject(Tree tree) {
    JsonObject object = new JsonObject();
    object.addProperty("tokenStart", tree.tokenStart());
    return object;
  }

  private JsonElement fromTree(Tree tree) {
    return tree.accept(this, null);
  }
}