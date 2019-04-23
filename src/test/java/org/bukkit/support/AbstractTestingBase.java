package org.bukkit.support;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.EnumResourcePackType;
import net.minecraft.server.LootTableRegistry;
import net.minecraft.server.ResourceManager;
import net.minecraft.server.ResourcePackVanilla;
import net.minecraft.server.TagRegistry;
import net.minecraft.server.Unit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.junit.Assert;

/**
 *  If you are getting: java.lang.ExceptionInInitializerError
 *    at net.minecraft.server.StatisticList.<clinit>(SourceFile:58)
 *    at net.minecraft.server.Item.<clinit>(SourceFile:252)
 *    at net.minecraft.server.Block.<clinit>(Block.java:577)
 *
 *  extend this class to solve it.
 */
public abstract class AbstractTestingBase {
    // Materials that only exist in block form (or are legacy)
    public static final List<Material> INVALIDATED_MATERIALS;

    public static final LootTableRegistry LOOT_TABLE_REGISTRY;
    public static final TagRegistry TAG_REGISTRY;

    static {
        DispenserRegistry.init();
        // Set up resource manager
        ResourceManager resourceManager = new ResourceManager(EnumResourcePackType.SERVER_DATA, Thread.currentThread());
        // add tags and loot tables for unit tests
        resourceManager.a(TAG_REGISTRY = new TagRegistry());
        resourceManager.a(LOOT_TABLE_REGISTRY = new LootTableRegistry());
        // Register vanilla pack
        resourceManager.a(MoreExecutors.directExecutor(), MoreExecutors.directExecutor(), Collections.singletonList(new ResourcePackVanilla("minecraft")), CompletableFuture.completedFuture(Unit.INSTANCE)).join();

        DummyServer.setup();
        DummyEnchantments.setup();

        ImmutableList.Builder<Material> builder = ImmutableList.builder();
        for (Material m : Material.values()) {
            if (m.isLegacy() || CraftMagicNumbers.getItem(m) == null) {
                builder.add(m);
            }
        }
        INVALIDATED_MATERIALS = builder.build();
        Assert.assertEquals("Expected 554 invalidated materials (got " + INVALIDATED_MATERIALS.size() + ")", 554, INVALIDATED_MATERIALS.size());
    }
}
