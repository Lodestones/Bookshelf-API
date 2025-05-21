package gg.lode.bookshelfapi;

import gg.lode.bookshelfapi.api.manager.*;
import gg.lode.bookshelfapi.api.manager.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Official API of the Bookshelf Plugin
 * This interface allows access to certain internals of the teams plugin.
 *
 * @author John Aquino
 */
public class BookshelfAPI {

    private static IBookshelfAPI api;

    public static class Builder {

        /**
         * Creates a builder that allows you to set which managers to register.
         * @return A builder that allows you to set which managers to register.
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Creates a builder that disables all managers.
         * This is useful for plugins that do not want to use the API.
         * @return A builder that disables all managers.
         */
        public static Builder createDisabled() {
            return new Builder()
                    .useMenuManager(false)
                    .useCooldownManager(false)
                    .useChatManager(false)
                    .useGameManager(false)
                    .usePlayerManager(false)
                    .useItemManager(false);
        }

        private boolean shouldRegisterMenuManager = true;
        private boolean shouldRegisterCooldownManager = true;
        private boolean shouldRegisterChatManager = true;
        private boolean shouldRegisterGameManager = true;
        private boolean shouldRegisterPlayerManager = true;
        private boolean shouldRegisterItemManager = true;

        public Builder useMenuManager(boolean shouldRegisterMenuManager) {
            this.shouldRegisterMenuManager = shouldRegisterMenuManager;
            return this;
        }

        public Builder useCooldownManager(boolean shouldRegisterCooldownManager) {
            this.shouldRegisterCooldownManager = shouldRegisterCooldownManager;
            return this;
        }

        public Builder useChatManager(boolean shouldRegisterChatManager) {
            this.shouldRegisterChatManager = shouldRegisterChatManager;
            return this;
        }

        public Builder useGameManager(boolean shouldRegisterGameManager) {
            this.shouldRegisterGameManager = shouldRegisterGameManager;
            return this;
        }

        public Builder usePlayerManager(boolean shouldRegisterPlayerManager) {
            this.shouldRegisterPlayerManager = shouldRegisterPlayerManager;
            return this;
        }

        public Builder useItemManager(boolean shouldRegisterItemManager) {
            this.shouldRegisterItemManager = shouldRegisterItemManager;
            return this;
        }

    }

    /**
     * Initializes the API.
     * This can be used if plugins attempt to shade the API into their plugin.
     * <p>
     * Remember to relocate the API to avoid conflicts with other plugins that uses the main plugin.
     * This method enables all managers. So note which plugins you use as other plugins may have registered their own Bookshelf API.
     *
     * @param plugin The main plugin that is using the API.
     */
    public static void init(JavaPlugin plugin) {
        init(plugin, Builder.create());
    }

    /**
     * Initializes the API.
     * This can be used if plugins attempt to shade the API into their plugin.
     * <p>
     * Remember to relocate the API to avoid conflicts with other plugins that uses the main plugin.
     *
     * @param plugin The main plugin that is using the API.
     * @param builder The builder that allows you to set which managers to register.
     */
    public static void init(JavaPlugin plugin, Builder builder) {
        BookshelfAPI.api = new IBookshelfAPI() {
            private final APIMenuManager menuManager = new APIMenuManager(plugin);
            private final APICooldownManager cooldownManager = new APICooldownManager(plugin);
            private final APIChatManager chatManager = new APIChatManager(plugin);
            private final APIGameManager gameManager = new APIGameManager(plugin);
            private final APIPlayerManager playerManager = new APIPlayerManager(plugin);
            private final APICustomItemManager itemManager = new APICustomItemManager(plugin);

            @Override
            public IMenuManager getMenuManager() {
                if (builder.shouldRegisterMenuManager) {
                    return menuManager;
                } else {
                    throw new UnsupportedOperationException("MenuManager is disabled, please enable it in BookshelfAPI.Builder!");
                }
            }

            @Override
            public ICooldownManager getCooldownManager() {
                if (builder.shouldRegisterCooldownManager) {
                    return cooldownManager;
                } else {
                    throw new UnsupportedOperationException("CooldownManager is disabled, please enable it in BookshelfAPI.Builder!");
                }
            }

            @Override
            public IChatManager getChatManager() {
                if (builder.shouldRegisterChatManager) {
                    return chatManager;
                } else {
                    throw new UnsupportedOperationException("ChatManager is disabled, please enable it in BookshelfAPI.Builder!");
                }
            }

            @Override
            public IGameManager getGameManager() {
                if (builder.shouldRegisterGameManager) {
                    return gameManager;
                } else {
                    throw new UnsupportedOperationException("GameManager is disabled, please enable it in BookshelfAPI.Builder!");
                }
            }

            @Override
            public IPlayerManager getPlayerManager() {
                if (builder.shouldRegisterPlayerManager) {
                    return playerManager;
                } else {
                    throw new UnsupportedOperationException("PlayerManager is disabled, please enable it in BookshelfAPI.Builder!");
                }
            }

            @Override
            public ICustomItemManager getItemManager() {
                if (builder.shouldRegisterItemManager) {
                    return itemManager;
                } else {
                    throw new UnsupportedOperationException("ItemManager is disabled, please enable it in BookshelfAPI.Builder!");
                }
            }

            @Override
            public IVanishManager getVanishManager() {
                throw new UnsupportedOperationException("VanishManager is only available with the Bookshelf plugin! Please install Bookshelf to use this feature.");
            }
        };
    }

    /**
     * Internal use of the API for Bookshelf to use.
     * DO NOT TOUCH!!
     * @param api {@link IBookshelfAPI}
     */
    public static void initInternalApi(IBookshelfAPI api) {
        BookshelfAPI.api = api;
    }

    /**
     * Retrieves the API that Bookshelf uses.
     */
    public static IBookshelfAPI getApi() {
        return api;
    }

    public static boolean is1_21() {
        return Bukkit.getServer().getMinecraftVersion().startsWith("1.21");
    }

    public static boolean isHigher1_21_4() {
        if (is1_21()) {
            String version = Bukkit.getServer().getMinecraftVersion();
            String[] split = version.split("\\.");
            if (split.length >= 3) {
                int minor = Integer.parseInt(split[1]);
                int patch = Integer.parseInt(split[2]);
                return minor > 21 || (minor == 21 && patch >= 4);
            }
        }

        return false;
    }

}
