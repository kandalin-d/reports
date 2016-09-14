/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InputParametersFrame extends AbstractFrame {
    public static final String REPORT_PARAMETER = "report";
    public static final String PARAMETERS_PARAMETER = "parameters";

    protected Report report;
    protected Map<String, Object> parameters;

    @Inject
    protected GridLayout parametersGrid;

    @Inject
    protected ReportService reportService;

    @Inject
    protected DataSupplier dataSupplier;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    protected ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        report = (Report) params.get(REPORT_PARAMETER);
        if (report != null && !report.getIsTmp()) {
            report = dataSupplier.reload(report, ReportService.MAIN_VIEW_NAME);
        }
        //noinspection unchecked
        parameters = (Map<String, Object>) params.get(PARAMETERS_PARAMETER);
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }

        if (report != null) {
            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                parametersGrid.setRows(report.getInputParameters().size());

                int currentGridRow = 0;
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    createComponent(parameter, currentGridRow);
                    currentGridRow++;
                }
            }
        }
    }

    public Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        for (String paramName : parameterComponents.keySet()) {
            Field parameterField = parameterComponents.get(paramName);
            Object value = parameterField.getValue();
            parameters.put(paramName, value);
        }
        return parameters;
    }

    protected void createComponent(ReportInputParameter parameter, int currentGridRow) {
        Field field = parameterFieldCreator.createField(parameter);
        field.setWidth("400px");

        Object value = parameters.get(parameter.getAlias());

        if (value == null && parameter.getDefaultValue() != null) {
            Class parameterClass = parameterClassResolver.resolveClass(parameter);
            if (parameterClass != null) {
                value = reportService.convertFromString(parameterClass, parameter.getDefaultValue());
            }
        }

        if (!(field instanceof TokenList)) {
            field.setValue(value);
        } else {
            CollectionDatasource datasource = (CollectionDatasource) field.getDatasource();
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                for (Object selected : collection) {
                    datasource.includeItem((Entity) selected);
                }
            }
        }

        Label label = parameterFieldCreator.createLabel(parameter, field);
        label.setStyleName("cuba-report-parameter-caption");

        if (currentGridRow == 0) {
            field.requestFocus();
        }

        parameterComponents.put(parameter.getAlias(), field);
        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    public Report getReport() {
        return report;
    }
}