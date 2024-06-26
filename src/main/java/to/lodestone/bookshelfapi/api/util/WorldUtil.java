package to.lodestone.bookshelfapi.api.util;

import net.kyori.adventure.util.TriState;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Random;

public class WorldUtil {

    public static World createWorld(String worldName) {
        return new WorldCreator(worldName)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .hardcore(false)
                .type(WorldType.FLAT)
                .keepSpawnLoaded(TriState.FALSE)
                .createWorld();
    }

    public static World createFlatWorld(String worldName) {
        return new WorldCreator(worldName)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .hardcore(false)
                .type(WorldType.FLAT)
                .keepSpawnLoaded(TriState.FALSE)
                .createWorld();
    }

    public static World createVoidWorld(String worldName) {
        World world = new WorldCreator(worldName)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .generator(new VoidGenerator())
                .hardcore(false)
                .type(WorldType.FLAT)
                .keepSpawnLoaded(TriState.FALSE)
                .createWorld();

        // this shouldn't happen right?
        assert world != null;

        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setTime(0);
        world.setSpawnLocation(0, 121, 0);

        return world;
    }

    public static void deleteWorld(JavaPlugin plugin, World world) {
        plugin.getServer().unloadWorld(world, false);
        deleteWorldFolder(world);
    }

    private static void deleteWorldFolder(World world) {
        if (world == null) return;
        try {
            File worldFolder = new File(world.getWorldFolder().getPath());
            if (worldFolder.exists()) {
                FileUtils.deleteDirectory(worldFolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class VoidGenerator extends ChunkGenerator {
        @NotNull
        @Override
        public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
            return createChunkData(world); // Return empty chunk data to generate a completely void world
        }
    }

}
