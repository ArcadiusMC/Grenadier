package net.forthecrown.grenadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext.DefaultExecutionRule;
import org.bukkit.plugin.java.JavaPlugin;

public class GrenadierPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Grenadier.plugin(this);
    new TestCommand();

    AnnotatedCommandContext ctx = AnnotatedCommandContext.create();

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

    ctx.setDefaultPermissionFormat("grenadier.test.{command}");
    ctx.setDefaultExecutes("defRun");
    ctx.setDefaultRule(DefaultExecutionRule.IF_NO_CHILDREN);

    ctx.registerCommand(new TestAnnotatedCommand());
    ctx.registerCommand(new TestCommand2());
    ctx.registerCommand(new TestCommand3());
    ctx.registerCommand(new DefaultExecutesTest());
    ctx.registerCommand(new AnnotationMapperTest());
    ctx.registerCommand(new ResourcedCommandTest());
    ctx.registerCommand(new SignEditCommand());
  }

  @Override
  public void onDisable() {

  }
}