package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.entity.LookAnchor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.grenadier.CommandBroadcastEvent;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.PermissionLevel;
import net.forthecrown.grenadier.types.CoordinateSuggestion;
import net.forthecrown.grenadier.types.CoordinateSuggestions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftVector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class CommandSourceImpl implements CommandSource {

  private static final Field silentField;

  static {
    Class<CommandSourceStack> stackClass = CommandSourceStack.class;
    Field silent = null;

    for (var f: stackClass.getDeclaredFields()) {
      if (f.getType() != Boolean.class && f.getType() != Boolean.TYPE) {
        continue;
      }

      silent = f;
      break;
    }

    Objects.requireNonNull(silent, "Silent field not found");

    silentField = silent;
    silentField.setAccessible(true);
  }

  @Getter
  private final CommandSourceStack stack;

  @Getter @Setter
  private GrenadierCommandNode currentNode;

  private ResultConsumer<CommandSource> consumer;

  public CommandSourceImpl(CommandSourceStack stack) {
    this.stack = Objects.requireNonNull(stack);
  }

  private CommandSourceImpl with(CommandSourceStack stack) {
    return with(stack, consumer);
  }

  private CommandSourceImpl with(CommandSourceStack stack,
                                 ResultConsumer<CommandSource> consumer
  ) {
    CommandSourceImpl source = new CommandSourceImpl(stack);
    source.consumer = consumer;
    return source;
  }

  @Override
  public CommandSender asBukkit() {
    return stack.getBukkitSender();
  }

  @Override
  public Component displayName() {
    return Grenadier.fromMessage(stack.getDisplayName());
  }

  @Override
  public String textName() {
    return stack.getTextName();
  }

  @Override
  public Location getLocation() {
    return stack.getBukkitLocation();
  }

  @Override
  public Location getAnchoredLocation() {
    var anchor = getAnchor();

    if (anchor == null || anchor == LookAnchor.FEET) {
      return getLocation();
    }

    if (asBukkit() instanceof LivingEntity entity) {
      double height = entity.getEyeHeight();
      return getLocation().add(0, height, 0);
    }

    return getLocation();
  }

  @Override
  public @Nullable LookAnchor getAnchor() {
    return stack.getAnchor() == Anchor.EYES
        ? LookAnchor.EYES
        : LookAnchor.FEET;
  }

  @Override
  public World getWorld() {
    return stack.getBukkitWorld();
  }

  @Override
  public Server getServer() {
    return stack.getServer().server;
  }

  @Override
  public boolean hasPermission(String s) {
    return asBukkit().hasPermission(s);
  }

  @Override
  public boolean hasPermission(PermissionLevel level) {
    return stack.hasPermission(level.ordinal());
  }

  @Override
  public boolean isOp() {
    return asBukkit().isOp();
  }

  @Override
  public void setOp(boolean value) {
    asBukkit().setOp(value);
  }

  @Override
  public void broadcastAdmin(Component message) {
    Set<Audience> viewers = new HashSet<>();

    Bukkit.getOnlinePlayers()
        .stream()
        .filter(player -> {
          if (!player.hasPermission("minecraft.admin.command_feedback")) {
            return false;
          }

          return currentNode == null
              || currentNode.canUse(Grenadier.createSource(player));
        })
        .forEach(viewers::add);

    if (!is(ConsoleCommandSender.class)) {
      viewers.add(Bukkit.getConsoleSender());
    }

    CommandBroadcastEvent event
        = new CommandBroadcastEvent(this, message, viewers);

    if (!event.callEvent() || event.getViewers().isEmpty()) {
      return;
    }

    event.getViewers().forEach(audience -> {
      Component formatted = event.getFormatter()
          .formatMessage(audience, event.getMessage(), this);

      audience.sendMessage(formatted);
    });
  }

  @Override
  public boolean shouldInformAdmins() {
    return stack.source.shouldInformAdmins();
  }

  @Override
  public @Nullable CoordinateSuggestion getRelevant3DCords() {
    if (!isPlayer()) {
      return null;
    }

    var target = asPlayerOrNull().rayTraceBlocks(5);

    if (target == null) {
      return null;
    }

    var hitPosition = target.getHitPosition();
    double x = hitPosition.getX();
    double y = hitPosition.getY();
    double z = hitPosition.getZ();

    return CoordinateSuggestions.create(x, y, z);
  }

  @Override
  public @Nullable CoordinateSuggestion getRelevant2DCords() {
    var relevant = getRelevant3DCords();

    if (relevant == null) {
      return null;
    }

    return CoordinateSuggestions.create(
        relevant.tooltip(),
        relevant.x(),
        relevant.z()
    );
  }

  @Override
  public Collection<String> getEntitySuggestions() {
    return stack.getSelectedEntities();
  }

  @Override
  public boolean isSilent() {
    try {
      return silentField.getBoolean(stack);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean acceptsSuccessMessage() {
    return stack.source.acceptsSuccess();
  }

  @Override
  public boolean acceptsFailureMessage() {
    return stack.source.acceptsFailure();
  }

  @Override
  public CommandSource silent() {
    return with(stack.withSuppressedOutput());
  }

  @Override
  public CommandSource withPosition(Vector vector) {
    return with(stack.withPosition(CraftVector.toNMS(vector)));
  }

  @Override
  public CommandSource withWorld(World world) {
    return with(stack.withLevel(((CraftWorld) world).getHandle()));
  }

  @Override
  public CommandSource facing(Vector vector) {
    return with(stack.facing(CraftVector.toNMS(vector)));
  }

  @Override
  public CommandSource withRotation(float yaw, float pitch) {
    return with(stack.withRotation(new Vec2(yaw, pitch)));
  }

  @Override
  public CommandSource withOutput(CommandSender sender) {
    return with(
        stack.withSource(
            VanillaCommandWrapper.getListener(sender).source
        )
    );
  }

  @Override
  public CommandSource addCallback(ResultConsumer<CommandSource> consumer) {
    if (this.consumer == null) {
      return with(stack, consumer);
    }

    return with(
        stack,

        (context, success, result) -> {
          this.consumer.onCommandComplete(context, success, result);
          consumer.onCommandComplete(context, success, result);
        }
    );
  }

  @Override
  public void onCommandComplete(CommandContext<CommandSource> context,
                                boolean success,
                                int result
  ) {
    if (consumer == null) {
      return;
    }

    consumer.onCommandComplete(context, success, result);
  }

  @Override
  public boolean overrideSelectorPermissions() {
    return stack.bypassSelectorPermissions;
  }
}