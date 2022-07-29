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

public enum NotificationPopUp {
    ;

    public static final int INFO = 0;
    public static final int ERROR = 1;

    public static void show(int mode, String message) {
        Notification notification;
        if (mode == NotificationPopUp.ERROR) {
            notification = new Notification();
            Div text = new Div(new Text(message));
            Button closeButton = new Button(new Icon("lumo", "cross"));
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            closeButton.getElement().setAttribute("aria-label", UI.getCurrent().getTranslation("close"));
            closeButton.addClickListener(event -> notification.close());

            HorizontalLayout layout = new HorizontalLayout(text, closeButton);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);

            notification.add(layout);
            closeButton.getElement().setAttribute("aria-label", "Close");
            closeButton.addClickListener(event -> notification.close());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            notification = Notification.show(message);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
    }
}
