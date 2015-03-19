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
package com.drisoftie.action.async.android;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.drisoftie.action.async.BaseAsyncAction;

/**
 * Dynamic {@link java.lang.reflect.Proxy} {@link java.lang.reflect.InvocationHandler} that intercepts interface callbacks of <b>any</b>
 * derived {@link android.view.View} (generic {@link ViewT} argument) and wraps them inside a generic three-step process.
 * <ol>
 * <li>The first step gives the possibility to handle a callback inside the same {@link Thread} it was invoked in (in most cases the UI
 * {@link Thread}, when dealing with UI). This step is also the one returning the return value of the action callback (if it has any, or
 * else it simply returns {@code null}).
 * <li>The second step runs inside a new {@link Thread} and is supposed to be the one doing the heavy work, so that the invoking action
 * thread (in Android referred to as the "UI {@link Thread}") is not occupied with this work. This step can also return a value.
 * <li>The third step runs again in the UI Thread and is useful to clean up stuff after the heavy work is done or it can  show the user
 * an indicator that the work is finished.
 * </ol>
 * <p>
 * Additionally it provides ways to apply various tag information to facilitate data transfer between those steps and to help identifying
 * which action is invoked, if the implemented interface provided multiple action callbacks.
 * </p>
 * Use this class in the following way:
 * <pre>
 * {@code
 * 	// this action applies to a View and its OnClickListener. It has no tags, no result and no method filters.
 * 	AsyncAction<View, Void, Void, Void> action = new AsyncAction<View, Void, Void, Void>(myView, OnClickListener.class,
 *                  // method to register the callback
 *                  "setOnClickListener") {
 *
 * 		public Object onActionPrepare(String methodName, Object[] methodArgs, Void tag1, Void tag2, Object[] additionalTags) {
 * 			// preparations, progress bar and return a click value (due to expected void, it's null)
 * 			return null;
 * 		}
 *
 * 		public Void onActionDoWork(String methodName, Object[] methodArgs, Void tag1, Void tag2, Object[] additionalTags) {
 * 			// actual work like internet access, loading stuff from database, heavy computing, no value to return
 * 		    return null;
 * 		}
 *
 * 		public void onActionAfterWork(String methodName, Object[] methodArgs, Void workResult, Void tag1, Void tag2,
 * 		             Object[] additionalTags) {
 * 		    // cleanup, hide progress bar, show results...
 * 		}
 * 	};
 * }
 * </pre>
 * For providing optional information, use it like this:
 * <pre>
 * {@code
 * 	// this action applies to a Button and its OnClickListener. It has method filters and tags.
 * 	AsyncAction<Button, Point, Animations, Void> action = new AsyncAction<Button, Point, Animations, Void>(myButton, OnClickListener.class,
 *                  // registration method
 *                  "setOnClickListener",
 *                  // allow only onClick methods to pass, filter
 *                  new String[] { "onClick" },
 *                  myAnimation, null, additionalTag1, additionalTag2, identifier) {
 *
 * 		public Object onActionPrepare(String methodName, Object[] methodArgs, Animation myAnimation, Void tag2, Object[] additionalTags) {
 * 			// use tags to start a fancy animation at a point
 * 			// tags usable for this work, tag array can be updated for future work (passed to every other step)
 * 			return true;
 * 		}
 *
 * 		public Point onActionDoWork(String methodName, Object[] methodArgs, Animation myAnimation, Void tag2, Object[] additionalTags) {
 * 			// actual work like internet access, loading stuff from database, heavy computing...
 * 			// tags usable for this work, tag array can be updated for future work
 * 		    return currentPoint;
 * 		}
 *
 * 		public void onActionAfterWork(String methodName, Object[] methodArgs, Point currentPoint, Animation myAnimation, Void tag2,
 * 	            	Object[] additionalTags) {
 * 			// cleanup and show user results
 * 		}
 * 	};
 * }
 * </pre>
 * When implementing multiple Java Interfaces, use it like this:
 * <pre>
 * {@code
 * 	// this action applies to a Button and its OnClickListener, additionally it implements another generic Interface. It has method filters
 * 	// and tags.
 * 	AsyncAction<Button, Point, Animation, Void> action = new AsyncAction<Button, Point, Animation, Void>(myButton,
 * 	                new Class[] { OnClickListener.class, IGenericInterface.class},
 *                  // registration method
 *                  "setOnClickListener",
 *                  // allow only onClick and invokeAction methods to pass, filter
 *                  new String[] { "onClick", "invokeAction" },
 *                  myAnimation, null, additionalTag1, additionalTag2, identifier) {
 *
 * 		public Object onActionPrepare(String methodName, Object[] methodArgs, Animation tag1, Void tag2, Object[] additionalTags) {
 * 			// find out what method was invoked
 * 			if (ActionMethod.ON_CLICK.matches.equals(methodName)) {
 * 				// OnClickListener was invoked
 *  			return null;
 * 			} else {
 * 				// the other interface was invoked
 * 			    return result;
 * 			}
 * 		}
 *
 * 		public Point onActionDoWork(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 	        // actual work like internet access, loading stuff from database, heavy computing...
 * 	        // tags usable for this work, tag array can be updated for future work
 *          return currentPoint;
 * 		}
 *
 * 		public void onActionAfterWork(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// cleanup and user indicator...
 * 			// tags usable for that...
 * 		}
 * 	};
 * }
 * </pre>
 *
 * @author Alexander Dridiger
 */
