package doonv.entityselectortools.creation;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.EntitySelectorToolsClient;
import doonv.entityselectortools.compat.AxiomCompat;
import doonv.entityselectortools.config.ClientConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static doonv.entityselectortools.EntitySelectorToolsClient.COPY_AS_PREDICATE_KEY;
import static doonv.entityselectortools.EntitySelectorToolsClient.overlayMessage;

public class CreationManager {
    public static @Nullable BlockPos pos1 = null;

    public static @Nullable BlockPos pos2 = null;

    public static void register() {
        if (AxiomCompat.isAxiomLoaded()) {
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                // todo: check if any of our keybinds match Axiom's, otherwise skip this
                if (client.player != null && inWandCreationMode(client.player)) {
                    AxiomCompat.suppressAxiomCopy();
                }
            });
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !inWandCreationMode(client.player) && CreationManager.hasVolume()) {
                CreationManager.clear();
            }

            copyFunctionality(EntitySelectorToolsClient.COPY_AS_SELECTOR_KEY, client, asSelector(), "Selector");
            copyFunctionality(EntitySelectorToolsClient.COPY_AS_PREDICATE_KEY, client, asPredicate(), "Predicate");
        });
    }

    private static void copyFunctionality(KeyMapping key, Minecraft client, Optional<String> selector, String msg) {
        while (key.consumeClick()) {
            if (client.player == null || !inWandCreationMode(client.player)) continue;

            selector.ifPresentOrElse(s -> {
                        client.keyboardHandler.setClipboard(s);

                        overlayMessage(client.player, Component.translatable(
                                "%s.boxCreate.copy%sSuccess".formatted(EntitySelectorTools.MOD_ID, msg)
                        ));
                    },
                    () -> overlayMessage(client.player, Component.translatable(
                                    "%s.boxCreate.copyNoVolume".formatted(EntitySelectorTools.MOD_ID))
                            .withStyle(ChatFormatting.RED))
            );
        }
    }

    public static void clear() {
        pos1 = null;
        pos2 = null;
    }

    public static boolean hasVolume() {
        return pos1 != null || pos2 != null;
    }

    public static Optional<AABB> volume() {
        if (pos1 == null && pos2 == null) {
            return Optional.empty();
        } else if (pos2 == null) {
            return Optional.of(new AABB(pos1));
        } else if (pos1 == null) {
            return Optional.of(new AABB(pos2));
        }

        return Optional.of(AABB.encapsulatingFullBlocks(pos1, pos2));
    }

    public static void expandTo(BlockPos clickedPos) {
        if (pos1 == null) {
            pos1 = clickedPos;
            return;
        }
        if (pos2 == null) {
            pos2 = clickedPos;
            return;
        }

        int minX = Math.min(clickedPos.getX(), Math.min(pos1.getX(), pos2.getX()));
        int minY = Math.min(clickedPos.getY(), Math.min(pos1.getY(), pos2.getY()));
        int minZ = Math.min(clickedPos.getZ(), Math.min(pos1.getZ(), pos2.getZ()));

        int maxX = Math.max(clickedPos.getX(), Math.max(pos1.getX(), pos2.getX()));
        int maxY = Math.max(clickedPos.getY(), Math.max(pos1.getY(), pos2.getY()));
        int maxZ = Math.max(clickedPos.getZ(), Math.max(pos1.getZ(), pos2.getZ()));

        pos1 = new BlockPos(minX, minY, minZ);
        pos2 = new BlockPos(maxX, maxY, maxZ);
    }

    public static BlockPos getTargetPos(LocalPlayer player) {
        HitResult hit = player.pick(ClientConfig.get().airPlaceDistance, 0, false);

        if (hit instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        } else {
            return BlockPos.containing(hit.getLocation());
        }
    }

    public static Optional<String> asSelector() {
        return volume().map(s -> "x=%.0f,y=%.0f,z=%.0f,dx=%.0f,dy=%.0f,dz=%.0f".formatted(s.minX, s.minY, s.minZ,
                s.maxX - s.minX - 1, s.maxY - s.minY - 1, s.maxZ - s.minZ - 1));
    }

    public static Optional<String> asPredicate() {
        return volume().map(s -> """
                "position": {"x": {"min": %.0f, "max": %.0f}, "y": {"min": %.0f, "max": %.0f}, "z": {"min": %.0f, "max": %.0f}}
                """.formatted(s.minX, s.maxX, s.minY, s.maxY, s.minZ, s.maxZ));
    }

    public static boolean inWandCreationMode(LocalPlayer player) {
        ClientConfig config = ClientConfig.get();

        if (config.boxCreationWithAxiomTool && AxiomCompat.isSelectorBuilderToolActive())
            return true;

        return config.boxCreationWithWand
                && player.gameMode() == GameType.CREATIVE
                && player.getInventory().getSelectedItem().is(config.wandItem);
    }
}
