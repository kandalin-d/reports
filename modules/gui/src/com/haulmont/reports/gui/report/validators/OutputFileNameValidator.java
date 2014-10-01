/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.validators;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.reports.gui.report.wizard.ReportWizardCreator;

/**
 * @author kozyaikin
 * @version $Id$
 */
public class OutputFileNameValidator implements Field.Validator {
    protected Messages messages = AppBeans.get(Messages.class);

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null || ((String) value).length() <= 0)
            throw new ValidationException(String.format(
                    messages.getMessage(OutputFileNameValidator.class, "validation.required.defaultMsg"),
                    messages.getMessage(ReportWizardCreator.class, "outputFileName")));
        else if (!((String) value).matches(messages.getMessage(OutputFileNameValidator.class, "outputFileNameRegexp")))
            throw new ValidationException(String.format(
                    messages.getMessage(OutputFileNameValidator.class, "fillCorrectOutputFileNameMsg"),
                    messages.getMessage(ReportWizardCreator.class, "outputFileName")));

    }
}
