package eu.endercentral.crazy_advancements;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import eu.endercentral.crazy_advancements.advancement.progress.GenericResult;
import eu.endercentral.crazy_advancements.command.ProgressChangeOperation;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class CommandHandler {

    private final CrazyAdvancementsAPI plugin;

    public CommandHandler(final CrazyAdvancementsAPI plugin) {
        this.plugin = plugin;
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::register);
    }

    private void register(ReloadableRegistrarEvent<Commands> commands) {
        commands.registrar().register(showToastCommand(), List.of("catoast", "toast"));
        commands.registrar().register(grantRevokeShared(Commands.literal("grant"), true), List.of("cagrant"));
        commands.registrar().register(grantRevokeShared(Commands.literal("revoke"), false), List.of("carevoke"));
        commands.registrar().register(setProgressCommand(), List.of("caprogress"));
        commands.registrar().register(Commands.literal("careload").requires(perm("crazyadvancements.command.reload")).executes(c -> {
            plugin.reload();
            c.getSource().getSender().sendRichMessage("<green>Reloaded.");
            return Command.SINGLE_SUCCESS;
        }).build());
    }

    private LiteralCommandNode<CommandSourceStack> showToastCommand() {
        return Commands.literal("showtoast")
            .requires(perm("crazyadvancements.command.showtoast"))
            .then(Commands.argument("targets", ArgumentTypes.players())
                .then(Commands.argument("icon", ArgumentTypes.itemStack())
                    .then(Commands.literal("task")
                        .then(showToastMessage("task"))
                    )
                    .then(Commands.literal("goal")
                        .then(showToastMessage("goal"))
                    )
                    .then(Commands.literal("challenge")
                        .then(showToastMessage("challenge"))
                    )
                )
            )
            .build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> showToastMessage(final String frameName) {
        return Commands.argument("message", StringArgumentType.greedyString())
            .executes(ctx -> showToast(ctx, frameName));
    }

    private int showToast(final CommandContext<CommandSourceStack> ctx, final String frameName) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetsResolver = ctx.getArgument(
            "targets", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = targetsResolver.resolve(ctx.getSource());
        final ItemStack icon = ctx.getArgument("icon", ItemStack.class);
        final AdvancementDisplay.AdvancementFrame frame = AdvancementDisplay.AdvancementFrame.parse(frameName);
        final String message = ctx.getArgument("message", String.class);
        ToastNotification toast = new ToastNotification(icon, MiniMessage.miniMessage().deserialize(message), frame);
        targets.forEach(toast::send);
        ctx.getSource().getSender().sendRichMessage("<green>Displayed toast to " + targets.size() + " players.");
        return Command.SINGLE_SUCCESS;
    }

    private LiteralCommandNode<CommandSourceStack> grantRevokeShared(
        final LiteralArgumentBuilder<CommandSourceStack> command, final boolean isGrant
    ) {
        return command.requires(perm("crazyadvancements.command.grantrevoke"))
            .then(Commands.argument("targets", ArgumentTypes.players())
                .then(Commands.argument("manager", StringArgumentType.string())
                    .then(Commands.argument("advancement", StringArgumentType.string())
                        .executes(c -> grantOrRevoke(c, isGrant))
                    )
                )
            )
            .build();
    }

    private int grantOrRevoke(final CommandContext<CommandSourceStack> ctx, final boolean isGrant) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetsResolver = ctx.getArgument(
            "targets", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = targetsResolver.resolve(ctx.getSource());
        final String managerName = ctx.getArgument("manager", String.class);
        final AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(managerName));
        if (manager == null) {
            throw new RuntimeException("Manager not found: " + managerName);
        }
        final String advancementName = ctx.getArgument("advancement", String.class);
        final Advancement advancement = manager.getAdvancement(new NameKey(advancementName));
        if (advancement == null) {
            throw new RuntimeException("Advancement not found in manager " + managerName + ": " + advancementName);
        }
        int successCount = 0, failCount = 0;
        for (final Player player : targets) {
            if (!manager.getPlayers().contains(player)) continue;
            boolean success = true;
            if (isGrant) {
                if (!advancement.isGranted(player)) {
                    GenericResult result = manager.grantAdvancement(player, advancement);
                    success = result == GenericResult.CHANGED;
                }
            } else {
                GenericResult result = manager.revokeAdvancement(player, advancement);
                success = result == GenericResult.CHANGED;
            }
            if (success) {
                successCount++;
                if (plugin.getFileAdvancementManager().equals(manager)) {
                    plugin.getFileAdvancementManager().saveProgress(player, advancement);
                }
            } else {
                failCount++;
            }
        }
        ctx.getSource().getSender().sendRichMessage("<green><action> advancement <advancement> to <successes> players (<failures> failures)",
            Placeholder.unparsed("action", isGrant ? "Granted" : "Revoked"),
            Placeholder.component("advancement", advancement.getDisplay().getTitle()),
            Placeholder.unparsed("successes", String.valueOf(successCount)),
            Placeholder.unparsed("failures", String.valueOf(failCount))
        );
        return Command.SINGLE_SUCCESS;
    }

    private LiteralCommandNode<CommandSourceStack> setProgressCommand() {
        return Commands.literal("setprogress")
            .requires(perm("crazyadvancements.command.grantrevoke"))
            .then(Commands.argument("targets", ArgumentTypes.players())
                .then(Commands.argument("manager", StringArgumentType.string())
                    .then(Commands.argument("advancement", StringArgumentType.string())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                            .then(setProgressOperationChoice("set"))
                            .then(setProgressOperationChoice("add"))
                            .then(setProgressOperationChoice("remove"))
                            .then(setProgressOperationChoice("multiply"))
                            .then(setProgressOperationChoice("divide"))
                            .then(setProgressOperationChoice("power"))
                        )
                    )
                )
            )
            .build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> setProgressOperationChoice(final String name) {
        return Commands.literal(name).executes(c -> setProgress(c, ProgressChangeOperation.parse(name)));
    }

    private int setProgress(final CommandContext<CommandSourceStack> ctx, final ProgressChangeOperation operation) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver targetsResolver = ctx.getArgument(
            "targets", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = targetsResolver.resolve(ctx.getSource());
        final String managerName = ctx.getArgument("manager", String.class);
        final AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(managerName));
        if (manager == null) {
            throw new RuntimeException("Manager not found: " + managerName);
        }
        final String advancementName = ctx.getArgument("advancement", String.class);
        final Advancement advancement = manager.getAdvancement(new NameKey(advancementName));
        if (advancement == null) {
            throw new RuntimeException("Advancement not found in manager " + managerName + ": " + advancementName);
        }
        final int amount = IntegerArgumentType.getInteger(ctx, "amount");
        for (final Player player : targets) {
            if (!manager.getPlayers().contains(player)) continue;
            int currentProgress = advancement.getProgress(player).getCriteriaProgress();
            int progress = operation.apply(currentProgress, amount);

            manager.setCriteriaProgress(player, advancement, progress);

            if (plugin.getFileAdvancementManager().equals(manager)) {
                plugin.getFileAdvancementManager().saveProgress(player, advancement);
            }
        }
        ctx.getSource().getSender().sendRichMessage("<green>Updated progress for " + targets.size() + " players.");
        return Command.SINGLE_SUCCESS;
    }

    private Predicate<CommandSourceStack> perm(final String permission) {
        return s -> s.getSender().hasPermission(permission);
    }

}
