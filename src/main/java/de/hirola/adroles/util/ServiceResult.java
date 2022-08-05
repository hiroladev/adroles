/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.util;

public final class ServiceResult {

    public boolean operationSuccessful;
    public String resultMessage;

    public ServiceResult(boolean operationSuccessful, String resultMessage) {
        this.operationSuccessful = operationSuccessful;
        this.resultMessage = resultMessage;
    }
}
