package cloud.natsuki.event;

import cloud.natsuki.command.nomonster_commands;
import cloud.natsuki.data.zone_manager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class server_lifecycle_events {

    @SubscribeEvent
    public static void on_server_starting(ServerStartingEvent event) {
        zone_manager.load_data(event.getServer());
    }

    @SubscribeEvent
    public static void on_register_commands(RegisterCommandsEvent event) {
        nomonster_commands.register(event.getDispatcher());
    }
}