/*
 *  Copyright [2013-2015] [Alexander Dridiger - drisoftie@gmail.com]
 *  *
 *  *   Licensed under the Apache License, Version 2.0 (the "License");
 *  *   you may not use this file except in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *   Unless required by applicable law or agreed to in writing, software
 *  *   distributed under the License is distributed on an "AS IS" BASIS,
 *  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *   See the License for the specific language governing permissions and
 *  *   limitations under the License.
 *
 */
package com.drisoftie.action.async;

/**
 * Convenient Enum to prevent using plain {@link String}s for naming common action registration methods (like <b>setOnClickListener</b>;
 * encapsulating their names.
 *
 * @author Alexander Dridiger
 */
public enum RegActionMethod {

    /**
     * {@link android.view.View#setOnClickListener(android.view.View.OnClickListener)}
     */
    SET_ONCLICK("setOnClickListener"),

    /**
     * {@link android.view.View#setOnLongClickListener(android.view.View.OnLongClickListener)}
     */
    SET_ONLONGCLICK("setOnLongClickListener"),

    /**
     * {@link android.widget.AbsListView#setMultiChoiceModeListener(android.widget.AbsListView.MultiChoiceModeListener)}
     */
    SET_MULTICHOICEMODELISTENER("setMultiChoiceModeListener"),

    /**
     * {@link android.widget.AdapterView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)}
     */
    SET_ONITEMSELECTEDLISTENER("setOnItemSelectedListener"),

    /**
     * {@link android.widget.AdapterView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)}
     */
    SET_ONITEMCLICKLISTENER("setOnItemClickListener"),

    /**
     * {@link android.widget.AdapterView#setOnItemLongClickListener(android.widget.AdapterView.OnItemLongClickListener)}
     */
    SET_ONITEMLONGCLICKLISTENER("setOnItemLongClickListener"),

    /**
     * {@link android.view.View#setOnDragListener(android.view.View.OnDragListener)}
     */
    SET_ONDRAGLISTENER("setOnDragListener");

    private String methodName;

    private RegActionMethod(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the method in its string form.
     *
     * @return the method
     */
    public String method() {
        return methodName;
    }
}