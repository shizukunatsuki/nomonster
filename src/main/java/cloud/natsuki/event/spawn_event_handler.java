package cloud.natsuki.event;

import cloud.natsuki.data.nomonster_zone;
import cloud.natsuki.data.zone_manager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory; // 导入实体类别，这是判断敌对生物的最佳方式
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
    private static final boolean enable_detailed_logging = false;

    @SubscribeEvent
    public static void on_entity_join_level(EntityJoinLevelEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Entity entity = event.getEntity();

        // --- 核心判断逻辑：检查实体的类别是否为“怪物” ---
        // 这是现代Minecraft版本中判断一个生物是否为“敌对生物”的最佳实践。
        // MobCategory.MONSTER 包含了所有天然对玩家有攻击性的生物（如僵尸、史莱姆、末影人等）。
        // 这种方法性能高、覆盖全面，并且对其他模组有良好的兼容性。
        if (entity.getType().getCategory() != MobCategory.MONSTER) {
            return;
        }

        BlockPos spawn_pos = entity.blockPosition();
        String dimension = event.getLevel().dimension().location().toString();

        Optional<nomonster_zone> matching_zone_opt = zone_manager.find_matching_zone(spawn_pos, dimension);

        if (matching_zone_opt.isPresent()) {
            // 核心功能：取消事件，阻止怪物生成
            event.setCanceled(true);

            // --- 健壮性处理：处理骑乘实体（如鸡骑士） ---
            // 当我们阻止一个作为“乘客”的敌对生物（如僵尸）生成时，
            // 必须一并移除它的“载具”（如鸡），以防止载具单独生成。
            if (entity.isPassenger()) {
                Entity vehicle = entity.getVehicle();
                if (vehicle != null) {
                    // discard() 是从世界中安全移除实体的标准方法。
                    vehicle.discard();
                }
            }

            // --- 调试日志（仅在需要时开启） ---
            if (enable_detailed_logging) {
                nomonster_zone zone = matching_zone_opt.get();

                double dx = zone.center().getX() - spawn_pos.getX();
                double dz = zone.center().getZ() - spawn_pos.getZ();
                double horizontal_distance_sq = dx * dx + dz * dz;

                LOGGER.info(
                        "Blocked {} from joining level inside zone '{}'. Center[x={}, z={}], Spawn[x={}, z={}]. DistanceSq: {} <= RadiusSq: {}",
                        entity.getType().toShortString(),
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