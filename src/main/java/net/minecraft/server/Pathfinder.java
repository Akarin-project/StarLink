package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.bukkit.starlink.annotation.ObfuscateHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.bukkit.entity.Fish;

public class Pathfinder {

    @ObfuscateHelper("nodePath") // StarLink
    private final Path a = new Path();
    @ObfuscateHelper("nodesSet") // StarLink
    private final Set<PathPoint> b = Sets.newHashSet();
    @ObfuscateHelper("pathNodes") // StarLink
    private final PathPoint[] c = new PathPoint[32];
    private final int d;
    @ObfuscateHelper("entityPathfinder") // StarLink
    private final PathfinderAbstract e;

    public Pathfinder(PathfinderAbstract pathfinderabstract, int i) {
        this.e = pathfinderabstract;
        this.d = i;
    }

    @Nullable
    @ObfuscateHelper("findPaths") // StarLink
    public synchronized PathEntity a(ChunkCache chunkcache, EntityInsentient entityinsentient, Set<BlockPosition> set, float f, int i, float f1) { // StarLink
        this.a.a();
        this.e.a(chunkcache, entityinsentient);
        @ObfuscateHelper("entityNode") // StarLink
        PathPoint pathpoint = this.e.b();
        @ObfuscateHelper("destToPos") // StarLink
        Map<PathDestination, BlockPosition> map = set.stream().collect(Collectors.toMap((blockposition) -> { // StarLink - fixes warning
            return this.e.a((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
        }, Function.identity()));
        PathEntity pathentity = this.a(pathpoint, map, f, i, f1);

        this.e.a();
        return pathentity;
    }

    @Nullable
    @ObfuscateHelper("findPaths0") // StarLink
    private PathEntity a(PathPoint pathpoint, Map<PathDestination, BlockPosition> map, float f, @ObfuscateHelper("maxAcceptedDistanceAsFinished") int i, float f1) {
	@ObfuscateHelper("destinations") // StarLink
        Set<PathDestination> set = map.keySet();

        pathpoint.e = 0.0F;
        pathpoint.f = this.a(pathpoint, set);
        pathpoint.g = pathpoint.f;
        this.a.a();
        this.b.clear();
        this.a.a(pathpoint);
        int j = 0;
        int k = (int) ((float) this.d * f1);

        while (!this.a.e()) {
            ++j;
            if (j >= k) {
                break;
            }

            @ObfuscateHelper("head") // StarLink
            PathPoint pathpoint1 = this.a.c();

            pathpoint1.i = true;
            // StarLink start - simplify loop
            boolean finishedAny = false;
            for (PathDestination dest : set) {
        	if (pathpoint1.c((PathPoint) dest) <= (float) i) {
        	    finishedAny = true;
        	    dest.e();
        	}
            }
            if (finishedAny) break;
            /*
            set.stream().filter((pathdestination) -> {
                return pathpoint1.c((PathPoint) pathdestination) <= (float) i;
            }).forEach(PathDestination::e);
            if (set.stream().anyMatch(PathDestination::f)) {
                break;
            }
            */
            // StarLink end

            if (pathpoint1.a(pathpoint) < f) {
        	@ObfuscateHelper("aroundNodesAmount") // StarLink
                int l = this.e.a(this.c, pathpoint1);

                for (int i1 = 0; i1 < l; ++i1) {
                    PathPoint pathpoint2 = this.c[i1];
                    @ObfuscateHelper("distance") // StarLink
                    float f2 = pathpoint1.a(pathpoint2);

                    pathpoint2.j = pathpoint1.j + f2;
                    float f3 = pathpoint1.e + f2 + pathpoint2.k;

                    if (pathpoint2.j < f && (!pathpoint2.c() || f3 < pathpoint2.e)) {
                        pathpoint2.h = pathpoint1;
                        pathpoint2.e = f3;
                        pathpoint2.f = this.a(pathpoint2, set) * 1.5F;
                        if (pathpoint2.c()) {
                            this.a.a(pathpoint2, pathpoint2.e + pathpoint2.f);
                        } else {
                            pathpoint2.g = pathpoint2.e + pathpoint2.f;
                            this.a.a(pathpoint2);
                        }
                    }
                }
            }
        }

        // StarLink start - simplify loop
        // Stream stream;

        boolean finishedAny = false;
        PathEntity nearest = null; // This was considered as an alternative when none finished
        PathEntity shortest = null;
        
        for (PathDestination dest : set) {
            boolean fin = dest.f();
            PathEntity each = a(dest.d(), map.isEmpty() || dest.position == null ? (BlockPosition) map.get(dest) : dest.position, fin);
            
    	    if (shortest == null || each.e() < shortest.e())
    		shortest = each;
            
            if (fin)
        	finishedAny = true;
            else if (!finishedAny && (nearest == null || each.l() < nearest.l()))
                nearest = each;
        }
        /*
        if (set.stream().anyMatch(PathDestination::f)) {
            stream = set.stream().filter(PathDestination::f).map((pathdestination) -> {
                return this.a(pathdestination.d(), (BlockPosition) map.get(pathdestination), true);
            }).sorted(Comparator.comparingInt(PathEntity::e));
        } else {
            stream = set.stream().map((pathdestination) -> {
                return this.a(pathdestination.d(), (BlockPosition) map.get(pathdestination), false);
            }).sorted(Comparator.comparingDouble(PathEntity::l).thenComparingInt(PathEntity::e));
        }
        */
        return finishedAny ? shortest : (shortest == null ? nearest : (nearest == null ? null : (shortest.e() < nearest.e() ? shortest : nearest)));

        /*
        Optional<PathEntity> optional = stream.findFirst();

        if (!optional.isPresent()) {
            return null;
        } else {
            PathEntity pathentity = (PathEntity) optional.get();

            return pathentity;
        }
        */
        // StarLink end
    }

    @ObfuscateHelper("getNearestDistance") // StarLink
    private float a(PathPoint pathpoint, Set<PathDestination> set) {
        @ObfuscateHelper("nearestDistance") // StarLink
        float f = Float.MAX_VALUE;

        @ObfuscateHelper("distanceBetween") // StarLink
        float f1;

        for (Iterator iterator = set.iterator(); iterator.hasNext(); f = Math.min(f1, f)) {
            PathDestination pathdestination = (PathDestination) iterator.next();

            f1 = pathpoint.a(pathdestination);
            pathdestination.a(f1, pathpoint);
        }

        return f;
    }

    @ObfuscateHelper("gatherAssociatedNodes") // StarLink
    private PathEntity a(PathPoint pathpoint, BlockPosition blockposition, boolean flag) { // StarLink - static
        List<PathPoint> list = Lists.newArrayList();
        @ObfuscateHelper("next") // StarLink
        PathPoint pathpoint1 = pathpoint;

        list.add(0, pathpoint);

        while (pathpoint1.h != null) {
            pathpoint1 = pathpoint1.h;
            list.add(0, pathpoint1);
        }

        return new PathEntity(list, blockposition, flag);
    }
}
