package net.minecraft.server;

import com.google.common.collect.Lists;

import java.util.List;

public class EntitySenses {

    private final EntityInsentient a;
    //private final List<Entity> b = Lists.newArrayList(); // StarLink
    //private final List<Entity> c = Lists.newArrayList(); // StarLink
    private final java.util.Map<Entity, Boolean> senses = com.google.common.collect.Maps.newConcurrentMap(); // StarLink

    public EntitySenses(EntityInsentient entityinsentient) {
        this.a = entityinsentient;
    }

    public void a() {
        //this.b.clear(); // StarLink
        //this.c.clear(); // StarLink
	senses.clear(); // StarLink
    }

    public boolean a(Entity entity) {
	// StarLink start
	return senses.computeIfAbsent(entity, e -> a.hasLineOfSight(e));
	/*
        if (this.b.contains(entity)) {
            return true;
        } else if (this.c.contains(entity)) {
            return false;
        } else {
            this.a.world.getMethodProfiler().enter("canSee");
            boolean flag = this.a.hasLineOfSight(entity);

            this.a.world.getMethodProfiler().exit();
            if (flag) {
                this.b.add(entity);
            } else {
                this.c.add(entity);
            }

            return flag;
        }
	*/
	// StarLink end
    }
}
