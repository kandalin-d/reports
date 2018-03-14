/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.parameter.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ScreensHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.gui.report.run.ParameterClassResolver;
import com.haulmont.reports.gui.report.run.ParameterFieldCreator;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

public class ParameterEditor extends AbstractEditor<ReportInputParameter> {
    @Inject
    protected Label defaultValueLabel;

    @Inject
    protected BoxLayout defaultValueBox;

    @Inject
    protected LookupField screen;

    @Inject
    protected LookupField enumeration;

    @Inject
    protected LookupField type;

    @Inject
    protected LookupField metaClass;

    @Inject
    protected Label enumerationLabel;

    @Inject
    protected Label screenLabel;

    @Inject
    protected Label metaClassLabel;

    @Inject
    protected GridLayout predefinedTransformationBox;

    @Inject
    protected CheckBox predefinedTransformation;

    @Inject
    protected SourceCodeEditor transformationScript;

    @Inject
    protected Label transformationScriptLabel;

    @Inject
    protected LookupField wildcards;

    @Inject
    protected Label wildcardsLabel;

    @Inject
    protected CheckBox defaultDateIsCurrentCheckBox;

    @Inject
    protected Label defaultDateIsCurrentLabel;

    @Inject
    protected Label requiredLabel;

    @Inject
    protected CheckBox required;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ReportService reportService;

    @Inject
    protected Datasource<ReportInputParameter> parameterDs;

    @Inject
    protected ScreensHelper screensHelper;

    protected ReportInputParameter parameter;

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    protected ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

    @Override
    public void setItem(Entity item) {
        ReportInputParameter newParameter = (ReportInputParameter) metadata.create(parameterDs.getMetaClass());
        metadata.getTools().copy(item, newParameter);
        newParameter.setId((UUID) item.getId());
        if (newParameter.getParameterClass() == null) {
            newParameter.setParameterClass(parameterClassResolver.resolveClass(newParameter));
        }

        super.setItem(newParameter);
        enableControlsByParamType(newParameter.getType());
        initScreensLookup();
        initTransformations();
    }

    @Override
    @SuppressWarnings({"unchecked", "serial"})
    public void init(Map<String, Object> params) {
        super.init(params);

        type.setOptionsList(Arrays.asList(ParameterType.TEXT, ParameterType.NUMERIC, ParameterType.BOOLEAN, ParameterType.ENUMERATION,
                ParameterType.DATE, ParameterType.TIME, ParameterType.DATETIME, ParameterType.ENTITY, ParameterType.ENTITY_LIST));

        initMetaClassLookup();

        initEnumsLookup();

        initListeners();
    }

    @Override
    public boolean commit() {
        if (super.commit()) {
            metadata.getTools().copy(getItem(), parameter);
            return true;
        }
        return false;
    }

    protected void initListeners() {
        type.addValueChangeListener(e -> enableControlsByParamType((ParameterType) e.getValue()));

        parameterDs.addItemPropertyChangeListener(e -> {
            boolean typeChanged = e.getProperty().equalsIgnoreCase("type");
            boolean classChanged = e.getProperty().equalsIgnoreCase("entityMetaClass")
                    || e.getProperty().equalsIgnoreCase("enumerationClass");
            boolean defaultDateIsCurrentChanged = e.getProperty().equalsIgnoreCase("defaultDateIsCurrent");
            ReportInputParameter parameter = getItem();
            if (typeChanged || classChanged) {
                parameter.setParameterClass(parameterClassResolver.resolveClass(parameter));

                if (typeChanged) {
                    parameter.setEntityMetaClass(null);
                    parameter.setEnumerationClass(null);
                }

                parameter.setDefaultValue(null);
                parameter.setScreen(null);

                initScreensLookup();

                initDefaultValueField();
            }

            if (defaultDateIsCurrentChanged) {
               initDefaultValueField();
               initCurrentDateTimeField();
            }

            ((DatasourceImplementation<ReportInputParameter>) parameterDs).modified(e.getItem());
        });
    }

    protected void initScreensLookup() {
        ReportInputParameter parameter = getItem();
        if (parameter.getType() == ParameterType.ENTITY ||  parameter.getType() == ParameterType.ENTITY_LIST) {
            Class clazz = parameterClassResolver.resolveClass(parameter);
            if (clazz != null) {
                Map<String, Object> screensMap = screensHelper.getAvailableBrowserScreens(clazz);
                screen.setOptionsMap(screensMap);
            }
        }
    }

    protected void initEnumsLookup() {
        Map<String, Object> enumsOptionsMap = new TreeMap<>();
        for (Class enumClass : metadata.getTools().getAllEnums()) {
            String enumLocalizedName = messages.getMessage(enumClass, enumClass.getSimpleName());
            enumsOptionsMap.put(enumLocalizedName + " (" + enumClass.getSimpleName() + ")", enumClass.getCanonicalName());
        }
        enumeration.setOptionsMap(enumsOptionsMap);
    }

