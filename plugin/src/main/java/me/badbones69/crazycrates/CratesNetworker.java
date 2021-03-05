package me.badbones69.crazycrates;

import net.spacedelta.lib.message.MessageBus;
import net.spacedelta.lib.util.ConcurrentUtils;
import org.bukkit.Bukkit;

public class CratesNetworker {

    public static final String CHANNEL = "remote-cmd",
    TAG_CMD = "command";

    public CratesNetworker(Main main) {
        final MessageBus messageBus = main.getLibrary().getMessageBus();

        messageBus.registerHandler(main, CHANNEL, dataBuffer -> runCommand(dataBuffer.readString(TAG_CMD)));
    }

    public void runCommand(String string) {
        ConcurrentUtils.ensureMain(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cc " + string + " -onlyhere"));
    }

}
