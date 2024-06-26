package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.particles.ParticleOptions;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.CraftParticle;

class ParticleArgumentImpl
    implements ParticleArgument, VanillaMappedArgument
{

  static final ParticleArgument INSTANCE = new ParticleArgumentImpl();

  @Override
  public Particle parse(StringReader reader) throws CommandSyntaxException {
    ParticleOptions nms = net.minecraft.commands.arguments.ParticleArgument.readParticle(reader, InternalUtil.CONTEXT);
    return CraftParticle.minecraftToBukkit(nms.getType());
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context, SuggestionsBuilder builder
  ) {
    return net.minecraft.commands.arguments.ParticleArgument
        .particle(InternalUtil.CONTEXT)
        .listSuggestions(context, builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.ParticleArgument.particle(context);
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}