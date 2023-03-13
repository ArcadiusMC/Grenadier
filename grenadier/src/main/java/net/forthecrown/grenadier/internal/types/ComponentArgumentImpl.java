package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandBuildContext;

public class ComponentArgumentImpl
    implements ComponentArgument, VanillaMappedArgument
{

  @Override
  public Component parse(StringReader reader) throws CommandSyntaxException {
    net.minecraft.network.chat.Component nms
        = net.minecraft.commands.arguments.ComponentArgument.textComponent()
        .parse(reader);

    return Grenadier.fromMessage(nms);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.ComponentArgument.textComponent();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}