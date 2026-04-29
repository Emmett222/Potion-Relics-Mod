package net.emmett222.potionrelicsmod.commands;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.ModItems;
import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.emmett222.potionrelicsmod.network.DragonWardApparitionPacket;
import net.emmett222.potionrelicsmod.network.ModMessages;
import net.emmett222.potionrelicsmod.shrines.RelicShrineManager;
import net.emmett222.potionrelicsmod.shrines.RelicShrineTheme;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RelicShrineCommands {
    private static final SimpleCommandExceptionType INVALID_RELIC = new SimpleCommandExceptionType(
            Component.translatable("command.potionrelicsmod.invalid_relic"));
    private static final SimpleCommandExceptionType INVALID_SCATTER_AREA = new SimpleCommandExceptionType(
            Component.translatable("command.potionrelicsmod.invalid_scatter_area"));
    private static final SimpleCommandExceptionType INVALID_SHRINE_LOCATION = new SimpleCommandExceptionType(
            Component.translatable("command.potionrelicsmod.invalid_shrine_location"));
    private static final Pattern SCATTER_AREA_PATTERN = Pattern.compile(
            "^\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*,\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*$");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> shrineCommand = Commands.literal("shrine")
                .then(Commands.literal("place")
                        .then(Commands.argument("relic", ResourceLocationArgument.id())
                                .suggests(RelicShrineCommands::suggestRelics)
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> placeShrine(context,
                                                ResourceLocationArgument.getId(context, "relic"),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"))))))
                .then(Commands.literal("scatterall")
                        .then(Commands.argument("area", StringArgumentType.greedyString())
                                .executes(RelicShrineCommands::scatterAllInArea)))
                .then(Commands.literal("lineup")
                        .executes(context -> lineup(context, 6))
                        .then(Commands.argument("spacing", IntegerArgumentType.integer(4, 16))
                                .executes(context -> lineup(context,
                                        IntegerArgumentType.getInteger(context, "spacing")))));

        LiteralArgumentBuilder<CommandSourceStack> dragonCommand = Commands.literal("dragon")
                .then(Commands.literal("apparition")
                        .executes(context -> summonDragonApparition(context, 120))
                        .then(Commands.argument("durationTicks", IntegerArgumentType.integer(20, 1200))
                                .executes(context -> summonDragonApparition(context,
                                        IntegerArgumentType.getInteger(context, "durationTicks")))));

        dispatcher.register(Commands.literal("potionrelics")
                .requires(source -> source.hasPermission(2))
                .then(shrineCommand)
                .then(dragonCommand));
    }

    private static int placeShrine(CommandContext<CommandSourceStack> context, ResourceLocation relicId, BlockPos pos)
            throws CommandSyntaxException {
        ItemStack relic = resolveRelic(relicId);
        ServerLevel level = context.getSource().getLevel();
        if (!RelicShrineManager.placeShrine(level, pos, relic)) {
            throw INVALID_SHRINE_LOCATION.create();
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.potionrelicsmod.place_success", relic.getHoverName(),
                        pos.getX(), pos.getY(), pos.getZ()),
                true);
        return 1;
    }

    private static int scatterAllInArea(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ScatterBounds bounds = parseScatterBounds(StringArgumentType.getString(context, "area"));
        int minSeparation = RelicShrineManager.calculateAutoScatterSeparation(bounds.minX(), bounds.minZ(),
                bounds.maxX(), bounds.maxZ(), ModItems.getRelicCount());
        int placed = RelicShrineManager.scatterAllRelicsInArea(context.getSource().getLevel(), bounds.minX(),
                bounds.minZ(), bounds.maxX(), bounds.maxZ(), minSeparation);
        context.getSource().sendSuccess(
                () -> Component.translatable("command.potionrelicsmod.scatter_area_success", placed,
                        ModItems.getRelicCount(), bounds.minX(), bounds.minZ(), bounds.maxX(), bounds.maxZ(),
                        minSeparation),
                true);
        return placed;
    }

    private static int lineup(CommandContext<CommandSourceStack> context, int spacing) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        Direction forward = player.getDirection();
        Direction right = forward.getClockWise();
        List<ItemStack> relics = ModItems.getDefaultRelicStacks();
        int relicCount = relics.size();
        int centerOffset = relicCount / 2;
        BlockPos anchor = player.blockPosition().relative(forward, Math.max(6, spacing));
        int placed = 0;

        for (int index = 0; index < relicCount; index++) {
            int rowOffset = index - centerOffset;
            int x = anchor.getX() + right.getStepX() * rowOffset * spacing;
            int z = anchor.getZ() + right.getStepZ() * rowOffset * spacing;
            BlockPos shrinePos = RelicShrineManager.suggestShrinePos(level, x, z,
                    RelicShrineTheme.fromRelic(relics.get(index)));
            if (RelicShrineManager.placeShrine(level, shrinePos, relics.get(index))) {
                placed++;
            }
        }

        final int placedCount = placed;
        context.getSource().sendSuccess(
                () -> Component.translatable("command.potionrelicsmod.lineup_success", placedCount, relicCount,
                        anchor.getX(), anchor.getY(), anchor.getZ(), spacing),
                true);
        return placed;
    }

    private static int summonDragonApparition(CommandContext<CommandSourceStack> context, int durationTicks)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ModMessages.sendToPlayer(player, DragonWardApparitionPacket.forEntity(player, durationTicks));
        context.getSource().sendSuccess(
                () -> Component.translatable("command.potionrelicsmod.dragon_apparition_success", durationTicks),
                false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestRelics(CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(ModItems.getRelicIds(), builder);
    }

    private static ItemStack resolveRelic(ResourceLocation relicId) throws CommandSyntaxException {
        Item item = ForgeRegistries.ITEMS.getValue(normalizeRelicId(relicId));
        if (item == null) {
            throw INVALID_RELIC.create();
        }

        ItemStack relic = new ItemStack(item);
        if (!BaseRelic.isRelic(relic)) {
            throw INVALID_RELIC.create();
        }

        return relic;
    }

    private static ResourceLocation normalizeRelicId(ResourceLocation relicId) {
        if (relicId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            return new ResourceLocation(PotionRelicsMod.MOD_ID, relicId.getPath().toLowerCase(Locale.ROOT));
        }

        return relicId;
    }

    private static ScatterBounds parseScatterBounds(String area) throws CommandSyntaxException {
        Matcher matcher = SCATTER_AREA_PATTERN.matcher(area);
        if (!matcher.matches()) {
            throw INVALID_SCATTER_AREA.create();
        }

        int firstX = parseCoordinate(matcher.group(1));
        int firstZ = parseCoordinate(matcher.group(2));
        int secondX = parseCoordinate(matcher.group(3));
        int secondZ = parseCoordinate(matcher.group(4));

        return new ScatterBounds(Math.min(firstX, secondX), Math.min(firstZ, secondZ), Math.max(firstX, secondX),
                Math.max(firstZ, secondZ));
    }

    private static int parseCoordinate(String coordinate) throws CommandSyntaxException {
        try {
            return Integer.parseInt(coordinate);
        } catch (NumberFormatException exception) {
            throw INVALID_SCATTER_AREA.create();
        }
    }

    private static final class ScatterBounds {
        private final int minX;
        private final int minZ;
        private final int maxX;
        private final int maxZ;

        private ScatterBounds(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        private int minX() {
            return minX;
        }

        private int minZ() {
            return minZ;
        }

        private int maxX() {
            return maxX;
        }

        private int maxZ() {
            return maxZ;
        }
    }
}
