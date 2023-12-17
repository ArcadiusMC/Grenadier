package net.forthecrown.grenadier;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.BoolArgumentType;
import java.util.List;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandListTests extends AbstractCommand {

  public static final List<String> COMMANDS = List.of(
      "test_type_mapping @e[limit=1]",
      "test_type_mapping @e[limit=1] true",
      "test_type_mapping 10 10 10",
      "def_exec_test",
      "map_result_test arg 10 10 10 @e",
      "annot_paste_test",
      "annot_paste_test 2",
      "resource_test literal",
      "resource_test other_literal",
      "resource_test 10 10 10",
      "resource_test 10 10 10 @e",
      "t_alias1 argName",
      "t_alias2 argName",
      "t_alias3 argName",
      "t_alias4 argName",
      "test_annotation block_text minecraft:stone_slab[waterlogged=true]",
      "test_annotation variables 4",
      "test_annotation integer_range 1..4",
      "test_annotation double_range 1.43..2.34",
      "test_annotation double_suffix 10unit_1",
      "test_annotation double_suffix 10",
      "test_annotation double_suffix 10unit_2",
      "test_alias_1",
      "test_alias_2",
      "grenadier_test positions vec2d 12.32 34.1",
      "grenadier_test positions vec2i 12 31",
      "grenadier_test positions vec3d 12.423 12.32 423.1",
      "grenadier_test positions vec3i 12 32 42",
      "grenadier_test string_word asdasd",
      "grenadier_test execution_runtime_error",
      "grenadier_test tag_path element1.element2[0].elem[]",
      "grenadier_test nbt_value 1b",
      "grenadier_test nbt_value {text:'Hello, world!'}",
      "grenadier_test item_array stone,stone{randomTag:1b}",
      "grenadier_test item stone{a_tag:1b}",
      "grenadier_test item_filter stone",
      "grenadier_test item_filter #leaves",
      "grenadier_test component {\"text\":\"Hello, world!\",\"color\":\"yellow\"}",
      "grenadier_test world world_the_end",
      "grenadier_test key minecraft:stone",
      "grenadier_test time 12days",
      "grenadier_test local_date 2023-10-11",
      "grenadier_test block stone_slab[waterlogged=true]",
      "grenadier_test block_filter stone",
      "grenadier_test enchantment minecraft:sharpness",
      "transformer-test literal",
      "transformer-test literal another-literal"
  );

  private static final ArgumentOption<Boolean> SILENT
      = Options.argument(BoolArgumentType.bool(), "silent");

  private static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addRequired(SILENT)
      .build();

  public CommandListTests() {
    super("test_lists");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("options", OPTIONS)
            .executes(c -> {

              CommandSource source = c.getSource();
              CommandSource cmdExecutor;

              var parsedOptions = c.getArgument("options", ParsedOptions.class);

              if (parsedOptions.getValueOptional(SILENT).orElse(false)) {
                cmdExecutor = source.silent();
              } else {
                cmdExecutor = source;
              }

              GrenadierProvider provider = Grenadier.getProvider();

              for (String s : COMMANDS) {
                source.sendMessage(
                    text()
                        .color(NamedTextColor.GRAY)
                        .append(text(">> Executing >> '", NamedTextColor.DARK_GRAY))
                        .append(text(s))
                        .append(text("'", NamedTextColor.DARK_GRAY))
                        .build()
                );

                provider.dispatch(cmdExecutor, s);
              }

              return 0;
            })
        );
  }
}
