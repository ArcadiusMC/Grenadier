package net.forthecrown.grenadier.annotations;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.HashMap;
import java.util.Map;
import net.forthecrown.grenadier.types.ArgumentTypes;

class TypeRegistryImpl implements TypeRegistry {

  static final TypeRegistryImpl GLOBAL = new TypeRegistryImpl();

  private final Map<String, TypeParser> parsers = new HashMap<>();

  public TypeRegistryImpl() {
    registerBuiltIn();
  }

  private void registerBuiltIn() {
    // Grenadier types
    register("item",          ArgumentTypes::item);
    register("item_filter",   ArgumentTypes::itemFilter);
    register("key",           ArgumentTypes::key);
    register("block",         ArgumentTypes::block);
    register("block_filter",  ArgumentTypes::blockFilter);
    register("component",     ArgumentTypes::component);
    register("vec3i",         ArgumentTypes::blockPosition);
    register("vec3d",         ArgumentTypes::position);
    register("vec2i",         ArgumentTypes::blockPosition2d);
    register("vec2d",         ArgumentTypes::position2d);
    register("world",         ArgumentTypes::world);
    register("nbt",           ArgumentTypes::binaryTag);
    register("compound_nbt",  ArgumentTypes::compoundTag);
    register("nbt_path",      ArgumentTypes::tagPath);
    register("gamemode",      ArgumentTypes::gameMode);
    register("time",          ArgumentTypes::time);
    register("date",          ArgumentTypes::localDate);
    register("objective",     ArgumentTypes::objective);
    register("team",          ArgumentTypes::team);
    register("entity",        ArgumentTypes::entity);
    register("entities",      ArgumentTypes::entities);
    register("player",        ArgumentTypes::player);
    register("players",       ArgumentTypes::players);
    register("loottable",     ArgumentTypes::lootTable);
    register("particle",      ArgumentTypes::particle);
    register("uuid",          ArgumentTypes::uuid);
    register("int_range",     ArgumentTypes::intRange);
    register("double_range",  ArgumentTypes::doubleRange);
    register("map",           BuiltInTypeParsers.MAP);
    register("array",         BuiltInTypeParsers.ARRAY);
    register("enum",          BuiltInTypeParsers.ENUM);

    // Brigadier types
    register("bool",          BoolArgumentType::bool);
    register("word",          StringArgumentType::word);
    register("string",        StringArgumentType::string);
    register("greedy_string", StringArgumentType::greedyString);
    register("int",           BuiltInTypeParsers.INT);
    register("float",         BuiltInTypeParsers.FLOAT);
    register("long",          BuiltInTypeParsers.LONG);
    register("double",        BuiltInTypeParsers.DOUBLE);
  }

  @Override
  public TypeParser<?> getParser(String name) {
    return parsers.get(name);
  }

  @Override
  public <T extends ArgumentType<?>> void register(String name,
                                                   TypeParser<T> parser
  ) throws IllegalArgumentException {
    Preconditions.checkArgument(
        !parsers.containsKey(name),
        "Type '%s' is already registered", name
    );

    parsers.put(name, parser);
  }
}