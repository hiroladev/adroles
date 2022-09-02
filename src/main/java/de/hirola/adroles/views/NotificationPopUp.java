/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.hirola.adroles.Global;

public enum NotificationPopUp {
    ;
    public static final int INFO = 0;
    public static final int ERROR = 1;

    public static void show(int mode, String message) {
        final Notification notification = new Notification();
        if (mode == NotificationPopUp.ERROR) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            Div text = new Div(new Text(message));

            Button closeButton = new Button(new Icon("lumo", "cross"));
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            closeButton.getElement().setAttribute("aria-label", UI.getCurrent().getTranslation("close"));
            closeButton.addClickListener(event -> notification.close());

            HorizontalLayout layout = new HorizontalLayout(text, closeButton);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            notification.add(layout);

        } else {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(Global.Component.DEFAULT_NOTIFICATION_DURATION);
            notification.setText(message);
        }
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(Global.Component.DEFAULT_NOTIFICATION_DURATION);
        notification.open();
    }

    public static void show(int mode, String message, String subMessage) {
        final Notification notification = new Notification();

        Div text = new Div(new Text(message));
        Div subText = new Div(new Text(subMessage));

        if (mode == NotificationPopUp.ERROR) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            Button closeButton = new Button(new Icon("lumo", "cross"));
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            closeButton.getElement().setAttribute("aria-label", UI.getCurrent().getTranslation("close"));
            closeButton.addClickListener(event -> notification.close());

            HorizontalLayout layout = new HorizontalLayout(text, subText, closeButton);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            notification.add(layout);

        } else {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(Global.Component.DEFAULT_NOTIFICATION_DURATION);

            HorizontalLayout layout = new HorizontalLayout(text, subText);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            notification.add(layout);
        }
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(Global.Component.DEFAULT_NOTIFICATION_DURATION);
        notification.open();
    }
}
