package gg.lode.bookshelfapi.api.compat;

import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reflection-based compatibility shim for Paper's Dialog API
 * ({@code io.papermc.paper.dialog}, added in paper-api 1.21.7 / adventure 4.22+).
 * <p>
 * Bookshelf-API compiles against paper-api 1.21.4, which has none of these
 * classes, so everything here is resolved reflectively at class-load time. On a
 * server older than 1.21.7 {@link #isSupported()} returns {@code false} and all
 * {@code show*} methods become no-ops that return {@code false}. On 1.21.7+ the
 * dialog renders natively.
 * <p>
 * Public surface deals only in plain Java types ({@link String}, {@link List},
 * {@link Player}) plus the small {@link Input}, {@link Button}, {@link Response}
 * and {@link Handler} helpers, so callers never touch a 1.21.7-only type.
 */
public final class DialogCompat {

    private DialogCompat() {
    }

    // ---- resolved reflection targets (null when unsupported) -------------

    private static final boolean SUPPORTED;

    private static final Method M_DIALOG_CREATE;
    private static final Method M_VIEW_GET_TEXT;
    private static final Method M_VIEW_GET_BOOLEAN;
    private static final Method M_VIEW_GET_FLOAT;

    private static final Method M_BASE_BUILDER;     // DialogBase.builder(Component)
    private static final Method M_BASE_BODY;        // Builder.body(List)
    private static final Method M_BASE_INPUTS;      // Builder.inputs(List)
    private static final Method M_BASE_BUILD;       // Builder.build()

    private static final Method M_BUTTON_BUILDER;   // ActionButton.builder(Component)
    private static final Method M_BUTTON_TOOLTIP;   // Builder.tooltip(Component)
    private static final Method M_BUTTON_ACTION;    // Builder.action(DialogAction)
    private static final Method M_BUTTON_BUILD;     // Builder.build()

    private static final Method M_ACTION_CUSTOMCLICK; // DialogAction.customClick(callback, options)
    private static final Class<?> C_ACTION_CALLBACK;  // DialogActionCallback (proxied)

    private static final Method M_BODY_PLAIN;       // DialogBody.plainMessage(Component)
    private static final Method M_INPUT_TEXT;       // DialogInput.text(String, Component)
    private static final Method M_INPUT_BOOL;       // DialogInput.bool(String, Component)
    private static final Method M_TEXT_MAXLENGTH;   // TextDialogInput.Builder.maxLength(int)
    private static final Method M_TEXT_INITIAL;     // TextDialogInput.Builder.initial(String)
    private static final Method M_TEXT_BUILD;       // TextDialogInput.Builder.build()
    private static final Method M_BOOL_BUILD;       // BooleanDialogInput.Builder.build()
    private static final Method M_MULTI_COLUMNS;    // MultiActionType.Builder.columns(int)
    private static final Method M_MULTI_BUILD;      // MultiActionType.Builder.build()

    private static final Method M_TYPE_NOTICE0;     // DialogType.notice()
    private static final Method M_TYPE_NOTICE1;     // DialogType.notice(ActionButton)
    private static final Method M_TYPE_CONFIRM;     // DialogType.confirmation(ActionButton, ActionButton)
    private static final Method M_TYPE_MULTI;       // DialogType.multiAction(List)

    private static final Method M_FACTORY_EMPTY;    // RegistryBuilderFactory.empty()
    private static final Method M_ENTRY_BASE;       // DialogRegistryEntry.Builder.base(DialogBase)
    private static final Method M_ENTRY_TYPE;       // DialogRegistryEntry.Builder.type(DialogType)

    private static final Method M_OPTIONS_BUILDER;  // ClickCallback.Options.builder()
    private static final Object CLICK_OPTIONS;      // prebuilt unlimited-use options

    private static final Method M_SHOW_DIALOG;      // Audience.showDialog(DialogLike)

    // Resolved on their own, so a server missing one of these still gets every other dialog.
    private static final Method M_BOOL_INITIAL;     // BooleanDialogInput.Builder.initial(boolean)
    private static final Method M_INPUT_RANGE;      // DialogInput.numberRange(String, Component, float, float)
    private static final Method M_RANGE_STEP;       // NumberRangeDialogInput.Builder.step(Float)
    private static final Method M_RANGE_INITIAL;    // NumberRangeDialogInput.Builder.initial(Float)
    private static final Method M_RANGE_FORMAT;     // NumberRangeDialogInput.Builder.labelFormat(String)
    private static final Method M_RANGE_BUILD;      // NumberRangeDialogInput.Builder.build()
    private static final Method M_BASE_ESCAPE;      // DialogBase.Builder.canCloseWithEscape(boolean)

    static {
        boolean ok = false;
        Method dialogCreate = null, viewText = null, viewBool = null, viewFloat = null;
        Method baseBuilder = null, baseBody = null, baseInputs = null, baseBuild = null;
        Method btnBuilder = null, btnTooltip = null, btnAction = null, btnBuild = null;
        Method actionCustomClick = null, bodyPlain = null, inputText = null, inputBool = null;
        Method textMax = null, textInit = null, textBuild = null, boolBuild = null;
        Method multiCols = null, multiBuild = null;
        Method typeNotice0 = null, typeNotice1 = null, typeConfirm = null, typeMulti = null;
        Method factoryEmpty = null, entryBase = null, entryType = null;
        Method optionsBuilder = null, showDialog = null;
        Class<?> actionCallback = null;
        Object clickOptions = null;

        try {
            Class<?> cDialog = Class.forName("io.papermc.paper.dialog.Dialog");
            Class<?> cView = Class.forName("io.papermc.paper.dialog.DialogResponseView");
            Class<?> cBase = Class.forName("io.papermc.paper.registry.data.dialog.DialogBase");
            Class<?> cBaseBuilder = Class.forName("io.papermc.paper.registry.data.dialog.DialogBase$Builder");
            Class<?> cButton = Class.forName("io.papermc.paper.registry.data.dialog.ActionButton");
            Class<?> cButtonBuilder = Class.forName("io.papermc.paper.registry.data.dialog.ActionButton$Builder");
            Class<?> cAction = Class.forName("io.papermc.paper.registry.data.dialog.action.DialogAction");
            actionCallback = Class.forName("io.papermc.paper.registry.data.dialog.action.DialogActionCallback");
            Class<?> cBody = Class.forName("io.papermc.paper.registry.data.dialog.body.DialogBody");
            Class<?> cInput = Class.forName("io.papermc.paper.registry.data.dialog.input.DialogInput");
            Class<?> cTextBuilder = Class.forName("io.papermc.paper.registry.data.dialog.input.TextDialogInput$Builder");
            Class<?> cBoolBuilder = Class.forName("io.papermc.paper.registry.data.dialog.input.BooleanDialogInput$Builder");
            Class<?> cType = Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
            Class<?> cMultiBuilder = Class.forName("io.papermc.paper.registry.data.dialog.type.MultiActionType$Builder");
            Class<?> cFactory = Class.forName("io.papermc.paper.registry.RegistryBuilderFactory");
            Class<?> cEntryBuilder = Class.forName("io.papermc.paper.registry.data.dialog.DialogRegistryEntry$Builder");
            Class<?> cClickCallback = Class.forName("net.kyori.adventure.text.event.ClickCallback");
            Class<?> cClickOptions = Class.forName("net.kyori.adventure.text.event.ClickCallback$Options");

            dialogCreate = cDialog.getMethod("create", Consumer.class);
            viewText = cView.getMethod("getText", String.class);
            viewBool = cView.getMethod("getBoolean", String.class);
            viewFloat = cView.getMethod("getFloat", String.class);

            baseBuilder = cBase.getMethod("builder", Component.class);
            baseBody = cBaseBuilder.getMethod("body", List.class);
            baseInputs = cBaseBuilder.getMethod("inputs", List.class);
            baseBuild = cBaseBuilder.getMethod("build");

            btnBuilder = cButton.getMethod("builder", Component.class);
            btnTooltip = cButtonBuilder.getMethod("tooltip", Component.class);
            btnAction = cButtonBuilder.getMethod("action", cAction);
            btnBuild = cButtonBuilder.getMethod("build");

            actionCustomClick = cAction.getMethod("customClick", actionCallback, cClickOptions);
            bodyPlain = cBody.getMethod("plainMessage", Component.class);
            inputText = cInput.getMethod("text", String.class, Component.class);
            inputBool = cInput.getMethod("bool", String.class, Component.class);
            textMax = cTextBuilder.getMethod("maxLength", int.class);
            textInit = cTextBuilder.getMethod("initial", String.class);
            textBuild = cTextBuilder.getMethod("build");
            boolBuild = cBoolBuilder.getMethod("build");
            multiCols = cMultiBuilder.getMethod("columns", int.class);
            multiBuild = cMultiBuilder.getMethod("build");

            typeNotice0 = cType.getMethod("notice");
            typeNotice1 = cType.getMethod("notice", cButton);
            typeConfirm = cType.getMethod("confirmation", cButton, cButton);
            typeMulti = cType.getMethod("multiAction", List.class);

            factoryEmpty = cFactory.getMethod("empty");
            entryBase = cEntryBuilder.getMethod("base", cBase);
            entryType = cEntryBuilder.getMethod("type", cType);

            Class<?> cOptionsBuilder = Class.forName("net.kyori.adventure.text.event.ClickCallback$Options$Builder");
            optionsBuilder = cClickOptions.getMethod("builder");
            Field unlimitedField = cClickCallback.getField("UNLIMITED_USES");
            Field lifetimeField = cClickCallback.getField("DEFAULT_LIFETIME");
            int unlimited = unlimitedField.getInt(null);
            Object defaultLifetime = lifetimeField.get(null);
            Object ob = optionsBuilder.invoke(null);
            ob = cOptionsBuilder.getMethod("uses", int.class).invoke(ob, unlimited);
            ob = cOptionsBuilder.getMethod("lifetime", java.time.temporal.TemporalAmount.class).invoke(ob, defaultLifetime);
            clickOptions = cOptionsBuilder.getMethod("build").invoke(ob);

            // Audience.showDialog(DialogLike) — resolve by name to avoid the absent param type
            for (Method m : Class.forName("net.kyori.adventure.audience.Audience").getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                    showDialog = m;
                    break;
                }
            }

            ok = showDialog != null && clickOptions != null;
        } catch (Throwable ignored) {
            // Older server: Dialog API absent → unsupported, all calls no-op
        }

        SUPPORTED = ok;
        M_DIALOG_CREATE = dialogCreate;
        M_VIEW_GET_TEXT = viewText;
        M_VIEW_GET_BOOLEAN = viewBool;
        M_VIEW_GET_FLOAT = viewFloat;
        M_BASE_BUILDER = baseBuilder;
        M_BASE_BODY = baseBody;
        M_BASE_INPUTS = baseInputs;
        M_BASE_BUILD = baseBuild;
        M_BUTTON_BUILDER = btnBuilder;
        M_BUTTON_TOOLTIP = btnTooltip;
        M_BUTTON_ACTION = btnAction;
        M_BUTTON_BUILD = btnBuild;
        M_ACTION_CUSTOMCLICK = actionCustomClick;
        C_ACTION_CALLBACK = actionCallback;
        M_BODY_PLAIN = bodyPlain;
        M_INPUT_TEXT = inputText;
        M_INPUT_BOOL = inputBool;
        M_TEXT_MAXLENGTH = textMax;
        M_TEXT_INITIAL = textInit;
        M_TEXT_BUILD = textBuild;
        M_BOOL_BUILD = boolBuild;
        M_MULTI_COLUMNS = multiCols;
        M_MULTI_BUILD = multiBuild;
        M_TYPE_NOTICE0 = typeNotice0;
        M_TYPE_NOTICE1 = typeNotice1;
        M_TYPE_CONFIRM = typeConfirm;
        M_TYPE_MULTI = typeMulti;
        M_FACTORY_EMPTY = factoryEmpty;
        M_ENTRY_BASE = entryBase;
        M_ENTRY_TYPE = entryType;
        M_OPTIONS_BUILDER = optionsBuilder;
        CLICK_OPTIONS = clickOptions;
        M_SHOW_DIALOG = showDialog;

        Method boolInitial = null, inputRange = null, rangeStep = null, rangeInitial = null;
        Method rangeFormat = null, rangeBuild = null, baseEscape = null;
        try {
            Class<?> cBoolBuilder = Class.forName("io.papermc.paper.registry.data.dialog.input.BooleanDialogInput$Builder");
            Class<?> cRangeBuilder = Class.forName("io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput$Builder");
            Class<?> cInput = Class.forName("io.papermc.paper.registry.data.dialog.input.DialogInput");
            Class<?> cBaseBuilder = Class.forName("io.papermc.paper.registry.data.dialog.DialogBase$Builder");
            boolInitial = cBoolBuilder.getMethod("initial", boolean.class);
            inputRange = cInput.getMethod("numberRange", String.class, Component.class, float.class, float.class);
            rangeStep = cRangeBuilder.getMethod("step", Float.class);
            rangeInitial = cRangeBuilder.getMethod("initial", Float.class);
            rangeFormat = cRangeBuilder.getMethod("labelFormat", String.class);
            rangeBuild = cRangeBuilder.getMethod("build");
            baseEscape = cBaseBuilder.getMethod("canCloseWithEscape", boolean.class);
        } catch (Throwable ignored) {
            // Leaves sliders, checkbox defaults and the escape option unavailable, nothing else.
        }
        M_BOOL_INITIAL = boolInitial;
        M_INPUT_RANGE = inputRange;
        M_RANGE_STEP = rangeStep;
        M_RANGE_INITIAL = rangeInitial;
        M_RANGE_FORMAT = rangeFormat;
        M_RANGE_BUILD = rangeBuild;
        M_BASE_ESCAPE = baseEscape;
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Whether the running server exposes the native Dialog API (1.21.7+). */
    public static boolean isSupported() {
        return SUPPORTED;
    }

    /**
     * Single-button informational dialog. {@code button} may be {@code null} for
     * a default close button.
     *
     * @return {@code true} if shown, {@code false} when unsupported
     */
    public static boolean notice(Player player, String title, List<String> body, @Nullable Button button) {
        if (!SUPPORTED || player == null) return false;
        try {
            Object base = buildBase(title, body, null);
            Object type = button == null
                    ? M_TYPE_NOTICE0.invoke(null)
                    : M_TYPE_NOTICE1.invoke(null, buildActionButton(button));
            return show(player, buildDialog(base, type));
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Single-button dialog with input fields, for forms like a settings screen. The button's
     * handler receives what was entered. When {@code canCloseWithEscape} is true, Escape closes
     * the dialog without running the handler, so nothing entered is applied.
     *
     * @return {@code true} if shown, {@code false} when unsupported
     */
    public static boolean notice(Player player, String title, @Nullable List<String> body,
                                 @Nullable List<Input> inputs, Button button, boolean canCloseWithEscape) {
        if (!SUPPORTED || player == null || button == null) return false;
        try {
            Object base = buildBase(title, body, inputs, canCloseWithEscape);
            Object type = M_TYPE_NOTICE1.invoke(null, buildActionButton(button));
            return show(player, buildDialog(base, type));
        } catch (Throwable t) {
            return false;
        }
    }

    /** Two-button yes/no dialog. */
    public static boolean confirmation(Player player, String title, List<String> body, Button yes, Button no) {
        if (!SUPPORTED || player == null) return false;
        try {
            Object base = buildBase(title, body, null);
            Object type = M_TYPE_CONFIRM.invoke(null, buildActionButton(yes), buildActionButton(no));
            return show(player, buildDialog(base, type));
        } catch (Throwable t) {
            return false;
        }
    }

    /** Multi-button dialog, optionally with input fields. */
    public static boolean multiAction(Player player, String title, @Nullable List<String> body,
                                      @Nullable List<Input> inputs, List<Button> buttons) {
        if (!SUPPORTED || player == null || buttons == null || buttons.isEmpty()) return false;
        try {
            Object base = buildBase(title, body, inputs);
            List<Object> actionButtons = new ArrayList<>(buttons.size());
            for (Button b : buttons) actionButtons.add(buildActionButton(b));
            Object multiBuilder = M_TYPE_MULTI.invoke(null, actionButtons);
            multiBuilder = M_MULTI_COLUMNS.invoke(multiBuilder, buttons.size() == 1 ? 1 : 2);
            Object type = M_MULTI_BUILD.invoke(multiBuilder);
            return show(player, buildDialog(base, type));
        } catch (Throwable t) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Internal builders
    // ------------------------------------------------------------------

    private static Object buildBase(String title, @Nullable List<String> body, @Nullable List<Input> inputs) throws Exception {
        return buildBase(title, body, inputs, null);
    }

    private static Object buildBase(String title, @Nullable List<String> body, @Nullable List<Input> inputs,
                                    @Nullable Boolean canCloseWithEscape) throws Exception {
        Object builder = M_BASE_BUILDER.invoke(null, mm(title));
        if (canCloseWithEscape != null && M_BASE_ESCAPE != null) {
            builder = M_BASE_ESCAPE.invoke(builder, canCloseWithEscape);
        }
        if (body != null && !body.isEmpty()) {
            List<Object> bodies = new ArrayList<>(body.size());
            for (String line : body) bodies.add(M_BODY_PLAIN.invoke(null, mm(line)));
            builder = M_BASE_BODY.invoke(builder, bodies);
        }
        if (inputs != null && !inputs.isEmpty()) {
            List<Object> in = new ArrayList<>(inputs.size());
            for (Input i : inputs) in.add(buildInput(i));
            builder = M_BASE_INPUTS.invoke(builder, in);
        }
        return M_BASE_BUILD.invoke(builder);
    }

    private static Object buildInput(Input input) throws Exception {
        if (input.type == Input.Type.BOOL) {
            Object b = M_INPUT_BOOL.invoke(null, input.key, mm(input.label));
            if (input.boolInitial != null && M_BOOL_INITIAL != null) {
                b = M_BOOL_INITIAL.invoke(b, input.boolInitial.booleanValue());
            }
            return M_BOOL_BUILD.invoke(b);
        }
        if (input.type == Input.Type.RANGE) {
            if (M_INPUT_RANGE == null) throw new IllegalStateException("Sliders are not supported on this server");
            Object b = M_INPUT_RANGE.invoke(null, input.key, mm(input.label), input.start, input.end);
            if (input.step != null) b = M_RANGE_STEP.invoke(b, input.step);
            if (input.rangeInitial != null) b = M_RANGE_INITIAL.invoke(b, input.rangeInitial);
            if (input.labelFormat != null) b = M_RANGE_FORMAT.invoke(b, input.labelFormat);
            return M_RANGE_BUILD.invoke(b);
        }
        Object b = M_INPUT_TEXT.invoke(null, input.key, mm(input.label));
        if (input.maxLength > 0) {
            b = M_TEXT_MAXLENGTH.invoke(b, input.maxLength);
        }
        if (input.initial != null) {
            b = M_TEXT_INITIAL.invoke(b, input.initial);
        }
        return M_TEXT_BUILD.invoke(b);
    }

    private static Object buildActionButton(Button button) throws Exception {
        Object proxy = Proxy.newProxyInstance(
                C_ACTION_CALLBACK.getClassLoader(),
                new Class[]{C_ACTION_CALLBACK},
                new CallbackHandler(button.handler));
        Object action = M_ACTION_CUSTOMCLICK.invoke(null, proxy, CLICK_OPTIONS);
        Object builder = M_BUTTON_BUILDER.invoke(null, mm(button.label));
        if (button.tooltip != null) {
            builder = M_BUTTON_TOOLTIP.invoke(builder, mm(button.tooltip));
        }
        builder = M_BUTTON_ACTION.invoke(builder, action);
        return M_BUTTON_BUILD.invoke(builder);
    }

    private static Object buildDialog(Object base, Object type) throws Exception {
        Consumer<Object> consumer = factory -> {
            try {
                Object entry = M_FACTORY_EMPTY.invoke(factory);
                entry = M_ENTRY_BASE.invoke(entry, base);
                M_ENTRY_TYPE.invoke(entry, type);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
        return M_DIALOG_CREATE.invoke(null, consumer);
    }

    private static boolean show(Player player, Object dialog) throws Exception {
        M_SHOW_DIALOG.invoke(player, dialog);
        return true;
    }

    private static Component mm(String miniMessage) {
        return MiniMessageHelper.deserialize(miniMessage);
    }

    // ------------------------------------------------------------------
    // Proxy handler bridging DialogActionCallback -> Handler
    // ------------------------------------------------------------------

    private record CallbackHandler(Handler handler) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "accept" -> {
                    if (args != null && args.length == 2 && args[1] instanceof Player player) {
                        handler.handle(new Response(args[0]), player);
                    }
                    return null;
                }
                case "toString" -> {
                    return "DialogCompat$CallbackHandler";
                }
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "equals" -> {
                    return proxy == (args == null ? null : args[0]);
                }
                default -> {
                    return null;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Public helper types
    // ------------------------------------------------------------------

    /** Callback invoked when a dialog button is clicked. */
    @FunctionalInterface
    public interface Handler {
        void handle(Response response, Player player);
    }

    /** Read-only view over the values a player submitted in a dialog. */
    public static final class Response {
        private final Object view;

        private Response(Object view) {
            this.view = view;
        }

        public @Nullable String getText(String key) {
            try {
                Object v = M_VIEW_GET_TEXT.invoke(view, key);
                return v == null ? null : v.toString();
            } catch (Throwable t) {
                return null;
            }
        }

        public @Nullable Boolean getBoolean(String key) {
            try {
                return (Boolean) M_VIEW_GET_BOOLEAN.invoke(view, key);
            } catch (Throwable t) {
                return null;
            }
        }

        public @Nullable Float getFloat(String key) {
            try {
                return (Float) M_VIEW_GET_FLOAT.invoke(view, key);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    /** An input field declaration. */
    public static final class Input {
        enum Type { TEXT, BOOL, RANGE }

        final Type type;
        final String key;
        final String label;
        final String initial;
        final int maxLength;
        Boolean boolInitial;
        float start;
        float end;
        Float step;
        Float rangeInitial;
        String labelFormat;

        private Input(Type type, String key, String label, String initial, int maxLength) {
            this.type = type;
            this.key = key;
            this.label = label;
            this.initial = initial;
            this.maxLength = maxLength;
        }

        public static Input text(String key, String label) {
            return new Input(Type.TEXT, key, label, null, 0);
        }

        public static Input text(String key, String label, @Nullable String initial, int maxLength) {
            return new Input(Type.TEXT, key, label, initial, maxLength);
        }

        public static Input bool(String key, String label) {
            return new Input(Type.BOOL, key, label, null, 0);
        }

        /** A checkbox that starts ticked or not. */
        public static Input bool(String key, String label, boolean initial) {
            Input input = new Input(Type.BOOL, key, label, null, 0);
            input.boolInitial = initial;
            return input;
        }

        /**
         * A slider from {@code start} to {@code end}. Read the chosen value back with
         * {@link Response#getFloat(String)}.
         *
         * @param step    the gap between values, or {@code null} for a smooth slider
         * @param initial where it starts, or {@code null} for the middle
         */
        public static Input slider(String key, String label, float start, float end,
                                   @Nullable Float step, @Nullable Float initial) {
            return slider(key, label, start, end, step, initial, null);
        }

        /**
         * A slider with a custom label, a translation-style format such as
         * {@code "%s: %s blocks"} where the first {@code %s} is the label and the second the value.
         */
        public static Input slider(String key, String label, float start, float end,
                                   @Nullable Float step, @Nullable Float initial, @Nullable String labelFormat) {
            Input input = new Input(Type.RANGE, key, label, null, 0);
            input.start = start;
            input.end = end;
            input.step = step;
            input.rangeInitial = initial;
            input.labelFormat = labelFormat;
            return input;
        }
    }

    /** A clickable dialog button with an inline handler. */
    public static final class Button {
        final String label;
        final String tooltip;
        final Handler handler;

        private Button(String label, @Nullable String tooltip, Handler handler) {
            this.label = label;
            this.tooltip = tooltip;
            this.handler = handler;
        }

        public static Button of(String label, Handler handler) {
            return new Button(label, null, handler);
        }

        public static Button of(String label, @Nullable String tooltip, Handler handler) {
            return new Button(label, tooltip, handler);
        }
    }
}
