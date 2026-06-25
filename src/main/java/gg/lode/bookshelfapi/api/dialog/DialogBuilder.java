package gg.lode.bookshelfapi.api.dialog;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for {@link BookshelfDialog}s. Accumulates body lines, inputs
 * and buttons, then produces a notice, multi-action or confirmation dialog.
 * All text is MiniMessage.
 *
 * <pre>{@code
 * BookshelfDialog dialog = DialogBuilder.create("<gold>Choose")
 *         .body("<gray>Pick an option.")
 *         .input(DialogBuilder.text("name", "<white>Name"))
 *         .button("<green>Confirm", DialogBuilder.custom("confirm", Map.of()))
 *         .button("<red>Cancel", DialogBuilder.custom("cancel", Map.of()))
 *         .multiAction();
 * DialogService.show(player, dialog);
 * }</pre>
 */
public final class DialogBuilder {

    private final String title;
    private @Nullable String externalTitle;
    private boolean canCloseWithEscape = true;
    private boolean pause = false;
    private DialogModel.AfterAction afterAction = DialogModel.AfterAction.CLOSE;
    private int columns = 2;

    private final List<DialogModel.Body> body = new ArrayList<>();
    private final List<DialogModel.Input> inputs = new ArrayList<>();
    private final List<DialogModel.ActionButton> buttons = new ArrayList<>();
    private @Nullable DialogModel.ActionButton exit;

    private DialogBuilder(String title) {
        this.title = title;
    }

    public static DialogBuilder create(String title) {
        return new DialogBuilder(title);
    }

    // ---- header options ---------------------------------------------------

    public DialogBuilder externalTitle(String externalTitle) {
        this.externalTitle = externalTitle;
        return this;
    }

    public DialogBuilder canCloseWithEscape(boolean value) {
        this.canCloseWithEscape = value;
        return this;
    }

    public DialogBuilder pause(boolean value) {
        this.pause = value;
        return this;
    }

    public DialogBuilder afterAction(DialogModel.AfterAction value) {
        this.afterAction = value;
        return this;
    }

    public DialogBuilder columns(int value) {
        this.columns = value;
        return this;
    }

    // ---- content ----------------------------------------------------------

    public DialogBuilder body(String text) {
        this.body.add(new DialogModel.TextBody(text));
        return this;
    }

    public DialogBuilder body(String text, int width) {
        this.body.add(new DialogModel.TextBody(text, width));
        return this;
    }

    public DialogBuilder input(DialogModel.Input input) {
        this.inputs.add(input);
        return this;
    }

    public DialogBuilder button(DialogModel.ActionButton button) {
        this.buttons.add(button);
        return this;
    }

    public DialogBuilder button(String label, @Nullable DialogModel.ButtonAction action) {
        this.buttons.add(new DialogModel.ActionButton(new DialogModel.ButtonData(label), action));
        return this;
    }

    public DialogBuilder button(String label, @Nullable String tooltip, int width, @Nullable DialogModel.ButtonAction action) {
        this.buttons.add(new DialogModel.ActionButton(new DialogModel.ButtonData(label, tooltip, width), action));
        return this;
    }

    public DialogBuilder exit(String label, @Nullable DialogModel.ButtonAction action) {
        this.exit = new DialogModel.ActionButton(new DialogModel.ButtonData(label), action);
        return this;
    }

    // ---- terminal builders ------------------------------------------------

    /** Single-button dialog using the given button. */
    public BookshelfDialog.Notice notice(String buttonLabel, @Nullable DialogModel.ButtonAction action) {
        return new BookshelfDialog.Notice(data(),
                new DialogModel.ActionButton(new DialogModel.ButtonData(buttonLabel), action));
    }

    /** Multi-action dialog using all added buttons + optional exit. */
    public BookshelfDialog.MultiAction multiAction() {
        return new BookshelfDialog.MultiAction(data(), List.copyOf(buttons), exit, columns);
    }

    /** Yes/no dialog. */
    public BookshelfDialog.Confirmation confirmation(String yesLabel, @Nullable DialogModel.ButtonAction yesAction,
                                                     String noLabel, @Nullable DialogModel.ButtonAction noAction) {
        return new BookshelfDialog.Confirmation(data(),
                new DialogModel.ActionButton(new DialogModel.ButtonData(yesLabel), yesAction),
                new DialogModel.ActionButton(new DialogModel.ButtonData(noLabel), noAction));
    }

    private DialogModel.Data data() {
        return new DialogModel.Data(title, externalTitle, canCloseWithEscape, pause,
                afterAction, List.copyOf(body), List.copyOf(inputs));
    }

    // ---- input factories --------------------------------------------------

    public static DialogModel.Input text(String key, String label) {
        return new DialogModel.Input(key, new DialogModel.TextInput(200, label, !label.isEmpty(), "", 256, null));
    }

    public static DialogModel.Input text(String key, String label, String initial, int maxLength) {
        return new DialogModel.Input(key, new DialogModel.TextInput(200, label, !label.isEmpty(), initial, maxLength, null));
    }

    public static DialogModel.Input checkbox(String key, String label, boolean initial) {
        return new DialogModel.Input(key, new DialogModel.BooleanInput(label, initial, "true", "false"));
    }

    // ---- action factories -------------------------------------------------

    public static DialogModel.ButtonAction custom(String id, Map<String, String> payload) {
        return new DialogModel.CustomAction(id, payload);
    }

    public static DialogModel.ButtonAction runCommand(String command) {
        return new DialogModel.RunCommandAction(command);
    }

    public static DialogModel.ButtonAction copyToClipboard(String value) {
        return new DialogModel.CopyToClipboardAction(value);
    }
}
