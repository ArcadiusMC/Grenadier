package net.forthecrown.grenadier;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.time.LocalDate;
import java.util.List;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.BlockArgument;
import net.forthecrown.grenadier.types.BlockFilterArgument;
import net.forthecrown.grenadier.types.ItemArgument;
import net.forthecrown.grenadier.types.ItemFilterArgument;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.nbt.path.TagPath;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

public class TestCommand extends AbstractCommand {

  private static final ArrayArgument<ItemArgument.Result> ITEM_ARRAY
      = ArgumentTypes.array(ArgumentTypes.item());

  public static final ArgumentOption<Integer> integerArgument
      = Options.argument(IntegerArgumentType.integer())
      .addLabel("an_integer")
      .setSuggester((context, builder) -> {
        return Completions.suggest(builder, "1", "2", "13");
      })
      .build();

  public static final ArgumentOption<String> stringArgument
      = Options.argument(StringArgumentType.word())
      .addLabel("a_string")
      .addLabel("string_2")
      .setSuggester((context, builder) -> {
        return Completions.suggest(builder, "word", "world", "thing");
      })
      .build();

  public static final FlagOption flag = Options.flag()
      .addLabel("flag")
      .build();

  public static final OptionsArgument options = OptionsArgument.builder()
      .addOptional(stringArgument)
      .addRequired(integerArgument)
      .addFlag(flag)
      .build();

