package cloud.natsuki;

import cloud.natsuki.event.server_lifecycle_events;
import cloud.natsuki.event.spawn_event_handler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("nomonster")
public class nomonster_mod {

    public nomonster_mod() {
        MinecraftForge.EVENT_BUS.register(spawn_event_handler.class);
        MinecraftForge.EVENT_BUS.register(server_lifecycle_events.class);
    }
}