/**
 * Grenadier command annotation library, see below for guides.
 * <p>
 * The annotation library works by taking in an object with a {@link net.forthecrown.grenadier.annotations.CommandData}
 * annotation, tokenizing the {@link net.forthecrown.grenadier.annotations.CommandData#value()}
 * and parsing it into an abstract command tree and finally resolving all
 * variable references, class field/method references and all other else before
 * the command is registered.
 * <br>
 * This framework makes the creation of commands easier as less boilerplate is
 * required and command trees can be created faster.
 *
 * <h2>Table of contents</h2>
 * <ol>
 *   <li><a href="#guide-section">Guide</a>
 *     <ol>
 *       <li><a href="#creating-a-command">Creating a basic command</a></li>
 *       <li><a href="#guide-vars">Using variables</a></li>
 *       <li><a href="#guide-defs">Permission and execution defaults</a></li>
 *     </ol>
 *   </li>
 *   <li><a href="#syntax-doc">Syntax Documentation</a>
 *     <ol>
 *       <li><a href="#syn-concepts">Concepts</a>
 *         <ol>
 *           <li><a href="#conc-refs">Method/Field References</a></li>
 *           <li><a href="#conc-vars">Variable References</a></li>
 *           <li><a href="#conc-names">Names</a></li>
 *         </ol>
 *       </li>
 *       <li><a href="#syn-nodes">Command node syntax</a></li>
 *       <li><a href="#syn-root-fields">Root-specific fields</a></li>
 *       <li><a href="#syn-argument-fields">Argument-specific fields</a></li>
 *       <li><a href="#syn-all-fields">Fields used by all nodes</a></li>
 *       <li><a href="#syn-children">Child nodes</a></li>
 *       <li><a href="#syn-comments">Comments</a></li>
 *       <li><a href="#syn-full-example">Full example</a></li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <h2 id="guide-section">Guide</h2>
 * This part will act as a guide on how to use the Grenadier Annotations library.
 *
 * <h2 id="creating-a-command">Creating a basic command</h2>
 * Let's start with a simple command that just says hello world when it's ran,
 * we would do that like so:
 * <pre><code>
 * &#064;CommandData("""
 * name = 'example_command'
 * executes = run()
 * """)
 * public class ExampleCommand {
 *
 *   public void run(CommandContext&lt;CommandSource> context) {
 *     CommandSource source = context.getSource();
 *     source.sendMessage("Hello, world");
 *   }
 * }
 * </code></pre>
 *
 * You can then register the command like so:
 * <pre><code>
 * public class ExamplePlugin extends JavaPlugin {
 *
 *   &#064;Override
 *   public void onEnable() {
 *     AnnotatedCommandContext ctx = AnnotatedCommandContext.create();
 *     ctx.registerCommand(new ExampleCommand());
 *   }
 * }
 * </code></pre>
 *
 * <h2 id="guide-vars">Using variables</h2>
 *
 * Before talking about variables, it must be made clear that there are 2 types
 * of variables, local and global. For this example, we'll show global variables
 * first.
 * <p>
 * Variables can be of any type and can be registered like this:
 * <pre><code>
 * AnnotatedCommandContext ctx = AnnotatedCommandContext.create();
 *
 * Map&lt;String, Object&gt; variables = ctx.getVariables();
 * variables.put("variable_name", "foobar");
 * </code></pre>
 *
 * You can then use variables like so:
 * <pre><code>
 * name = 'command_name'
 *
 * literal(@variable_name) {
 *   executes = methodName()
 * }
 * </code></pre>
 * Variables can be used in lots of places, including in argument names,
 * permission fields, as command names and even for the 'executes', 'suggests'
 * and 'requires' fields. Make sure to read the <a href="syntax-doc">Syntax
 * documentation</a> section for a full guide on variables and the syntax.
 *
 * <h2 id="guide-defs">Permission and executes defaults</h2>
 *
 * Default values for both command permissions and 'executes' can be registered
 * to avoid typing similar permissions and names over and over again.
 *
 * <br>
 * Permission defaults can be registered like so:
 * <pre><code>
 * AnnotatedCommandContext ctx = AnnotatedCommandContext.create();
 * ctx.setDefaultPermissionFormat("commands.permission.{command}");
 * </code></pre>
 * The {@code {command}} is a placeholder for the command's name.
 *
 * <p>
 * Execution defaults allow you to set a method name that will be used
 * automatically by a command's 'root' node if no other 'executes' value was
 * specified.
 * <br>
 *
 * We can set a default execution value like so:
 * <pre><code>
 * AnnotatedCommandContext ctx = AnnotatedCommandContext.create();
 * ctx.setDefaultExecutes("execute");
 * </code></pre>
 *
 * and then use it like so:
 * <pre><code>
 * // No 'executes' was specified, so it defaults to what we set as the
 * // default above
 * &#064;CommandData("name = 'example_command'")
 * public class ExampleCommand {
 *
 *   // Default execution method
 *   public void execute(CommandContext&lt;CommandSource&gt; context) {
 *
 *   }
 * }
 * </code></pre>
 *
 * Normally, the default execution method will be used if the 'root' node has no
 * specified 'executes' value set. This can be changed to make sure the default
 * is only used when there's no other command on the root node and when there's
 * no child nodes on the command. This can be done like so:
 *
 * <pre><code>
 * AnnotatedCommandContext ctx = AnnotatedCommandContext.create();
 * ctx.setDefaultRule(DefaultExecutionRule.IF_NO_CHILDREN);
 * </code></pre>
 *
 * <h2 id="syntax-doc">Syntax documentation</h2>
 * Before any of the general syntax is documented, a couple concepts have to be
 * cleared up.
 *
 * <h2 id="syn-concepts">Concepts</h2>
 *
 * <h3 id="conc-refs">Method/Field References</h3>
 *
 * These are values that reference a method or a field within a class. Examples:
 * <pre>
 * // References a field within the command's class
 * fieldName
 *
 * // References a method within the command's class, do not
 * // specify any actual parameters here, the '()' are just to
 * // tell the parser this is referencing a method instead
 * // of a field
 * methodName()
 * </pre>
 * Both of the above-shown values can be combined like so: <pre>
 * field.method().field.method().method()
 * </pre>
 * This does become a bit tricky for the behind-the-scenes evaluator as it'll
 * have to trudge through all those field and method references anytime the
 * command is executed, suggestions are needed or a requirements check is done.
 *
 * <h3 id="conc-vars">Variable references</h3>
 *
 * Far simpler than method/field references, these just reference a variable
 * registered in {@link net.forthecrown.grenadier.annotations.AnnotatedCommandContext#getVariables()}.
 * <br>
 * Examples: <pre>
 * &#064;variableName
 * </pre>
 * The values registered in {@link net.forthecrown.grenadier.annotations.AnnotatedCommandContext#getVariables()}
 * are not the only variables commands can use, those are just the 'global'
 * variables. Each command can register local variables by declaring a void
 * method that takes a {@code Map<String, Object>} as input and annotating it
 * with {@link net.forthecrown.grenadier.annotations.VariableInitializer}. This
 * will be called before the command is 'compiled' to initialize local
 * variables within that command's scope.
 *
 * <h3 id="conc-names">Names</h3>
 * For the most part, and most commonly, names are just quoted strings,
 * eg: {@code 'name'}, but there are times when this is not enough. So, whenever
 * a name is referenced in this documentation it can be 1 or 3 types of values.
 * These are: A) Quoted strings, B) Variable references or C) Field references.
 * In the case of C, they reference a field member of the command class.
 * <p>
 * As an example, we'll use the 3 above name types in declaring a
 * {@code literal} node:
 * <pre><code>
 * // Quoted string name
 * literal('name') = methodName()
 *
 * // Variable reference name
 * literal(@name) = methodName()
 *
 * // Field reference
 * literal(argumentName) = methodName()
 * </code></pre>
 *
 * <h2 id="syn-nodes">Command node syntax</h2>
 * The top level of the Annotation input is the 'root' node, it allows users to
 * specify details about the base command such as aliases, permission, name and
 * description.
 *
 * <h2 id="syn-root-fields">Root specific fields</h2>
 *
 * <h3>Name</h3>
 * See <a href="#conc-names">Concepts, Names</a> for valid input. Determines the
 * name of the command.
 * <p>
 * Examples: <pre>
 * name = 'command_name'
 * name = @variable
 * name = classField
 * </pre>
 *
 * <h3>Permission</h3>
 * A quoted string or '@' variable reference. Specifies the permission the
 * command uses. Examples: <pre>
 * permission = 'foo.bar.foobar'
 * permission = @permission_variable // Must be a string variable
 * </pre>
 *
 * Using the {@link net.forthecrown.grenadier.annotations.AnnotatedCommandContext}
 * a default permission can be set that is applied to all commands registered
 * using that specific context. See {@link net.forthecrown.grenadier.annotations.AnnotatedCommandContext#setDefaultPermissionFormat(String)}
 * for more info.
 *
 * <h3>Description</h3>
 * A quoted string, specifies the command's description. Example:
 * <pre>
 * description = 'This command does a thing'
 * </pre>
 * See <a href="syn-all-desc">All Nodes, Descriptions</a> for more info on
 * descriptions
 *
 * <h3>Aliases</h3>
 * A special list of either quoted strings, unquoted strings, or variable
 * references. These obviously determine the aliases a command uses. Examples:
 * <pre>
 * aliases = alias1 | alias2 | alias3
 * </pre>
 *
 * <h2 id="syn-argument-fields">Argument node specific fields</h2>
 *
 * <h3>Suggests</h3>
 * A field/method reference, a variable reference, or a string suggestions list.
 * Overrides the argument's ArgumentType suggestions. Examples: <pre>
 * // Method reference, must take in a CommandContext and SuggestionsBuilder,
 * // and return a CompletableFuture&lt;Suggestions>
 * suggests = getSomeSuggestions()
 *
 * suggests = [ 'foo', 'bar', 'foobar' ]
 *
 * // Variable must be a SuggestionProvider
 * suggests = @suggestion_variable
 * </pre>
 * Just like the <a href="syn-exec">Executes</a> field, the 'suggests' field
 * is flexible when it comes to a method reference. Any of the following method
 * parameters are automatically filled by the system:
 * <ol>
 *   <li>
 *     Any parameter with the {@link net.forthecrown.grenadier.CommandSource}
 *     type is set to the current command's command source
 *   </li>
 *   <li>
 *     Any parameter with the {@link com.mojang.brigadier.context.CommandContext}
 *     type is set to the current command context
 *   </li>
 *   <li>
 *     Any parameter with the {@link com.mojang.brigadier.suggestion.SuggestionsBuilder}
 *     type is set to the suggestions builder
 *   </li>
 * </ol>
 * The rest of the parameters are treated as argument values within the command.
 * See the 'Executes' section for more info on that.
 *
 * <h2 id="syn-all-fields">Fields used by all nodes</h2>
 *
 * Fields that are accepted by all nodes (root, argument and literal)
 *
 * <h3 id="syn-exec">Executes</h3>
 *
 * A method/field reference or a variable reference. In the case of variable
 * reference, the variable must be a {@link com.mojang.brigadier.Command}.
 * Otherwise, in the case of a field reference, the field must point to a
 * {@link com.mojang.brigadier.Command} value.
 *
 * <p>
 * In the case the executes value references a method then the method can be
 * very flexible. For starters, the method must be a publicly accessible method.
 * <br>
 * If the method has any {@link com.mojang.brigadier.context.CommandContext}
 * parameters, then the context of the command being executed is inputted for
 * that parameter. The rest of the parameters are treated as arguments in the
 * command itself and are filled by calling {@link com.mojang.brigadier.context.CommandContext#getArgument(String, Class)}
 * with the parameter's name and type.
 * <p>
 * If you're encountering issues with the parameter names disappearing during
 * compilation, you can use the {@link net.forthecrown.grenadier.annotations.Argument}
 * annotation to preserve it, for example:
 * <pre>
 * public void run(@Argument("argumentName") int value) {
 *   // Do a thing
 * }
 * </pre>
 * The {@link net.forthecrown.grenadier.annotations.Argument} annotation can
 * also be used to avoid the {@link java.lang.IllegalStateException} thrown when
 * one of the parameter arguments cannot be found like so:
 * <pre><code>
 * public void run(@Argument(value = "value", optional = true) int value) {
 *
 * }
 * </code></pre>
 * This allows for 1 executes method to be reused for several arguments where
 * some arguments may be missing.
 * <br>
 * Examples: <pre>
 * executes = methodName()
 * executes = @variableName
 * executes = fieldName
 * </pre>
 *
 * <p>
 * If the 'executes' field is unset of the 'root' command node, then it may be
 * assigned a default value, if it exists. See {@link net.forthecrown.grenadier.annotations.AnnotatedCommandContext#getDefaultExecutes()}
 * for more info.
 *
 * <p>
 * Since the 'map_result' can be applied to any argument to change it's result
 * type, any value output from any modifier is also valid for executes methods.
 * For example, say we have an argument that returns an intermediary object,
 * like {@link net.forthecrown.grenadier.types.PositionArgument} which returns
 * {@link net.forthecrown.grenadier.types.ParsedPosition}. If we have a
 * map_result that converts the position into a location with {@link net.forthecrown.grenadier.types.ParsedPosition#apply(net.forthecrown.grenadier.CommandSource)}
 * then we can use either the parsed position or location in method parameters.
 *
 * <h3>Requires</h3>
 * Accepts a field/method reference, a variable name or a special 'permission'
 * value.
 * <p>
 * Sets the requirement a command source must pass to use the command node and
 * any child nodes.
 * <br>
 * In the case this field is set to a variable name then the variable must be a
 * {@link java.util.function.Predicate}. Otherwise, in the case of a field
 * reference the field must be a {@link java.util.function.Predicate} and
 * finally, in the case of a method reference the method must return a
 * {@code boolean} and accept a single parameter, a {@link net.forthecrown.grenadier.CommandSource}.
 * <br>
 * If the 'requires' is set to a permission value, the permission value can be
 * a quoted string, a reference to a field in the command class or a variable.
 * The variable must be a string or a {@link org.bukkit.permissions.Permission}
 * <p>
 * Examples: <pre>
 * requires = permission('permission.name')
 * requires = permission(PERMISSION_CONSTANT)
 * requires = permission(@permissionVariable)
 *
 * requires = @variableName
 * requires = fieldName
 * requires = methodName()
 * </pre>
 *
 * <h3>Map Result</h3>
 * A field/method reference or variable reference. Allows for the result of an
 * argument type to mapped to a different type.
 * <br>
 * Results can be mapped with a reference to a method/field within the command's
 * class or with in the argument result's class.
 * <br>
 * In the case the value of this is a variable reference, then the variable must
 * be an instance of {@link net.forthecrown.grenadier.annotations.ArgumentModifier}.
 *
 * <br>
 * Examples:
 * <pre>
 * // References a method within the argument result
 * map_result = result.apply()
 * // References a method in the command class
 * map_result = mapArgument()
 * map_result = @variable
 * </pre>
 *
 * <p>
 * If you need to map the value of an argument other than the current one, you
 * can use {@code map_result(argument name)}, for example:
 * <pre><code>
 * argument('arg1', entities) {
 *
 *   argument('arg2', int(min=1, max=2) {
 *     map_result('arg1') = result.findEntities()
 *   }
 * }
 * </code></pre>
 *
 * <h3 id="syn-all-desc">Description</h3>
 * A quoted string, variable or translatable text key.
 * <br>
 * Describes an argument's use description. If this is set in the root node,
 * then this becomes the command's description and won't become the usage text
 * unless there's an executes method in the root node.
 * <br>
 * Literal description Example: <pre><code>
 * description = 'Argument description'
 * </code></pre>
 * Translatable description example: <pre><code>
 * description = translatable('translation.key')
 * </code></pre>
 * See {@link net.kyori.adventure.translation.TranslationRegistry},
 * {@link net.kyori.adventure.translation.GlobalTranslator} or
 * {@link net.kyori.adventure.text.TranslatableComponent} for more info on
 * translatable text.
 * <p>
 * Variable description example: <pre><code>
 * description = @desc_variable
 * </code></pre>
 * The variable must be an instance of {@link net.kyori.adventure.text.Component}
 * <p>
 * This and the {@code label} values are a part of the usage/help system
 * of the command system. When this description is compiled it will be given to
 * the {@link net.forthecrown.grenadier.annotations.SyntaxConsumer} of the
 * current context with the path to the argument provided as well.
 *
 * <h3>Syntax Label</h3>
 * A quoted string, field name or variable that overrides the default node name
 * for usage/help messages.
 * <br>
 * Example: <pre><code>
 * argument('pos', vec3i) {
 *   label = '&lt;pos: x,y,z&gt;'
 * }
 * </code></pre>
 * In the above example, when the node's name is used in a help message, it will
 * say '&lt;pos: x,y,z&gt;' instead of '&lt;pos&gt;'
 *
 * <h2 id="syn-children">Child nodes</h2>
 *
 * There are 2 types of child nodes, literals and argument nodes, exactly like
 * there are in normal Brigadier.
 *
 * <h3>Literals</h3>
 *
 * Literals only require the name that will be input and are declared so:
 * <pre>
 * literal('foobar') {
 *
 * }
 * </pre>
 *
 * In the case a simple node with nothing but an executes is needed, you can
 * use the following examples:
 * <pre>
 * literal('foobar') = executesMethod()
 * literal('foobar') = commandField
 * literal('foobar') = @executes_variable
 * </pre>
 * See <a href="#syn-exec">Executes</a> section for how executes methods are
 * specified. And see <a href="conc-names">Concepts, Names</a> for valid name
 * input
 *
 * <h3>Arguments</h3>
 *
 * Arguments require the name of the argument and the argument type. For info
 * about the argument types and their parsers, see the {@link net.forthecrown.grenadier.annotations.TypeRegistry}.
 * <p>
 * Examples:
 * <pre>
 * argument('argumentName', int(min=1, max=5)) {
 *
 * }
 * </pre>
 * Just like with literals, if all that is required is an empty node with an
 * executes function, you can use the following examples:
 * <pre>
 * argument('arg', int(min=1, max=5)) = executesMethod()
 * argument('arg', int(min=1, max=5)) = commandField
 * argument('arg', int(min=1, max=5)) = @executes_variable
 * </pre>
 * See <a href="#syn-exec">Executes</a> section for how executes methods are
 * specified. And see <a href="conc-names">Concepts, Names</a> for valid name
 * input
 *
 * <h2 id="syn-comments">Comments</h2>
 * Comments inside {@link net.forthecrown.grenadier.annotations.CommandData}
 * values are the same as in java, '//' starts a line comment, and '/*' specifies
 * a comment that lasts until '*&#47;'
 *
 * <h2 id="syn-full-example">Full example</h2>
 *
 * <pre>
 * name = 'signedit'
 * aliases = sign | signs
 * permission = 'commands.admin.signedit'
 * description = 'Allows you to edit signs'
 *
 * argument(SIGN_ARG, vec3i) {
 *   map_result = positionToSign()
 *   label = "&lt;sign: x,y,z&gt;"
 *
 *   literal('clear') {
 *     description = "Clears a &lt;sign&gt;"
 *     executes = clear()
 *   }
 *
 *   literal('copy') {
 *     description = "Copies a sign that you can later paste"
 *     executes = copy()
 *   }
 *
 *   literal('paste') {
 *     description = "Pastes a previously copied sign"
 *     executes = paste()
 *   }
 *
 *   literal('glowing').argument(GLOW_ARG, bool) {
 *     description = "Changes whether the sign is glowing or not"
 *     executes = setGlowing()
 *   }
 *
 *   argument(LINE_ARG, int(min=1, max=4)) {
 *     label = '&lt;line: 1..4&gt;'
 *     suggests = ['1', '2', '3', '4']
 *
 *     literal('set') {
 *       argument(TEXT_ARG, greedy_string) {
 *         description = "Sets the sign's &lt;line&gt; to &lt;text&gt;"
 *
 *         map_result = stringToComponent()
 *
 *         suggests = suggestSignLine()
 *         executes = setLine()
 *       }
 *     }
 *
 *     literal('clear') {
 *       executes = clearLine()
 *       description = "Clears a sign's &lt;line&gt;"
 *     }
 *   }
 * }
 * </pre>
 *
 * @see net.forthecrown.grenadier.annotations.AnnotatedCommandContext
 * Central command registration and handling class
 *
 * @see net.forthecrown.grenadier.annotations.TypeRegistry
 * Argument type parser registry
 */
package net.forthecrown.grenadier.annotations;