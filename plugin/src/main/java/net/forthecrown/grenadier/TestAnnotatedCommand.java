package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.types.BlockArgument.Result;
import net.forthecrown.grenadier.types.DoubleRangeArgument.DoubleRange;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;

@CommandData("""
name = 'test_annotation'
executes = run()
aliases = t_alias1
        | t_alias2
        | t_alias3
        | t_alias4

literal('argName') {
  executes = run1()
  requires = permission('grenadier.test')
}

literal('block_test') {
  argument('block', block) {
    executes = run2()
  }
}

literal('suggestions_test') {
  argument('intValue', int(min=1, max=3)) {
    suggests = ['1', '2', '3']
    executes = run4()
  }
}

literal('variables') {
  argument(@int_arg_name, @int_arg) {
    suggests = @int_suggestions
    executes = @int_exec
  }
}

literal('integer_range') {
  argument('intRange', int_range) = testIntRange()
}

literal('double_range') {
  argument('doubleRange', double_range) = testDoubleRange()
}

""")
public class TestAnnotatedCommand {

  public int run(CommandContext<CommandSource> context) {
    context.getSource().sendMessage("Helloo!");
    return 0;
  }

  public int run1(CommandContext<CommandSource> context) {
    context.getSource().sendMessage("Run2");
    return 0;
  }

  public int run2(
      CommandContext<CommandSource> context,
      @Argument("block") Result block
  ) {
    context.getSource().sendMessage(block.toString());
    return 0;
  }

  public int run4(
      CommandContext<CommandSource> context,
      @Argument("intValue") int intValue
  ) {
    context.getSource().sendMessage("intValue=" + intValue);
    return 0;
  }

  public int testDoubleRange(
      CommandContext<CommandSource> context,
      @Argument("doubleRange") DoubleRange range
  ) {
    context.getSource().sendMessage(range.toString());

    double[] values = {
        1, 10, 200, 224, -1954.054D,
        Double.NaN,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY
    };

    for (var d: values) {
      boolean contains = range.contains(d);

      context.getSource().sendMessage(
          "contains " + d + " = " + contains
      );
    }

    return 0;
  }

  public int testIntRange(
      CommandContext<CommandSource> context,
      @Argument("intRange") IntRange range
  ) {
    context.getSource().sendMessage(range.toString());

    int[] values = {
        1, 10, 200, 33004, 3847, -15614
    };

    for (int value : values) {
      boolean contains = range.contains(value);

      context.getSource().sendMessage(
          "contains " + value + " = " + contains
      );
    }

    return 0;
  }
}