public abstract class AndroidAction<ViewT extends View, ResultT, Tag1T, Tag2T> extends BaseAsyncAction<ViewT, ResultT, Tag1T, Tag2T> {

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Object, Object...)
     */
    public AndroidAction(Tag1T tag1, Tag2T tag2, Object... additionalTags) {
        super(tag1, tag2, additionalTags);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Class, String)
     */
    public AndroidAction(ViewT view, Class<?> actionType, String regMethodName) {
        super(view, actionType, regMethodName);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object[], Class, String)
     */
    public AndroidAction(ViewT[] views, Class<?> actionType, String regMethodName) {
        super(views, actionType, regMethodName);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Class[], String)
     */
    public AndroidAction(ViewT view, Class<?>[] actionTypes, String regMethodName) {
        super(view, actionTypes, regMethodName);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object[], Class[], String)
     */
    public AndroidAction(ViewT[] views, Class<?>[] actionTypes, String regMethodName) {
        super(views, actionTypes, regMethodName);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Class, String, String[])
     */
    public AndroidAction(ViewT view, Class<?> actionTypes, String regMethodName, String[] actionMethodNames) {
        super(view, actionTypes, regMethodName, actionMethodNames);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object[], Class, String, String[])
     */
    public AndroidAction(ViewT[] views, Class<?> actionTypes, String regMethodName, String[] actionMethodNames) {
        super(views, actionTypes, regMethodName, actionMethodNames);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Class[], String, String[])
     */
    public AndroidAction(ViewT view, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames) {
        super(view, actionTypes, regMethodName, actionMethodNames);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object[], Class[], String, String[])
     */
    public AndroidAction(ViewT[] views, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames) {
        super(views, actionTypes, regMethodName, actionMethodNames);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Class, String, String[], Object, Object,
     * Object...)
     */
    public AndroidAction(ViewT view, Class<?> actionType, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                         Object... additionalTags) {
        super(view, actionType, regMethodName, actionMethodNames, tag1, tag2, additionalTags);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object[], Class, String, String[], Object, Object,
     * Object...)
     */
    public AndroidAction(ViewT[] views, Class<?> actionType, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                         Object... additionalTags) {
        super(views, actionType, regMethodName, actionMethodNames, tag1, tag2, additionalTags);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object, Class[], String, String[], Object, Object,
     * Object...)
     */
    public AndroidAction(ViewT view, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                         Object... additionalTags) {
        super(view, actionTypes, regMethodName, actionMethodNames, tag1, tag2, additionalTags);
    }

    /**
     * @see com.drisoftie.action.async.BaseAsyncAction#BaseAsyncAction(Object[], Class[], String, String[], Object, Object,
     * Object...)
     */
    public AndroidAction(ViewT[] views, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                         Object... additionalTags) {
        super(views, actionTypes, regMethodName, actionMethodNames, tag1, tag2, additionalTags);
    }

    @Override
    public void invokeRunnableOnViewImpl(ViewT view, Runnable runnable) {
        view.post(runnable);
    }

    @Override
    public void invokeRunnableOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}