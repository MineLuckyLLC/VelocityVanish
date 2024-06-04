package net.minelucky.vanish.utils.adventure;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.minelucky.vanish.ruom.Ruom;

public class AdventureApi {

    private static BukkitAudiences adventure;

    public static BukkitAudiences get() {
        return adventure;
    }

    public static void initialize() {
        if (adventure == null)
            adventure = BukkitAudiences.create(Ruom.getPlugin());
    }
}