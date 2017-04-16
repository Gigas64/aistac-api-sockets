/*
 * @(#)AiStacApiSocketException.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.exceptions;

import io.aistac.common.canonical.exceptions.AiStacSchemaException;

/**
 * The {@code AiStacApiSocketException} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 26-Mar-2016
 */
public class AiStacApiSocketException extends AiStacSchemaException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an instance of {@code OathouseSocketException} with the specified detail message.
     * The severity level is set to medium, application error.
     * @param msg the detail message.
    */
    public AiStacApiSocketException(String msg) {
        super(SEVERITY.MEDIUM, msg);
    }
    /**
     * Constructs an instance of {@code OathouseSocketException} with the specified detail message and a severity:
     * application threatening (high severity), application error (medium severity) or application warning (low severity).
     * @param severity the severity level of the Exception
     * @param msg the detail message.
     */
    public AiStacApiSocketException(SEVERITY severity, String msg) {
        super(severity, msg);
    }

}
