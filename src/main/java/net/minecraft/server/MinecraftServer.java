package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// CraftBukkit start
import jline.console.ConsoleReader;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.Main;
import org.bukkit.event.server.ServerLoadEvent;
// CraftBukkit end
import org.bukkit.craftbukkit.SpigotTimings; // Spigot
import org.spigotmc.SlackActivityAccountant; // Spigot

public abstract class MinecraftServer extends IAsyncTaskHandlerReentrant<TickTask> implements IMojangStatistics, ICommandListener, AutoCloseable, Runnable {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final File b = new File("usercache.json");
    private static final CompletableFuture<Unit> i = CompletableFuture.completedFuture(Unit.INSTANCE);
    public static final WorldSettings c = (new WorldSettings((long) "North Carolina".hashCode(), EnumGamemode.SURVIVAL, true, false, WorldType.NORMAL)).a();
    public Convertable convertable;
    private final MojangStatisticsGenerator snooper = new MojangStatisticsGenerator("server", this, SystemUtils.getMonotonicMillis());
    public File universe;
    private final List<Runnable> tickables = Lists.newArrayList();
    private final GameProfiler methodProfiler = new GameProfiler(this::ak);
    private ServerConnection serverConnection;
    public final WorldLoadListenerFactory worldLoadListenerFactory;
    private final ServerPing serverPing = new ServerPing();
    private final Random q = new Random();
    public final DataFixer dataConverterManager;
    private String serverIp;
    private int serverPort = -1;
    public final Map<DimensionManager, WorldServer> worldServer = Maps.newLinkedHashMap(); // CraftBukkit - keep order, k+v already use identity methods
    private PlayerList playerList;
    private volatile boolean isRunning = true;
    private boolean isStopped;
    private int ticks;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean A;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int G;
    private int H;
    public final long[] f = new long[100];
    @Nullable
    private KeyPair I;
    @Nullable
    private String J;
    private final String K;
    private boolean demoMode;
    private boolean bonusChest;
    private String O = "";
    private String P = "";
    private volatile boolean hasTicked;
    private long lastOverloadTime;
    @Nullable
    private IChatBaseComponent S;
    private boolean T;
    private boolean U;
    @Nullable
    private final YggdrasilAuthenticationService yggdrasilAuthenticationService;
    private final MinecraftSessionService minecraftSessionService;
    private final GameProfileRepository gameProfileRepository;
    private final UserCache userCache;
    private long Z;
    public final Thread serverThread = (Thread) SystemUtils.a((new Thread(this, "Server thread")), (thread) -> { // CraftBukkit - decompile error
        thread.setUncaughtExceptionHandler((thread1, throwable) -> {
            MinecraftServer.LOGGER.error(throwable);
        });
    });
    private long nextTick = SystemUtils.getMonotonicMillis();
    private long ab;
    private boolean ac;
    private final IReloadableResourceManager ae;
    private final ResourcePackRepository<ResourcePackLoader> resourcePackRepository;
    @Nullable
    private ResourcePackSourceFolder resourcePackFolder;
    public CommandDispatcher commandDispatcher;
    private final CraftingManager craftingManager;
    private final TagRegistry tagRegistry;
    private final ScoreboardServer scoreboardServer;
    @Nullable
    private PersistentCommandStorage persistentCommandStorage;
    private final BossBattleCustomData bossBattleCustomData;
    private final LootPredicateManager lootPredicateManager;
    private final LootTableRegistry lootTableRegistry;
    private final AdvancementDataWorld advancementDataWorld;
    private final CustomFunctionData customFunctionData;
    private final CircularTimer circularTimer;
    private boolean as;
    private boolean forceUpgrade;
    private boolean eraseCache;
    private float av;
    public final Executor executorService;
    @Nullable
    private String ax;

    // CraftBukkit start
    public org.bukkit.craftbukkit.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    public static int currentTick = (int) (System.currentTimeMillis() / 50);
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    public int autosavePeriod;
    public File bukkitDataPackFolder;
    public CommandDispatcher vanillaCommandDispatcher;
    private boolean forceTicks;
    // CraftBukkit end
    // Spigot start
    public static final int TPS = 20;
    public static final int TICK_TIME = 1000000000 / TPS;
    private static final int SAMPLE_INTERVAL = 100;
    public final double[] recentTps = new double[ 3 ];
    public final SlackActivityAccountant slackActivityAccountant = new SlackActivityAccountant();
    // Spigot end

