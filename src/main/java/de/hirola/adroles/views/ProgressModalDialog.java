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
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.progressbar.ProgressBar;
import de.hirola.adroles.Global;

public final class ProgressModalDialog {
    private final Dialog dialog;
    private final ProgressBar progressBar;
    private final Div progressBarLabel, progressBarSubLabel;

    public ProgressModalDialog(){
        dialog = new Dialog();
        progressBar = new ProgressBar();
        progressBarLabel = new Div();
        progressBarSubLabel = new Div();
        buildProgressDialog();
    }

    public void open(String titleKey, String messageKey, String subMessageKey) {
        dialog.setHeaderTitle(UI.getCurrent().getTranslation(titleKey));
        progressBarLabel.setText(UI.getCurrent().getTranslation(messageKey));
        progressBarSubLabel.setText(UI.getCurrent().getTranslation(subMessageKey));
        dialog.open();
    }

    public void close() {
        dialog.close();
    }

    private void buildProgressDialog() {
        progressBar.setIndeterminate(true);
        progressBarSubLabel.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.add(progressBarLabel, progressBar, progressBarSubLabel);
    }
}
