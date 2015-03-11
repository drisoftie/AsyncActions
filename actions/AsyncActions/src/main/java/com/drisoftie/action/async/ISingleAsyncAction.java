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

import java.util.List;

/**
 * A generic action interface that defines a single action based on registering and invoking the action in a three way style - preparing the
 * action, executing the action, cleaning up the action.
 *
 * @author Alexander Dridiger
 */
public interface ISingleAsyncAction<ViewT, ResultT, Tag1T, Tag2T> extends IAsyncAction<ViewT> {

    /**
     * Possibility to register and implement an action.
     */
    void registerAction();

    /**
     * Possibility to register and implement an action.
     *
     * @param bindings the bindings to register the action
     */
    void registerAction(List<AsyncAction.ActionBinding<ViewT>> bindings);

    /**
     * For invoking the action manually, if it implements an {@link com.drisoftie.action.async.IGenericAction} Interface.
     *
     * @param methodArgs the arguments to invoke the method with
     */
    void invokeSelf(Object... methodArgs);

    /**
     * For invoking the action manually inside the UI {@link Thread}. Searches for action callbacks of type
     * {@link com.drisoftie.action.async.IGenericAction} and invokes its {@link IGenericAction#invokeAction(Object...)} method.
     *
     * @param methodArgs the arguments to invoke the action
     */
    void invokeSelfInUiThread(Object... methodArgs);

    /**
     * Returns the registered handler (implemented Java Interface) as an instance of the {@code clazz} argument of the first registered
     * {@link ViewT}.
     *
     * @param clazz the class to cast to
     * @return the handler or {@code null}
     */
    <HandlerT> HandlerT getHandlerImpl(Class<HandlerT> clazz);

    /**
     * Returns the registered handler (implemented Java Interface) as an instance of the {@code clazz} argument of the given {@link ViewT}.
     *
     * @param view  the view the handler is bound to
     * @param clazz the class to cast to
     * @return the handler or {@code null}
     */
    <HandlerT> HandlerT getHandlerImpl(ViewT view, Class<HandlerT> clazz);

    /**
     * Replaces the formerly registered {@link ViewT} targets with the given new targets.
     *
     * @param views new view targets
     */
    void replaceViewTargets(ViewT... views);

    /**
     * Indicator if the asynchronous work should be done or if invocation should stop after
     * {@link #onActionPrepare(String, Object[], Object, Object, Object[])}.
     *
     * @param run if it should run in background
     */
    void setRunWorkThread(boolean run);

    /**
     * Indicator if the asynchronous work should be done or if invocation should stop <b>ONCE</b> after
     * {@link #onActionPrepare(String, Object[], Object, Object, Object[])}.
     */
    void skipWorkThreadOnce();

    /**
     * Do (lightweight) preparations <b>before</b> asynchronous work begins. Keep in mind that this is the <b>only</b> place where a result
     * can be returned to the implemented {@code actionType}. Thus it <b>MUST</b> return the appropriate return type (or {@code null} if the
     * return type is {@code void}).<br>
     * The reason is that the UI {@link Thread} should <b>NOT</b> be occupied, therefore the other callbacks do not run at the time of the
     * method invocation, but <b>afterwards</b>. <br>
     * However <b>NOTE</b> that {@link #onActionPrepare(String, Object[], Object, Object, Object[])} <b>CAN</b> be invoked <b>OUTSIDE</b>
     * the UI {@link Thread}.
     *
     * @param methodName     the name of the invoked method
     * @param methodArgs     the arguments provided by the called method {@code methodName}
     * @param tag1           optional tag
     * @param tag2           optional tag
     * @param additionalTags optional additional tags
     * @return an <b>optional</b> return value for the implemented {@code actionType}
     */
    Object onActionPrepare(String methodName, Object[] methodArgs, Tag1T tag1, Tag2T tag2, Object[] additionalTags);

    /**
     * Do work asynchronously.<br>
     * Runs outside the UI {@link Thread}.
     *
     * @param methodName     the name of the invoked method
     * @param methodArgs     the arguments provided by the called method {@code methodName}
     * @param tag1           optional tag
     * @param tag2           optional tag
     * @param additionalTags optional additional tags
     * @return possibility to return a work result for the UI {@link Thread} in
     * {@link #onActionAfterWork(String, Object[], Object, Object, Object, Object[])}
     */
    ResultT onActionDoWork(String methodName, Object[] methodArgs, Tag1T tag1, Tag2T tag2, Object[] additionalTags);

    /**
     * Possibility to clean up <b>after</b> asynchronous work. Runs on UI {@link Thread}.
     *
     * @param methodName     the name of the invoked method
     * @param methodArgs     the arguments provided by the called method {@code methodName}
     * @param workResult     the formerly returned work result
     * @param tag1           optional tag
     * @param tag2           optional tag
     * @param additionalTags optional additional tags
     */
    void onActionAfterWork(String methodName, Object[] methodArgs, ResultT workResult, Tag1T tag1, Tag2T tag2, Object[] additionalTags);

    /**
     * Framework dependent. E.g. in Android it's {@link android.view.View#post(Runnable)}.
     *
     * @param view     the view to invoke the runnable from
     * @param runnable invoke this runnable on the view thread
     */
    void invokeRunnableOnViewImpl(ViewT view, Runnable runnable);

    /**
     * Framework dependent.
     *
     * @param runnable invoke this runnable on the ui thread
     */
    void invokeRunnableOnUiThread(Runnable runnable);
}