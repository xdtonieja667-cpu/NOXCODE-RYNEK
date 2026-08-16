package pl.noxcode.rynek.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class Messages {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final Component PREFIX = LEGACY.deserialize("&8[&bNOX&cCODE&8] &r");

    private Messages() {
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(PREFIX.append(LEGACY.deserialize(message)));
    }
}
