/**
 * The {@link net.forthecrown.grenadier.types.options.OptionsArgument} allows
 * for the parsing of a list of options and flags. Example:
 * <pre> optionName = value option2=another_value -flag -other_flag </pre>
 *
 * <h2>Option types</h2>
 * There are 2 types of options that can be used in the options argument.
 * <p>
 * The first is a simple flag option ({@link net.forthecrown.grenadier.types.options.FlagOption})
 * that can be set or not set, example: {@code -flag} (Using just {@code flag}
 * works too)
 * <p>
 * The other type is an argument option ({@link net.forthecrown.grenadier.types.options.OptionsArgument})
 * that also requires a value, example: {@code option_name=value}
 *
 * <h2>Usage</h2>
 * The following is an example on how to create options, the options argument
 * and how to access the option values after parsing.
 *
 * <p>
 * Creating options:
 * <pre><code>
 * static final FlagOption EXAMPLE_FLAG
 *    = Options.flag("foo");
 *
 * static final ArgumentOption&#60;Integer> ARGUMENT
 *    = Options.argument(IntegerArgumentType.integer(), "integer_arg");
 *
 * static final OptionsArgument OPTIONS = OptionsArgument.builder()
 *    .addFlag(EXAMPLE_FLAG)
 *    .addRequired(ARGUMENT)
 *    .build();
 * </code></pre>
 * Next, let's assume the input we parsed is {@code integer_arg=12 -foo}, here's
 * how we would access that during command execution
 * <pre><code>
 * CommandContext&#60;CommandSource> context = // ..
 * CommandSource source = context.getSource();
 *
 * ParsedOptions options
 *    = ArgumentTypes.getOptions(context, "argument name");
 *
 * int intValue = options.getValue(ARGUMENT);
 * boolean flag = options.hasFlag(EXAMPLE_FLAG);
 *
 * </code></pre>
 *
 * <h3>Option use conditions</h3>
 * Each option can have a specific use condition that command source objects
 * must pass in order to use the option. For example, if we wanted to limit a
 * specific option to a single world, (for example, the 'command_world' world),
 * we could do so like this:
 * <pre><code>
 * static final ArgumentOption&lt;Integer> integerOption
 *     = Options.argument(IntegerArgumentType.integer())
 *     .setLabel("integerOption")
 *
 *     .setCondition(source -> {
 *       World world = source.getWorld();
 *       return world.getName().equals("command_world");
 *     })
 *
 *     .build()
 * </code></pre>
 * That option won't be suggested or accessible if you're not in the
 * 'command_world' world
 *
 * <h3>Custom suggestions for argument options</h3>
 * Most of the time, {@link net.forthecrown.grenadier.types.options.ArgumentOption}s
 * will use the suggestions provided by their argument type, this behaviour can
 * be overridden, like so:
 * <pre><code>
 * final ArgumentOption&lt;Integer> integerOption
 *     = Options.argument(IntegerArgumentType.integer())
 *     .setLabel("integerOption")
 *
 *     .setSuggester((context, builder) -> {
 *       return Completions.suggest(builder, "1", "2", "3");
 *     })
 *
 *     .build();
 * </code></pre>
 *
 * <h3>Dependant and Mutually Exclusive Options</h3>
 * Dependent options are options which depend on each other, simple as that. You can declare which
 * options are dependent on what options using the {@link net.forthecrown.grenadier.types.options.OptionsArgumentBuilder}
 * like so:
 * <pre><code>
 * ArgumentOption&lt;Integer> integerOption = // ...
 * ArgumentOption&lt;String> stringOption = // ...
 * ArgumentOption&lt;Boolean> booleanOption = // ...
 *
 * OptionsArgument options = OptionsArgument.builder()
 *     .allOf(integerOption, stringOption)
 *     .addOptional(booleanOption)
 *     .build();
 * </code></pre>
 *
 * In that example, the {@code integerOption} and {@code stringOption} depend on each other, meaning
 * if one is specified in a command, the must be as well.
 * <p>
 * The opposite of that are mutually exclusive options, meaning that of a given set of options, only
 * one is allowed to be specified, like so:
 * <pre><code>
 *  ArgumentOption&lt;Integer> integerOption = // ...
 *  ArgumentOption&lt;String> stringOption = // ...
 *  ArgumentOption&lt;Boolean> booleanOption = // ...
 *
 *  OptionsArgument options = OptionsArgument.builder()
 *      .oneOf(integerOption, stringOption)
 *      .addOptional(booleanOption)
 *      .build();
 * </code></pre>
 *
 * In that example, only {@code integerOption} or {@code stringOption} can be specified in command
 * input, if both are specified, the command will fail to parse.
 * <p>
 * If you wish to force one of the options to be specified, then
 * replace {@link net.forthecrown.grenadier.types.options.OptionsArgumentBuilder#oneOf(net.forthecrown.grenadier.types.options.Option...)}
 * or {@link net.forthecrown.grenadier.types.options.OptionsArgumentBuilder#allOf(net.forthecrown.grenadier.types.options.Option...)}
 * with {@link net.forthecrown.grenadier.types.options.OptionsArgumentBuilder#requireOneOf(net.forthecrown.grenadier.types.options.Option...)}
 * or {@link net.forthecrown.grenadier.types.options.OptionsArgumentBuilder#requireAllOf(net.forthecrown.grenadier.types.options.Option...)}
 *
 * @see net.forthecrown.grenadier.types.options.OptionsArgument
 * Options argument
 *
 * @see net.forthecrown.grenadier.types.options.FlagOption
 * Flag option
 *
 * @see net.forthecrown.grenadier.types.options.ArgumentOption
 * Argument option
 *
 * @see net.forthecrown.grenadier.types.options.ParsedOptions
 * Options argument parse result
 */
package net.forthecrown.grenadier.types.options;