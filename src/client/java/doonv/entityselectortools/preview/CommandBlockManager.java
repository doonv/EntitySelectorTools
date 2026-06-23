package doonv.entityselectortools.preview;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandBlockManager {
    public static final Map<Integer, BlockPos> PENDING_QUERIES = new ConcurrentHashMap<>();

    private static final AtomicInteger TRANSACTION_ID_COUNTER = new AtomicInteger(0);

    public static void register() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || !hasOp(player)) {
                return;
            }
            if (blockEntity instanceof CommandBlockEntity) {
                requestData(blockEntity);
            }
        });

        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof CommandBlockEntity) {
                BlockPos pos = blockEntity.getBlockPos();
                SelectorOverlayRenderer.COMMAND_BLOCK_SELECTORS.remove(pos);
                PENDING_QUERIES.entrySet().removeIf(e -> e.getValue().equals(pos));
            }
        });

        ClientEntityEvents.ENTITY_UNLOAD.register(
                (entity, level) -> SelectorOverlayRenderer.removeMinecartEntry(entity.getId()));
    }

    private static boolean hasOp(LocalPlayer player) {
        //? if >=1.21.11 {
        return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS));
        //?} else {
        /*return player.getPermissionLevel() >= 2;
         *///?}
    }

    public static void clear() {
        PENDING_QUERIES.clear();
        SelectorOverlayRenderer.COMMAND_BLOCK_SELECTORS.clear();
        SelectorOverlayRenderer.COMMAND_MINECART_TEMPLATES.clear();
    }

    private static void requestData(BlockEntity be) {
        BlockPos pos = be.getBlockPos();

        if (SelectorOverlayRenderer.COMMAND_BLOCK_SELECTORS.containsKey(pos)) return;

        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            int transactionId = TRANSACTION_ID_COUNTER.getAndIncrement();
            PENDING_QUERIES.put(transactionId, pos);
            connection.send(new ServerboundBlockEntityTagQueryPacket(transactionId, pos));
        }
    }

    public static void updateBlock(BlockPos pos, String command) {
        var selectors = SelectorParserUtils.parseSelectors(command, pos);
        if (!selectors.isEmpty()) {
            SelectorOverlayRenderer.COMMAND_BLOCK_SELECTORS.put(pos, selectors);
        } else {
            SelectorOverlayRenderer.COMMAND_BLOCK_SELECTORS.remove(pos);
        }
    }

    public static void updateEntity(int entityId, String command) {
        if (!command.isEmpty()) {
            SelectorOverlayRenderer.COMMAND_MINECART_TEMPLATES.put(entityId,
                    SelectorParserUtils.parseSelectors(command));
        } else {
            SelectorOverlayRenderer.removeMinecartEntry(entityId);
        }
    }

    public static void onQueryResponse(int transactionId, CompoundTag tag) {
        BlockPos pos = PENDING_QUERIES.remove(transactionId);
        if (pos != null) {
            tag.getString("Command").ifPresent(command -> updateBlock(pos, command));
        }
    }
}
