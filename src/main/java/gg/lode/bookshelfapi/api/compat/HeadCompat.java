package gg.lode.bookshelfapi.api.compat;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Player heads drawn inline in text, from a skin texture.
 *
 * <p>This is the object text component Minecraft added in 1.21.9 (Adventure 4.25). Bookshelf-API
 * compiles against an older Paper that has none of it, so everything is reached by reflection.
 * On a server without it {@link #isSupported()} is false and {@link #texture(String)} returns
 * null, so callers can simply leave the head out.
 *
 * <p>MiniMessage's {@code <head>} tag only takes a player name, a UUID or a resource pack
 * texture path, which is why custom skins, like the flag heads from minecraft-heads.com, have
 * to be built here instead.
 */
public final class HeadCompat {

    private static final Method M_COMPONENT_OBJECT;   // Component.object(ObjectContents)
    private static final Method M_PLAYER_HEAD;        // ObjectContents.playerHead()
    private static final Method M_PROPERTY;           // PlayerHeadObjectContents.property(String, String)
    private static final Method M_PROFILE_PROPERTY;   // Builder.profileProperty(ProfileProperty)
    private static final Method M_HAT;                // Builder.hat(boolean)
    private static final Method M_BUILD;              // Builder.build()
    private static final boolean SUPPORTED;

    static {
        Method object = null, playerHead = null, property = null, profileProperty = null, hat = null, build = null;
        try {
            Class<?> contents = Class.forName("net.kyori.adventure.text.object.ObjectContents");
            Class<?> head = Class.forName("net.kyori.adventure.text.object.PlayerHeadObjectContents");
            Class<?> builder = Class.forName("net.kyori.adventure.text.object.PlayerHeadObjectContents$Builder");
            Class<?> profile = Class.forName("net.kyori.adventure.text.object.PlayerHeadObjectContents$ProfileProperty");

            object = Component.class.getMethod("object", contents);
            playerHead = contents.getMethod("playerHead");
            property = head.getMethod("property", String.class, String.class);
            profileProperty = builder.getMethod("profileProperty", profile);
            hat = builder.getMethod("hat", boolean.class);
            build = builder.getMethod("build");
        } catch (Throwable ignored) {
            // Older Adventure: no object components, heads are left out.
        }
        M_COMPONENT_OBJECT = object;
        M_PLAYER_HEAD = playerHead;
        M_PROPERTY = property;
        M_PROFILE_PROPERTY = profileProperty;
        M_HAT = hat;
        M_BUILD = build;
        SUPPORTED = build != null;
    }

    private HeadCompat() {
    }

    /** Whether this server can draw heads in text (1.21.9+). */
    public static boolean isSupported() {
        return SUPPORTED;
    }

    /**
     * A head wearing the skin at {@code textures.minecraft.net/texture/<hash>}.
     *
     * @param hash the texture hash, the last part of the skin URL
     * @return the head, or null when the server can't draw heads in text or the hash is blank
     */
    public static @Nullable Component texture(@Nullable String hash) {
        if (!SUPPORTED || hash == null || hash.isBlank()) return null;
        try {
            Object builder = M_PLAYER_HEAD.invoke(null);
            builder = M_PROFILE_PROPERTY.invoke(builder, M_PROPERTY.invoke(null, "textures", textureValue(hash.trim())));
            builder = M_HAT.invoke(builder, true);
            return (Component) M_COMPONENT_OBJECT.invoke(null, M_BUILD.invoke(builder));
        } catch (Throwable t) {
            return null;
        }
    }

    /** The base64 {@code textures} property value for a skin hash, as a profile carries it. */
    public static String textureValue(String hash) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
