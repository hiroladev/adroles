/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.progressbar.ProgressBar;
import de.hirola.adroles.Global;

public final class ProgressModalDialog {

    private final Dialog dialog = new Dialog();

    public ProgressModalDialog(String titleKey, String messageKey, String subMessageKey) {
        buildProgressDialog(UI.getCurrent().getTranslation(titleKey),
                UI.getCurrent().getTranslation(messageKey),
                UI.getCurrent().getTranslation(subMessageKey));
    }

    public void open() {
        if (!dialog.isOpened()) {
            dialog.open();
        }
    }

    public void close() {
        dialog.close();
    }

    private void buildProgressDialog(String title, String message, String subMessage) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        Div progressBarLabel = new Div();
        progressBarLabel.setText(message);
        Div progressBarSubLabel = new Div();
        progressBarSubLabel.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        progressBarSubLabel.setText(subMessage);

        dialog.setHeaderTitle(title);
        dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.add(progressBarLabel, progressBar, progressBarSubLabel);
    }

}
