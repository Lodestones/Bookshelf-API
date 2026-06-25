package gg.lode.bookshelfapi.api.dialog;

import java.util.List;

/**
 * A renderable dialog. One of the concrete dialog types supported by the
 * vanilla client (MC 1.21.6+). Build instances directly or via
 * {@link DialogBuilder}, then show with {@link DialogService#show}.
 */
public sealed interface BookshelfDialog
        permits BookshelfDialog.Notice, BookshelfDialog.MultiAction, BookshelfDialog.Confirmation {

    /** Common header/body/input data. */
    DialogModel.Data data();

    /** Single-button informational dialog. */
    record Notice(DialogModel.Data data, DialogModel.ActionButton button) implements BookshelfDialog {
    }

    /** Multiple action buttons laid out in a grid, plus an optional exit button. */
    record MultiAction(DialogModel.Data data,
                       List<DialogModel.ActionButton> actions,
                       DialogModel.ActionButton exit,
                       int columns) implements BookshelfDialog {
    }

    /** Yes/no dialog. */
    record Confirmation(DialogModel.Data data,
                        DialogModel.ActionButton yes,
                        DialogModel.ActionButton no) implements BookshelfDialog {
    }
}
