package cloud.natsuki.event;

import cloud.natsuki.data.nomonster_zone;
import cloud.natsuki.data.zone_manager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Mod.EventBusSubscriber
public class spawn_event_handler {

    private static final Logger LOGGER = LogManager.getLogger("NoMonster");

    // --- 用于控制详细日志输出的开关 ---
    // 设置为 false: 在生产环境中运行时，不会打印任何怪物被阻止的日志，保持后台干净。
    // 设置为 true: 在需要调试范围问题时，会打印详细的诊断日志。
    private static final boolean enable_detailed_logging = false;

    @SubscribeEvent
    public static void on_entity_join_level(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Monster) || event.isCanceled()) {
            return;
        }

        BlockPos spawn_pos = event.getEntity().blockPosition();
        String dimension = event.getLevel().dimension().location().toString();

        Optional<nomonster_zone> matching_zone_opt = zone_manager.find_matching_zone(spawn_pos, dimension);

        if (matching_zone_opt.isPresent()) {
            // 核心功能：取消事件，阻止怪物生成
            event.setCanceled(true);

            // --- 只有在开关打开时才执行日志记录 ---
            if (enable_detailed_logging) {
                nomonster_zone zone = matching_zone_opt.get();

                // 为了性能，只在需要记录日志时才进行这些计算
                double dx = zone.center().getX() - spawn_pos.getX();
                double dz = zone.center().getZ() - spawn_pos.getZ();
                double horizontal_distance_sq = dx * dx + dz * dz;

                LOGGER.info(
                        "Blocked {} from joining level inside zone '{}'. Center[x={}, z={}], Spawn[x={}, z={}]. DistanceSq: {} <= RadiusSq: {}",
                        event.getEntity().getType().toShortString(),
                        zone.name(),
                        zone.center().getX(), zone.center().getZ(),
                        spawn_pos.getX(), spawn_pos.getZ(),
                        horizontal_distance_sq,
                        zone.radiusSquared()
                );
            }
        }
    }
}