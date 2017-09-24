/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.util.converter.AbstractObjectToStringConverter;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.UUID;

public class CubaObjectToStringConverter extends AbstractObjectToStringConverter {
    @Inject
    protected DataManager dataManager;

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        if (paramValue == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return (String) paramValue;
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            return EntityLoadInfo.create((Entity) paramValue).toString();
        } else {
            Datatype datatype = Datatypes.get(parameterClass);
            if (datatype != null) {
                return datatype.format(paramValue);
            } else {
                return String.valueOf(paramValue);
            }
        }
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        if (paramValueStr == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return paramValueStr;
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            EntityLoadInfo entityLoadInfo = EntityLoadInfo.parse(paramValueStr);
            if (entityLoadInfo != null) {
                return dataManager.load(new LoadContext(entityLoadInfo.getMetaClass()).setId(entityLoadInfo.getId()));
            } else {
                UUID id = UUID.fromString(paramValueStr);
                return dataManager.load(new LoadContext(parameterClass).setId(id));
            }
        } else {
            Datatype datatype = Datatypes.get(parameterClass);
            if (datatype != null) {
                try {
                    return datatype.parse(paramValueStr);
                } catch (ParseException e) {
                    throw new ReportingException(
                            String.format("Couldn't read value [%s] with datatype [%s].",
                                    paramValueStr, datatype));
                }
            } else {
                return convertFromStringUnresolved(parameterClass, paramValueStr);
            }
        }
    }
}
