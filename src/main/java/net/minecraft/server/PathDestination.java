package net.minecraft.server;

import cc.bukkit.starlink.annotation.ObfuscateHelper;

public class PathDestination extends PathPoint {

    @ObfuscateHelper("maxDistance") // StarLink
    private float m = Float.MAX_VALUE;
    @ObfuscateHelper("progress") // StarLink
    private PathPoint n;
    @ObfuscateHelper("finished") // StarLink
    private boolean o;
    @javax.annotation.Nullable protected BlockPosition position; // StarLink - add field

    public PathDestination(PathPoint pathpoint) {
        super(pathpoint.a, pathpoint.b, pathpoint.c);
    }

    @ObfuscateHelper("setProgressNode") // StarLink
    public void a(float f, PathPoint pathpoint) {
        if (f < this.m) {
            this.m = f;
            this.n = pathpoint;
        }

    }

    @ObfuscateHelper("lastProgressNode") // StarLink
    public PathPoint d() {
        return this.n;
    }

    @ObfuscateHelper("finishPathfind") // StarLink
    public void e() {
        this.o = true;
    }

    @ObfuscateHelper("isFinished") // StarLink
    public boolean f() {
        return this.o;
    }
}
