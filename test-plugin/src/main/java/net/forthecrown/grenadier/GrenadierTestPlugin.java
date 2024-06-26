package net.forthecrown.grenadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.HashMap;
import java.util.Map;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.grenadier.annotations.CommandDataLoader;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.SuffixedNumberArgument;
import org.bukkit.plugin.java.JavaPlugin;

public class GrenadierTestPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    new TestCommand();
    new CommandListTests();
    new VanillaOverrideTest();

    new PluginOverrideTest();

    try {
      new CustomTypeFailTest();
    } catch (RuntimeException exc) {
      getSLF4JLogger().warn("Expected error, registration failed", exc);
    }

    AnnotatedCommandContext ctx = AnnotatedCommandContext.create();
    ctx.addLoader(CommandDataLoader.resources(getClassLoader()));

    SuggestionProvider<CommandSource> int_suggestions = (context, builder) -> {
      return Completions.suggest(builder, "1", "2", "3", "4", "5");
    };

    Command<CommandSource> intExec = context -> {
      context.getSource().sendMessage("lol");

      int value = context.getArgument("integer_arg", Integer.class);
      context.getSource().sendMessage("value=" + value);

      return 0;
    };

    var vars = ctx.getVariables();
    vars.put("int_suggestions", int_suggestions);
    vars.put("int_exec", intExec);
    vars.put("int_arg", IntegerArgumentType.integer(1, 5));
    vars.put("int_arg_name", "integer_arg");

    Map<String, Double> units = new HashMap<>();
    units.put("ten", 10.0);
    units.put("hundred", 100.0);

    SuffixedNumberArgument<Double> suffixed = ArgumentTypes.suffixedDouble(units, 1, 10000);
    vars.put("suffixed", suffixed);

    ctx.setDefaultPermissionFormat("grenadier.test.{command}");
    ctx.setDefaultExecutes("defRun");
    ctx.setWarningsEnabled(true);
    ctx.setFatalErrors(false);

    ctx.registerCommand(new TestAnnotatedCommand());
    ctx.registerCommand(new TestCommand2());
    ctx.registerCommand(new TestCommand3());
    ctx.registerCommand(new DefaultExecutesTest());
    ctx.registerCommand(new AnnotationMapperTest());
    ctx.registerCommand(new ResourcedCommandTest());
    ctx.registerCommand(new SignEditCommand());
    ctx.registerCommand(new PasteAnnotationTest());
    ctx.registerCommand(new MapperTestCommand());
    ctx.registerCommand(new TransformerTestCommand());
  }

  @Override
  public void onDisable() {

  }
}