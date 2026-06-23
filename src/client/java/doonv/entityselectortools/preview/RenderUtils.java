package doonv.entityselectortools.preview;

//?if >=1.21.11 {

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.awt.*;
//? } else {

/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

*///?}

public class RenderUtils {
    //?if >=1.21.11 {
    public static Color multiplyAlpha(Color color, float factor) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        int newAlpha = Math.round(a * factor);
        newAlpha = Math.clamp(newAlpha, 0, 255);

        return new Color(r, g, b, newAlpha);
    }

    public record SphereGizmo(Vec3 pos, float radius, int segments, GizmoStyle style) implements Gizmo {
        @Override
        public void emit(@NonNull GizmoPrimitives primitives, float opacity) {
            // Prevent division by zero or invisible shapes
            if (this.segments < 3) return;

            // Render solid 3D sphere fill
            if (this.style.hasFill()) {
                int color = this.style.multipliedFill(opacity);
                int longs = this.segments;
                int lats = Math.max(3, this.segments / 2);

                // Precalculate longitudes (theta) to save trigonometric overhead
                double[] cosTheta = new double[longs + 1];
                double[] sinTheta = new double[longs + 1];
                for (int j = 0; j <= longs; j++) {
                    double theta = 2.0 * Math.PI * j / longs;
                    cosTheta[j] = Math.cos(theta);
                    sinTheta[j] = Math.sin(theta);
                }

                // Generate and draw latitude rings using Quads
                for (int i = 0; i < lats; i++) {
                    double phi1 = Math.PI * i / lats - Math.PI / 2.0;
                    double phi2 = Math.PI * (i + 1) / lats - Math.PI / 2.0;

                    double cosPhi1 = Math.cos(phi1);
                    double sinPhi1 = Math.sin(phi1);
                    double cosPhi2 = Math.cos(phi2);
                    double sinPhi2 = Math.sin(phi2);

                    for (int j = 0; j < longs; j++) {
                        Vec3 p1 = this.pos.add(this.radius * cosPhi1 * cosTheta[j], this.radius * sinPhi1,
                                this.radius * cosPhi1 * sinTheta[j]);
                        Vec3 p2 = this.pos.add(this.radius * cosPhi2 * cosTheta[j], this.radius * sinPhi2,
                                this.radius * cosPhi2 * sinTheta[j]);
                        Vec3 p3 = this.pos.add(this.radius * cosPhi2 * cosTheta[j + 1], this.radius * sinPhi2,
                                this.radius * cosPhi2 * sinTheta[j + 1]);
                        Vec3 p4 = this.pos.add(this.radius * cosPhi1 * cosTheta[j + 1], this.radius * sinPhi1,
                                this.radius * cosPhi1 * sinTheta[j + 1]);

                        // Emit quad in Counter-Clockwise (CCW) order (p1 -> p4 -> p3 -> p2)
                        // which is standard for standard backface culling in OpenGL
                        primitives.addQuad(p1, p4, p3, p2, color);
                    }
                }
            }

            // Render wireframe outline as 3 intersecting circles
            if (this.style.hasStroke()) {
                int color = this.style.multipliedStroke(opacity);
                float strokeWidth = this.style.strokeWidth();
                float segmentSizeRadians = (float) (Math.PI * 2 / this.segments);

                Vec3[] xzVertices = new Vec3[this.segments + 1];
                Vec3[] xyVertices = new Vec3[this.segments + 1];
                Vec3[] yzVertices = new Vec3[this.segments + 1];

                for (int i = 0; i < this.segments; i++) {
                    float f = i * segmentSizeRadians;
                    double cos = this.radius * Math.cos(f);
                    double sin = this.radius * Math.sin(f);

                    xzVertices[i] = this.pos.add(cos, 0.0, sin);
                    xyVertices[i] = this.pos.add(cos, sin, 0.0);
                    yzVertices[i] = this.pos.add(0.0, cos, sin);
                }

                // Close the loops
                xzVertices[this.segments] = xzVertices[0];
                xyVertices[this.segments] = xyVertices[0];
                yzVertices[this.segments] = yzVertices[0];

                for (int j = 0; j < this.segments; j++) {
                    primitives.addLine(xzVertices[j], xzVertices[j + 1], color, strokeWidth);
                    primitives.addLine(xyVertices[j], xyVertices[j + 1], color, strokeWidth);
                    primitives.addLine(yzVertices[j], yzVertices[j + 1], color, strokeWidth);
                }
            }
        }
    }
    //?} else {

    /*/^*
     * Renders a low-clutter sphere gizmo using 3 intersecting orthogonal rings (XY, XZ, YZ planes).
     * Ideal for visualizing ranges, radiuses, and spherical boundaries without screen clutter.
     * Ensure the VertexConsumer is set up for line segments (e.g., RenderType.lines()).
     ^/
    public static void renderSphereGizmo(PoseStack.Pose pose, VertexConsumer buffer, float centerX, float centerY, float centerZ, float radius, int segments, float red, float green, float blue, float alpha) {
        float segmentAngle = (float) (2 * Math.PI / segments);

        for (int i = 0; i < segments; i++) {
            float angle1 = i * segmentAngle;
            float angle2 = (i + 1) * segmentAngle;

            float cos1 = (float) Math.cos(angle1) * radius;
            float sin1 = (float) Math.sin(angle1) * radius;
            float cos2 = (float) Math.cos(angle2) * radius;
            float sin2 = (float) Math.sin(angle2) * radius;

            // --- Ring 1: XY Plane (Z is constant at 0) ---
            float dx1 = cos2 - cos1;
            float dy1 = sin2 - sin1;
            float len1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
            float nx1 = dx1 / len1;
            float ny1 = dy1 / len1;

            buffer.addVertex(pose, centerX + cos1, centerY + sin1, centerZ)
                    .setColor(red, green, blue, alpha).setNormal(pose, nx1, ny1, 0);
            buffer.addVertex(pose, centerX + cos2, centerY + sin2, centerZ)
                    .setColor(red, green, blue, alpha).setNormal(pose, nx1, ny1, 0);

            // --- Ring 2: XZ Plane (Y is constant at 0) ---
            float dx2 = cos2 - cos1;
            float dz2 = sin2 - sin1;
            float len2 = (float) Math.sqrt(dx2 * dx2 + dz2 * dz2);
            float nx2 = dx2 / len2;
            float nz2 = dz2 / len2;

            buffer.addVertex(pose, centerX + cos1, centerY, centerZ + sin1)
                    .setColor(red, green, blue, alpha).setNormal(pose, nx2, 0, nz2);
            buffer.addVertex(pose, centerX + cos2, centerY, centerZ + sin2)
                    .setColor(red, green, blue, alpha).setNormal(pose, nx2, 0, nz2);

            // --- Ring 3: YZ Plane (X is constant at 0) ---
            float dy3 = cos2 - cos1;
            float dz3 = sin2 - sin1;
            float len3 = (float) Math.sqrt(dy3 * dy3 + dz3 * dz3);
            float ny3 = dy3 / len3;
            float nz3 = dz3 / len3;

            buffer.addVertex(pose, centerX, centerY + cos1, centerZ + sin1)
                    .setColor(red, green, blue, alpha).setNormal(pose, 0, ny3, nz3);
            buffer.addVertex(pose, centerX, centerY + cos2, centerZ + sin2)
                    .setColor(red, green, blue, alpha).setNormal(pose, 0, ny3, nz3);
        }
    }

    public static void renderFilledSphere(PoseStack.Pose pose, VertexConsumer buffer, float centerX, float centerY, float centerZ, float radius, int latitudes, int longitudes, float red, float green, float blue, float alpha) {
        for (int i = 0; i < latitudes; i++) {
            float theta1 = (float) (i * Math.PI / latitudes);
            float theta2 = (float) ((i + 1) * Math.PI / latitudes);

            float sinTheta1 = (float) Math.sin(theta1);
            float cosTheta1 = (float) Math.cos(theta1);
            float sinTheta2 = (float) Math.sin(theta2);
            float cosTheta2 = (float) Math.cos(theta2);

            for (int j = 0; j < longitudes; j++) {
                float phi1 = (float) (j * 2 * Math.PI / longitudes);
                float phi2 = (float) ((j + 1) * 2 * Math.PI / longitudes);

                float sinPhi1 = (float) Math.sin(phi1);
                float cosPhi1 = (float) Math.cos(phi1);
                float sinPhi2 = (float) Math.sin(phi2);
                float cosPhi2 = (float) Math.cos(phi2);

                float nx1 = sinTheta1 * cosPhi1;
                float nz1 = sinTheta1 * sinPhi1;

                float nx2 = sinTheta2 * cosPhi1;
                float nz2 = sinTheta2 * sinPhi1;

                float nx3 = sinTheta2 * cosPhi2;
                float nz3 = sinTheta2 * sinPhi2;

                float nx4 = sinTheta1 * cosPhi2;
                float nz4 = sinTheta1 * sinPhi2;

                // Filled quads need the actual surface normal
                buffer.addVertex(pose, centerX + nx1 * radius, centerY + cosTheta1 * radius, centerZ + nz1 * radius)
                        .setColor(red, green, blue, alpha)
                        .setNormal(pose, nx1, cosTheta1, nz1);

                buffer.addVertex(pose, centerX + nx2 * radius, centerY + cosTheta2 * radius, centerZ + nz2 * radius)
                        .setColor(red, green, blue, alpha)
                        .setNormal(pose, nx2, cosTheta2, nz2);

                buffer.addVertex(pose, centerX + nx3 * radius, centerY + cosTheta2 * radius, centerZ + nz3 * radius)
                        .setColor(red, green, blue, alpha)
                        .setNormal(pose, nx3, cosTheta2, nz3);

                buffer.addVertex(pose, centerX + nx4 * radius, centerY + cosTheta1 * radius, centerZ + nz4 * radius)
                        .setColor(red, green, blue, alpha)
                        .setNormal(pose, nx4, cosTheta1, nz4);
            }
        }
    }
    *///?}
}
