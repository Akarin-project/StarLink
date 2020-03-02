package net.minecraft.server;

import cc.bukkit.starlink.annotation.ObfuscateHelper;

public class Path {

    @ObfuscateHelper("nodes") // StarLink
    private PathPoint[] a = new PathPoint[128];
    @ObfuscateHelper("index") // StarLink
    private int b;

    public Path() {}

    @ObfuscateHelper("addNode") // StarLink
    public PathPoint a(PathPoint pathpoint) {
        if (pathpoint.d >= 0) {
            throw new IllegalStateException("OW KNOWS!");
        } else {
            if (this.b == this.a.length) {
                PathPoint[] apathpoint = new PathPoint[this.b << 1];

                System.arraycopy(this.a, 0, apathpoint, 0, this.b);
                this.a = apathpoint;
            }

            this.a[this.b] = pathpoint;
            pathpoint.d = this.b;
            this.a(this.b++);
            return pathpoint;
        }
    }

    @ObfuscateHelper("resetIndex") // StarLink
    public void a() {
        this.b = 0;
    }

    @ObfuscateHelper("poll") // StarLink
    public PathPoint c() {
        PathPoint pathpoint = this.a[0];

        this.a[0] = this.a[--this.b];
        this.a[this.b] = null;
        if (this.b > 0) {
            this.b(0);
        }

        pathpoint.d = -1;
        return pathpoint;
    }

    public void a(PathPoint pathpoint, float f) {
        float f1 = pathpoint.g;

        pathpoint.g = f;
        if (f < f1) {
            this.a(pathpoint.d);
        } else {
            this.b(pathpoint.d);
        }

    }

    private void a(int i) {
        PathPoint pathpoint = this.a[i];

        int j;

        for (float f = pathpoint.g; i > 0; i = j) {
            j = i - 1 >> 1;
            PathPoint pathpoint1 = this.a[j];

            if (f >= pathpoint1.g) {
                break;
            }

            this.a[i] = pathpoint1;
            pathpoint1.d = i;
        }

        this.a[i] = pathpoint;
        pathpoint.d = i;
    }

    private void b(@ObfuscateHelper("desiredRadius") int i) { // StarLink
        PathPoint pathpoint = this.a[i];
        @ObfuscateHelper("nodeRadius") // StarLink
        float f = pathpoint.g;

        while (true) {
            @ObfuscateHelper("maxDesiredRadius") // StarLink
            int j = 1 + (i << 1);
            @ObfuscateHelper("destDistance") // StarLink
            int k = j + 1;

            if (j >= this.b) {
                break;
            }

            @ObfuscateHelper("fareastNode") // StarLink
            PathPoint pathpoint1 = this.a[j];
            @ObfuscateHelper("nearRadius") // StarLink
            float f1 = pathpoint1.g;
            @ObfuscateHelper("possibleDest") // StarLink
            PathPoint pathpoint2;
            @ObfuscateHelper("destRadius") // StarLink
            float f2;

            if (k >= this.b) {
                pathpoint2 = null;
                f2 = Float.POSITIVE_INFINITY;
            } else {
                pathpoint2 = this.a[k];
                f2 = pathpoint2.g;
            }

            if (f1 < f2) {
                if (f1 >= f) {
                    break;
                }

                this.a[i] = pathpoint1;
                pathpoint1.d = i;
                i = j;
            } else {
                if (f2 >= f) {
                    break;
                }

                this.a[i] = pathpoint2;
                pathpoint2.d = i;
                i = k;
            }
        }

        this.a[i] = pathpoint;
        pathpoint.d = i;
    }

    @ObfuscateHelper("isFresh") // StarLink
    public boolean e() {
        return this.b == 0;
    }
}
