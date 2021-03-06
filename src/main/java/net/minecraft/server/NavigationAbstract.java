package net.minecraft.server;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cc.bukkit.starlink.annotation.ObfuscateHelper;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public abstract class NavigationAbstract {

    protected final EntityInsentient a;
    protected final World b;
    @Nullable
    protected PathEntity c;
    protected double d;
    private final AttributeInstance p;
    protected int e;
    protected int f;
    protected Vec3D g;
    protected Vec3D h;
    protected long i;
    protected long j;
    protected double k;
    protected float l;
    @ObfuscateHelper("needPathfind") // StarLink
    protected boolean m;
    protected long n;
    protected PathfinderAbstract o;
    @ObfuscateHelper("origin") // StarLink
    private BlockPosition q;
    private int r;
    private float s;
    private final Pathfinder t;
    private long lastPathfindAsync; // StarLink

    public NavigationAbstract(EntityInsentient entityinsentient, World world) {
        this.g = Vec3D.a;
        this.h = Vec3D.a;
        this.l = 0.5F;
        this.s = 1.0F;
        this.a = entityinsentient;
        this.b = world;
        this.p = entityinsentient.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        int i = MathHelper.floor(this.p.getValue() * 16.0D);

        this.t = this.a(i);
    }

    public void g() {
        this.s = 1.0F;
    }

    public void a(float f) {
        this.s = f;
    }

    public BlockPosition h() {
        return this.q;
    }

    protected abstract Pathfinder a(int i);

    public void a(double d0) {
        this.d = d0;
    }

    public boolean i() {
        return this.m;
    }

    @ObfuscateHelper("doPathfind") // StarLink
    public void j() {
        if (this.b.getTime() - this.n > 20L) {
            if (this.q != null) {
                this.c = null;
                this.c = this.a(this.q, this.r);
                this.n = this.b.getTime();
                this.m = false;
            }
        } else {
            this.m = true;
        }

    }
    // StarLink start
    public void doPathfind(ChunkCache cache) {
        PathEntity result = findPath(cache, Collections.singleton(this.q), this.r);
        NavigationAbstract.this.b.getMinecraftServer().processQueue.add(() -> {
            if (result != null && result.k() != null)
                this.q = result.k();
            
            NavigationAbstract.this.c = result;
    	    lastPathfindAsync = this.b.getTime();
        });
    }
    
    public ChunkCache cacheChunk() {
        float f = (float) this.p.getValue();
        BlockPosition blockposition = new BlockPosition(this.a);
        int k = (int) (f + (float) 8);
        return new ChunkCache(this.b, blockposition.b(-k, -k, -k), blockposition.b(k, k, k));
    }
    
    public boolean needPathfind() {
	++this.e;
	if (this.b.getTime() - this.lastPathfindAsync > 20L) {
            if (this.q != null) {
                return this.c == null || this.c.b();
            }
        }
	return false;
    }
    // StarLink end

    @Nullable
    public final PathEntity a(double d0, double d1, double d2, int i) {
        return this.a(new BlockPosition(d0, d1, d2), i);
    }

    @Nullable
    public PathEntity a(Stream<BlockPosition> stream, int i) {
        return this.a((Set) stream.collect(Collectors.toSet()), 8, false, i);
    }

    @Nullable
    public PathEntity a(BlockPosition blockposition, int i) {
        return this.a(ImmutableSet.of(blockposition), 8, false, i);
    }

    @Nullable
    public PathEntity a(Entity entity, int i) {
        return this.a(ImmutableSet.of(new BlockPosition(entity)), 16, true, i);
    }

    @Nullable
    @ObfuscateHelper("findPath") // StarLink
    protected PathEntity a(@ObfuscateHelper("allowedOrigins") Set<BlockPosition> set, int i, boolean flag, int j) { // StarLink
        if (set.isEmpty()) {
            return null;
        } else if (this.a.locY() < 0.0D) {
            return null;
        } else if (!this.a()) {
            return null;
        } else if (this.c != null && !this.c.b() && set.contains(this.q)) {
            return this.c;
        } else {
            this.b.getMethodProfiler().enter("pathfind");
            float f = (float) this.p.getValue();
            BlockPosition blockposition = flag ? (new BlockPosition(this.a)).up() : new BlockPosition(this.a);
            int k = (int) (f + (float) i);
            ChunkCache chunkcache = new ChunkCache(this.b, blockposition.b(-k, -k, -k), blockposition.b(k, k, k));
            PathEntity pathentity = this.t.a(chunkcache, this.a, set, f, j, this.s);

            this.b.getMethodProfiler().exit();
            if (pathentity != null && pathentity.k() != null) {
                this.q = pathentity.k();
                this.r = j;
            }

            return pathentity;
        }
    }
    // StarLink start
    protected PathEntity findPath(ChunkCache cache, Set<BlockPosition> set, int j) {
        if (this.a.locY() < 0.0D) {
            return null;
        } else if (!this.a()) {
            return null;
        } else {
            return this.t.a(cache, this.a, set, f, j, this.s);
        }
    }
    // StarLink end

    public boolean a(double d0, double d1, double d2, double d3) {
        return this.a(this.a(d0, d1, d2, 1), d3);
    }

    public boolean a(Entity entity, double d0) {
        PathEntity pathentity = this.a(entity, 1);

        return pathentity != null && this.a(pathentity, d0);
    }

    public boolean a(@Nullable PathEntity pathentity, double d0) {
        if (pathentity == null) {
            this.c = null;
            return false;
        } else {
            if (!pathentity.a(this.c)) {
                this.c = pathentity;
            }

            if (this.m()) {
                return false;
            } else {
                this.F_();
                if (this.c.e() <= 0) {
                    return false;
                } else {
                    this.d = d0;
                    Vec3D vec3d = this.b();

                    this.f = this.e;
                    this.g = vec3d;
                    return true;
                }
            }
        }
    }

    @Nullable
    public PathEntity k() {
        return this.c;
    }

    @ObfuscateHelper("doTick") // StarLink
    public void c() {
        ++this.e;
        if (this.m) {
            this.j();
        }

        if (!this.m()) {
            Vec3D vec3d;

            if (this.a()) {
                this.l();
            } else if (this.c != null && this.c.f() < this.c.e()) {
                vec3d = this.b();
                Vec3D vec3d1 = this.c.a(this.a, this.c.f());

                if (vec3d.y > vec3d1.y && !this.a.onGround && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                    this.c.c(this.c.f() + 1);
                }
            }

            PacketDebug.a(this.b, this.a, this.c, this.l);
            if (!this.m()) {
                vec3d = this.c.a((Entity) this.a);
                BlockPosition blockposition = new BlockPosition(vec3d);

                this.a.getControllerMove().a(vec3d.x, this.b.getType(blockposition.down()).isAir() ? vec3d.y : PathfinderNormal.a((IBlockAccess) this.b, blockposition), vec3d.z, this.d);
            }
        }
    }
    // StarLink start
    public void doTick(PathEntity pathEntity) {
	if (shouldContinuePathfind(pathEntity)) return;
	
	Vec3D vec3d;

        if (this.a()) {
            applyUnit(pathEntity);
        } else if (pathEntity.f() < pathEntity.e()) {
            vec3d = this.b();
            Vec3D vec3d1 = pathEntity.a(this.a, pathEntity.f());

            if (vec3d.y > vec3d1.y && !this.a.onGround && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
        	pathEntity.c(pathEntity.f() + 1);
            }
        }

        if (shouldContinuePathfind(pathEntity)) return;
        
        vec3d = pathEntity.a((Entity) this.a);
        BlockPosition blockposition = new BlockPosition(vec3d);

        this.a.getControllerMove().a(vec3d.x, this.b.getType(blockposition.down()).isAir() ? vec3d.y : PathfinderNormal.a((IBlockAccess) this.b, blockposition), vec3d.z, this.d);
    }
    
    protected void applyUnit(PathEntity pathEntity) {
        Vec3D vec3d = this.b();

        this.l = this.a.getWidth() > 0.75F ? this.a.getWidth() / 2.0F : 0.75F - this.a.getWidth() / 2.0F;
        Vec3D vec3d1 = pathEntity.g();

        if (Math.abs(this.a.locX() - (vec3d1.x + 0.5D)) < (double) this.l && Math.abs(this.a.locZ() - (vec3d1.z + 0.5D)) < (double) this.l && Math.abs(this.a.locY() - vec3d1.y) < 1.0D) {
            pathEntity.c(pathEntity.f() + 1);
        }

        this.setUnit(pathEntity, vec3d);
    }

    protected void setUnit(PathEntity pathEntity, Vec3D vec3d) {
        if (this.e - this.f > 100) {
            if (vec3d.distanceSquared(this.g) < 2.25D) {
                this.o();
            }

            this.f = this.e;
            this.g = vec3d;
        }

        if (!pathEntity.b()) {
            Vec3D vec3d1 = pathEntity.g();

            if (vec3d1.equals(this.h)) {
                this.i += SystemUtils.getMonotonicMillis() - this.j;
            } else {
                this.h = vec3d1;
                double d0 = vec3d.f(this.h);

                this.k = this.a.dt() > 0.0F ? d0 / (double) this.a.dt() * 1000.0D : 0.0D;
            }

            if (this.k > 0.0D && (double) this.i > this.k * 3.0D) {
                this.h = Vec3D.a;
                this.i = 0L;
                this.k = 0.0D;
                this.o();
            }

            this.j = SystemUtils.getMonotonicMillis();
        }

    }
    // StarLink end

    protected void l() {
        Vec3D vec3d = this.b();

        this.l = this.a.getWidth() > 0.75F ? this.a.getWidth() / 2.0F : 0.75F - this.a.getWidth() / 2.0F;
        Vec3D vec3d1 = this.c.g();

        if (Math.abs(this.a.locX() - (vec3d1.x + 0.5D)) < (double) this.l && Math.abs(this.a.locZ() - (vec3d1.z + 0.5D)) < (double) this.l && Math.abs(this.a.locY() - vec3d1.y) < 1.0D) {
            this.c.c(this.c.f() + 1);
        }

        this.a(vec3d);
    }

    protected void a(Vec3D vec3d) {
        if (this.e - this.f > 100) {
            if (vec3d.distanceSquared(this.g) < 2.25D) {
                this.o();
            }

            this.f = this.e;
            this.g = vec3d;
        }

        if (this.c != null && !this.c.b()) {
            Vec3D vec3d1 = this.c.g();

            if (vec3d1.equals(this.h)) {
                this.i += SystemUtils.getMonotonicMillis() - this.j;
            } else {
                this.h = vec3d1;
                double d0 = vec3d.f(this.h);

                this.k = this.a.dt() > 0.0F ? d0 / (double) this.a.dt() * 1000.0D : 0.0D;
            }

            if (this.k > 0.0D && (double) this.i > this.k * 3.0D) {
                this.h = Vec3D.a;
                this.i = 0L;
                this.k = 0.0D;
                this.o();
            }

            this.j = SystemUtils.getMonotonicMillis();
        }

    }

    @ObfuscateHelper("shouldContinuePathfind") // StarLink
    public boolean m() {
        return this.c == null || this.c.b();
    }
    // StarLink start
    public boolean shouldContinuePathfind(PathEntity pathEntity) {
        return pathEntity == null || pathEntity.b();
    }
    // StarLink end

    public boolean n() {
        return !this.m();
    }

    public void o() {
        this.c = null;
    }

    @ObfuscateHelper("getEntityPathfindUnit") // StarLink
    protected abstract Vec3D b();

    @ObfuscateHelper("canPathfind") // StarLink
    protected abstract boolean a();

    protected boolean p() {
        return this.a.az() || this.a.aH();
    }

    protected void F_() {
        if (this.c != null) {
            for (int i = 0; i < this.c.e(); ++i) {
                PathPoint pathpoint = this.c.a(i);
                PathPoint pathpoint1 = i + 1 < this.c.e() ? this.c.a(i + 1) : null;
                IBlockData iblockdata = this.b.getType(new BlockPosition(pathpoint.a, pathpoint.b, pathpoint.c));
                Block block = iblockdata.getBlock();

                if (block == Blocks.CAULDRON) {
                    this.c.a(i, pathpoint.a(pathpoint.a, pathpoint.b + 1, pathpoint.c));
                    if (pathpoint1 != null && pathpoint.b >= pathpoint1.b) {
                        this.c.a(i + 1, pathpoint1.a(pathpoint1.a, pathpoint.b + 1, pathpoint1.c));
                    }
                }
            }

        }
    }

    protected abstract boolean a(Vec3D vec3d, Vec3D vec3d1, int i, int j, int k);

    public boolean a(BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.down();

        return this.b.getType(blockposition1).g(this.b, blockposition1);
    }

    public PathfinderAbstract q() {
        return this.o;
    }

    public void d(boolean flag) {
        this.o.c(flag);
    }

    public boolean r() {
        return this.o.e();
    }

    public void b(BlockPosition blockposition) {
        if (this.c != null && !this.c.b() && this.c.e() != 0) {
            PathPoint pathpoint = this.c.c();
            Vec3D vec3d = new Vec3D(((double) pathpoint.a + this.a.locX()) / 2.0D, ((double) pathpoint.b + this.a.locY()) / 2.0D, ((double) pathpoint.c + this.a.locZ()) / 2.0D);

            if (blockposition.a((IPosition) vec3d, (double) (this.c.e() - this.c.f()))) {
                this.j();
            }

        }
    }
}
