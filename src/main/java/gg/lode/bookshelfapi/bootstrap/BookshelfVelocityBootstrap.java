package gg.lode.bookshelfapi.bootstrap;

import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Velocity counterpart of {@link BookshelfBootstrap}. The
 * Bookshelf-Loader jar is a Velocity plugin shim that downloads the
 * impl, instantiates the entry class via URLClassLoader, and forwards
 * proxy lifecycle hooks here.
 *
 * <p>The {@code pluginHost} argument is the loader shim's own @Plugin
 * instance. Pass it into Velocity APIs that key off the registered
 * plugin (event registration, scheduler, command meta).
 * Implementations MUST have a public no-arg constructor.
 */
public interface BookshelfVelocityBootstrap {
    /**
     * Called from the loader shim's {@code ProxyInitializeEvent} handler.
     *
     * @param pluginHost  the loader shim @Plugin instance — use for
     *                    event/scheduler/command registration
     * @param proxy       Velocity proxy server
     * @param dataDir     the shim's @DataDirectory path
     * @param logger      the shim's plugin logger
     */
    void onInit(Object pluginHost, ProxyServer proxy, Path dataDir, Logger logger);

    /** Called from the loader shim's {@code ProxyShutdownEvent} handler. */
    void onShutdown();
}
