package me.badbones69.crazycrates.cratetypes;

import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.Prize;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CrateVirtualOnTheGo {

    /**
     * Click and instant reward crate
     *
     * like {@link CrateOnTheGo} but for virtual instant reward click.
     *
     * @author Ellie
     * @param crate crate to give
     * @param player player to issue prize to
     */
    public static void issueReward(Crate crate, Player player) {
        final Prize prize = crate.pickPrize(player);
        if (prize != null) {
            if (prize.useFireworks()) {
                Methods.fireWork(player.getLocation().add(0, 1, 0));
            }

            CrazyCrates.getInstance().givePrize(player, prize);

            player.playSound(player.getLocation(), CrazyCrates.getInstance().getSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"), 1, 1);
            Bukkit.getPluginManager().callEvent(new PlayerPrizeEvent(player, crate, crate.getName(), prize));
        } else
            player.sendMessage(Methods.getPrefix("&cNo prize was found, please report this issue if you think this is an error."));

        CrazyCrates.getInstance().removePlayerFromOpeningList(player);
    }

}
