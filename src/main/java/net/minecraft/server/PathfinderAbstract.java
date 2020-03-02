package net.minecraft.server;

import cc.bukkit.starlink.annotation.ObfuscateHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class PathfinderAbstract {

    protected ChunkCache a;
    @ObfuscateHelper("owner") // StarLink
    protected EntityInsentient b;
    @ObfuscateHelper("allNodes") // StarLink
    protected final Int2ObjectMap<PathPoint> c = new Int2ObjectOpenHashMap();
    @ObfuscateHelper("unitWidth") // StarLink
    protected int d;
    @ObfuscateHelper("unitHeight") // StarLink
    protected int e;
    @ObfuscateHelper("unitWidth") // StarLink
    protected int f;
    protected boolean g;
    protected boolean h;
    protected boolean i;

    public PathfinderAbstract() {}

    @ObfuscateHelper("open") // StarLink
    public void a(ChunkCache chunkcache, EntityInsentient entityinsentient) {
        this.a = chunkcache;
        this.b = entityinsentient;
        this.c.clear();
        this.d = MathHelper.d(entityinsentient.getWidth() + 1.0F);
        this.e = MathHelper.d(entityinsentient.getHeight() + 1.0F);
        this.f = MathHelper.d(entityinsentient.getWidth() + 1.0F);
    }

    @ObfuscateHelper("close") // StarLink
    public void a() {
        this.a = null;
        this.b = null;
    }

    @ObfuscateHelper("getNodeAt") // StarLink
    protected PathPoint a(int i, int j, int k) {
        return (PathPoint) this.c.computeIfAbsent(PathPoint.b(i, j, k), (l) -> {
            return new PathPoint(i, j, k);
        });
    }

    @ObfuscateHelper("getNodeByEntity") // StarLink
    public abstract PathPoint b();

    public abstract PathDestination a(double d0, double d1, double d2);

    @ObfuscateHelper("measureNodesAround") // StarLink
    public abstract int a(@ObfuscateHelper("container") PathPoint[] apathpoint, @ObfuscateHelper("origin") PathPoint pathpoint); // StarLink

    public abstract PathType a(IBlockAccess iblockaccess, int i, int j, int k, EntityInsentient entityinsentient, int l, int i1, int j1, boolean flag, boolean flag1);

    public abstract PathType a(IBlockAccess iblockaccess, int i, int j, int k);

    public void a(boolean flag) {
        this.g = flag;
    }

    public void b(boolean flag) {
        this.h = flag;
    }

    public void c(boolean flag) {
        this.i = flag;
    }

    public boolean c() {
        return this.g;
    }

    public boolean d() {
        return this.h;
    }

    public boolean e() {
        return this.i;
    }
}
