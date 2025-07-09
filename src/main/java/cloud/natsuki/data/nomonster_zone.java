package cloud.natsuki.data;

import net.minecraft.core.BlockPos;

// 此文件已符合规范
public record nomonster_zone(
        String name,
        String dimension,
        BlockPos center,
        double radiusSquared
) {
}