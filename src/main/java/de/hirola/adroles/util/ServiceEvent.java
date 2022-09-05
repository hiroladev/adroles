/*
 * *
 *  * Copyright 2022 by Michael Schmidt, Hirola Consulting
 *  * This software us licensed under the AGPL-3.0 or later.
 *  *
 *  *
 *  * @author Michael Schmidt (Hirola)
 *  * @since v0.1
 *
 */

package de.hirola.adroles.util;

import org.springframework.context.ApplicationEvent;

import javax.validation.constraints.NotNull;

public class ServiceEvent extends ApplicationEvent {
    private final ServiceResult serviceResult;

    public ServiceEvent(Object source, @NotNull ServiceResult serviceResult) {
        super(source);
        this.serviceResult = serviceResult;
    }
    public ServiceResult getServiceResult() {
        return serviceResult;
    }
}
