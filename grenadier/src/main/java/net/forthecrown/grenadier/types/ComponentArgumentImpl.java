package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandBuildContext;

class ComponentArgumentImpl
    implements ComponentArgument, VanillaMappedArgument
{

  static final ComponentArgument INSTANCE = new ComponentArgumentImpl();

  @Override
  public Component parse(StringReader reader) throws CommandSyntaxException {
    net.minecraft.network.chat.Component nms
        = net.minecraft.commands.arguments.ComponentArgument.textComponent(InternalUtil.CONTEXT)
        .parse(reader);

    return Grenadier.fromMessage(nms);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.ComponentArgument.textComponent(InternalUtil.CONTEXT);
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}