package me.starchaser.restcraft;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

public class MessageInterceptingCommandRunner implements CommandSender {
    private final CommandSender wrappedSender;
    private final Spigot spigotWrapper;
    private final StringBuilder msgLog = new StringBuilder();

    private class Spigot extends CommandSender.Spigot {
        /**
         * Sends this sender a chat component.
         *
         * @param component the components to send
         */
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
            msgLog.append(BaseComponent.toLegacyText(component)).append('\n');
            wrappedSender.spigot().sendMessage();
        }

        /**
         * Sends an array of components as a single message to the sender.
         *
         * @param components the components to send
         */
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
            msgLog.append(BaseComponent.toLegacyText(components)).append('\n');
            wrappedSender.spigot().sendMessage(components);
        }
    }

    public String getMessageLog() {
        return msgLog.toString();
    }

    public String getMessageLogStripColor() {
        return ChatColor.stripColor(msgLog.toString());
    }

    public void clearMessageLog() {
        msgLog.setLength(0);
    }


    public MessageInterceptingCommandRunner(CommandSender wrappedSender) {
        this.wrappedSender = wrappedSender;
        spigotWrapper = new Spigot();
    }

    @Override
    public void sendMessage(String message) {
        wrappedSender.sendMessage(message);
        msgLog.append(message).append('\n');
    }

    @Override
    public void sendMessage(String[] messages) {
        wrappedSender.sendMessage(messages);
        for (String message : messages) {
            msgLog.append(message).append('\n');
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {

    }

    @Override
    public void sendMessage(UUID sender, String... messages) {

    }

    @Override
    public Server getServer() {
        return wrappedSender.getServer();
    }

    @Override
    public String getName() {
        return "_StarChaser";
    }

    @Override
    public CommandSender.Spigot spigot() {
        return spigotWrapper;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return wrappedSender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return wrappedSender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return wrappedSender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return wrappedSender.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return wrappedSender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return wrappedSender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return wrappedSender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return wrappedSender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        wrappedSender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        wrappedSender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return wrappedSender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return wrappedSender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        wrappedSender.setOp(value);
    }
}