    protected void initMetaClassLookup() {
        Map<String, Object> metaClassesOptionsMap = new TreeMap<>();
        Collection<MetaClass> classes = metadata.getSession().getClasses();
        for (MetaClass clazz : classes) {
            if (!metadata.getTools().isSystemLevel(clazz)) {
                String caption = messages.getTools().getDetailedEntityCaption(clazz);
                metaClassesOptionsMap.put(caption, clazz.getName());
            }
        }
        metaClass.setOptionsMap(metaClassesOptionsMap);
    }

    protected void initDefaultValueField() {
        defaultValueLabel.setVisible(false);
        defaultValueBox.removeAll();
        ReportInputParameter parameter = getItem();
        if (canHaveDefaultValue()) {
            Field field = parameterFieldCreator.createField(parameter);
            field.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    parameter.setDefaultValue(reportService.convertToString(e.getValue().getClass(), e.getValue()));
                } else {
                    parameter.setDefaultValue(null);
                }
            });

            if (parameter.getParameterClass() != null) {
                field.setValue(reportService.convertFromString(parameter.getParameterClass(), parameter.getDefaultValue()));
            }
            field.setRequired(false);

            defaultValueBox.add(field);
            defaultValueLabel.setVisible(true);
        }
    }

    protected void initCurrentDateTimeField() {
        boolean parameterDateOrTime = isParameterDateOrTime();
        defaultDateIsCurrentLabel.setVisible(parameterDateOrTime);
        defaultDateIsCurrentCheckBox.setVisible(parameterDateOrTime);
    }

    protected boolean canHaveDefaultValue() {
        ReportInputParameter parameter = getItem();
        if (parameter == null) {
            return false;
        }

        if (isParameterDateOrTime() && BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
            return false;
        }

        ParameterType type = parameter.getType();
        return type != null
                && type != ParameterType.ENTITY_LIST
                && (type != ParameterType.ENTITY || StringUtils.isNotBlank(parameter.getEntityMetaClass()))
                && (type != ParameterType.ENUMERATION || StringUtils.isNotBlank(parameter.getEnumerationClass()));
    }

    protected void enableControlsByParamType(ParameterType type) {
        boolean isEntity = type == ParameterType.ENTITY || type == ParameterType.ENTITY_LIST;
        boolean isEnum = type == ParameterType.ENUMERATION;
        boolean isText = type == ParameterType.TEXT;

        metaClass.setVisible(isEntity);
        metaClassLabel.setVisible(isEntity);

        screen.setVisible(isEntity);
        screenLabel.setVisible(isEntity);

        enumeration.setVisible(isEnum);
        enumerationLabel.setVisible(isEnum);

        predefinedTransformationBox.setVisible(isText);

        initDefaultValueField();
        initCurrentDateTimeField();
    }

    protected void initTransformations() {
        ReportInputParameter parameter = getItem();
        predefinedTransformation.setValue(parameter.getPredefinedTransformation() != null);
        enableControlsByTransformationType(parameter.getPredefinedTransformation() != null);
        predefinedTransformation.addValueChangeListener(e -> {
            boolean hasPredefinedTransformation = e.getValue() != null && (Boolean)e.getValue() ;
            enableControlsByTransformationType(hasPredefinedTransformation);
            if (hasPredefinedTransformation) {
                parameter.setTransformationScript(null);
            } else {
                parameter.setPredefinedTransformation(null);
            }
        });
    }

    protected void enableControlsByTransformationType(boolean hasPredefinedTransformation) {
        transformationScript.setVisible(!hasPredefinedTransformation);
        transformationScriptLabel.setVisible(!hasPredefinedTransformation);
        wildcards.setVisible(hasPredefinedTransformation);
        wildcardsLabel.setVisible(hasPredefinedTransformation);
    }

    public void getValidationScriptHelp() {
        showMessageDialog(getMessage("validationScript"), getMessage("validationScriptHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(600f));
    }

    public void getLocaleTextHelp() {
        showMessageDialog(getMessage("localeText"), getMessage("parameter.localeTextHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(560f));
    }

    public void getTransformationScriptHelp() {
        showMessageDialog(getMessage("transformationScript"), getMessage("parameter.transformationScriptHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(560f));
    }

    protected boolean isParameterDateOrTime() {
        ReportInputParameter parameter = getItem();
        return Optional.ofNullable(parameter)
                .map(reportInputParameter ->
                        ParameterType.DATE.equals(parameter.getType()) ||
                        ParameterType.DATETIME.equals(parameter.getType()) ||
                        ParameterType.TIME.equals(parameter.getType()))
                .orElse(false);
    }
}
