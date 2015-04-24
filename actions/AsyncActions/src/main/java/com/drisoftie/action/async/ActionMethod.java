/*
 * Copyright [2015] [Alexander Dridiger - drisoftie@gmail.com]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.drisoftie.action.async;

/**
 * Convenient Enum to prevent using plain {@link String}s for naming common action methods, encapsulating their names.
 *
 * @author Alexander Dridiger
 */
public enum ActionMethod {

    /**
     * {@link android.view.View.OnClickListener#onClick(android.view.View)} or
     * {@link android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)}
     */
    ON_CLICK("onClick"),

    /**
     * {@link android.view.View.OnLongClickListener#onLongClick(android.view.View)}
     */
    ON_LONG_CLICK("onLongClick"),

    /**
     * {@link android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)}
     */
    ON_ITEM_CLICK("onItemClick"),

    /**
     * {@link android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)}
     */
    ON_ITEM_SELECTED("onItemSelected"),

    /**
     * {@link android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)}
     */
    ON_CHECKED_CHANGED("onCheckedChanged"),

    /**
     * {@link com.drisoftie.action.async.IGenericAction#invokeAction(Object...)}
     */
    INVOKE_ACTION("invokeAction"),

    /**
     * For methods that are called <b>onResult(...)</b>
     */
    ON_RESULT("onResult"),

    /**
     * For methods that are called <b>onResults(...)</b>
     */
    ON_RESULTS("onResults"),

    /**
     * For methods that are called <b>onResultReady(...)</b>
     */
    ON_RESULT_READY("onResultReady"),

    /**
     * {@link android.widget.AbsListView.MultiChoiceModeListener#onCreateActionMode(android.view.ActionMode, android.view.Menu)}
     */
    ON_CREATE_ACTION_MODE("onCreateActionMode"),

    /**
     * {@link android.widget.AbsListView.MultiChoiceModeListener#onPrepareActionMode(android.view.ActionMode, android.view.Menu)}
     */
    ON_PREPARE_ACTION_MODE("onPrepareActionMode"),

    /**
     * {@link android.widget.AbsListView.MultiChoiceModeListener#onItemCheckedStateChanged(android.view.ActionMode, int, long, boolean)}
     */
    ON_ITEM_CHECKED_STATE_CHANGED("onItemCheckedStateChanged"),

    /**
     * {@link android.widget.AbsListView.MultiChoiceModeListener#onActionItemClicked(android.view.ActionMode, android.view.MenuItem)}
     */
    ON_ACTION_ITEM_CLICKED("onActionItemClicked"),

    /**
     * {@link android.widget.AbsListView.MultiChoiceModeListener#onDestroyActionMode(android.view.ActionMode)}
     */
    ON_DESTROY_ACTION_MODE("onDestroyActionMode"),

    /**
     * {@link android.nfc.NfcAdapter.OnNdefPushCompleteCallback#onNdefPushComplete(android.nfc.NfcEvent)}
     */
    ON_NDEF_PUSH_COMPLETE("onNdefPushComplete");

    private String methodName;

    private ActionMethod(String methodName) {
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

    /**
     * Helper method to check if it matches another method.
     *
     * @param methodName method to check
     * @return if matched
     */
    public boolean matches(String methodName) {
        return this.methodName.equals(methodName);
    }
}
