/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

/**
 * @author artamonov
 */
public class TablePrintFormAction extends AbstractPrintFormAction implements Action.HasBeforeAfterHandlers {
    protected final Window window;
    protected final Table table;

    protected Runnable beforeActionPerformedHandler;
    protected Runnable afterActionPerformedHandler;

    public TablePrintFormAction(Window window, Table table) {
        this("tableReport", window, table);
    }

    public TablePrintFormAction(String id, Window window, Table table) {
        super(id);

        this.window = window;
        this.table = table;
        this.caption = messages.getMessage(getClass(), "actions.Report");
        this.icon = "icons/reports-print.png";
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null) {
            beforeActionPerformedHandler.run();
        }

        final Set selected = table.getSelected();

        DialogAction cancelAction = new DialogAction(Type.CANCEL);

        if (CollectionUtils.isNotEmpty(selected)) {
            Action printSelectedAction = new AbstractAction("actions.printSelected", Status.PRIMARY) {
                @Override
                public void actionPerform(Component component) {
                    printSelected(selected);
                }

                @Override
                public String getIcon() {
                    return "icons/reports-print-row.png";
                }
            };

            Action printAllAction = new AbstractAction("actions.printAll") {
                @Override
                public void actionPerform(Component component) {
                    printAll();
                }

                @Override
                public String getIcon() {
                    return "icons/reports-print-all.png";
                }
            };

            Action[] actions;
            if (selected.size() > 1) {
                actions = new Action[]{printAllAction, printSelectedAction, cancelAction};

                window.showOptionDialog(messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelectedheader"),
                        messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelected"),
                        Frame.MessageType.CONFIRMATION,
                        actions);
            } else {
                printSelected(selected);
            }
        } else {
            CollectionDatasource ds = table.getDatasource();

            if ((ds.getState() == Datasource.State.VALID) && (ds.size() > 0)) {
                Action yesAction = new DialogAction(Type.OK) {
                    @Override
                    public void actionPerform(Component component) {
                        printAll();
                    }
                };

                cancelAction.setPrimary(true);

                window.showOptionDialog(messages.getMessage(getClass(), "notifications.confirmPrintAllheader"),
                        messages.getMessage(getClass(), "notifications.confirmPrintAll"),
                        Frame.MessageType.CONFIRMATION, new Action[]{yesAction, cancelAction});
            } else {
                window.showNotification(messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"),
                        Frame.NotificationType.HUMANIZED);
            }
        }

        if (afterActionPerformedHandler != null) {
            afterActionPerformedHandler.run();
        }
    }

    protected void printSelected(Set selected) {
        CollectionDatasource datasource = table.getDatasource();
        MetaClass metaClass = datasource.getMetaClass();

        openRunReportScreen(window, selected, metaClass);
    }

    protected void printAll() {
        CollectionDatasource datasource = table.getDatasource();

        MetaClass metaClass = datasource.getMetaClass();

        LoadContext loadContext = datasource.getCompiledLoadContext();

        ParameterPrototype parameterPrototype = new ParameterPrototype(metaClass.getName());
        parameterPrototype.setMetaClassName(metaClass.getName());
        LoadContext.Query query = loadContext.getQuery();
        parameterPrototype.setQueryString(query.getQueryString());
        parameterPrototype.setQueryParams(query.getParameters());
        parameterPrototype.setViewName(loadContext.getView().getName());
        parameterPrototype.setFirstResult(query.getFirstResult());
        parameterPrototype.setMaxResults(query.getMaxResults());

        openRunReportScreen(window, parameterPrototype, metaClass);
    }

    @Override
    public Runnable getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(Runnable handler) {
        this.beforeActionPerformedHandler = handler;
    }

    @Override
    public Runnable getAfterActionPerformedHandler() {
        return afterActionPerformedHandler;
    }

    @Override
    public void setAfterActionPerformedHandler(Runnable handler) {
        this.afterActionPerformedHandler = handler;
    }
}