    public MinecraftServer(OptionSet options, Proxy proxy, DataFixer datafixer, CommandDispatcher commanddispatcher, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository, UserCache usercache, WorldLoadListenerFactory worldloadlistenerfactory, String s) {
        super("Server");
        this.ae = new ResourceManager(EnumResourcePackType.SERVER_DATA, this.serverThread);
        this.resourcePackRepository = new ResourcePackRepository<>(ResourcePackLoader::new);
        this.craftingManager = new CraftingManager();
        this.tagRegistry = new TagRegistry();
        this.scoreboardServer = new ScoreboardServer(this);
        this.bossBattleCustomData = new BossBattleCustomData(this);
        this.lootPredicateManager = new LootPredicateManager();
        this.lootTableRegistry = new LootTableRegistry(this.lootPredicateManager);
        this.advancementDataWorld = new AdvancementDataWorld();
        this.customFunctionData = new CustomFunctionData(this);
        this.circularTimer = new CircularTimer();
        this.proxy = proxy;
        this.commandDispatcher = this.vanillaCommandDispatcher = commanddispatcher; // CraftBukkit
        this.yggdrasilAuthenticationService = yggdrasilauthenticationservice;
        this.minecraftSessionService = minecraftsessionservice;
        this.gameProfileRepository = gameprofilerepository;
        this.userCache = usercache;
        // this.universe = file; // CraftBukkit
        // this.serverConnection = new ServerConnection(this); // CraftBukkit // Spigot
        this.worldLoadListenerFactory = worldloadlistenerfactory;
        // this.convertable = new Convertable(file.toPath(), file.toPath().resolve("../backups"), datafixer); // CraftBukkit - moved to DedicatedServer.init
        this.dataConverterManager = datafixer;
        this.ae.a((IReloadListener) this.tagRegistry);
        this.ae.a((IReloadListener) this.lootPredicateManager);
        this.ae.a((IReloadListener) this.craftingManager);
        this.ae.a((IReloadListener) this.lootTableRegistry);
        this.ae.a((IReloadListener) this.customFunctionData);
        this.ae.a((IReloadListener) this.advancementDataWorld);
        this.executorService = SystemUtils.e();
        this.K = s;
        // CraftBukkit start
        this.options = options;
        // Try to see if we're actually running in a terminal, disable jline if not
        if (System.console() == null && System.getProperty("jline.terminal") == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
            Main.useJline = false;
        }

        try {
            reader = new ConsoleReader(System.in, System.out);
            reader.setExpandEvents(false); // Avoid parsing exceptions for uncommonly used event designators
        } catch (Throwable e) {
            try {
                // Try again with jline disabled for Windows users without C++ 2008 Redistributable
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");
                Main.useJline = false;
                reader = new ConsoleReader(System.in, System.out);
                reader.setExpandEvents(false);
            } catch (IOException ex) {
                LOGGER.warn((String) null, ex);
            }
        }
        Runtime.getRuntime().addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread(this));
    }
    // CraftBukkit end

    private void initializeScoreboards(WorldPersistentData worldpersistentdata) {
        PersistentScoreboard persistentscoreboard = (PersistentScoreboard) worldpersistentdata.a(PersistentScoreboard::new, "scoreboard");

        persistentscoreboard.a((Scoreboard) this.getScoreboard());
        this.getScoreboard().a((Runnable) (new RunnableSaveScoreboard(persistentscoreboard)));
    }

    protected abstract boolean init() throws IOException;

    public void convertWorld(String s) {
        if (this.getConvertable().isConvertable(s)) {
            MinecraftServer.LOGGER.info("Converting map!");
            this.b((IChatBaseComponent) (new ChatMessage("menu.convertingLevel", new Object[0])));
            this.getConvertable().convert(s, new IProgressUpdate() {
                private long b = SystemUtils.getMonotonicMillis();

                @Override
                public void a(IChatBaseComponent ichatbasecomponent) {}

                @Override
                public void a(int i) {
                    if (SystemUtils.getMonotonicMillis() - this.b >= 1000L) {
                        this.b = SystemUtils.getMonotonicMillis();
                        MinecraftServer.LOGGER.info("Converting... {}%", i);
                    }

                }

                @Override
                public void c(IChatBaseComponent ichatbasecomponent) {}
            });
        }

        if (this.forceUpgrade) {
            MinecraftServer.LOGGER.info("Forcing world upgrade! {}", s); // CraftBukkit
            WorldData worlddata = this.getConvertable().b(s); // CraftBukkit

            if (worlddata != null) {
                WorldUpgrader worldupgrader = new WorldUpgrader(s, this.getConvertable(), worlddata, this.eraseCache); // CraftBukkit
                IChatBaseComponent ichatbasecomponent = null;

                while (!worldupgrader.b()) {
                    IChatBaseComponent ichatbasecomponent1 = worldupgrader.g();

                    if (ichatbasecomponent != ichatbasecomponent1) {
                        ichatbasecomponent = ichatbasecomponent1;
                        MinecraftServer.LOGGER.info(worldupgrader.g().getString());
                    }

                    int i = worldupgrader.d();

                    if (i > 0) {
                        int j = worldupgrader.e() + worldupgrader.f();

                        MinecraftServer.LOGGER.info("{}% completed ({} / {} chunks)...", MathHelper.d((float) j / (float) i * 100.0F), j, i);
                    }

                    if (this.isStopped()) {
                        worldupgrader.a();
                    } else {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException interruptedexception) {
                            ;
                        }
                    }
                }
            }
        }

    }

    protected synchronized void b(IChatBaseComponent ichatbasecomponent) {
        this.S = ichatbasecomponent;
    }

    protected void a(String s, String s1, long i, WorldType worldtype, JsonElement jsonelement) {
        // this.convertWorld(s); // CraftBukkit - moved down
        this.b((IChatBaseComponent) (new ChatMessage("menu.loadingLevel", new Object[0])));
        /* CraftBukkit start - Remove ticktime arrays and worldsettings
        WorldNBTStorage worldnbtstorage = this.getConvertable().a(s, this);

        this.a(this.getWorld(), worldnbtstorage);
        WorldData worlddata = worldnbtstorage.getWorldData();
        WorldSettings worldsettings;

        if (worlddata == null) {
            if (this.isDemoMode()) {
                worldsettings = MinecraftServer.c;
            } else {
                worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
                worldsettings.setGeneratorSettings(jsonelement);
                if (this.bonusChest) {
                    worldsettings.a();
                }
            }

            worlddata = new WorldData(worldsettings, s1);
        } else {
            worlddata.setName(s1);
            worldsettings = new WorldSettings(worlddata);
        }

        worlddata.a(this.getServerModName(), this.q().isPresent());
        this.a(worldnbtstorage.getDirectory(), worlddata);
        */
        int worldCount = 3;

        for (int j = 0; j < worldCount; ++j) {
            WorldServer world;
            WorldData worlddata;
            byte dimension = 0;

            if (j == 1) {
                if (getAllowNether()) {
                    dimension = -1;
                } else {
                    continue;
                }
            }

            if (j == 2) {
                if (server.getAllowEnd()) {
                    dimension = 1;
                } else {
                    continue;
                }
            }

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? s : s + "_" + worldType;
            this.convertWorld(name); // Run conversion now

            org.bukkit.generator.ChunkGenerator gen = this.server.getGenerator(name);
            WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.setGeneratorSettings(jsonelement);

            if (j == 0) {
                WorldNBTStorage worldnbtstorage = new WorldNBTStorage(server.getWorldContainer(), s1, this, this.dataConverterManager);
                worlddata = worldnbtstorage.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, s1);
                }
                worlddata.checkName(s1); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                this.a(worldnbtstorage.getDirectory(), worlddata);
                WorldLoadListener worldloadlistener = this.worldLoadListenerFactory.create(11);

                if (this.isDemoMode()) {
                    worlddata.a(MinecraftServer.c);
                }
                world = new WorldServer(this, this.executorService, worldnbtstorage, worlddata, DimensionManager.OVERWORLD, this.methodProfiler, worldloadlistener, org.bukkit.World.Environment.getEnvironment(dimension), gen);

                WorldPersistentData worldpersistentdata = world.getWorldPersistentData();
                this.initializeScoreboards(worldpersistentdata);
                this.server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
                this.persistentCommandStorage = new PersistentCommandStorage(worldpersistentdata);
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);
                File oldLevelDat = new File(new File(s), "level.dat"); // The data folders exist on first run as they are created in the PersistentCollection constructor above, but the level.dat won't

                if (!newWorld.isDirectory() && oldWorld.isDirectory() && oldLevelDat.isFile()) {
                    MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    MinecraftServer.LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    MinecraftServer.LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    MinecraftServer.LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        MinecraftServer.LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            MinecraftServer.LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                com.google.common.io.Files.copy(oldLevelDat, new File(new File(name), "level.dat"));
                                org.apache.commons.io.FileUtils.copyDirectory(new File(new File(s), "data"), new File(new File(name), "data"));
                            } catch (IOException exception) {
                                MinecraftServer.LOGGER.warn("Unable to migrate world data.");
                            }
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            MinecraftServer.LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        MinecraftServer.LOGGER.warn("Could not create path for " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                WorldNBTStorage worldnbtstorage = new WorldNBTStorage(server.getWorldContainer(), name, this, this.dataConverterManager);
                // world =, b0 to dimension, s1 to name, added Environment and gen
                worlddata = worldnbtstorage.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, name);
                }
                worlddata.checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                WorldLoadListener worldloadlistener = this.worldLoadListenerFactory.create(11);
                world = new SecondaryWorldServer(this.getWorldServer(DimensionManager.OVERWORLD), this, this.executorService, worldnbtstorage, DimensionManager.a(dimension), this.methodProfiler, worldloadlistener, worlddata, org.bukkit.World.Environment.getEnvironment(dimension), gen);
            }

            this.initWorld(world, worlddata, worldsettings);
            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(world.getWorld()));

            this.worldServer.put(world.getWorldProvider().getDimensionManager(), world);
            this.getPlayerList().setPlayerFileData(world);

            if (worlddata.getCustomBossEvents() != null) {
                this.getBossBattleCustomData().load(worlddata.getCustomBossEvents());
            }
        }
        this.a(this.getDifficulty(), true);
        for (WorldServer worldserver : this.getWorlds()) {
            this.loadSpawn(worldserver.getChunkProvider().playerChunkMap.worldLoadListener, worldserver);
            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(worldserver.getWorld()));
        }

        this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        this.serverConnection.acceptConnections();
        // CraftBukkit end

    }

    // CraftBukkit start
    public void initWorld(WorldServer worldserver1, WorldData worlddata, WorldSettings worldsettings) {
        worldserver1.getWorldBorder().b(worlddata);

        // CraftBukkit start
        if (worldserver1.generator != null) {
            worldserver1.getWorld().getPopulators().addAll(worldserver1.generator.getDefaultPopulators(worldserver1.getWorld()));
        }
        // CraftBukkit end

        if (!worlddata.u()) {
            try {
                worldserver1.a(worldsettings);
                if (worlddata.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    this.a(worlddata);
                }

                worlddata.d(true);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception initializing level");

                try {
                    worldserver1.a(crashreport);
                } catch (Throwable throwable1) {
                    ;
                }

                throw new ReportedException(crashreport);
            }

            worlddata.d(true);
        }
    }
    // CraftBukkit end

    private void a(WorldData worlddata) {
        worlddata.f(false);
        worlddata.c(true);
        worlddata.setStorm(false);
        worlddata.setThundering(false);
        worlddata.g(1000000000);
        worlddata.setDayTime(6000L);
        worlddata.setGameType(EnumGamemode.SPECTATOR);
        worlddata.setHardcore(false);
        worlddata.setDifficulty(EnumDifficulty.PEACEFUL);
        worlddata.e(true);
        ((GameRules.GameRuleBoolean) worlddata.v().get(GameRules.DO_DAYLIGHT_CYCLE)).a(false, this);
    }

    protected void a(File file, WorldData worlddata) {
        this.resourcePackRepository.a((ResourcePackSource) (new ResourcePackSourceVanilla()));
        this.resourcePackFolder = new ResourcePackSourceFolder(new File(file, "datapacks"));
        // CraftBukkit start
        bukkitDataPackFolder = new File(new File(file, "datapacks"), "bukkit");
        if (!bukkitDataPackFolder.exists()) {
            bukkitDataPackFolder.mkdirs();
        }
        File mcMeta = new File(bukkitDataPackFolder, "pack.mcmeta");
        try {
            com.google.common.io.Files.write("{\n"
                    + "    \"pack\": {\n"
                    + "        \"description\": \"Data pack for resources provided by Bukkit plugins\",\n"
                    + "        \"pack_format\": " + SharedConstants.getGameVersion().getPackVersion() + "\n"
                    + "    }\n"
                    + "}\n", mcMeta, com.google.common.base.Charsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Could not initialize Bukkit datapack", ex);
        }
        // CraftBukkit end
        this.resourcePackRepository.a((ResourcePackSource) this.resourcePackFolder);
        this.resourcePackRepository.a();
        List<ResourcePackLoader> list = Lists.newArrayList();
        Iterator iterator = worlddata.O().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            ResourcePackLoader resourcepackloader = this.resourcePackRepository.a(s);

            if (resourcepackloader != null) {
                list.add(resourcepackloader);
            } else {
                MinecraftServer.LOGGER.warn("Missing data pack {}", s);
            }
        }

        this.resourcePackRepository.a((Collection) list);
        this.b(worlddata);
        this.bb();
    }

    // CraftBukkit start
    public void loadSpawn(WorldLoadListener worldloadlistener, WorldServer worldserver) {
        if (true || !worldserver.getWorld().getKeepSpawnInMemory()) { // StarLink
            return;
        }

        this.b((IChatBaseComponent) (new ChatMessage("menu.generatingTerrain", new Object[0])));
        // WorldServer worldserver = this.getWorldServer(DimensionManager.OVERWORLD);
        this.forceTicks = true;
        // CraftBukkit end

        MinecraftServer.LOGGER.info("Preparing start region for dimension '{}'/{}", worldserver.getWorldData().getName(), DimensionManager.a(worldserver.worldProvider.getDimensionManager().getType())); // CraftBukkit
        BlockPosition blockposition = worldserver.getSpawn();

        worldloadlistener.a(new ChunkCoordIntPair(blockposition));
        ChunkProviderServer chunkproviderserver = worldserver.getChunkProvider();

        chunkproviderserver.getLightEngine().a(500);
        this.nextTick = SystemUtils.getMonotonicMillis();
        chunkproviderserver.addTicket(TicketType.START, new ChunkCoordIntPair(blockposition), 11, Unit.INSTANCE);

        while (chunkproviderserver.b() != 441) {
            // CraftBukkit start
            // this.nextTick = SystemUtils.getMonotonicMillis() + 10L;
            this.executeModerately();
            // CraftBukkit end
        }

        // CraftBukkit start
        // this.nextTick = SystemUtils.getMonotonicMillis() + 10L;
        this.executeModerately();
        // Iterator iterator = DimensionManager.a().iterator();

        if (true) {
            DimensionManager dimensionmanager = worldserver.worldProvider.getDimensionManager();
            ForcedChunk forcedchunk = (ForcedChunk) worldserver.getWorldPersistentData().b(ForcedChunk::new, "chunks");
            // CraftBukkit end

            if (forcedchunk != null) {
                WorldServer worldserver1 = this.getWorldServer(dimensionmanager);
                LongIterator longiterator = forcedchunk.a().iterator();

                while (longiterator.hasNext()) {
                    long i = longiterator.nextLong();
                    ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i);

                    worldserver1.getChunkProvider().a(chunkcoordintpair, true);
                }
            }
        }

        // CraftBukkit start
        // this.nextTick = SystemUtils.getMonotonicMillis() + 10L;
        this.executeModerately();
        // CraftBukkit end
        worldloadlistener.b();
        chunkproviderserver.getLightEngine().a(5);

        // CraftBukkit start
        this.forceTicks = false;
        // CraftBukkit end
    }

    protected void a(String s, WorldNBTStorage worldnbtstorage) {
        File file = new File(worldnbtstorage.getDirectory(), "resources.zip");

        if (file.isFile()) {
            try {
                this.setResourcePack("level://" + URLEncoder.encode(s, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
            } catch (UnsupportedEncodingException unsupportedencodingexception) {
                MinecraftServer.LOGGER.warn("Something went wrong url encoding {}", s);
            }
        }

    }

    public abstract boolean getGenerateStructures();

    public abstract EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int j();

    public abstract int k();

    public abstract boolean l();

    public boolean saveChunks(boolean flag, boolean flag1, boolean flag2) {
        boolean flag3 = false;

        for (Iterator iterator = this.getWorlds().iterator(); iterator.hasNext(); flag3 = true) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (!flag) {
                MinecraftServer.LOGGER.info("Saving chunks for level '{}'/{}", worldserver.getWorldData().getName(), DimensionManager.a(worldserver.worldProvider.getDimensionManager()));
            }

            try {
                worldserver.save((IProgressUpdate) null, flag1, worldserver.savingDisabled && !flag2);
            } catch (ExceptionWorldConflict exceptionworldconflict) {
                MinecraftServer.LOGGER.warn(exceptionworldconflict.getMessage());
            }
        }

        // CraftBukkit start - moved to WorldServer.save
        /*
        WorldServer worldserver1 = this.getWorldServer(DimensionManager.OVERWORLD);
        WorldData worlddata = worldserver1.getWorldData();

        worldserver1.getWorldBorder().save(worlddata);
        worlddata.setCustomBossEvents(this.getBossBattleCustomData().save());
        worldserver1.getDataManager().saveWorldData(worlddata, this.getPlayerList().save());
        */
        // CraftBukkit end
        return flag3;
    }

    @Override
    public void close() {
        this.stop();
    }

    // CraftBukkit start
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    public final boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }
    // CraftBukkit end

    protected void stop() {
        // CraftBukkit start - prevent double stopping on multiple threads
        synchronized(stopLock) {
            if (hasStopped) return;
            hasStopped = true;
        }
        // CraftBukkit end
        MinecraftServer.LOGGER.info("Stopping server");
        // CraftBukkit start
        if (this.server != null) {
            this.server.disablePlugins();
        }
        // CraftBukkit end
        if (this.getServerConnection() != null) {
            this.getServerConnection().b();
        }

        if (this.playerList != null) {
            MinecraftServer.LOGGER.info("Saving players");
            this.playerList.savePlayers();
            this.playerList.shutdown();
            try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
        }

        MinecraftServer.LOGGER.info("Saving worlds");
        Iterator iterator = this.getWorlds().iterator();

        WorldServer worldserver;

        while (iterator.hasNext()) {
            worldserver = (WorldServer) iterator.next();
            if (worldserver != null) {
                worldserver.savingDisabled = false;
            }
        }

        this.saveChunks(false, true, false);
        iterator = this.getWorlds().iterator();

        while (iterator.hasNext()) {
            worldserver = (WorldServer) iterator.next();
            if (worldserver != null) {
                try {
                    worldserver.close();
                } catch (IOException ioexception) {
                    MinecraftServer.LOGGER.error("Exception closing the level", ioexception);
                }
            }
        }

        if (this.snooper.d()) {
            this.snooper.e();
        }

        // Spigot start
        if (org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) {
            LOGGER.info("Saving usercache.json");
            this.getUserCache().c();
        }
        // Spigot end
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void b(String s) {
        this.serverIp = s;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void safeShutdown(boolean flag) {
        this.isRunning = false;
        if (flag) {
            try {
                this.serverThread.join();
            } catch (InterruptedException interruptedexception) {
                MinecraftServer.LOGGER.error("Error while shutting down", interruptedexception);
            }
        }

    }

    // Spigot Start
    private static double calcTps(double avg, double exp, double tps)
    {
        return ( avg * exp ) + ( tps * ( 1 - exp ) );
    }
    // Spigot End

    public void run() {
        try {
            if (this.init()) {
                this.nextTick = SystemUtils.getMonotonicMillis();
                this.serverPing.setMOTD(new ChatComponentText(this.motd));
                this.serverPing.setServerInfo(new ServerPing.ServerData(SharedConstants.getGameVersion().getName(), SharedConstants.getGameVersion().getProtocolVersion()));
                this.a(this.serverPing);

                // Spigot start
                Arrays.fill( recentTps, 20 );
                long curTime, tickSection = SystemUtils.getMonotonicMillis(), tickCount = 1;
                while (this.isRunning) {
                    long i = (curTime = SystemUtils.getMonotonicMillis()) - this.nextTick;

                    if (i > 5000L && this.nextTick - this.lastOverloadTime >= 30000L) { // CraftBukkit
                        long j = i / 50L;

                        if (server.getWarnOnOverload()) // CraftBukkit
                        MinecraftServer.LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                        this.nextTick += j * 50L;
                        this.lastOverloadTime = this.nextTick;
                    }

                    if ( tickCount++ % SAMPLE_INTERVAL == 0 )
                    {
                        double currentTps = 1E3 / ( curTime - tickSection ) * SAMPLE_INTERVAL;
                        recentTps[0] = calcTps( recentTps[0], 0.92, currentTps ); // 1/exp(5sec/1min)
                        recentTps[1] = calcTps( recentTps[1], 0.9835, currentTps ); // 1/exp(5sec/5min)
                        recentTps[2] = calcTps( recentTps[2], 0.9945, currentTps ); // 1/exp(5sec/15min)
                        tickSection = curTime;
                    }
                    // Spigot end

                    MinecraftServer.currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
                    this.nextTick += 50L;
                    if (this.T) {
                        this.T = false;
                        this.methodProfiler.d().d();
                    }

                    this.methodProfiler.a();
                    this.methodProfiler.enter("tick");
                    this.a(this::canSleepForTick);
                    this.methodProfiler.exitEnter("nextTickWait");
                    this.ac = true;
                    this.ab = Math.max(SystemUtils.getMonotonicMillis() + 50L, this.nextTick);
                    this.sleepForTick();
                    this.methodProfiler.exit();
                    this.methodProfiler.b();
                    this.hasTicked = true;
                }
            } else {
                this.a((CrashReport) null);
            }
        } catch (Throwable throwable) {
            MinecraftServer.LOGGER.error("Encountered an unexpected exception", throwable);
            // Spigot Start
            if ( throwable.getCause() != null )
            {
                MinecraftServer.LOGGER.error( "\tCause of unexpected exception was", throwable.getCause() );
            }
            // Spigot End
            CrashReport crashreport;

            if (throwable instanceof ReportedException) {
                crashreport = this.b(((ReportedException) throwable).a());
            } else {
                crashreport = this.b(new CrashReport("Exception in server tick loop", throwable));
            }

            File file = new File(new File(this.z(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.a(file)) {
                MinecraftServer.LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
            } else {
                MinecraftServer.LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.a(crashreport);
        } finally {
            try {
                this.isStopped = true;
                this.stop();
            } catch (Throwable throwable1) {
                MinecraftServer.LOGGER.error("Exception stopping the server", throwable1);
            } finally {
                org.spigotmc.WatchdogThread.doStop(); // Spigot
                // CraftBukkit start - Restore terminal to original settings
                try {
                    reader.getTerminal().restore();
                } catch (Exception ignored) {
                }
                // CraftBukkit end
                this.exit();
            }

        }

    }

    private boolean canSleepForTick() {
        // CraftBukkit start
        return this.forceTicks || this.isEntered() || SystemUtils.getMonotonicMillis() < (this.ac ? this.ab : this.nextTick);
    }

    private void executeModerately() {
        this.executeAll();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }
    // CraftBukkit end

    protected void sleepForTick() {
        this.executeAll();
        this.awaitTasks(() -> {
            return !this.canSleepForTick();
        });
    }

    @Override
    protected TickTask postToMainThread(Runnable runnable) {
        return new TickTask(this.ticks, runnable);
    }

    protected boolean canExecute(TickTask ticktask) {
        return ticktask.a() + 3 < this.ticks || this.canSleepForTick();
    }

    @Override
    public boolean executeNext() {
        boolean flag = this.ba();

        this.ac = flag;
        return flag;
    }

    private boolean ba() {
        if (super.executeNext()) {
            return true;
        } else {
            if (this.canSleepForTick()) {
                Iterator iterator = this.getWorlds().iterator();

                while (iterator.hasNext()) {
                    WorldServer worldserver = (WorldServer) iterator.next();

                    if (worldserver.getChunkProvider().runTasks()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected void c(TickTask ticktask) {
        this.getMethodProfiler().c("runTask");
        super.executeTask(ticktask);
    }

    public void a(ServerPing serverping) {
        File file = this.d("server-icon.png");

        if (!file.exists()) {
            file = this.getConvertable().b(this.getWorld(), "icon.png");
        }

        if (file.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file);

                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuffer bytebuffer = Base64.getEncoder().encode(bytebuf.nioBuffer());

                serverping.setFavicon("data:image/png;base64," + StandardCharsets.UTF_8.decode(bytebuffer));
            } catch (Exception exception) {
                MinecraftServer.LOGGER.error("Couldn't load server icon", exception);
            } finally {
                bytebuf.release();
            }
        }

    }

    public File z() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {}

    protected void exit() {}

    protected void a(BooleanSupplier booleansupplier) {
        SpigotTimings.serverTickTimer.startTiming(); // Spigot
        this.slackActivityAccountant.tickStarted(); // Spigot
        long i = SystemUtils.getMonotonicNanos();

        ++this.ticks;
        this.b(booleansupplier);
        if (i - this.Z >= 5000000000L) {
            this.Z = i;
            this.serverPing.setPlayerSample(new ServerPing.ServerPingPlayerSample(this.getMaxPlayers(), this.getPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getPlayerCount(), 12)];
            int j = MathHelper.nextInt(this.q, 0, this.getPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = ((EntityPlayer) this.playerList.getPlayers().get(j + k)).getProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.serverPing.b().a(agameprofile);
        }

        if (autosavePeriod > 0 && this.ticks % autosavePeriod == 0) { // CraftBukkit
            SpigotTimings.worldSaveTimer.startTiming(); // Spigot
            MinecraftServer.LOGGER.debug("Autosave started");
            this.methodProfiler.enter("save");
            this.playerList.savePlayers();
            this.saveChunks(true, false, false);
            this.methodProfiler.exit();
            MinecraftServer.LOGGER.debug("Autosave finished");
            SpigotTimings.worldSaveTimer.stopTiming(); // Spigot
        }

        this.methodProfiler.enter("snooper");
        if (((DedicatedServer) this).getDedicatedServerProperties().snooperEnabled && !this.snooper.d() && this.ticks > 100) { // Spigot
            this.snooper.a();
        }

        if (((DedicatedServer) this).getDedicatedServerProperties().snooperEnabled && this.ticks % 6000 == 0) { // Spigot
            this.snooper.b();
        }

        this.methodProfiler.exit();
        this.methodProfiler.enter("tallying");
        long l = this.f[this.ticks % 100] = SystemUtils.getMonotonicNanos() - i;

        this.av = this.av * 0.8F + (float) l / 1000000.0F * 0.19999999F;
        long i1 = SystemUtils.getMonotonicNanos();

        this.circularTimer.a(i1 - i);
        this.methodProfiler.exit();
        org.spigotmc.WatchdogThread.tick(); // Spigot
        this.slackActivityAccountant.tickEnded(l); // Spigot
        SpigotTimings.serverTickTimer.stopTiming(); // Spigot
        org.spigotmc.CustomTimingsHandler.tick(); // Spigot
    }

    protected void b(BooleanSupplier booleansupplier) {
        SpigotTimings.schedulerTimer.startTiming(); // Spigot
        this.server.getScheduler().mainThreadHeartbeat(this.ticks); // CraftBukkit
        SpigotTimings.schedulerTimer.stopTiming(); // Spigot
        this.methodProfiler.enter("commandFunctions");
        SpigotTimings.commandFunctionsTimer.startTiming(); // Spigot
        this.getFunctionData().tick();
        SpigotTimings.commandFunctionsTimer.stopTiming(); // Spigot
        this.methodProfiler.exitEnter("levels");
        Iterator iterator = this.getWorlds().iterator();

        // CraftBukkit start
        // Run tasks that are waiting on processing
        SpigotTimings.processQueueTimer.startTiming(); // Spigot
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
        SpigotTimings.processQueueTimer.stopTiming(); // Spigot

        SpigotTimings.timeUpdateTimer.startTiming(); // Spigot
        // Send time updates to everyone, it will get the right time from the world the player is in.
        if (this.ticks % 20 == 0) {
            for (int i = 0; i < this.getPlayerList().players.size(); ++i) {
                EntityPlayer entityplayer = (EntityPlayer) this.getPlayerList().players.get(i);
                entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(entityplayer.world.getTime(), entityplayer.getPlayerTime(), entityplayer.world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))); // Add support for per player time
            }
        }
        SpigotTimings.timeUpdateTimer.stopTiming(); // Spigot

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (true || worldserver.worldProvider.getDimensionManager() == DimensionManager.OVERWORLD || this.getAllowNether()) { // CraftBukkit
                this.methodProfiler.a(() -> {
                    return worldserver.getWorldData().getName() + " " + IRegistry.DIMENSION_TYPE.getKey(worldserver.worldProvider.getDimensionManager());
                });
                /* Drop global time updates
                if (this.ticks % 20 == 0) {
                    this.methodProfiler.enter("timeSync");
                    this.playerList.a((Packet) (new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))), worldserver.worldProvider.getDimensionManager());
                    this.methodProfiler.exit();
                }
                // CraftBukkit end */

                this.methodProfiler.enter("tick");

                try {
                    worldserver.timings.doTick.startTiming(); // Spigot
                    worldserver.doTick(booleansupplier);
                    worldserver.timings.doTick.stopTiming(); // Spigot
                } catch (Throwable throwable) {
                    // Spigot Start
                    CrashReport crashreport;
                    try {
                        crashreport = CrashReport.a(throwable, "Exception ticking world");
                    } catch (Throwable t) {
                        throw new RuntimeException("Error generating crash report", t);
                    }
                    // Spigot End

                    worldserver.a(crashreport);
                    throw new ReportedException(crashreport);
                }

                this.methodProfiler.exit();
                this.methodProfiler.exit();
            }
        }

        this.methodProfiler.exitEnter("connection");
        SpigotTimings.connectionTimer.startTiming(); // Spigot
        this.getServerConnection().c();
        SpigotTimings.connectionTimer.stopTiming(); // Spigot
        this.methodProfiler.exitEnter("players");
        SpigotTimings.playerListTimer.startTiming(); // Spigot
        this.playerList.tick();
        SpigotTimings.playerListTimer.stopTiming(); // Spigot
        if (SharedConstants.b) {
            GameTestHarnessTicker.a.b();
        }

        this.methodProfiler.exitEnter("server gui refresh");

        SpigotTimings.tickablesTimer.startTiming(); // Spigot
        for (int i = 0; i < this.tickables.size(); ++i) {
            ((Runnable) this.tickables.get(i)).run();
        }
        SpigotTimings.tickablesTimer.stopTiming(); // Spigot

        this.methodProfiler.exit();
    }

    public boolean getAllowNether() {
        return true;
    }

    public void b(Runnable runnable) {
        this.tickables.add(runnable);
    }

    public static void main(final OptionSet optionset) { // CraftBukkit - replaces main(String[] astring)
        /* CraftBukkit start - Replace everything
        OptionParser optionparser = new OptionParser();
        OptionSpec<Void> optionspec = optionparser.accepts("nogui");
        OptionSpec<Void> optionspec1 = optionparser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpec<Void> optionspec2 = optionparser.accepts("demo");
        OptionSpec<Void> optionspec3 = optionparser.accepts("bonusChest");
        OptionSpec<Void> optionspec4 = optionparser.accepts("forceUpgrade");
        OptionSpec<Void> optionspec5 = optionparser.accepts("eraseCache");
        OptionSpec<Void> optionspec6 = optionparser.accepts("help").forHelp();
        OptionSpec<String> optionspec7 = optionparser.accepts("singleplayer").withRequiredArg();
        OptionSpec<String> optionspec8 = optionparser.accepts("universe").withRequiredArg().defaultsTo(".", new String[0]);
        OptionSpec<String> optionspec9 = optionparser.accepts("world").withRequiredArg();
        OptionSpec<Integer> optionspec10 = optionparser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, new Integer[0]);
        OptionSpec<String> optionspec11 = optionparser.accepts("serverId").withRequiredArg();
        NonOptionArgumentSpec nonoptionargumentspec = optionparser.nonOptions();

        try {
            OptionSet optionset = optionparser.parse(astring);

            if (optionset.has(optionspec6)) {
                optionparser.printHelpOn(System.err);
                return;
            }
            */ // CraftBukkit end

        try {
            java.nio.file.Path java_nio_file_path = Paths.get("server.properties");
            DedicatedServerSettings dedicatedserversettings = new DedicatedServerSettings(optionset); // CraftBukkit - CLI argument support

            dedicatedserversettings.save();
            java.nio.file.Path java_nio_file_path1 = Paths.get("eula.txt");
            EULA eula = new EULA(java_nio_file_path1);

            if (optionset.has("initSettings")) { // CraftBukkit
                MinecraftServer.LOGGER.info("Initialized '" + java_nio_file_path.toAbsolutePath().toString() + "' and '" + java_nio_file_path1.toAbsolutePath().toString() + "'");
                return;
            }

            // Spigot Start
            boolean eulaAgreed = Boolean.getBoolean( "com.mojang.eula.agree" );
            if ( eulaAgreed )
            {
                System.err.println( "You have used the Spigot command line EULA agreement flag." );
                System.err.println( "By using this setting you are indicating your agreement to Mojang's EULA (https://account.mojang.com/documents/minecraft_eula)." );
                System.err.println( "If you do not agree to the above EULA please stop your server and remove this flag immediately." );
            }
            // Spigot End
            if (!eula.a() && !eulaAgreed) { // Spigot
                MinecraftServer.LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            CrashReport.h();
            DispenserRegistry.init();
            DispenserRegistry.c();
            File s = (File) optionset.valueOf("universe"); // CraftBukkit
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            UserCache usercache = new UserCache(gameprofilerepository, new File(s, MinecraftServer.b.getName()));
            // CraftBukkit start
            String s1 = (String) Optional.ofNullable(optionset.valueOf("world")).orElse(dedicatedserversettings.getProperties().levelName);
            final DedicatedServer dedicatedserver = new DedicatedServer(optionset, dedicatedserversettings, DataConverterRegistry.a(), yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache, WorldLoadListenerLogger::new, s1);

            /*
            dedicatedserver.i((String) optionset.valueOf(optionspec7));
            dedicatedserver.setPort((Integer) optionset.valueOf(optionspec10));
            dedicatedserver.e(optionset.has(optionspec2));
            dedicatedserver.f(optionset.has(optionspec3));
            dedicatedserver.setForceUpgrade(optionset.has(optionspec4));
            dedicatedserver.setEraseCache(optionset.has(optionspec5));
            dedicatedserver.c((String) optionset.valueOf(optionspec11));
            */
            boolean flag = !optionset.has("nogui") && !optionset.nonOptionArguments().contains("nogui");

            if (flag && !GraphicsEnvironment.isHeadless()) {
                dedicatedserver.bc();
            }

            /*
            dedicatedserver.startServerThread();
            Thread thread = new Thread("Server Shutdown Thread") {
                public void run() {
                    dedicatedserver.safeShutdown(true);
                }
            };

            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(MinecraftServer.LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
            */

            if (optionset.has("port")) {
                int port = (Integer) optionset.valueOf("port");
                if (port > 0) {
                    dedicatedserver.setPort(port);
                }
            }

            if (optionset.has("universe")) {
                dedicatedserver.universe = (File) optionset.valueOf("universe");
            }

            if (optionset.has("forceUpgrade")) {
                dedicatedserver.setForceUpgrade(true);
            }

            if (optionset.has("eraseCache")) {
                dedicatedserver.setEraseCache(true);
            }

            dedicatedserver.serverThread.start();
            // CraftBukkit end
        } catch (Exception exception) {
            MinecraftServer.LOGGER.fatal("Failed to start the minecraft server", exception);
        }

    }

    protected void c(String s) {
        this.ax = s;
    }

    protected void setForceUpgrade(boolean flag) {
        this.forceUpgrade = flag;
    }

    protected void setEraseCache(boolean flag) {
        this.eraseCache = flag;
    }

    public void startServerThread() {
        /* CraftBukkit start - prevent abuse
        this.serverThread.start();
        // CraftBukkit end */
    }

    public File d(String s) {
        return new File(this.z(), s);
    }

    public void info(String s) {
        MinecraftServer.LOGGER.info(s);
    }

    public void warning(String s) {
        MinecraftServer.LOGGER.warn(s);
    }

    public WorldServer getWorldServer(DimensionManager dimensionmanager) {
        return (WorldServer) this.worldServer.get(dimensionmanager);
    }

    public Iterable<WorldServer> getWorlds() {
        return this.worldServer.values();
    }

    public String getVersion() {
        return SharedConstants.getGameVersion().getName();
    }

    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayers() {
        return this.playerList.e();
    }

    public boolean isDebugging() {
        return false;
    }

    public void g(String s) {
        MinecraftServer.LOGGER.error(s);
    }

    public void h(String s) {
        if (this.isDebugging()) {
            MinecraftServer.LOGGER.info(s);
        }

    }

    public String getServerModName() {
        return "Spigot"; // Spigot - Spigot > // CraftBukkit - cb > vanilla!
    }

    public CrashReport b(CrashReport crashreport) {
        if (this.playerList != null) {
            crashreport.g().a("Player Count", () -> {
                return this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers();
            });
        }

        crashreport.g().a("Data Packs", () -> {
            StringBuilder stringbuilder = new StringBuilder();
            Iterator iterator = this.resourcePackRepository.d().iterator();

            while (iterator.hasNext()) {
                ResourcePackLoader resourcepackloader = (ResourcePackLoader) iterator.next();

                if (stringbuilder.length() > 0) {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(resourcepackloader.e());
                if (!resourcepackloader.c().a()) {
                    stringbuilder.append(" (incompatible)");
                }
            }

            return stringbuilder.toString();
        });
        if (this.ax != null) {
            crashreport.g().a("Server Id", () -> {
                return this.ax;
            });
        }

        return crashreport;
    }

    public abstract Optional<String> q();

    public boolean K() {
        return true; // CraftBukkit
    }

    @Override
    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        MinecraftServer.LOGGER.info(ichatbasecomponent.getString());
    }

    public KeyPair getKeyPair() {
        return this.I;
    }

    public int getPort() {
        return this.serverPort;
    }

    public void setPort(int i) {
        this.serverPort = i;
    }

    public String getSinglePlayerName() {
        return this.J;
    }

    public void i(String s) {
        this.J = s;
    }

    public boolean isEmbeddedServer() {
        return this.J != null;
    }

    public String getWorld() {
        return this.K;
    }

    public void a(KeyPair keypair) {
        this.I = keypair;
    }

    public void a(EnumDifficulty enumdifficulty, boolean flag) {
        Iterator iterator = this.getWorlds().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();
            WorldData worlddata = worldserver.getWorldData();

            if (flag || !worlddata.isDifficultyLocked()) {
                if (worlddata.isHardcore()) {
                    worlddata.setDifficulty(EnumDifficulty.HARD);
                    worldserver.setSpawnFlags(true, true);
                } else if (this.isEmbeddedServer()) {
                    worlddata.setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    worlddata.setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
                }
            }
        }

        this.getPlayerList().getPlayers().forEach(this::a);
    }

    public void d(boolean flag) {
        Iterator iterator = this.getWorlds().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();
            WorldData worlddata = worldserver.getWorldData();

            worlddata.e(flag);
        }

        this.getPlayerList().getPlayers().forEach(this::a);
    }

    private void a(EntityPlayer entityplayer) {
        WorldData worlddata = entityplayer.getWorldServer().getWorldData();

        entityplayer.playerConnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
    }

    protected boolean getSpawnMonsters() {
        return true;
    }

    public boolean isDemoMode() {
        return this.demoMode;
    }

    public void e(boolean flag) {
        this.demoMode = flag;
    }

    public void f(boolean flag) {
        this.bonusChest = flag;
    }

    public Convertable getConvertable() {
        return this.convertable;
    }

    public String getResourcePack() {
        return this.O;
    }

    public String getResourcePackHash() {
        return this.P;
    }

    public void setResourcePack(String s, String s1) {
        this.O = s;
        this.P = s1;
    }

    @Override
    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", false);
        mojangstatisticsgenerator.a("whitelist_count", 0);
        if (this.playerList != null) {
            mojangstatisticsgenerator.a("players_current", this.getPlayerCount());
            mojangstatisticsgenerator.a("players_max", this.getMaxPlayers());
            mojangstatisticsgenerator.a("players_seen", this.getWorldServer(DimensionManager.OVERWORLD).getDataManager().getSeenPlayers().length);
        }

        mojangstatisticsgenerator.a("uses_auth", this.onlineMode);
        mojangstatisticsgenerator.a("gui_state", this.aj() ? "enabled" : "disabled");
        mojangstatisticsgenerator.a("run_time", (SystemUtils.getMonotonicMillis() - mojangstatisticsgenerator.g()) / 60L * 1000L);
        mojangstatisticsgenerator.a("avg_tick_ms", (int) (MathHelper.a(this.f) * 1.0E-6D));
        int i = 0;
        Iterator iterator = this.getWorlds().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (worldserver != null) {
                WorldData worlddata = worldserver.getWorldData();

                mojangstatisticsgenerator.a("world[" + i + "][dimension]", worldserver.worldProvider.getDimensionManager());
                mojangstatisticsgenerator.a("world[" + i + "][mode]", worlddata.getGameType());
                mojangstatisticsgenerator.a("world[" + i + "][difficulty]", worldserver.getDifficulty());
                mojangstatisticsgenerator.a("world[" + i + "][hardcore]", worlddata.isHardcore());
                mojangstatisticsgenerator.a("world[" + i + "][generator_name]", worlddata.getType().name());
                mojangstatisticsgenerator.a("world[" + i + "][generator_version]", worlddata.getType().getVersion());
                mojangstatisticsgenerator.a("world[" + i + "][height]", this.G);
                mojangstatisticsgenerator.a("world[" + i + "][chunks_loaded]", worldserver.getChunkProvider().h());
                ++i;
            }
        }

        mojangstatisticsgenerator.a("worlds", i);
    }

    public abstract boolean m();

    public boolean getOnlineMode() {
        return this.onlineMode;
    }

    public void setOnlineMode(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean Y() {
        return this.A;
    }

    public void h(boolean flag) {
        this.A = flag;
    }

    public boolean getSpawnAnimals() {
        return this.spawnAnimals;
    }

    public void setSpawnAnimals(boolean flag) {
        this.spawnAnimals = flag;
    }

    public boolean getSpawnNPCs() {
        return this.spawnNPCs;
    }

    public abstract boolean n();

    public void setSpawnNPCs(boolean flag) {
        this.spawnNPCs = flag;
    }

    public boolean getPVP() {
        return this.pvpMode;
    }

    public void setPVP(boolean flag) {
        this.pvpMode = flag;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public int getMaxBuildHeight() {
        return this.G;
    }

    public void b(int i) {
        this.G = i;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void a(PlayerList playerlist) {
        this.playerList = playerlist;
    }

    public abstract boolean p();

    public void setGamemode(EnumGamemode enumgamemode) {
        Iterator iterator = this.getWorlds().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            worldserver.getWorldData().setGameType(enumgamemode);
        }

    }

    @Nullable
    public ServerConnection getServerConnection() {
        return this.serverConnection == null ? this.serverConnection = new ServerConnection(this) : this.serverConnection; // Spigot
    }

    public boolean aj() {
        return false;
    }

    public abstract boolean a(EnumGamemode enumgamemode, boolean flag, int i);

    public int ak() {
        return this.ticks;
    }

    public void al() {
        this.T = true;
    }

    public int getSpawnProtection() {
        return 16;
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return false;
    }

    public void setForceGamemode(boolean flag) {
        this.U = flag;
    }

    public boolean getForceGamemode() {
        return this.U;
    }

    public int getIdleTimeout() {
        return this.H;
    }

    public void setIdleTimeout(int i) {
        this.H = i;
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return this.minecraftSessionService;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.gameProfileRepository;
    }

    public UserCache getUserCache() {
        return this.userCache;
    }

    public ServerPing getServerPing() {
        return this.serverPing;
    }

    public void invalidatePingSample() {
        this.Z = 0L;
    }

    public int ax() {
        return 29999984;
    }

    @Override
    public boolean isNotMainThread() {
        return super.isNotMainThread() && !this.isStopped();
    }

    @Override
    public Thread getThread() {
        return this.serverThread;
    }

    public int aA() {
        return 256;
    }

    public long aB() {
        return this.nextTick;
    }

    public DataFixer aC() {
        return this.dataConverterManager;
    }

    public int a(@Nullable WorldServer worldserver) {
        return worldserver != null ? worldserver.getGameRules().getInt(GameRules.SPAWN_RADIUS) : 10;
    }

    public AdvancementDataWorld getAdvancementData() {
        return this.advancementDataWorld;
    }

    public CustomFunctionData getFunctionData() {
        return this.customFunctionData;
    }

    public void reload() {
        if (!this.isMainThread()) {
            this.execute(this::reload);
        } else {
            this.getPlayerList().savePlayers();
            this.resourcePackRepository.a();
            this.b(this.getWorldServer(DimensionManager.OVERWORLD).getWorldData());
            this.getPlayerList().reload();
            this.bb();
        }
    }

    private void b(WorldData worlddata) {
        List<ResourcePackLoader> list = Lists.newArrayList(this.resourcePackRepository.d());
        Iterator iterator = this.resourcePackRepository.b().iterator();

        while (iterator.hasNext()) {
            ResourcePackLoader resourcepackloader = (ResourcePackLoader) iterator.next();

            if (!worlddata.N().contains(resourcepackloader.e()) && !list.contains(resourcepackloader)) {
                MinecraftServer.LOGGER.info("Found new data pack {}, loading it automatically", resourcepackloader.e());
                resourcepackloader.h().a(list, resourcepackloader, (resourcepackloader1) -> {
                    return resourcepackloader1;
                }, false);
            }
        }

        this.resourcePackRepository.a((Collection) list);
        List<IResourcePack> list1 = Lists.newArrayList();

        this.resourcePackRepository.d().forEach((resourcepackloader1) -> {
            list1.add(resourcepackloader1.d());
        });
        CompletableFuture<Unit> completablefuture = this.ae.a(this.executorService, this, list1, MinecraftServer.i);

        this.awaitTasks(completablefuture::isDone);

        try {
            completablefuture.get();
        } catch (Exception exception) {
            MinecraftServer.LOGGER.error("Failed to reload data packs", exception);
        }

        worlddata.O().clear();
        worlddata.N().clear();
        this.resourcePackRepository.d().forEach((resourcepackloader1) -> {
            worlddata.O().add(resourcepackloader1.e());
        });
        this.resourcePackRepository.b().forEach((resourcepackloader1) -> {
            if (!this.resourcePackRepository.d().contains(resourcepackloader1)) {
                worlddata.N().add(resourcepackloader1.e());
            }

        });
    }

    public void a(CommandListenerWrapper commandlistenerwrapper) {
        if (this.aT()) {
            PlayerList playerlist = commandlistenerwrapper.getServer().getPlayerList();
            WhiteList whitelist = playerlist.getWhitelist();

            if (whitelist.isEnabled()) {
                List<EntityPlayer> list = Lists.newArrayList(playerlist.getPlayers());
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                    if (!whitelist.isWhitelisted(entityplayer.getProfile())) {
                        entityplayer.playerConnection.disconnect(new ChatMessage("multiplayer.disconnect.not_whitelisted", new Object[0]));
                    }
                }

            }
        }
    }

    public IReloadableResourceManager getResourceManager() {
        return this.ae;
    }

    public ResourcePackRepository<ResourcePackLoader> getResourcePackRepository() {
        return this.resourcePackRepository;
    }

    public CommandDispatcher getCommandDispatcher() {
        return this.commandDispatcher;
    }

    public CommandListenerWrapper getServerCommandListener() {
        return new CommandListenerWrapper(this, this.getWorldServer(DimensionManager.OVERWORLD) == null ? Vec3D.a : new Vec3D(this.getWorldServer(DimensionManager.OVERWORLD).getSpawn()), Vec2F.a, this.getWorldServer(DimensionManager.OVERWORLD), 4, "Server", new ChatComponentText("Server"), this, (Entity) null);
    }

    @Override
    public boolean shouldSendSuccess() {
        return true;
    }

    @Override
    public boolean shouldSendFailure() {
        return true;
    }

    public CraftingManager getCraftingManager() {
        return this.craftingManager;
    }

    public TagRegistry getTagRegistry() {
        return this.tagRegistry;
    }

    public ScoreboardServer getScoreboard() {
        return this.scoreboardServer;
    }

    public PersistentCommandStorage aO() {
        if (this.persistentCommandStorage == null) {
            throw new NullPointerException("Called before server init");
        } else {
            return this.persistentCommandStorage;
        }
    }

    public LootTableRegistry getLootTableRegistry() {
        return this.lootTableRegistry;
    }

    public LootPredicateManager aQ() {
        return this.lootPredicateManager;
    }

    public GameRules getGameRules() {
        return this.getWorldServer(DimensionManager.OVERWORLD).getGameRules();
    }

    public BossBattleCustomData getBossBattleCustomData() {
        return this.bossBattleCustomData;
    }

    public boolean aT() {
        return this.as;
    }

    public void n(boolean flag) {
        this.as = flag;
    }

    public float aU() {
        return this.av;
    }

    public int b(GameProfile gameprofile) {
        if (this.getPlayerList().isOp(gameprofile)) {
            OpListEntry oplistentry = (OpListEntry) this.getPlayerList().getOPs().get(gameprofile);

            return oplistentry != null ? oplistentry.a() : (this.a(gameprofile) ? 4 : (this.isEmbeddedServer() ? (this.getPlayerList().v() ? 4 : 0) : this.j()));
        } else {
            return 0;
        }
    }

    public GameProfiler getMethodProfiler() {
        return this.methodProfiler;
    }

    public Executor aX() {
        return this.executorService;
    }

    public abstract boolean a(GameProfile gameprofile);

    public void a(java.nio.file.Path java_nio_file_path) throws IOException {
        java.nio.file.Path java_nio_file_path1 = java_nio_file_path.resolve("levels");
        Iterator iterator = this.worldServer.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<DimensionManager, WorldServer> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = DimensionManager.a((DimensionManager) entry.getKey());
            java.nio.file.Path java_nio_file_path2 = java_nio_file_path1.resolve(minecraftkey.getNamespace()).resolve(minecraftkey.getKey());

            Files.createDirectories(java_nio_file_path2);
            ((WorldServer) entry.getValue()).a(java_nio_file_path2);
        }

        this.d(java_nio_file_path.resolve("gamerules.txt"));
        this.e(java_nio_file_path.resolve("classpath.txt"));
        this.c(java_nio_file_path.resolve("example_crash.txt"));
        this.b(java_nio_file_path.resolve("stats.txt"));
        this.f(java_nio_file_path.resolve("threads.txt"));
    }

    private void b(java.nio.file.Path java_nio_file_path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(java_nio_file_path);
        Throwable throwable = null;

        try {
            bufferedwriter.write(String.format("pending_tasks: %d\n", this.bh()));
            bufferedwriter.write(String.format("average_tick_time: %f\n", this.aU()));
            bufferedwriter.write(String.format("tick_times: %s\n", Arrays.toString(this.f)));
            bufferedwriter.write(String.format("queue: %s\n", SystemUtils.e()));
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (bufferedwriter != null) {
                if (throwable != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    bufferedwriter.close();
                }
            }

        }

    }

    private void c(java.nio.file.Path java_nio_file_path) throws IOException {
        CrashReport crashreport = new CrashReport("Server dump", new Exception("dummy"));

        this.b(crashreport);
        BufferedWriter bufferedwriter = Files.newBufferedWriter(java_nio_file_path);
        Throwable throwable = null;

        try {
            bufferedwriter.write(crashreport.e());
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (bufferedwriter != null) {
                if (throwable != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    bufferedwriter.close();
                }
            }

        }

    }

    private void d(java.nio.file.Path java_nio_file_path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(java_nio_file_path);
        Throwable throwable = null;

        try {
            final List<String> list = Lists.newArrayList();
            final GameRules gamerules = this.getGameRules();

            GameRules.a(new GameRules.GameRuleVisitor() {
                @Override
                public <T extends GameRules.GameRuleValue<T>> void a(GameRules.GameRuleKey<T> gamerules_gamerulekey, GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
                    list.add(String.format("%s=%s\n", gamerules_gamerulekey.a(), gamerules.get(gamerules_gamerulekey).toString()));
                }
            });
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();

                bufferedwriter.write(s);
            }
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (bufferedwriter != null) {
                if (throwable != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    bufferedwriter.close();
                }
            }

        }

    }

    private void e(java.nio.file.Path java_nio_file_path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(java_nio_file_path);
        Throwable throwable = null;

        try {
            String s = System.getProperty("java.class.path");
            String s1 = System.getProperty("path.separator");
            Iterator iterator = Splitter.on(s1).split(s).iterator();

            while (iterator.hasNext()) {
                String s2 = (String) iterator.next();

                bufferedwriter.write(s2);
                bufferedwriter.write("\n");
            }
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (bufferedwriter != null) {
                if (throwable != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    bufferedwriter.close();
                }
            }

        }

    }

    private void f(java.nio.file.Path java_nio_file_path) throws IOException {
        ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] athreadinfo = threadmxbean.dumpAllThreads(true, true);

        Arrays.sort(athreadinfo, Comparator.comparing(ThreadInfo::getThreadName));
        BufferedWriter bufferedwriter = Files.newBufferedWriter(java_nio_file_path);
        Throwable throwable = null;

        try {
            ThreadInfo[] athreadinfo1 = athreadinfo;
            int i = athreadinfo.length;

            for (int j = 0; j < i; ++j) {
                ThreadInfo threadinfo = athreadinfo1[j];

                bufferedwriter.write(threadinfo.toString());
                bufferedwriter.write(10);
            }
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (bufferedwriter != null) {
                if (throwable != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    bufferedwriter.close();
                }
            }

        }

    }

    private void bb() {
        Block.REGISTRY_ID.forEach(IBlockData::c);
    }

    // CraftBukkit start
    @Override
    public boolean isMainThread() {
        return super.isMainThread() || this.isStopped(); // CraftBukkit - MC-142590
    }

    // @Deprecated // StarLink
    public static MinecraftServer getServer() {
        return (Bukkit.getServer() instanceof CraftServer) ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }
    // CraftBukkit end
}
