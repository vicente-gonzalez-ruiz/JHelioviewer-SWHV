package org.helioviewer.jhv.base.scale;

import org.helioviewer.jhv.math.MathUtils;
import org.helioviewer.jhv.math.Quat;
import org.helioviewer.jhv.math.Vec2;
import org.helioviewer.jhv.math.Vec3;
import org.helioviewer.jhv.position.Position;

public interface Transform {

    Vec2 transform(Position viewpoint, Vec3 pt, GridScale scale);

    Vec3 transformInverse(Quat frame, Vec2 pt);

    Transform transformpolar = new TransformPolar();
    Transform transformlatitudinal = new TransformLatitudinal();

    class TransformPolar implements Transform {
        @Override
        public Vec2 transform(Position viewpoint, Vec3 pt, GridScale scale) {
            Quat q = new Quat(viewpoint.lat, 0);
            pt = q.rotateInverseVector(pt);
            double r = Math.sqrt(pt.x * pt.x + pt.y * pt.y);
            double theta = Math.atan2(-pt.x, -pt.y);
            theta += 2 * Math.PI;
            theta %= 2 * Math.PI;
            double scaledr = scale.getYValueInv(r);
            double scaledtheta = scale.getXValueInv(theta * MathUtils.radeg);
            return new Vec2(scaledtheta, scaledr);
        }

        @Override
        public Vec3 transformInverse(Quat frame, Vec2 pt) {
            double r = pt.y;
            double theta = -pt.x / MathUtils.radeg;
            double y = r * Math.cos(theta);
            double x = r * Math.sin(theta);
            double z = Math.sqrt(Math.max(0, 1 - x * x - y * y));
            return frame.rotateInverseVector(new Vec3(x, y, z));
        }
    }

    class TransformLatitudinal implements Transform {
        @Override
        public Vec2 transform(Position viewpoint, Vec3 pt, GridScale scale) {
            double theta = Math.PI / 2 - Math.acos(-pt.y);
            double phi = Math.atan2(pt.x, pt.z);
            phi -= Math.PI + viewpoint.lon;
            phi += 6 * Math.PI;
            phi %= 2 * Math.PI;
            double scaledphi = scale.getXValueInv(phi * MathUtils.radeg);
            double scaledtheta = scale.getYValueInv(theta * MathUtils.radeg);
            return new Vec2(scaledphi, scaledtheta);
        }

        @Override
        public Vec3 transformInverse(Quat frame, Vec2 pt) {
            double phi = pt.x / MathUtils.radeg;
            double theta = pt.y / MathUtils.radeg;
            phi += Math.PI;
            theta = -theta + Math.PI / 2;
            return new Vec3(Math.sin(theta) * Math.sin(phi), Math.cos(theta), Math.sin(theta) * Math.cos(phi));
        }
    }

}
