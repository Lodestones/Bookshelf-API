package gg.lode.bookshelfapi.api.dialog;

import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Renders {@link BookshelfDialog}s onto the running server and shows them to
 * players using Paper's <b>stable</b> Dialog API
 * ({@code io.papermc.paper.dialog.Dialog} + {@code io.papermc.paper.registry.data.dialog.*})
 * via reflection, plus {@code Audience#showDialog}. Reflecting the Paper API
 * rather than the internal {@code net.minecraft.server.dialog.*} NMS classes
 * keeps this working across server updates — the NMS signatures churn between
 * point releases (they did between 1.21.6 and 1.21.11), but the Paper API does
 * not.
 * <p>
 * Nothing here is compiled against a specific server version, so it loads on any
 * build. If the server lacks the Paper dialog API (pre-1.21.6/1.21.7) or a
 * signature differs, {@link #isSupported()} is {@code false} and
 * {@link #show}/{@link #clear} return {@code false} instead of throwing.
 * <p>
 * Button responses are captured separately by the platform packet listener
 * (Bookshelf-Paper) and routed through {@link DialogResponseRouter}; a
 * {@link DialogModel.CustomAction} button maps to a Paper custom-click action
 * keyed {@code bookshelf:<id>}.
 */
public final class DialogService {

    private DialogService() {
    }

    private static final boolean SUPPORTED;
    private static final String INIT_ERROR; // null when supported; else the failing reflection lookup
    private static volatile String lastShowError; // diagnostic for the most recent failed show()

    // Paper Dialog API classes
    private static final Class<?> C_AFTER;          // DialogBase.DialogAfterAction (enum)
    private static final Class<?> C_INPUT;          // DialogInput
    private static final Class<?> C_DIALOG_LIKE;    // net.kyori.adventure.dialog.DialogLike

    // factory / builder methods
    private static final Method M_CREATE;           // Dialog.create(Consumer)
    private static final Method M_FACTORY_EMPTY;    // RegistryBuilderFactory.empty()
    private static final Method M_ENTRY_BASE;       // DialogRegistryEntry.Builder.base(DialogBase)
    private static final Method M_ENTRY_TYPE;       // DialogRegistryEntry.Builder.type(DialogType)
    private static final Method M_BASE_BUILDER;     // DialogBase.builder(Component)
    private static final Method M_BASE_BODY;        // .body(List)
    private static final Method M_BASE_INPUTS;      // .inputs(List)
    private static final Method M_BASE_CLOSE;       // .canCloseWithEscape(boolean)
    private static final Method M_BASE_AFTER;       // .afterAction(DialogAfterAction)
    private static final Method M_BASE_EXT_TITLE;   // .externalTitle(Component)
    private static final Method M_BASE_PAUSE;       // .pause(boolean)
    private static final Method M_BASE_BUILD;       // .build()
    private static final Method M_BODY_PLAIN;       // DialogBody.plainMessage(Component, int)
    private static final Method M_INPUT_TEXT;       // DialogInput.text(String, Component)
    private static final Method M_INPUT_BOOL;       // DialogInput.bool(String, Component)
    private static final Method M_INPUT_NUMRANGE;   // DialogInput.numberRange(String, Component, float, float)
    private static final Method M_TYPE_NOTICE;      // DialogType.notice(ActionButton)
    private static final Method M_TYPE_CONFIRM;     // DialogType.confirmation(ActionButton, ActionButton)
    private static final Method M_TYPE_MULTI;       // DialogType.multiAction(List, ActionButton, int)
    private static final Method M_BUTTON_CREATE;    // ActionButton.create(Component, Component, int, DialogAction)
    private static final Method M_ACTION_CUSTOM;    // DialogAction.customClick(Key, BinaryTagHolder)
    private static final Method M_ACTION_STATIC;    // DialogAction.staticAction(ClickEvent)
    private static final Method M_SHOW;             // Player.showDialog(DialogLike)
    private static final Method M_CLOSE;            // Player.closeDialog()

    static {
        boolean ok = false;
        String initError = null;
        Class<?> cAfter = null, cInput = null, cDialogLike = null;
        Method create = null, factoryEmpty = null, entryBase = null, entryType = null,
                baseBuilder = null, baseBody = null, baseInputs = null, baseClose = null,
                baseAfter = null, baseExt = null, basePause = null, baseBuild = null,
                bodyPlain = null, inputText = null, inputBool = null, inputNumRange = null,
                typeNotice = null, typeConfirm = null, typeMulti = null, buttonCreate = null,
                actionCustom = null, actionStatic = null, show = null, close = null;

        try {
            String base = "io.papermc.paper.registry.data.dialog.";
            Class<?> cDialog = Class.forName("io.papermc.paper.dialog.Dialog");
            Class<?> cFactory = Class.forName("io.papermc.paper.registry.RegistryBuilderFactory");
            Class<?> cEntryBuilder = Class.forName("io.papermc.paper.registry.data.DialogRegistryEntry$Builder");
            Class<?> cBase = Class.forName(base + "DialogBase");
            Class<?> cBaseBuilder = Class.forName(base + "DialogBase$Builder");
            cAfter = Class.forName(base + "DialogBase$DialogAfterAction");
            Class<?> cBody = Class.forName(base + "body.DialogBody");
            cInput = Class.forName(base + "input.DialogInput");
            Class<?> cType = Class.forName(base + "type.DialogType");
            Class<?> cButton = Class.forName(base + "ActionButton");
            Class<?> cAction = Class.forName(base + "action.DialogAction");
            cDialogLike = Class.forName("net.kyori.adventure.dialog.DialogLike");
            Class<?> cBinaryTag = Class.forName("net.kyori.adventure.nbt.api.BinaryTagHolder");

            create = cDialog.getMethod("create", Consumer.class);
            factoryEmpty = cFactory.getMethod("empty");
            entryBase = cEntryBuilder.getMethod("base", cBase);
            entryType = cEntryBuilder.getMethod("type", cType);

            baseBuilder = cBase.getMethod("builder", Component.class);
            baseBody = cBaseBuilder.getMethod("body", List.class);
            baseInputs = cBaseBuilder.getMethod("inputs", List.class);
            baseClose = cBaseBuilder.getMethod("canCloseWithEscape", boolean.class);
            baseAfter = cBaseBuilder.getMethod("afterAction", cAfter);
            baseExt = cBaseBuilder.getMethod("externalTitle", Component.class);
            basePause = cBaseBuilder.getMethod("pause", boolean.class);
            baseBuild = cBaseBuilder.getMethod("build");

            bodyPlain = cBody.getMethod("plainMessage", Component.class, int.class);

            inputText = cInput.getMethod("text", String.class, Component.class);
            inputBool = cInput.getMethod("bool", String.class, Component.class);
            inputNumRange = cInput.getMethod("numberRange", String.class, Component.class, float.class, float.class);

            typeNotice = cType.getMethod("notice", cButton);
            typeConfirm = cType.getMethod("confirmation", cButton, cButton);
            typeMulti = cType.getMethod("multiAction", List.class, cButton, int.class);

            buttonCreate = cButton.getMethod("create", Component.class, Component.class, int.class, cAction);
            actionCustom = cAction.getMethod("customClick", Key.class, cBinaryTag);
            actionStatic = cAction.getMethod("staticAction", ClickEvent.class);

            show = Player.class.getMethod("showDialog", cDialogLike);
            close = Player.class.getMethod("closeDialog");

            ok = true;
        } catch (Throwable t) {
            // Pre-dialog server or signature drift → unsupported
            StackTraceElement[] st = t.getStackTrace();
            String where = st.length > 0 ? " @ " + st[0] : "";
            initError = t.getClass().getSimpleName() + ": " + t.getMessage() + where;
        }

        INIT_ERROR = initError;
        SUPPORTED = ok;
        C_AFTER = cAfter;
        C_INPUT = cInput;
        C_DIALOG_LIKE = cDialogLike;
        M_CREATE = create;
        M_FACTORY_EMPTY = factoryEmpty;
        M_ENTRY_BASE = entryBase;
        M_ENTRY_TYPE = entryType;
        M_BASE_BUILDER = baseBuilder;
        M_BASE_BODY = baseBody;
        M_BASE_INPUTS = baseInputs;
        M_BASE_CLOSE = baseClose;
        M_BASE_AFTER = baseAfter;
        M_BASE_EXT_TITLE = baseExt;
        M_BASE_PAUSE = basePause;
        M_BASE_BUILD = baseBuild;
        M_BODY_PLAIN = bodyPlain;
        M_INPUT_TEXT = inputText;
        M_INPUT_BOOL = inputBool;
        M_INPUT_NUMRANGE = inputNumRange;
        M_TYPE_NOTICE = typeNotice;
        M_TYPE_CONFIRM = typeConfirm;
        M_TYPE_MULTI = typeMulti;
        M_BUTTON_CREATE = buttonCreate;
        M_ACTION_CUSTOM = actionCustom;
        M_ACTION_STATIC = actionStatic;
        M_SHOW = show;
        M_CLOSE = close;
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Whether the running server supports native dialogs (MC 1.21.6+ with Paper dialog API). */
    public static boolean isSupported() {
        return SUPPORTED;
    }

    /**
     * Why dialog support failed to initialize, or {@code null} when supported.
     * Names the first reflection lookup that drifted on this server version.
     */
    public static @Nullable String initError() {
        return INIT_ERROR;
    }

    /** Diagnostic for the most recent {@link #show} that returned {@code false}, or {@code null}. */
    public static @Nullable String lastError() {
        return lastShowError;
    }

    /**
     * Shows a dialog to a player.
     *
     * @return {@code true} if shown, {@code false} when unsupported or on failure
     */
    public static boolean show(Player player, BookshelfDialog dialog) {
        if (!SUPPORTED || player == null || dialog == null) return false;
        try {
            Object paperDialog = buildDialog(dialog);
            M_SHOW.invoke(player, paperDialog);
            return true;
        } catch (Throwable t) {
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            StackTraceElement[] st = cause.getStackTrace();
            String where = st.length > 0 ? " @ " + st[0] : "";
            lastShowError = cause.getClass().getSimpleName() + ": " + cause.getMessage() + where;
            return false;
        }
    }

    /** Clears any open dialog for the player. */
    public static boolean clear(Player player) {
        if (!SUPPORTED || player == null) return false;
        try {
            M_CLOSE.invoke(player);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Paper Dialog API construction (reflective)
    // ------------------------------------------------------------------

    private static Object buildDialog(BookshelfDialog dialog) throws Exception {
        Object base = buildBase(dialog.data());
        Object type = buildType(dialog);

        // Dialog.create(factory -> factory.empty().base(base).type(type))
        Consumer<Object> consumer = factory -> {
            try {
                Object entry = M_FACTORY_EMPTY.invoke(factory);
                Object entry2 = M_ENTRY_BASE.invoke(entry, base);
                M_ENTRY_TYPE.invoke(entry2 != null ? entry2 : entry, type);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return M_CREATE.invoke(null, consumer);
    }

    private static Object buildBase(DialogModel.Data data) throws Exception {
        Object builder = M_BASE_BUILDER.invoke(null, component(data.title()));

        List<Object> bodies = new ArrayList<>();
        if (data.body() != null) {
            for (DialogModel.Body b : data.body()) {
                if (b instanceof DialogModel.TextBody text) {
                    bodies.add(M_BODY_PLAIN.invoke(null, component(text.text()), text.width()));
                }
            }
        }
        List<Object> inputs = new ArrayList<>();
        if (data.inputs() != null) {
            for (DialogModel.Input in : data.inputs()) {
                inputs.add(buildInput(in));
            }
        }

        builder = chain(builder, M_BASE_BODY, bodies);
        builder = chain(builder, M_BASE_INPUTS, inputs);
        builder = chain(builder, M_BASE_CLOSE, data.canCloseWithEscape());
        builder = chain(builder, M_BASE_AFTER, enumConstant(C_AFTER, data.afterAction().name()));
        builder = chain(builder, M_BASE_PAUSE, data.pause());
        if (data.externalTitle() != null) {
            builder = chain(builder, M_BASE_EXT_TITLE, component(data.externalTitle()));
        }
        return M_BASE_BUILD.invoke(builder);
    }

    private static Object buildInput(DialogModel.Input in) throws Exception {
        DialogModel.InputControl control = in.control();
        Object built;
        if (control instanceof DialogModel.BooleanInput b) {
            built = M_INPUT_BOOL.invoke(null, in.key(), component(b.label()));
        } else if (control instanceof DialogModel.TextInput t) {
            built = M_INPUT_TEXT.invoke(null, in.key(), component(t.label()));
        } else if (control instanceof DialogModel.NumberRangeInput n) {
            built = M_INPUT_NUMRANGE.invoke(null, in.key(), component(n.label()), n.start(), n.end());
        } else {
            // SingleOptionInput and any future control: not yet mapped to the Paper API.
            throw new IllegalArgumentException("Unsupported input control: " + control.getClass().getSimpleName());
        }
        return finishBuilder(built);
    }

    private static Object buildType(BookshelfDialog dialog) throws Exception {
        if (dialog instanceof BookshelfDialog.Notice notice) {
            return M_TYPE_NOTICE.invoke(null, button(notice.button()));
        } else if (dialog instanceof BookshelfDialog.Confirmation confirmation) {
            return M_TYPE_CONFIRM.invoke(null, button(confirmation.yes()), button(confirmation.no()));
        } else if (dialog instanceof BookshelfDialog.MultiAction multi) {
            List<Object> buttons = new ArrayList<>();
            for (DialogModel.ActionButton b : multi.actions()) {
                buttons.add(button(b));
            }
            Object exit = multi.exit() != null ? button(multi.exit()) : null;
            int columns = multi.columns() <= 0 ? 2 : multi.columns();
            return M_TYPE_MULTI.invoke(null, buttons, exit, columns);
        }
        throw new IllegalArgumentException("Unknown dialog type: " + dialog.getClass());
    }

    private static Object button(DialogModel.ActionButton button) throws Exception {
        Component label = component(button.data().label());
        Component tooltip = button.data().tooltip() != null ? component(button.data().tooltip()) : null;
        Object action = button.action() != null ? action(button.action()) : null;
        return M_BUTTON_CREATE.invoke(null, label, tooltip, button.data().width(), action);
    }

    private static Object action(DialogModel.ButtonAction action) throws Exception {
        if (action instanceof DialogModel.CopyToClipboardAction copy) {
            return M_ACTION_STATIC.invoke(null, ClickEvent.copyToClipboard(copy.value()));
        } else if (action instanceof DialogModel.RunCommandAction run) {
            return M_ACTION_STATIC.invoke(null, ClickEvent.runCommand(run.command()));
        } else if (action instanceof DialogModel.CustomAction custom) {
            // Input values are delivered with the custom-click packet automatically;
            // button additions (payload) are omitted here and the listener still
            // routes by the bookshelf:<id> key.
            return M_ACTION_CUSTOM.invoke(null, Key.key("bookshelf", custom.id()), null);
        }
        throw new IllegalArgumentException("Unknown button action: " + action.getClass());
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private static Component component(String miniMessage) {
        return MiniMessageHelper.deserialize(miniMessage);
    }

    /** Invoke a builder method and return the result if it's the builder, else the original (mutable builders return {@code this}). */
    private static Object chain(Object builder, Method method, Object arg) throws Exception {
        Object result = method.invoke(builder, arg);
        return result != null ? result : builder;
    }

    /** Some input factories return a finished input, others a Builder — finalize either way. */
    private static Object finishBuilder(Object maybeBuilder) throws Exception {
        if (C_INPUT.isInstance(maybeBuilder)) return maybeBuilder;
        return maybeBuilder.getClass().getMethod("build").invoke(maybeBuilder);
    }

    private static Object enumConstant(Class<?> enumClass, String name) {
        Object[] constants = enumClass.getEnumConstants();
        for (Object c : constants) {
            if (((Enum<?>) c).name().equals(name)) return c;
        }
        return constants[0];
    }
}
