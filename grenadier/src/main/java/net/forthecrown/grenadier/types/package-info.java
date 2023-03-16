/**
 * Contains the various built-in argument types Grenadier has.
 * <p>
 * Most argument types have a vanilla counterpart that the API types are mapped
 * to. Grenadier does also feature several argument types that have no vanilla
 * counterpart and have more general use cases.
 *
 * <table>
 * <caption>General use argument types</caption>
 * <thead>
 *   <tr><th>Argument type</th><th>Description</th><th>Example</th></tr>
 * </thead>
 * <tbody>
 * <tr>
 *   <td>{@link net.forthecrown.grenadier.types.MapArgument}</td>
 *   <td>Takes a string 2 object map and parses a map value via its string key</td>
 *   <td><code>a_value</code></td>
 * </tr>
 * <tr>
 *   <td>{@link net.forthecrown.grenadier.types.EnumArgument}</td>
 *   <td>Parses an enum constant from it's {@link java.lang.Enum#name()}</td>
 *   <td><code>red</code></td>
 * </tr>
 * <tr>
 *   <td>{@link net.forthecrown.grenadier.types.ArrayArgument}</td>
 *   <td>Parses a {@link java.util.List} of a specified type of arguments</td>
 *   <td><code>minecraft:stone,minecraft:oak_log</code></td>
 * </tr>
 * <tr>
 *   <td>{@link net.forthecrown.grenadier.types.LocalDateArgument}</td>
 *   <td>Parses a {@link java.time.LocalDate} object from the ISO-8601 format</td>
 *   <td><code>12-12-2023</code></td>
 * </tr>
 * <tr>
 *   <td>{@link net.forthecrown.grenadier.types.TimeArgument}</td>
 *   <td>Parses a {@link java.time.Duration}</td>
 *   <td><code>3s, 3seconds, 5m, 5minutes</code></td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 *
 * <table>
 *   <caption>Minecraft argument types</caption>
 *   <thead>
 *     <tr><th>Argument type</th><th>Description</th><th>Example</th></tr>
 *   </thead>
 *
 *   <tbody>
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.BlockArgument}</td>
 *     <td>Parses a block state and optionally NBT data that can be applied to a block</td>
 *     <td><code>minecraft:chest[facing=west]{dataKey:1b}</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.BlockFilterArgument}</td>
 *     <td>Parses a block or block tag that can be used to as a predicate for blocks</td>
 *     <td><code>#minecraft:buttons[face=ceiling]</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.ComponentArgument}</td>
 *     <td>Parses a text component from a JSON format</td>
 *     <td><code>{"text":"Some text","color":"red"}</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.EntityArgument}</td>
 *     <td>Parses an entity selector that returns 1 or more entities/players</td>
 *     <td><code>@e[limit=1,distance=..2]</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.GameModeArgument}</td>
 *     <td>Game mode value from a non-vanilla set of labels</td>
 *     <td><code>creative, survival, c, s</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.ItemArgument}</td>
 *     <td>Parses an itemstack along with item NBT data</td>
 *     <td><code>minecraft:stone{nbtKey:1b}</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.ItemFilterArgument}</td>
 *     <td>Parses an item or itemtag along with NBT data that will be used as a predicate against other items</td>
 *     <td><code>#minecraft:flowers{nbtKey:1b}</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.KeyArgument}</td>
 *     <td>Parses a namespaced key</td>
 *     <td><code>minecraft:stone</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.NbtArgument}</td>
 *     <td>Parses NBT data in the SNBT format</td>
 *     <td><code>{foo:'bar',abc:1b}</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.ObjectiveArgument}</td>
 *     <td>Parses a scoreboard objective from its name</td>
 *     <td><code>deaths, noClip</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.ParticleArgument}</td>
 *     <td>Parses a particle by its key</td>
 *     <td><code>minecraft:explosion_emitter</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.PositionArgument}</td>
 *     <td>Parses a relative/local/absolute 2D or 3D position</td>
 *     <td><code>~ ~ ~</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.TagPathArgument}</td>
 *     <td>Parses an NBT path</td>
 *     <td><code>key1.key2[]</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.TeamArgument}</td>
 *     <td>Parses a scoreboard team from its name</td>
 *     <td><code>team1, Team2</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.UuidArgument}</td>
 *     <td>Parses a UUID by its hexadecimal value</td>
 *     <td><code>21290ce5-679c-4917-b30e-168c0d450c72</code></td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link net.forthecrown.grenadier.types.WorldArgument}</td>
 *     <td>Parses a world by its name</td>
 *     <td><code>world, world_the_end</code></td>
 *   </tr>
 *
 *   </tbody>
 * </table>
 *
 * @see net.forthecrown.grenadier.types.ArgumentTypes
 * Accessing and creating argument type
 *
 */
package net.forthecrown.grenadier.types;