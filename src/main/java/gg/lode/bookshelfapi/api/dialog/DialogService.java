package gg.lode.bookshelfapi.api.dialog;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.chat.clickevent.ClickEvent;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.ConfirmationDialog;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog;
import com.github.retrooper.packetevents.protocol.dialog.action.Action;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.action.StaticAction;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.dialog.input.BooleanInputControl;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import com.github.retrooper.packetevents.protocol.dialog.input.InputControl;
import com.github.retrooper.packetevents.protocol.dialog.input.NumberRangeInputControl;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Renders {@link BookshelfDialog}s by building a PacketEvents
 * {@code Dialog} graph and sending {@code WrapperPlayServerShowDialog}.
 * Cross-version, no Paper Dialog API or NMS reflection needed — works on
 * any server (MC 1.21.6+) where the standalone PacketEvents plugin is
 * loaded.
 * <p>
 * The PacketEvents dialog model mirrors FancyDialogs'
 * <a href="https://github.com/FancyInnovations/FancyPlugins">FancySitula
 * approach</a> but ships in PacketEvents itself, so no per-version NMS
 * impl modules are required.
 */
public final class DialogService {

    private DialogService() {
    }

    private static volatile String lastShowError;

    public static boolean isSupported() {
        try {
            return PacketEvents.getAPI() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /** First reflection lookup that failed at class-load. Always null for this path. */
    public static @Nullable String initError() {
        return null;
    }

    /** Diagnostic for the most recent {@link #show} that returned {@code false}, or {@code null}. */
    public static @Nullable String lastError() {
        return lastShowError;
    }

    public static boolean show(Player player, BookshelfDialog dialog) {
        if (player == null || dialog == null) return false;
        try {
            Dialog packet = toPacketDialog(dialog);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerShowDialog(packet));
            return true;
        } catch (Throwable t) {
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            StackTraceElement[] st = cause.getStackTrace();
            String where = st.length > 0 ? " @ " + st[0] : "";
            lastShowError = cause.getClass().getSimpleName() + ": " + cause.getMessage() + where;
            return false;
        }
    }

    public static boolean clear(Player player) {
        if (player == null) return false;
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerClearDialog());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Model conversion (BookshelfDialog -> PacketEvents Dialog)
    // ------------------------------------------------------------------

    private static Dialog toPacketDialog(BookshelfDialog dialog) {
        CommonDialogData common = toCommon(dialog.data());
        if (dialog instanceof BookshelfDialog.Notice notice) {
            return new NoticeDialog(common, toButton(notice.button()));
        } else if (dialog instanceof BookshelfDialog.Confirmation confirmation) {
            return new ConfirmationDialog(common, toButton(confirmation.yes()), toButton(confirmation.no()));
        } else if (dialog instanceof BookshelfDialog.MultiAction multi) {
            List<ActionButton> buttons = new ArrayList<>(multi.actions().size());
            for (DialogModel.ActionButton b : multi.actions()) buttons.add(toButton(b));
            ActionButton exit = multi.exit() != null ? toButton(multi.exit()) : null;
            int columns = multi.columns() <= 0 ? 2 : multi.columns();
            return new MultiActionDialog(common, buttons, exit, columns);
        }
        throw new IllegalArgumentException("Unknown dialog type: " + dialog.getClass());
    }

    private static CommonDialogData toCommon(DialogModel.Data data) {
        Component title = component(data.title());
        Component externalTitle = data.externalTitle() != null ? component(data.externalTitle()) : title;

        List<DialogBody> bodies = new ArrayList<>();
        if (data.body() != null) {
            for (DialogModel.Body b : data.body()) {
                if (b instanceof DialogModel.TextBody text) {
                    bodies.add(new PlainMessageDialogBody(new PlainMessage(component(text.text()), text.width())));
                }
            }
        }

        List<Input> inputs = new ArrayList<>();
        if (data.inputs() != null) {
            for (DialogModel.Input in : data.inputs()) inputs.add(toInput(in));
        }

        DialogAction after = switch (data.afterAction()) {
            case CLOSE -> DialogAction.CLOSE;
            case NONE -> DialogAction.NONE;
            case WAIT_FOR_RESPONSE -> DialogAction.WAIT_FOR_RESPONSE;
        };

        return new CommonDialogData(title, externalTitle, data.canCloseWithEscape(), data.pause(), after, bodies, inputs);
    }

    private static Input toInput(DialogModel.Input in) {
        InputControl control;
        if (in.control() instanceof DialogModel.BooleanInput b) {
            control = new BooleanInputControl(component(b.label()), b.initial(), b.onTrue(), b.onFalse());
        } else if (in.control() instanceof DialogModel.NumberRangeInput n) {
            Float initial = n.initial();
            Float step = n.step();
            NumberRangeInputControl.RangeInfo range = new NumberRangeInputControl.RangeInfo(n.start(), n.end(), initial, step);
            control = new NumberRangeInputControl(n.width(), component(n.label()), n.labelFormat(), range);
        } else if (in.control() instanceof DialogModel.TextInput t) {
            // TextInput / SingleOptionInput omitted on this pass — extend when needed.
            control = new BooleanInputControl(component(t.label()), false, "true", "false");
        } else {
            throw new IllegalArgumentException("Unsupported input control: " + in.control().getClass());
        }
        return new Input(in.key(), control);
    }

    private static ActionButton toButton(DialogModel.ActionButton button) {
        Component label = component(button.data().label());
        Component tooltip = button.data().tooltip() != null ? component(button.data().tooltip()) : null;
        CommonButtonData common = new CommonButtonData(label, tooltip, button.data().width());
        Action action = button.action() != null ? toAction(button.action()) : null;
        return new ActionButton(common, action);
    }

    private static Action toAction(DialogModel.ButtonAction action) {
        if (action instanceof DialogModel.CustomAction custom) {
            NBTCompound additions = new NBTCompound();
            if (custom.payload() != null) {
                for (var e : custom.payload().entrySet()) {
                    additions.setTag(e.getKey(), new NBTString(e.getValue()));
                }
            }
            return new DynamicCustomAction(new ResourceLocation("bookshelf", custom.id()), additions);
        } else if (action instanceof DialogModel.CopyToClipboardAction copy) {
            ClickEvent click = ClickEvent.fromAdventure(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(copy.value()));
            return new StaticAction(click);
        } else if (action instanceof DialogModel.RunCommandAction run) {
            ClickEvent click = ClickEvent.fromAdventure(net.kyori.adventure.text.event.ClickEvent.runCommand(run.command()));
            return new StaticAction(click);
        }
        throw new IllegalArgumentException("Unknown action type: " + action.getClass());
    }

    private static Component component(String miniMessage) {
        return MiniMessageHelper.deserialize(miniMessage);
    }
}
