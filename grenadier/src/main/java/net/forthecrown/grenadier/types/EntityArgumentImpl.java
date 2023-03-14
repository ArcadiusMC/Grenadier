package net.forthecrown.grenadier.types;

import static net.forthecrown.grenadier.internal.InternalUtil.unwrap;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.PermissionLevel;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.utils.Readers;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
class EntityArgumentImpl implements EntityArgument, VanillaMappedArgument {

  static final EntityArgument PLAYER   = new EntityArgumentImpl(false, false);
  static final EntityArgument PLAYERS  = new EntityArgumentImpl( true, false);
  static final EntityArgument ENTITY   = new EntityArgumentImpl(false,  true);
  static final EntityArgument ENTITIES = new EntityArgumentImpl( true,  true);

  private final boolean allowsMultiple;
  private final boolean includesEntities;

  @Override
  public EntitySelector parse(StringReader reader, boolean overridePermissions)
      throws CommandSyntaxException
  {
    final int start = reader.getCursor();

    EntitySelectorParser parser
        = new EntitySelectorParser(reader, overridePermissions, false);

    var nms = parser.parse(overridePermissions);
    var exceptions = Grenadier.exceptions();

    if (nms.getMaxResults() > 1 && !allowsMultiple) {
      reader.setCursor(start);

      if (includesEntities) {
        throw exceptions.selectorOnlyOnePlayer(reader);
      } else {
        throw exceptions.selectorOnlyOneEntity(reader);
      }
    }

    if (!includesEntities && nms.includesEntities() && nms.isSelfSelector()) {
      reader.setCursor(start);
      throw exceptions.selectorOnlyPlayersAllowed(reader);
    }

    return new ResultImpl(nms, Readers.clone(reader, start));
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource s)) {
      return Suggestions.empty();
    }

    boolean hasPermission
        = s.hasPermission("minecraft.command.selector", PermissionLevel.GAME_MASTERS)
        || s.overrideSelectorPermissions();

    EntitySelectorParser parser = new EntitySelectorParser(
        Readers.forSuggestions(builder),
        hasPermission,
        true
    );

    try {
      parser.parse();
    } catch (CommandSyntaxException exc) {
      // Ignored
    }

    return parser.fillSuggestions(builder, builder1 -> {
      var entities = s.getEntitySuggestions();

      Completions.suggest(builder, entities);
      Completions.suggest(builder, s.getVisiblePlayers().map(Player::getName));
    });
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    if (allowsMultiple) {
      if (includesEntities) {
        return net.minecraft.commands.arguments.EntityArgument.entities();
      } else {
        return net.minecraft.commands.arguments.EntityArgument.players();
      }
    } else {
      if (includesEntities) {
        return net.minecraft.commands.arguments.EntityArgument.entity();
      } else {
        return net.minecraft.commands.arguments.EntityArgument.player();
      }
    }
  }

  static class ResultImpl implements EntitySelector {

    private final net.minecraft.commands.arguments.selector.EntitySelector handle;
    private final StringReader reader;

    public ResultImpl(
        net.minecraft.commands.arguments.selector.EntitySelector handle,
        StringReader reader
    ) {
      this.handle = handle;
      this.reader = reader;
    }

    @Override
    public Player findPlayer(CommandSource source)
        throws CommandSyntaxException
    {
      List<Player> players = findPlayers(source);

      if (players.isEmpty()) {
        throw Grenadier.exceptions().noPlayerFound();
      } else if (players.size() > 1) {
        throw Grenadier.exceptions().selectorOnlyOnePlayer(reader);
      }

      return players.get(0);
    }

    @Override
    public Entity findEntity(CommandSource source)
        throws CommandSyntaxException
    {
      var entities = findEntities(source);

      if (entities.isEmpty()) {
        throw Grenadier.exceptions().noEntityFound();
      } else if (entities.size() > 1) {
        throw Grenadier.exceptions().selectorOnlyOneEntity(reader);
      }

      return entities.get(0);
    }

    @Override
    public List<Player> findPlayers(CommandSource source)
        throws CommandSyntaxException
    {
      return handle.findPlayers(unwrap(source))
          .stream()
          .map(ServerPlayer::getBukkitEntity)
          .filter(source::canSee)
          .collect(Collectors.toList());
    }

    @Override
    public List<Entity> findEntities(CommandSource source)
        throws CommandSyntaxException
    {
      return handle.findEntities(unwrap(source))
          .stream()
          .map(net.minecraft.world.entity.Entity::getBukkitEntity)
          .filter(source::canSee)
          .collect(Collectors.toList());
    }

    @Override
    public boolean isSelfSelector() {
      return handle.isSelfSelector();
    }

    @Override
    public boolean isWorldLimited() {
      return handle.isWorldLimited();
    }

    @Override
    public boolean includesEntities() {
      return handle.includesEntities();
    }

    @Override
    public int getMaxResults() {
      return handle.getMaxResults();
    }
  }
}