  public TestCommand() {
    super("grenadier_test");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .withAliases("test_alias_1", "test_alias_2")
        .withDescription("A command description")
        .withPermission("grenadier.commands.test")

        .executes(context -> {
          context.getSource().sendMessage("Hello, world!");
          return 0;
        })

        .then(literal("positions")
            .then(literal("vec2d")
                .then(argument("p", ArgumentTypes.position2d())
                    .executes(c -> {
                      Location location = ArgumentTypes.getLocation(c, "p");
                      c.getSource().sendMessage(location.toString());
                      return 0;
                    })
                )
            )

            .then(literal("vec2i")
                .then(argument("p", ArgumentTypes.blockPosition2d())
                    .executes(c -> {
                      Location location = ArgumentTypes.getLocation(c, "p");
                      c.getSource().sendMessage(location.toString());
                      return 0;
                    })
                )
            )

            .then(literal("vec3d")
                .then(argument("p", ArgumentTypes.position())
                    .executes(c -> {
                      Location location = ArgumentTypes.getLocation(c, "p");
                      c.getSource().sendMessage(location.toString());
                      return 0;
                    })
                )
            )

            .then(literal("vec3i")
                .then(argument("p", ArgumentTypes.blockPosition())
                    .executes(c -> {
                      Location location = ArgumentTypes.getLocation(c, "p");
                      c.getSource().sendMessage(location.toString());
                      return 0;
                    })
                )
            )
        )

        .then(literal("options")
            .then(argument("args", options)
                .executes(c -> {
                  ParsedOptions parsedOptions
                      = c.getArgument("args", ParsedOptions.class);

                  c.getSource().sendSuccess(text(parsedOptions.toString()));
                  return 0;
                })
            )
        )

        .then(literal("string_word")
            .then(argument("word", StringArgumentType.word())
                .executes(c -> {
                  var source = c.getSource();
                  String word = c.getArgument("word", String.class);

                  source.sendMessage(word);
                  return 0;
                })
            )
        )

        .then(literal("suggestion_error")
            .then(argument("argument", StringArgumentType.greedyString())

                // If these aren't handled, then they tend to crash
                // the server lmao
                .suggests((context, builder) -> {
                  throw new RuntimeException("Suggestion error");
                })

                .executes(c -> {
                  c.getSource().sendMessage(":|");
                  return 0;
                })
            )
        )

        .then(literal("execution_runtime_error")
            .executes(c -> {
              throw new RuntimeException("Execution error");
            })
        )

        .then(literal("tag_path")
            .then(argument("path", ArgumentTypes.tagPath())
                .executes(c -> {
                  CommandSource source = c.getSource();
                  TagPath path = c.getArgument("path", TagPath.class);

                  source.sendSuccess(text(path.getInput()));
                  return 0;
                })
            )
        )

        .then(literal("nbt_value")
            .then(argument("value", ArgumentTypes.binaryTag())
                .executes(c -> {
                  BinaryTag tag = c.getArgument("value", BinaryTag.class);
                  Component component = PaperNbt.asComponent(tag, "  ", true);
                  c.getSource().sendSuccess(component);
                  return 0;
                })
            )
        )

        .then(literal("item_array")
            .then(argument("value", ITEM_ARRAY)
                .executes(c -> {
                  List<ItemArgument.Result> list
                      = c.getArgument("value", List.class);

                  c.getSource().sendSuccess(text(list.toString()));
                  return 0;
                })
            )
        )

        .then(literal("item")
            .then(argument("item_value", ArgumentTypes.item())
                .executes(c -> {
                  var item = c.getArgument("item_value", ItemArgument.Result.class);
                  var builder = text();

                  builder.append(text("material=" + item.getMaterial()));
                  var tag = item.getTag();

                  if (tag != null) {
                    builder.append(newline())
                        .append(text("tag="))
                        .append(PaperNbt.asComponent(tag));
                  }

                  c.getSource().sendSuccess(builder.build());
                  return 0;
                })
            )
        )

        .then(literal("item_filter")
            .then(argument("filter", ArgumentTypes.itemFilter())
                .executes(c -> {
                  ItemFilterArgument.Result result
                      = c.getArgument("filter", ItemFilterArgument.Result.class);

                  c.getSource().sendSuccess(text(result.toString()));
                  return 0;
                })
            )
        )

        .then(literal("component")
            .then(argument("text", ArgumentTypes.component())
                .executes(c -> {
                  Component component = c.getArgument("text", Component.class);
                  c.getSource().sendSuccess(component);
                  return 0;
                })
            )
        )

        .then(literal("world")
            .then(argument("world_val", ArgumentTypes.world())
                .executes(c -> {
                  World w = c.getArgument("world_val", World.class);
                  c.getSource().sendSuccess(text(w.toString()));
                  return 0;
                })
            )
        )

        .then(literal("key")
            .then(argument("key_v", ArgumentTypes.key())
                .executes(c -> {
                  NamespacedKey key = c.getArgument("key_v", NamespacedKey.class);
                  c.getSource().sendSuccess(text(key.asString()));
                  return 0;
                })
            )
        )

        .then(literal("time")
            .then(argument("time_v", ArgumentTypes.time())
                .executes(c -> {
                  var duration = ArgumentTypes.getDuration(c, "time_v");
                  c.getSource().sendSuccess(text(duration.toString()));
                  return 0;
                })
            )
        )

        .then(literal("local_date")
            .then(argument("date", ArgumentTypes.localDate())
                .executes(c -> {
                  LocalDate date = c.getArgument("date", LocalDate.class);
                  c.getSource().sendSuccess(text(date.toString()));
                  return 0;
                })
            )
        )

        .then(literal("objective")
            .then(argument("obj", ArgumentTypes.objective())
                .executes(c -> {
                  var obj = c.getArgument("obj", Objective.class);
                  c.getSource().sendSuccess(obj.displayName());
                  return 0;
                })
            )
        )

        .then(literal("team")
            .then(argument("team_v", ArgumentTypes.team())
                .executes(c -> {
                  var team = c.getArgument("team_v", Team.class);
                  c.getSource().sendSuccess(team.displayName());
                  return 0;
                })
            )
        )

        .then(literal("block")
            .then(argument("block_v", ArgumentTypes.block())
                .executes(c -> {
                  BlockArgument.Result block
                      = c.getArgument("block_v", BlockArgument.Result.class);

                  c.getSource().sendSuccess(text(block.toString()));
                  return 0;
                })
            )
        )

        .then(literal("block_filter")
            .then(argument("filter", ArgumentTypes.blockFilter())
                .executes(c -> {
                  BlockFilterArgument.Result result
                      = c.getArgument("filter", BlockFilterArgument.Result.class);

                  c.getSource().sendSuccess(text(result.toString()));
                  return 0;
                })
            )
        )

        .then(literal("enchantment")
            .then(argument("ench", ArgumentTypes.enchantment())
                .executes(c -> {
                  Enchantment enchantment
                      = c.getArgument("ench", Enchantment.class);

                  c.getSource().sendSuccess(enchantment.displayName(1));
                  return 0;
                })
            )
        );
  }
}