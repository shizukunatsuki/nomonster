package cloud.natsuki.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class zone_manager {
    private static final Logger logger = LogManager.getLogger("NoMonster");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String config_file_name = "nomonster-zones.json";
    private static volatile Map<String, nomonster_zone> zones = ImmutableMap.of();
    public static synchronized boolean add_zone(nomonster_zone zone) { if (zones.containsKey(zone.name())) { return false; } Map<String, nomonster_zone> new_zones = new ConcurrentHashMap<>(zones); new_zones.put(zone.name(), zone); zones = ImmutableMap.copyOf(new_zones); return true; }
    public static synchronized boolean remove_zone(String name) { if (!zones.containsKey(name)) { return false; } Map<String, nomonster_zone> new_zones = new ConcurrentHashMap<>(zones); new_zones.remove(name); zones = ImmutableMap.copyOf(new_zones); return true; }
    public static Optional<nomonster_zone> get_zone(String name) { return Optional.ofNullable(zones.get(name)); }
    public static Collection<nomonster_zone> get_all_zones() { return zones.values(); }
    private static Path get_config_file(MinecraftServer server) { return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve(config_file_name); }
    public static void save_data(MinecraftServer server) { Path config_file = get_config_file(server); try (FileWriter writer = new FileWriter(config_file.toFile())) { gson.toJson(zones, writer); logger.info("NoMonster zones saved successfully."); } catch (IOException e) { logger.error("Failed to save NoMonster zones", e); } }
    public static synchronized void load_data(MinecraftServer server) { Path config_file = get_config_file(server); if (!Files.exists(config_file)) { logger.info("NoMonster zones file not found, starting with empty list."); zones = ImmutableMap.of(); return; } try (FileReader reader = new FileReader(config_file.toFile())) { Type type = new TypeToken<ConcurrentHashMap<String, nomonster_zone>>() {}.getType(); Map<String, nomonster_zone> loaded_zones = gson.fromJson(reader, type); if (loaded_zones != null) { zones = ImmutableMap.copyOf(loaded_zones); logger.info("Loaded {} NoMonster zones.", zones.size()); } else { zones = ImmutableMap.of(); logger.warn("NoMonster zones file was empty or malformed, starting with empty list."); } } catch (IOException e) { logger.error("Failed to load NoMonster zones", e); zones = ImmutableMap.of(); } }
    //
    public static Optional<nomonster_zone> find_matching_zone(BlockPos pos, String dimension) {
        for (nomonster_zone zone : zones.values()) {
            if (zone.dimension().equals(dimension)) {
                double dx = zone.center().getX() - pos.getX();
                double dz = zone.center().getZ() - pos.getZ();
                double horizontal_distance_sq = dx * dx + dz * dz;

                if (horizontal_distance_sq <= zone.radiusSquared()) {
                    // 找到匹配项，立即返回包含该区域的 Optional
                    return Optional.of(zone);
                }
            }
        }
        // 循环结束都没有找到，返回空的 Optional
        return Optional.empty();
    }
}