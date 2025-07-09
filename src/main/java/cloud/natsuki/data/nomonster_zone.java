package cloud.natsuki.data;

import net.minecraft.core.BlockPos;

public record nomonster_zone(
        String name,
        String dimension,
        BlockPos center,
        double radiusSquared
) {
}