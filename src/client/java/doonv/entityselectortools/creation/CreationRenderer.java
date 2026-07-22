package doonv.entityselectortools.creation;

import doonv.entityselectortools.config.ClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;

import net.minecraft.core.BlockPos;


//? if >= 1.21.11 {
import doonv.entityselectortools.preview.RenderUtils;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Util;

import java.util.concurrent.TimeUnit;
//?} else {
/*import net.minecraft.Util;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
*///?}

public class CreationRenderer {
    //? if <1.21.11 {
    /*private static final RenderPipeline DEBUG_STRIP_NO_DEPTH = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/debug_strip_no_depth")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build()
    );

    private static final RenderType DEBUG_TRIANGLE_STRIP_TRANSLUCENT = RenderType.create(
            "debug_triangle_strip_translucent",
            1536,
            false,
            true,
            DEBUG_STRIP_NO_DEPTH,
            RenderType.CompositeState.builder()
                    .createCompositeState(false)
    );
    *///?}


    public static void register() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(CreationRenderer::render);
    }

    public static void render(WorldRenderContext context) {
        if (!(Minecraft.getInstance().player instanceof LocalPlayer player && context.gameRenderer() instanceof GameRenderer renderer))
            return;
        if (!CreationManager.inWandCreationMode(player)) {
            renderer.setRenderBlockOutline(true);
            return;
        }
        renderer.setRenderBlockOutline(false);
        BlockPos target = CreationManager.getTargetPos(player);

        ClientConfig config = ClientConfig.get();
        //? if >=1.21.11 {
        CreationManager.volume().ifPresent(volume -> {
            //~ if >=26.2 'timeSource.' -> 'timeSource().'
            double secs = (double) Util.timeSource.get(TimeUnit.MICROSECONDS) / 500000;
            int bgColor = RenderUtils.multiplyAlpha(config.selectionColor,
                    ((float) Math.sin(secs) + 1.3f) / 6).getRGB();
            Gizmos.cuboid(volume, GizmoStyle.strokeAndFill(config.selectionColor.getRGB(), 2.5F, bgColor));
        });
        Gizmos.cuboid(target, GizmoStyle.stroke(config.selectionColor.brighter().getRGB()));
        //?} else {
        /*PoseStack poseStack = context.matrices();
        Vec3 cameraPos = renderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer lines = context.consumers().getBuffer(RenderType.lines());

        float[] c = config.selectionColor.getRGBComponents(null);
        CreationManager.volume().ifPresent(
                volume -> ShapeRenderer.renderLineBox(poseStack.last(), lines, volume, c[0], c[1], c[2], c[3]));

        ShapeRenderer.renderLineBox(poseStack.last(), lines, new AABB(target), c[0], c[1], c[2], c[3]);

        VertexConsumer fill = context.consumers().getBuffer(DEBUG_TRIANGLE_STRIP_TRANSLUCENT);
        CreationManager.volume().ifPresent(volume -> {
            double secs = (double) Util.getMillis() / 500;
            ShapeRenderer.addChainedFilledBoxVertices(poseStack, fill, volume.minX, volume.minY, volume.minZ,
                    volume.maxX, volume.maxY, volume.maxZ, c[0], c[1], c[2],
                    c[3] * ((float) Math.sin(secs) + 1.3f) / 6);
        });

        poseStack.popPose();
        *///?}
    }
}
