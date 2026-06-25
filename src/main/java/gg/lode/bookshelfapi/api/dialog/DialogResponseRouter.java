package gg.lode.bookshelfapi.api.dialog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central registry that routes a {@link DialogResponse} to a handler by the
 * clicked button's custom action id.
 * <p>
 * Register a handler for each {@link DialogModel.CustomAction} id you use, then
 * the platform packet listener (Bookshelf-Paper) calls {@link #dispatch} when a
 * matching custom-click arrives. Handlers run on the main server thread.
 * <p>
 * Handlers are persistent until {@link #unregister}ed — filter on
 * {@link DialogResponse#player()} if a handler is shared across players.
 */
public final class DialogResponseRouter {

    private static final Map<String, Consumer<DialogResponse>> HANDLERS = new ConcurrentHashMap<>();

    private DialogResponseRouter() {
    }

    /** Register (or replace) the handler for a custom action id. */
    public static void register(String actionId, Consumer<DialogResponse> handler) {
        HANDLERS.put(actionId, handler);
    }

    /** Remove the handler for a custom action id. */
    public static void unregister(String actionId) {
        HANDLERS.remove(actionId);
    }

    public static boolean isRegistered(String actionId) {
        return HANDLERS.containsKey(actionId);
    }

    /** Remove all handlers. */
    public static void clear() {
        HANDLERS.clear();
    }

    /**
     * Dispatch a response to its registered handler.
     *
     * @return {@code true} if a handler was found and invoked
     */
    public static boolean dispatch(DialogResponse response) {
        Consumer<DialogResponse> handler = HANDLERS.get(response.actionId());
        if (handler == null) return false;
        handler.accept(response);
        return true;
    }
}
