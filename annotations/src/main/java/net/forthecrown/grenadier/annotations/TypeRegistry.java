package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Map;
import java.util.function.Supplier;
import net.forthecrown.grenadier.annotations.compiler.CompileContext;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.TypeInfoTree;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.types.ArgumentTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Argument type registry used to parse {@link ArgumentType} instances from
 * tokenized input.
 *
 * <table>
 * <caption>Built-in argument types</caption>
 *
 * <thead>
 *   <tr>
 *     <td>Type name</td>
 *     <td>ArgumentType</td>
 *     <td>Input Examples</td>
 *     <td>Code equivalent</td>
 *   </tr>
 * </thead>
 * <tbody>
 *   <tr>
 *     <td>Grenadier types</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>item</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.ItemArgument}</td>
 *     <td><code>argument('arg', item)</code></td>
 *     <td>{@link ArgumentTypes#item()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>item_filter</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.ItemFilterArgument}</td>
 *     <td><code>argument('arg', item_filter)</code></td>
 *     <td>{@link ArgumentTypes#itemFilter()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>block</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.BlockArgument}</td>
 *     <td><code>argument('arg', block)</code></td>
 *     <td>{@link ArgumentTypes#block()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>block_filter</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.BlockFilterArgument}</td>
 *     <td><code>argument('arg', block_filter)</code></td>
 *     <td>{@link ArgumentTypes#blockFilter()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>component</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.ComponentArgument}</td>
 *     <td><code>argument('arg', component)</code></td>
 *     <td>{@link ArgumentTypes#component()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>key</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.KeyArgument}</td>
 *     <td><code>argument('arg', key)</code></td>
 *     <td>{@link ArgumentTypes#key()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>vec3i</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.PositionArgument}</td>
 *     <td><code>argument('arg', vec3i)</code></td>
 *     <td>{@link ArgumentTypes#blockPosition()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>vec3d</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.PositionArgument}</td>
 *     <td><code>argument('arg', vec3d)</code></td>
 *     <td>{@link ArgumentTypes#position()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>vec2i</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.PositionArgument}</td>
 *     <td><code>argument('arg', vec2i)</code></td>
 *     <td>{@link ArgumentTypes#blockPosition2d()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>vec2d</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.PositionArgument}</td>
 *     <td><code>argument('arg', vec2d)</code></td>
 *     <td>{@link ArgumentTypes#position2d()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>world</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.WorldArgument}</td>
 *     <td><code>argument('arg', world)</code></td>
 *     <td>{@link ArgumentTypes#world()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>nbt</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.NbtArgument}</td>
 *     <td><code>argument('arg', nbt)</code></td>
 *     <td>{@link ArgumentTypes#binaryTag()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>compound_tag</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.NbtArgument}</td>
 *     <td><code>argument('arg', nbt)</code></td>
 *     <td>{@link ArgumentTypes#compoundTag()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>nbt_path</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.TagPathArgument}</td>
 *     <td><code>argument('arg', nbt_path)</code></td>
 *     <td>{@link ArgumentTypes#tagPath()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>gamemode</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.GameModeArgument}</td>
 *     <td><code>argument('arg', gamemode)</code></td>
 *     <td>{@link ArgumentTypes#gameMode()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>time</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.TimeArgument}</td>
 *     <td><code>argument('arg', time)</code></td>
 *     <td>{@link ArgumentTypes#time()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>date</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.LocalDateArgument}</td>
 *     <td><code>argument('arg', date)</code></td>
 *     <td>{@link ArgumentTypes#localDate()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>objective</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.ObjectiveArgument}</td>
 *     <td><code>argument('arg', objective)</code></td>
 *     <td>{@link ArgumentTypes#objective()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>team</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.TeamArgument}</td>
 *     <td><code>argument('arg', team)</code></td>
 *     <td>{@link ArgumentTypes#team()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>entities</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.EntityArgument}</td>
 *     <td><code>argument('arg', entities)</code></td>
 *     <td>{@link ArgumentTypes#entities()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>entity</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.EntityArgument}</td>
 *     <td><code>argument('arg', entity)</code></td>
 *     <td>{@link ArgumentTypes#entity()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>players</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.EntityArgument}</td>
 *     <td><code>argument('arg', players)</code></td>
 *     <td>{@link ArgumentTypes#players()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>player</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.EntityArgument}</td>
 *     <td><code>argument('arg', player)</code></td>
 *     <td>{@link ArgumentTypes#player()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>loottable</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.LootTableArgument}</td>
 *     <td><code>argument('arg', loottable)</code></td>
 *     <td>{@link ArgumentTypes#lootTable()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>particle</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.ParticleArgument}</td>
 *     <td><code>argument('arg', particle)</code></td>
 *     <td>{@link ArgumentTypes#particle()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>uuid</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.UuidArgument}</td>
 *     <td><code>argument('arg', uuid)</code></td>
 *     <td>{@link ArgumentTypes#uuid()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>int_range</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.IntRangeArgument}</td>
 *     <td><code>argument('arg', int_range)</code></td>
 *     <td>{@link ArgumentTypes#intRange()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>double_range</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.DoubleRangeArgument}</td>
 *     <td><code>argument('arg', double_range)</code></td>
 *     <td>{@link ArgumentTypes#doubleRange()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>map</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.MapArgument}</td>
 *     <td><code>argument('arg', map(values=@map_values))</code></td>
 *     <td>{@link ArgumentTypes#map(Map)}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>array</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.ArrayArgument}</td>
 *     <td><code>argument('arg', array(values=@arg_type))</code></td>
 *     <td>{@link ArgumentTypes#array(ArgumentType)}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>enum</code></td>
 *     <td>{@link net.forthecrown.grenadier.types.EnumArgument}</td>
 *     <td>
 *       <code>argument('arg', enum(type=me.example.EnumClass))</code>
 *       <br> or
 *       <code>argument('arg', enum(type=@enum_variable))</code>
 *     </td>
 *     <td>{@link ArgumentTypes#enumType(Class)}</td>
 *   </tr>
 *
 *   <tr>
 *     <td>Brigadier types</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>bool</code></td>
 *     <td>{@link BoolArgumentType}</td>
 *     <td><code>argument('arg', bool)</code></td>
 *     <td>{@link BoolArgumentType#bool()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>word</code></td>
 *     <td>{@link StringArgumentType}</td>
 *     <td><code>argument('arg', word)</code></td>
 *     <td>{@link StringArgumentType#word()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>string</code></td>
 *     <td>{@link StringArgumentType}</td>
 *     <td><code>argument('arg', string)</code></td>
 *     <td>{@link StringArgumentType#string()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>greedy_string</code></td>
 *     <td>{@link StringArgumentType}</td>
 *     <td><code>argument('arg', greedy_string)</code></td>
 *     <td>{@link StringArgumentType#greedyString()}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>int</code></td>
 *     <td>{@link IntegerArgumentType}</td>
 *     <td>
 *       <code>argument('arg', int(min=0, max=10))</code>
 *       <br> or
 *       <code>argument('arg', int(max=10))</code>
 *       <br> or
 *       <code>argument('arg', int(min=0))</code>
 *       <br> or
 *       <code>argument('arg', int)</code>
 *     </td>
 *     <td>{@link IntegerArgumentType#integer(int, int)}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>float</code></td>
 *     <td>{@link FloatArgumentType}</td>
 *     <td>
 *       <code>argument('arg', float(min=0, max=10))</code>
 *       <br> or
 *       <code>argument('arg', float(max=10))</code>
 *       <br> or
 *       <code>argument('arg', float(min=0))</code>
 *       <br> or
 *       <code>argument('arg', float)</code>
 *     </td>
 *     <td>{@link FloatArgumentType#floatArg(float, float)}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>double</code></td>
 *     <td>{@link DoubleArgumentType}</td>
 *     <td>
 *       <code>argument('arg', double(min=0, max=10))</code>
 *       <br> or
 *       <code>argument('arg', double(max=10))</code>
 *       <br> or
 *       <code>argument('arg', double(min=0))</code>
 *       <br> or
 *       <code>argument('arg', double)</code>
 *     </td>
 *     <td>{@link DoubleArgumentType#doubleArg(double, double)}</td>
 *   </tr>
 *
 *   <tr>
 *     <td><code>long</code></td>
 *     <td>{@link LongArgumentType}</td>
 *     <td>
 *       <code>argument('arg', long(min=0, max=10))</code>
 *       <br> or
 *       <code>argument('arg', long(max=10))</code>
 *       <br> or
 *       <code>argument('arg', long(min=0))</code>
 *       <br> or
 *       <code>argument('arg', long)</code>
 *     </td>
 *     <td>{@link LongArgumentType#longArg(long, long)}</td>
 *   </tr>
 *
 * </tbody>
 * </table>
 */
public interface TypeRegistry {

  /**
   * Creates a new type registry, all built in grenadier types are included
   * @return New type registry
   */
  static TypeRegistry newRegistry() {
    return new TypeRegistryImpl();
  }

  /**
   * Gets the global type registry
   * @return Global type registry
   */
  static TypeRegistry global() {
    return TypeRegistryImpl.GLOBAL;
  }

  /**
   * Gets a type parser via the specified {@code name}, the name is specified
   * like so: <pre>
   * argument('argName', greedy_string)
   *                     |-----------| Type name
   * </pre>
   *
   * @param name Type name
   * @return Found parser, or {@code null}, if no parser was found
   */
  TypeParser<?> getParser(String name);

  /**
   * Registers a new type parser
   * @param name Type name
   * @param parser Type parser
   * @param <T> Parser's type
   * @throws IllegalArgumentException If a type parser with the specified
   *                                  {@code name} is already registered
   */
  <T extends ArgumentType<?>> void register(String name, TypeParser<T> parser)
      throws IllegalArgumentException;

  /**
   * Registers a simple type parser
   * @param name Type name
   * @param supplier Type factory
   * @param <T> Parser's type
   * @throws IllegalArgumentException If a type parser with the specified
   *                                  {@code name} is already registered
   */
  default <T extends ArgumentType<?>> void register(
      String name,
      Supplier<T> supplier
  ) throws IllegalArgumentException {
    register(name, (info, context) -> Result.success(supplier.get()));
  }

  /**
   * Type parser that parses input given by a user and returns
   * an {@link ArgumentType}.
   *
   * @param <T> Argument type returned by the parser
   */
  @FunctionalInterface
  interface TypeParser<T extends ArgumentType<?>> {

    /**
     * Parses an argument type.
     *
     * @param info    Argument type info
     * @param context Command compilation context
     *
     * @return Parsed argument type
     */
    @NotNull Result<T> parse(TypeInfoTree info, CompileContext context);
  }
}