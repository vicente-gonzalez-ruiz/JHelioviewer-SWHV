package org.helioviewer.jhv.camera.annotate;

import org.helioviewer.jhv.base.Colors;
import org.helioviewer.jhv.camera.Camera;
import org.helioviewer.jhv.camera.InteractionAnnotate.AnnotationMode;
import org.helioviewer.jhv.display.Display;
import org.helioviewer.jhv.display.Viewport;
import org.helioviewer.jhv.math.Vec2;
import org.helioviewer.jhv.math.Vec3;
import org.helioviewer.jhv.opengl.BufVertex;
import org.helioviewer.jhv.opengl.GLHelper;
import org.helioviewer.jhv.position.Position;
import org.json.JSONObject;

public class AnnotateCircle extends AbstractAnnotateable {

    private static final int SUBDIVISIONS = 90;

    public AnnotateCircle(JSONObject jo) {
        super(jo);
    }

    private static void drawCircle(Position viewpoint, Viewport vp, Vec3 bp, Vec3 ep, BufVertex buf, byte[] color) {
        double cosf = Vec3.dot(bp, ep);
        double r = Math.sqrt(1 - cosf * cosf);
        // P = center + r cos(A) (bp x ep) + r sin(A) ep

        Vec3 center = Vec3.multiply(bp, cosf * radius);
        Vec3 u = Vec3.cross(bp, ep);
        u.normalize();
        Vec3 v = Vec3.cross(bp, u);

        Vec3 vx = new Vec3();
        Vec2 previous = null;

        for (int i = 0; i <= SUBDIVISIONS; i++) {
            double t = i * 2. * Math.PI / SUBDIVISIONS;
            double cosr = Math.cos(t) * r;
            double sinr = Math.sin(t) * r;
            vx.x = center.x + cosr * u.x + sinr * v.x;
            vx.y = center.y + cosr * u.y + sinr * v.y;
            vx.z = center.z + cosr * u.z + sinr * v.z;
            if (Display.mode == Display.DisplayMode.Orthographic) {
                if (i == 0) {
                    buf.putVertex(vx, Colors.Null);
                }
                buf.putVertex(vx, color);
                if (i == SUBDIVISIONS) {
                    buf.putVertex(vx, Colors.Null);
                }
            } else {
                vx.y = -vx.y;
                if (i == 0) {
                    GLHelper.drawVertex(viewpoint, vp, vx, previous, buf, Colors.Null);
                }
                previous = GLHelper.drawVertex(viewpoint, vp, vx, previous, buf, color);
                if (i == SUBDIVISIONS) {
                    GLHelper.drawVertex(viewpoint, vp, vx, previous, buf, Colors.Null);
                }
            }
        }
    }

    @Override
    public void draw(Position viewpoint, Viewport vp, boolean active, BufVertex buf) {
        boolean dragged = beingDragged();
        if ((startPoint == null || endPoint == null) && !dragged)
            return;

        byte[] color = dragged ? dragColor : (active ? activeColor : baseColor);
        Vec3 p0 = dragged ? dragStartPoint : startPoint;
        Vec3 p1 = dragged ? dragEndPoint : endPoint;

        drawCircle(viewpoint, vp, p0, p1, buf, color);
    }

    @Override
    public void mousePressed(Camera camera, int x, int y) {
        Vec3 pt = computePoint(camera, x, y);
        if (pt != null)
            dragStartPoint = pt;
    }

    @Override
    public void mouseDragged(Camera camera, int x, int y) {
        Vec3 pt = computePoint(camera, x, y);
        if (pt != null)
            dragEndPoint = pt;
    }

    @Override
    public void mouseReleased() {
        if (beingDragged()) {
            startPoint = dragStartPoint;
            endPoint = dragEndPoint;
        }
        dragStartPoint = null;
        dragEndPoint = null;
    }

    @Override
    public boolean beingDragged() {
        return dragEndPoint != null && dragStartPoint != null;
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public String getType() {
        return AnnotationMode.Circle.toString();
    }

}
