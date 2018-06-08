/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.wizard.step;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.reports.gui.report.wizard.ReportWizardCreator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StepFrame {
    protected final String name;
    protected final Frame frame;
    protected final ReportWizardCreator wizard;

    protected InitStepFrameHandler initFrameHandler;
    protected BeforeHideStepFrameHandler beforeHideFrameHandler;
    protected BeforeShowStepFrameHandler beforeShowFrameHandler;

    protected boolean isLast;
    protected boolean isFirst;

    protected boolean validateBeforeNext = true;
    protected boolean validateBeforePrev = false;

    protected FrameValidator frameValidator = new FrameValidator();
    protected boolean isInitialized;

    public StepFrame(ReportWizardCreator reportWizardCreatorEditor, String name, String frameComponentName) {
        this.wizard = reportWizardCreatorEditor;
        this.name = name;
        this.frame = (Frame) reportWizardCreatorEditor.getComponent(frameComponentName);
        if (frame == null) {
            throw new UnsupportedOperationException("Frame component is not found");
        }
    }

    public void setInitFrameHandler(InitStepFrameHandler initFrameHandler) {
        this.initFrameHandler = initFrameHandler;
    }

    public void setBeforeHideFrameHandler(BeforeHideStepFrameHandler beforeHideFrameHandler) {
        this.beforeHideFrameHandler = beforeHideFrameHandler;
    }

    public void setBeforeShowFrameHandler(BeforeShowStepFrameHandler beforeShowFrameHandler) {
        this.beforeShowFrameHandler = beforeShowFrameHandler;
    }

    public void initFrame() {
        if (!isInitialized) {
            doDefaultInit();
            if (initFrameHandler != null) {
                initFrameHandler.initFrame();
            }
            isInitialized = true;
        }

    }

    private void doDefaultInit() {
        new DefaultFrameInitializer().initFrame();
    }

    public List<String> validateFrame() {
        return frameValidator.validateAllComponents();
    }

    public void beforeHide() {
        if (beforeHideFrameHandler == null) {
            return;
        }
        beforeHideFrameHandler.beforeHideFrame();
    }

    public void beforeShow() {
        if (beforeShowFrameHandler == null) {
            return;
        }
        beforeShowFrameHandler.beforeShowFrame();
    }

    public String getName() {
        return name;
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public boolean isValidateBeforeNext() {
        return validateBeforeNext;
    }

    public void setValidateBeforeNext(boolean validateBeforeNext) {
        this.validateBeforeNext = validateBeforeNext;
    }

    public boolean isValidateBeforePrev() {
        return validateBeforePrev;
    }

    public void setValidateBeforePrev(boolean validateBeforePrev) {
        this.validateBeforePrev = validateBeforePrev;
    }

    public interface BeforeHideStepFrameHandler {
        void beforeHideFrame();
    }

    public interface BeforeShowStepFrameHandler {
        void beforeShowFrame();
    }

    public interface InitStepFrameHandler {
        void initFrame();
    }

    protected class DefaultFrameInitializer implements InitStepFrameHandler {
        @Override
        public void initFrame() {
            for (Component c : frame.getComponents()) {
                if (c instanceof Field) {
                    Field field = (Field) c;
                    if (field.isRequired() && StringUtils.isBlank(field.getRequiredMessage()) && StringUtils.isBlank(field.getCaption())) {
                        field.setRequiredMessage(getDefaultRequiredMessage(wizard.getMessage(field.getId())));
                    }
                }
            }
        }
    }

    protected class FrameValidator {
        public List<String> validateAllComponents() {
            List<String> errors = new ArrayList<>();
            for (Component c : frame.getComponents()) {
                if (c instanceof Validatable) {
                    Validatable validatable = (Validatable) c;
                    try {
                        validatable.validate();
                    } catch (ValidationException e) {
                        errors.add(e.getMessage());
                    }
                }
            }
            return errors;
        }
    }

    protected String getDefaultRequiredMessage(String name) {
        Messages messages = AppBeans.get(Messages.NAME);
                return messages.formatMessage(messages.getMainMessagePack(),
                "validation.required.defaultMsg", name);
    }
}