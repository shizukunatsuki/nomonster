package cloud.natsuki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import cloud.natsuki.data.nomonster_zone;
import cloud.natsuki.data.zone_manager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class nomonster_commands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("nomonster")
                .requires(source -> source.hasPermission(2))
                .then(add_command())
                .then(delete_command())
                .then(list_command());

        dispatcher.register(command);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> add_command() {
        return Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                .then(Commands.argument("range", DoubleArgumentType.doubleArg(0.1))
                                        // 方法引用已修改
                                        .executes(nomonster_commands::execute_add))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> delete_command() {
        return Commands.literal("delete")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(nomonster_commands::execute_delete));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> list_command() {
        return Commands.literal("list")
                .executes(nomonster_commands::execute_list);
    }

    private static int execute_add(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        BlockPos center = BlockPosArgument.getBlockPos(context, "center");
        double range = DoubleArgumentType.getDouble(context, "range");
        String dimension = source.getLevel().dimension().location().toString();

        nomonster_zone zone = new nomonster_zone(name, dimension, center, range * range);

        if (zone_manager.add_zone(zone)) {
            zone_manager.save_data(source.getServer());
            source.sendSuccess(() -> Component.literal("成功添加无怪物区域 '" + name + "'"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("错误：名称为 '" + name + "' 的区域已存在。"));
            return 0;
        }
    }

    private static int execute_delete(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");

        if (zone_manager.remove_zone(name)) {
            zone_manager.save_data(source.getServer());
            source.sendSuccess(() -> Component.literal("成功删除无怪物区域 '" + name + "'"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("错误：未找到名称为 '" + name + "' 的区域。"));
            return 0;
        }
    }

    private static int execute_list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        var zones = zone_manager.get_all_zones();

        if (zones.isEmpty()) {
            source.sendSuccess(() -> Component.literal("当前没有已定义的无怪物区域。"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("--- 无怪物区域列表 ---"), false);
        for (nomonster_zone zone : zones) {
            String message = String.format(
                    " - %s: 中心[%d, %d, %d], 半径[%.1f], 维度[%s]",
                    zone.name(),
                    zone.center().getX(), zone.center().getY(), zone.center().getZ(),
                    Math.sqrt(zone.radiusSquared()),
                    zone.dimension()
            );
            source.sendSuccess(() -> Component.literal(message), false);
        }
        return zones.size();
    }
}