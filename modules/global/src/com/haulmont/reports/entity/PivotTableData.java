/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.entity;

import com.haulmont.cuba.core.entity.KeyValueEntity;

import java.io.Serializable;
import java.util.List;

public class PivotTableData implements Serializable {
    protected String pivotTableJson;
    protected List<KeyValueEntity> values;

    public PivotTableData(String pivotTableJson, List<KeyValueEntity> values) {
        this.values = values;
        this.pivotTableJson = pivotTableJson;
    }

    public List<KeyValueEntity> getValues() {
        return values;
    }

    public void setValues(List<KeyValueEntity> values) {
        this.values = values;
    }

    public String getPivotTableJson() {
        return pivotTableJson;
    }

    public void setPivotTableJson(String pivotTableJson) {
        this.pivotTableJson = pivotTableJson;
    }
}
