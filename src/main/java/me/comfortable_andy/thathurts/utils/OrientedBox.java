package me.comfortable_andy.thathurts.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static me.comfortable_andy.thathurts.utils.PositionUtil.*;

@Accessors(chain = true)
@Getter
@Setter
@ToString
public class OrientedBox extends OrientedCollider {

    public OrientedBox(Vector3f center, Vector3f min, Vector3f max) {
        super(center, initAABB(new Vector3f(min), new Vector3f(max)));
    }

    public OrientedBox(Vector center, Vector relativeMin, Vector relativeMax) {
        this(
                convertJoml(center),
                convertJoml(relativeMin),
                convertJoml(relativeMax)
        );
    }

    public OrientedBox(BoundingBox box) {
        this(box.getCenter(), box.getMin().subtract(box.getCenter()), box.getMax().subtract(box.getCenter()));
    }

    @Override
    public Collection<Side> computeSides() {
        return super.computeSides();
    }

    public static List<Vertex> initAABB(Vector3f min, Vector3f max) {
        final Vector3f extents = max.sub(min, new Vector3f());
        Vertex[] vert = new Vertex[8];
        vert[7] = new Vertex(new Vector3f(max));
        vert[6] = new Vertex(new Vector3f(max).sub(extents.x, 0, 0))
                .connect(vert[7]);
        vert[5] = new Vertex(new Vector3f(max).sub(0, 0, extents.z))
                .connect(vert[7]);
        vert[4] = new Vertex(new Vector3f(max).sub(extents.x, 0, extents.z))
                .connect(vert[6]).connect(vert[5]);
        vert[3] = new Vertex(new Vector3f(min).add(extents.x, 0, extents.z))
                .connect(vert[7]);
        vert[2] = new Vertex(new Vector3f(min).add(extents.x, 0, 0))
                .connect(vert[5]).connect(vert[3]);
        vert[1] = new Vertex(new Vector3f(min).add(0, 0, extents.z))
                .connect(vert[6]).connect(vert[3]);
        vert[0] = new Vertex(new Vector3f(min))
                .connect(vert[4]).connect(vert[2]).connect(vert[1]);
        return new ArrayList<>(Arrays.asList(vert));
    }

}