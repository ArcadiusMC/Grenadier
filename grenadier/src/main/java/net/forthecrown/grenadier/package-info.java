/**
 * Main Grenadier package, see below for use guides.
 * <p>
 * There are 2 ways to create commands, the first being by extending {@link net.forthecrown.grenadier.AbstractCommand},
 * the second way is to use the {@link net.forthecrown.grenadier.GrenadierCommand}
 * class.
 * <p>
 * Grenadier also uses a command source object separate from Bukkit's
 * {@link org.bukkit.command.CommandSender}. Grenadier uses {@link net.forthecrown.grenadier.CommandSource}.
 * So the following code would not work:
 * <pre><code>
 * CommandSource source = // ...
 *
 * if (source instanceof Player) {
 *   // Do a thing
 * } else {
 *   // Do another thing
 * }
 * </code></pre>
 *
 * Instead, you'll now have to use the following:
 * <pre><code>
 * CommandSource source = // ...
 *
 * // Will throw a CommandSyntaxException if the source is not a player,
 * // thus performing the check for you
 * Player player = source.asPlayer();
 * </code></pre>
 *
 * Grenadier commands can also be used in the {@code /execute} command
 *
 * @see net.forthecrown.grenadier.CommandSource
 * Grenadier's Command source object
 *
 * @see net.forthecrown.grenadier.AbstractCommand
 * Extendable command class
 *
 * @see net.forthecrown.grenadier.GrenadierCommand
 * Grenadier command builder
 *
 * @see net.forthecrown.grenadier.Grenadier
 * Grenadier's singleton access
 *
 * @see net.forthecrown.grenadier.Completions
 * Command suggestion utilities
 *
 * @see net.forthecrown.grenadier.types
 * Argument types built into Grenadier
 */
package net.forthecrown.grenadier;