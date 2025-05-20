package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Task;
import gg.lode.bookshelfapi.api.compat.LuckPermsCompat;
import gg.lode.bookshelfapi.api.event.PlayerChatEvent;
import gg.lode.bookshelfapi.api.manager.IChatManager;
import gg.lode.bookshelfapi.api.util.MiniMessageUtil;
import gg.lode.bookshelfapi.api.util.StringUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class APIChatManager implements Listener, IChatManager {
    private final JavaPlugin plugin;
    private final HashMap<UUID, String> spamMessageCooldown = new HashMap<>();
    private final HashMap<UUID, Long> spamCooldown = new HashMap<>();
    private boolean chatMuted = false;

    public APIChatManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @SuppressWarnings("deprecated")
    @EventHandler(priority = EventPriority.HIGH)
    public void on(PlayerChatEvent event) {
        Component suffix;
        Component prefix;
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isOp()) {
            String rawMessage;
            if (this.chatMuted && !this.canPlayerSpeak(player.getUniqueId())) {
                player.sendMessage(MiniMessageUtil.deserialize(
                        "<red><bold>UH OH! <reset><red>Chat is currently muted!"));
                event.setCancelled(true);
                return;
            }
            if (!this.canPlayerSpeak(player.getUniqueId()) && this.containsDuplicateCharacterSegment(
                    rawMessage = MiniMessageUtil.serialize(event.message()).toLowerCase())) {
                Bukkit.broadcast(MiniMessageUtil.deserialize("<gray><italic>[%s: Has spammed an abundance of characters: %s]", player.getName(), rawMessage), "lodestone.bookshelf.alerts");
                player.sendMessage(MiniMessageUtil.deserialize("<red><bold>UH OH! <reset><red>Your message contains an absurd amount of character spam!"));
                event.setCancelled(true);
                return;
            }
        }
        if (!player.hasPermission("lodestone.bookshelf.chat_bypass")) {
            int chatCooldown = 3;
            String cooldownNode = LuckPermsCompat.getGroupNodeValue(player.getUniqueId(),
                    "chat_cooldown.");
            if (cooldownNode != null) {
                try {
                    chatCooldown = Integer.parseInt(cooldownNode);
                } catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            if (this.spamCooldown.containsKey(player.getUniqueId())
                    && this.spamCooldown.get(player.getUniqueId()) - System.currentTimeMillis() > 0L) {
                player.sendMessage(MiniMessageUtil.deserialize(
                        "<red><bold>UH OH! <reset><red>You can send messages every %s!",
                        StringUtil.getTimeString((long) chatCooldown * 1000L)));
                event.setCancelled(true);
                return;
            }
            if (this.spamMessageCooldown.containsKey(player.getUniqueId())
                    && this.spamMessageCooldown.get(player.getUniqueId())
                    .equalsIgnoreCase(MiniMessageUtil.serialize(event.message()))) {
                Bukkit.broadcast(MiniMessageUtil.deserialize(
                        "<gray><italic>[%s: Has sent the same message twice.]",
                        player.getName()), "lodestone.bookshelf.alerts");
                player.sendMessage(MiniMessageUtil.deserialize(
                        "<red><bold>UH OH! <reset><red>You cannot send the same message twice in a row!"
                ));
                event.setCancelled(true);
                return;
            }
            this.spamCooldown.put(player.getUniqueId(),
                    (System.currentTimeMillis() + (long) chatCooldown * 1000L));
            this.spamMessageCooldown.put(player.getUniqueId(),
                    MiniMessageUtil.serialize(event.message()));
        }
        if (!event.isModified()) {
            String prefixStr = LuckPermsCompat.getGroupNodeValue(player.getUniqueId(), "prefix.");
            String suffixStr = LuckPermsCompat.getGroupNodeValue(player.getUniqueId(), "suffix.");
            String rankColor = LuckPermsCompat.getGroupNodeValue(player.getUniqueId(), "rank_color.");
            String chatColor = LuckPermsCompat.getGroupNodeValue(player.getUniqueId(), "chat_color.");
            if (chatColor != null) {
                event.messageColor(chatColor);
            }
            if (suffixStr != null) {
                event.suffix(MiniMessageUtil.persistStyle(
                        MiniMessageUtil.deserialize(suffixStr),
                        event.suffix() == null ? Component.empty() : event.suffix()));
            }
            if (prefixStr != null) {
                event.prefix(MiniMessageUtil.persistStyle(
                        MiniMessageUtil.deserialize(prefixStr),
                        event.prefix() == null ? Component.empty() : event.prefix()));
            }
            if (rankColor != null) {
                event.playerColor(rankColor);
            }
        }
        prefix = event.prefix();
        suffix = event.suffix();
        boolean hasNoPrefix = prefix == null || MiniMessageUtil.serialize(prefix).isBlank();
        boolean hasNoSuffix = suffix == null || MiniMessageUtil.serialize(suffix).isBlank();
        Component tellRawComponent = Component.empty();
        if (!hasNoPrefix) {
            tellRawComponent = tellRawComponent.append(prefix);
        }
        tellRawComponent = tellRawComponent.append(MiniMessageUtil.deserialize("%s<%s>%s: <reset>".trim(),
                hasNoPrefix || MiniMessageUtil.serialize(prefix).endsWith(" ") ? "" : " ",
                event.playerColor(), event.getPlayer().getDisplayName().replaceAll("§[a-zA-Z0-9]", "")));
        tellRawComponent = tellRawComponent.append(event.message())
                .color(TextColor.fromHexString(event.messageColor()))
                .append(Component
                        .text(hasNoSuffix || MiniMessageUtil.serialize(suffix).endsWith(" ") ? ""
                                : " "));
        if (!hasNoSuffix) {
            tellRawComponent = tellRawComponent.append(suffix);
        }
        for (Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (event.getPermission() != null && !p.hasPermission(event.getPermission())
                    || event.getViewers().size() > 0 && !event.getViewers().contains(p.getUniqueId()))
                continue;
            p.sendMessage(tellRawComponent);
        }
        this.plugin.getServer().getConsoleSender().sendMessage(tellRawComponent);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        this.spamCooldown.remove(event.getPlayer().getUniqueId());
        this.spamMessageCooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, event.message());
        if (player.isOp()) {
            playerChatEvent.messageColor("white");
            playerChatEvent.playerColor("white");
            playerChatEvent.prefix(MiniMessageUtil.deserialize("<red><bold>STAFF<reset>"));
        }
        Task.later(plugin, playerChatEvent::callEvent, 1L);
        event.setCancelled(true);
    }

    @Override
    public boolean isChatMuted() {
        return this.chatMuted;
    }

    @Override
    public void setChatMuted(boolean value) {
        this.chatMuted = value;
    }

    @Override
    public boolean canPlayerSpeak(UUID uniqueId) {
        Player player = this.plugin.getServer().getPlayer(uniqueId);
        return player != null && player.hasPermission("lodestone.bookshelf.chat_bypass");
    }

    private boolean containsDuplicateCharacterSegment(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (String word : str.split("\\s+")) {
            if (!this.isSingleCharacterRepeated(word))
                continue;
            return true;
        }
        return false;
    }

    private boolean isSingleCharacterRepeated(String word) {
        if (word.length() < 5) {
            return false;
        }
        char firstChar = word.charAt(0);
        for (int i = 1; i < word.length(); ++i) {
            if (word.charAt(i) == firstChar)
                continue;
            return false;
        }
        return true;
    }
}