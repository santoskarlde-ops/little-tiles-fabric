package team.creative.littletiles.client.render;

import team.creative.creativecore.client.render.face.VectorFanClient;
import team.creative.creativecore.common.util.math.vec.Vec3f;

/** Access the CreativeCore line-normal calculation used for rendered lines. */
public final class LineNormals extends VectorFanClient {
    private LineNormals() {}

    public static void set(Vec3f normal, float x1, float y1, float z1, float x2, float y2, float z2) {
        setLineNormal(normal, x1, y1, z1, x2, y2, z2);
    }
}
