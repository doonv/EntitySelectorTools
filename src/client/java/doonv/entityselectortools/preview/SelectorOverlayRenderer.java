package doonv.entityselectortools.preview;

import doonv.entityselectortools.config.ClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//? if >=1.21.11 {
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
//?} else {
/*import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
*///?}

public class SelectorOverlayRenderer {
    /// Permanent cache for Command Blocks' entity selector volumes (updates via {@link CommandBlockManager})
    public static final Map<BlockPos, List<EntitySelectorVolume>> COMMAND_BLOCK_SELECTORS = new ConcurrentHashMap<>();

    public static final Map<Integer, List<VolumeTemplate>> COMMAND_MINECART_TEMPLATES = new ConcurrentHashMap<>();

    /// Temporary cache for {@link EntitySelectorVolume}s checked by the server (usually only datapacks)
    private static final List<EphemeralVolume> SERVER_VOLUMES = new ArrayList<>();

    /// Computes an appropriate number of sphere segments based on its apparent size on screen.
    /// Uses a square-root curve: perceived detail scales with `sqrt(angular size)`,
    /// so `segments = sqrt(radius / distance) * maxSegments`.
    private static int computeSphereSegments(Vec3 center, float radius, Camera camera, int maxSegments) {
        //~ if >=1.21.11 '.getPosition()' -> '.position()'
        double dist = center.distanceTo(camera.position());
        if (dist < radius) return maxSegments;
        double angularRadius = radius / dist;
        int segments = (int) (Math.sqrt(angularRadius) * maxSegments);
        return Math.clamp(segments, 4, maxSegments);
    }

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(SelectorOverlayRenderer::render);
    }

    public static void removeMinecartEntry(int entityId) {
        COMMAND_MINECART_TEMPLATES.remove(entityId);
    }

    public static void addServerSelector(EntitySelectorVolume volume) {
        long expiry = System.currentTimeMillis() + ClientConfig.get().serverVolumeExpiryTimeMillis;
        synchronized (SERVER_VOLUMES) {
            SERVER_VOLUMES.add(new EphemeralVolume(volume, expiry));
        }
    }

    //? if >=1.21.11 {
    private static void render(LevelRenderContext context) {
        ClientConfig config = ClientConfig.get();
        //~ if >=26.2 'getMainCamera()' -> 'mainCamera()'
        Camera camera = context.gameRenderer().mainCamera();

        if (ClientConfig.get().commandBlockVolumesEnabled) {
            COMMAND_BLOCK_SELECTORS.forEach((_, volumes) -> {
                for (EntitySelectorVolume volume : volumes) {
                    renderVolume(volume, camera, config);
                }
            });

            var level = Minecraft.getInstance().level;
            if (level != null) {
                COMMAND_MINECART_TEMPLATES.forEach((entityId, templates) -> {
                    Entity entity = level.getEntity(entityId);
                    if (entity == null) return;
                    Vec3 pos = entity.getPosition(
                            Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
                    for (VolumeTemplate template : templates) {
                        renderVolume(template.evaluate(pos), camera, config);
                    }
                });
            }
        }

        if (ClientConfig.get().serverVolumesEnabled) {
            long now = System.currentTimeMillis();
            synchronized (SERVER_VOLUMES) {
                Iterator<EphemeralVolume> it = SERVER_VOLUMES.iterator();

                while (it.hasNext()) {
                    EphemeralVolume vol = it.next();
                    if (now > vol.expiryTime) {
                        it.remove();
                    } else {
                        renderVolume(vol.volume, camera, config);
                    }
                }
            }
        }
    }

    private static void renderVolume(EntitySelectorVolume volume, Camera camera, ClientConfig config) {
        Color boxColor = config.getBoxColor(volume);
        Color sphereMinColor = config.getSphereMinColor(volume);
        Color sphereMaxColor = config.getSphereMaxColor(volume);
        volume.aabb().ifPresent(vol -> Gizmos.cuboid(vol, GizmoStyle.stroke(boxColor.getRGB())));
        volume.distance().min().ifPresent(radius -> {
            GizmoStyle s = GizmoStyle.strokeAndFill(sphereMinColor.getRGB(), 2.5F,
                    RenderUtils.multiplyAlpha(sphereMinColor,
                            config.sphereBackgroundTransparency).getRGB());
            int segments = config.adaptiveSphereResolution
                    ? computeSphereSegments(volume.center(), radius.floatValue(), camera, config.sphereSegments)
                    : config.sphereSegments;
            Gizmos.addGizmo(new RenderUtils.SphereGizmo(volume.center(), radius.floatValue(),
                    segments, s));
        });
        volume.distance().max().ifPresent(radius -> {
            GizmoStyle s = GizmoStyle.strokeAndFill(sphereMaxColor.getRGB(), 2.5F,
                    RenderUtils.multiplyAlpha(sphereMaxColor,
                            config.sphereBackgroundTransparency).getRGB());
            int segments = config.adaptiveSphereResolution
                    ? computeSphereSegments(volume.center(), radius.floatValue(), camera, config.sphereSegments)
                    : config.sphereSegments;
            Gizmos.addGizmo(new RenderUtils.SphereGizmo(volume.center(), radius.floatValue(),
                    segments, s));
        });
    }
    //?} else {
    /*private static void render(LevelRenderContext context) {
        PoseStack poseStack = context.matrices();
        Camera camera = context.gameRenderer().getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        ClientConfig config = ClientConfig.get();
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        VertexConsumer backgroundConsumer = context.consumers().getBuffer(RenderType.debugStructureQuads());

        iterCommandBlockSelectorVolumes(volume ->
                drawVolumeBackground(poseStack.last(), backgroundConsumer, volume, camera, config));
        iterMinecartSelectorVolumes(tickDelta, volume ->
                drawVolumeBackground(poseStack.last(), backgroundConsumer, volume, camera, config));
        if (ClientConfig.get().serverVolumesEnabled)
            iterServerSelectorVolumes(volume ->
                    drawVolumeBackground(poseStack.last(), backgroundConsumer, volume, camera, config));

        VertexConsumer linesConsumer = context.consumers().getBuffer(RenderType.lines());
        iterCommandBlockSelectorVolumes(volume ->
                drawVolumeLines(poseStack.last(), linesConsumer, volume, camera, config));
        iterMinecartSelectorVolumes(tickDelta, volume ->
                drawVolumeLines(poseStack.last(), linesConsumer, volume, camera, config));

        if (ClientConfig.get().serverVolumesEnabled)
            iterServerSelectorVolumes(volume ->
                    drawVolumeLines(poseStack.last(), linesConsumer, volume, camera, config));

        poseStack.popPose();
    }

    private static void iterCommandBlockSelectorVolumes(Consumer<EntitySelectorVolume> f) {
        if (!ClientConfig.get().commandBlockVolumesEnabled) return;

        COMMAND_BLOCK_SELECTORS.forEach((pos, volumes) -> {
            for (EntitySelectorVolume volume : volumes) {
                f.accept(volume);
            }
        });
    }

    private static void iterMinecartSelectorVolumes(float tickDelta, Consumer<EntitySelectorVolume> f) {
        if (!ClientConfig.get().commandBlockVolumesEnabled) return;

        var level = Minecraft.getInstance().level;
        if (level == null) return;
        COMMAND_MINECART_TEMPLATES.forEach((entityId, templates) -> {
            Entity entity = level.getEntity(entityId);
            if (entity == null) return;
            Vec3 pos = entity.getPosition(tickDelta);
            for (VolumeTemplate template : templates) {
                f.accept(template.evaluate(pos));
            }
        });
    }

    private static void drawVolumeLines(PoseStack.Pose pose, VertexConsumer lines, EntitySelectorVolume volume, Camera camera, ClientConfig config) {
        Color boxColor = config.getBoxColor(volume);
        Color minColor = config.getSphereMinColor(volume);
        Color maxColor = config.getSphereMaxColor(volume);
        float[] c = boxColor.getRGBComponents(null);
        volume.aabb().ifPresent(aabb ->
                ShapeRenderer.renderLineBox(pose, lines, aabb, c[0], c[1], c[2], c[3])
        );

        var center = volume.center();
        minColor.getRGBComponents(c);
        volume.distance().min().ifPresent(min -> {
            int segments = config.adaptiveSphereResolution
                    ? computeSphereSegments(volume.center(), min.floatValue(), camera, config.sphereSegments)
                    : config.sphereSegments;
            RenderUtils.renderSphereGizmo(pose, lines, (float) center.x, (float) center.y, (float) center.z,
                    min.floatValue(), segments, c[0], c[1], c[2], c[3]);
        });
        maxColor.getRGBComponents(c);
        volume.distance().max().ifPresent(max -> {
            int segments = config.adaptiveSphereResolution
                    ? computeSphereSegments(volume.center(), max.floatValue(), camera, config.sphereSegments)
                    : config.sphereSegments;
            RenderUtils.renderSphereGizmo(pose, lines, (float) center.x, (float) center.y, (float) center.z,
                    max.floatValue(), segments, c[0], c[1], c[2], c[3]);
        });
    }

    private static void drawVolumeBackground(PoseStack.Pose pose, VertexConsumer solid, EntitySelectorVolume volume, Camera camera, ClientConfig config) {
        Color color = config.getSphereMaxColor(volume);
        float[] c = color.getRGBComponents(null);
        c[3] *= config.sphereBackgroundTransparency;
        var center = volume.center();
        volume.distance().max().ifPresent(max -> {
            int segments = config.adaptiveSphereResolution
                    ? computeSphereSegments(volume.center(), max.floatValue(), camera, config.sphereSegments)
                    : config.sphereSegments;
            RenderUtils.renderFilledSphere(pose, solid, (float) center.x, (float) center.y, (float) center.z,
                    max.floatValue(), segments, segments, c[0], c[1], c[2], c[3]);
        });
    }

    private static void iterServerSelectorVolumes(Consumer<EntitySelectorVolume> f) {
        long now = System.currentTimeMillis();
        synchronized (SERVER_VOLUMES) {
            Iterator<EphemeralVolume> it = SERVER_VOLUMES.iterator();

            while (it.hasNext()) {
                EphemeralVolume vol = it.next();
                if (now > vol.expiryTime) {
                    it.remove();
                } else {
                    f.accept(vol.volume);
                }
            }
        }
    }
    *///?}

    private record EphemeralVolume(EntitySelectorVolume volume, long expiryTime) {}
}
