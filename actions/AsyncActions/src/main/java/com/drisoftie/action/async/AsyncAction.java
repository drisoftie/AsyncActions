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

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.view.View;

import com.drisoftie.action.async.handler.IFinishedHandler;
import com.drisoftie.action.async.thread.BaseRunThread;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamic {@link java.lang.reflect.Proxy} {@link java.lang.reflect.InvocationHandler} that intercepts interface callbacks of <b>any</b>
 * derived {@link android.view.View} (generic
 * {@link ViewT} argument) and wraps them inside a generic three-step process.
 * <p/>
 * <ol>
 * <li>The first step gives the possibility to handle the callback inside the {@link Thread} it was invoked in (in most cases the UI
 * {@link Thread}, when dealing with UI). This step is also the one returning the return value of the action callback (if it has any, or
 * else it simply returns {@code null}).
 * <p/>
 * <li>The second step runs inside a new {@link Thread} and is supposed to be the one doing the heavy work, so that the invoking action
 * thread (in Android referred to as the "UI {@link Thread}") is not occupied with this work.
 * <p/>
 * <li>The third step runs again in the UI Thread and is useful to clean up stuff after the heavy work is done or show the user an indicator
 * that the work is finished.
 * </ol>
 * <p/>
 * Additionally it provides ways to apply various tag information to facilitate data transfer between those steps and to help identifying
 * which action is invoked, if the implemented interface provided multiple action callbacks.
 * <p/>
 * Use this class in the following way:
 * <p/>
 * <p/>
 * <pre>
 * {@code
 * 	// this action applies to a View and its OnClickListener. It has no tags, no result and method filters.
 * 	AsyncAction<View, Void, Void, Void> action = new AsyncAction<View, Void, Void, Void>(myView, OnClickListener.class,
 * 				"setOnClickListener") {
 *
 * 		public Object onActionPrepare(String methodName, Object[] methodArgs, Object tag1, Object tag2, Object[] additionalTags) {
 * 			// preparations and return value...
 * 			return null;
 * 		}
 *
 * 		public Void onActionDoWork(String methodName, Object[] methodArgs, Object tag1, Object tag2, Object[] additionalTags) {
 * 			// actual work like internet access, loading stuff from database, heavy computing...
 * 		   return null;
 * 		}
 *
 * 		public void onActionAfterWork(String methodName, Object[] methodArgs, Object tag1, Object tag2, Object[] additionalTags) {
 * 			// cleanup and user indicator...
 * 		}
 * 	};
 *
 * </pre>
 * <p/>
 * <p/>
 * For providing optional information, use it like this:
 * <p/>
 * <p/>
 * <pre>
 * {@code
 * 	// this action applies to a Button and its OnClickListener. It has method filters and tags.
 * 	AsyncAction<Button, Point, Animation> action = new AsyncAction<Button, Point, Animation>(myButton, OnClickListener.class,
 * 				"setOnClickListener", new String[] { "onClick" }, myPoint, myAnimation, additionalTag1, additionalTag2, identifier) {
 *
 * 		public Object onActionPrepare(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// use tags to start a fancy animation at a point
 * 			// tags usable for this work, tag array can be updated for future work (passed to every other step)
 * 			return null;
 * 		}
 *
 * 		public void onActionDoWork(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// actual work like internet access, loading stuff from database, heavy computing...
 * 			// tags usable for this work, tag array can be updated for future work
 * 		}
 *
 * 		public void onActionAfterWork(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// cleanup and user indicator...
 * 			// tags usable for that...
 * 		}
 * 	};
 *
 * </pre>
 * <p/>
 * <p/>
 * When implementing multiple Java Interfaces, use it like this:
 * <p/>
 * <p/>
 * <pre>
 * {@code
 * 	// this action applies to a Button and its OnClickListener, additionally it implements another generic Interface. It has method filters
 * 	// and tags.
 * 	AsyncAction<Button, Point, Animation> action = new AsyncAction<Button, Point, Animation>(myButton, new Class[] { OnClickListener.class,
 * 				IGenericInterface.class}, "setOnClickListener", new String[] { "onClick" }, myPoint, myAnimation, additionalTag1,
 * 				additionalTag2, identifier) {
 *
 * 		public Object onActionPrepare(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// find out what method was invoked
 * 			if ("onClick".equals(methodName)) {
 * 				// OnClickListener was invoked
 * 			} else {
 * 				// the other interface was invoked
 * 			}
 * 			return null;
 * 		}
 *
 * 		public void onActionDoWork(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// actual work like internet access, loading stuff from database, heavy computing...
 * 			// tags usable for this work, tag array can be updated for future work
 * 		}
 *
 * 		public void onActionAfterWork(String methodName, Object[] methodArgs, Point tag1, Animation tag2, Object[] additionalTags) {
 * 			// cleanup and user indicator...
 * 			// tags usable for that...
 * 		}
 * 	};
 *
 * </pre>
 *
 * @author Alexander Dridiger
 */
public abstract class AsyncAction<ViewT extends View, ResultT, Tag1T, Tag2T> implements ISingleAsyncAction<ViewT, ResultT, Tag1T, Tag2T>,
        InvocationHandler {

    private Tag1T tag1;
    private Tag2T tag2;
    private Object[] tags;
    private List<ActionBinding<ViewT>> bindings;

    private boolean runWorkThread = true;
    private boolean skipWorkThread = false;

    private List<ActionThread> actionThreads = Collections.synchronizedList(new ArrayList<ActionThread>());

    /**
     * Minimal constructor for setting tags.
     *
     * @param tag1           optional tag
     * @param tag2           optional tag
     * @param additionalTags optional tags
     */
    public AsyncAction(Tag1T tag1, Tag2T tag2, Object... additionalTags) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tags = additionalTags;
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Constructor with no optional parameters. Filtering for method names of the
     * implemented {@code actionType} are not
     * used, so that <b>every</b> method is processed. Also no optional tags are used here.
     *
     * @param view          the <b>optional</b> {@link android.view.View} this {@link com.drisoftie.action.async.AsyncAction} is bound
     *                      to; if {@code null},
     *                      {@code regMethodName} is ignored
     * @param actionType    a single Java Interface this {@link com.drisoftie.action.async.AsyncAction} implements
     * @param regMethodName the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie.action
     *                      .async.AsyncAction} to the given {@link android.view.View};
     *                      case
     *                      sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType)
     */
    public AsyncAction(ViewT view, Class<?> actionType, String regMethodName) {
        this(view, actionType, regMethodName, null, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Constructor with no optional parameters. Filtering for method names of the
     * implemented {@code actionType} are not
     * used, so that <b>every</b> method is processed. Also no optional tags are used here.
     *
     * @param views         the <b>optional</b> {@link android.view.View}s this {@link com.drisoftie.action.async.AsyncAction} is bound
     *                      to; if {@code null},
     *                      {@code regMethodName} is ignored
     * @param actionType    a single Java Interface this {@link com.drisoftie.action.async.AsyncAction} implements
     * @param regMethodName the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie.action
     *                      .async.AsyncAction} to the given {@link android.view.View};
     *                      case
     *                      sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType)
     */
    public AsyncAction(ViewT[] views, Class<?> actionType, String regMethodName) {
        this(views, actionType, regMethodName, null, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Constructor with no optional parameters. Filtering for method names of the
     * implemented {@code actionType} are not
     * used, so that <b>every</b> method is processed. Also no optional tags are used here.
     *
     * @param view          the <b>optional</b> {@link android.view.View} this {@link com.drisoftie.action.async.AsyncAction} is bound
     *                      to; if {@code null},
     *                      {@code regMethodName} is ignored
     * @param actionTypes   an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                      implements
     * @param regMethodName the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie.action
     *                      .async.AsyncAction} to the given {@link android.view.View};
     *                      case
     *                      sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                      {@code actionType} to the given {@link android.view.View}
     */
    public AsyncAction(ViewT view, Class<?>[] actionTypes, String regMethodName) {
        this(view, actionTypes, regMethodName, null, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Constructor with no optional parameters. Filtering for method names of the
     * implemented {@code actionType} are not
     * used, so that <b>every</b> method is processed. Also no optional tags are used here.
     *
     * @param views         the <b>optional</b> {@link android.view.View}s this {@link com.drisoftie.action.async.AsyncAction} is bound
     *                      to; if {@code null},
     *                      {@code regMethodName} is ignored
     * @param actionTypes   an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                      implements
     * @param regMethodName the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie.action
     *                      .async.AsyncAction} to the given {@link android.view.View};
     *                      case
     *                      sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                      {@code actionType} to the given {@link android.view.View}
     */
    public AsyncAction(ViewT[] views, Class<?>[] actionTypes, String regMethodName) {
        this(views, actionTypes, regMethodName, null, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} No optional tags, but filtering for method names of the implemented {@code
     * actionType} are used. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param view              the <b>optional</b> {@link android.view.View} this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionTypes       an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                          implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                          {@code actionType} to the given {@link android.view.View}
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     */
    public AsyncAction(ViewT view, Class<?> actionTypes, String regMethodName, String[] actionMethodNames) {
        this(view, actionTypes, regMethodName, actionMethodNames, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} No optional tags, but filtering for method names of the implemented {@code
     * actionType} are used. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param views             the <b>optional</b> {@link android.view.View}s this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionTypes       an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                          implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                          {@code actionType} to the given {@link android.view.View}
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     */
    public AsyncAction(ViewT[] views, Class<?> actionTypes, String regMethodName, String[] actionMethodNames) {
        this(views, actionTypes, regMethodName, actionMethodNames, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} No optional tags, but filtering for method names of the implemented {@code
     * actionType} are used. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param view              the <b>optional</b> {@link android.view.View} this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionTypes       an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                          implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                          {@code actionType} to the given {@link android.view.View}
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     */
    public AsyncAction(ViewT view, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames) {
        this(view, actionTypes, regMethodName, actionMethodNames, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} No optional tags, but filtering for method names of the implemented {@code
     * actionType} are used. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param views             the <b>optional</b> {@link android.view.View}s this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionTypes       an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                          implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                          {@code actionType} to the given {@link android.view.View}
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     */
    public AsyncAction(ViewT[] views, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames) {
        this(views, actionTypes, regMethodName, actionMethodNames, null, null);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Optional tags are used and also filtering for method names of the implemented
     * {@code actionType}. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param view              the <b>optional</b> {@link android.view.View} this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionType        a single Java Interface this {@link com.drisoftie.action.async.AsyncAction} implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType)
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     * @param tag1              optional tag
     * @param tag2              optional tag
     * @param additionalTags    optional tags
     */
    public AsyncAction(ViewT view, Class<?> actionType, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                       Object... additionalTags) {
        this(view, new Class[]{actionType}, regMethodName, actionMethodNames, tag1, tag2, additionalTags);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Optional tags are used and also filtering for method names of the implemented
     * {@code actionType}. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param views             the <b>optional</b> {@link android.view.View}s this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionType        a single Java Interface this {@link com.drisoftie.action.async.AsyncAction} implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType)
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     * @param tag1              optional tag
     * @param tag2              optional tag
     * @param additionalTags    optional tags
     */
    public AsyncAction(ViewT[] views, Class<?> actionType, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                       Object... additionalTags) {
        this(views, new Class[]{actionType}, regMethodName, actionMethodNames, tag1, tag2, additionalTags);
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Optional tags are used and also filtering for method names of the implemented
     * {@code actionType}. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param view              the <b>optional</b> {@link android.view.View} this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionTypes       an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                          implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                          {@code actionType} to the given {@link android.view.View}
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     * @param tag1              optional tag
     * @param tag2              optional tag
     * @param additionalTags    optional tags
     */
    public AsyncAction(ViewT view, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                       Object... additionalTags) {
        List<AsyncAction.ActionBinding<ViewT>> bindings = new ArrayList<>();

        ActionBinding<ViewT> binding = new ActionBinding<>();
        binding.view = view;
        for (Class<?> actionType : actionTypes) {
            binding.registrations.add(new MutableTriple<Class<?>, String, String[]>(actionType, regMethodName, actionMethodNames));
        }

        bindings.add(binding);

        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tags = additionalTags;

        this.bindings = bindings;

        registerAction();
    }

    /**
     * {@link com.drisoftie.action.async.AsyncAction} Optional tags are used and also filtering for method names of the implemented
     * {@code actionType}. Keep in mind,
     * that a method is <b>ignored</b>, if the appropriate method name is not provided inside the {@code actionMethodNames} parameter.
     *
     * @param views             the <b>optional</b> {@link android.view.View}s this {@link com.drisoftie.action.async.AsyncAction} is
     *                          bound to; if {@code null},
     *                          {@code regMethodName} is ignored
     * @param actionTypes       an {@link java.lang.reflect.Array} of Java Interfaces this {@link com.drisoftie.action.async.AsyncAction}
     *                          implements
     * @param regMethodName     the name of the registration {@link java.lang.reflect.Method} used to bind this {@link com.drisoftie
     *                          .action.async.AsyncAction} to the given {@link
     *                          android.view.View}; case
     *                          sensitive; <b>MUST</b> be of the signature regMethodName(ActionType actionType); tries to bind every given
     *                          {@code actionType} to the given {@link android.view.View}
     * @param actionMethodNames can be {@code null}; the names of the {@link java.lang.reflect.Method}s that should be invoked,
     *                          all others will be ignored
     * @param tag1              optional tag
     * @param tag2              optional tag
     * @param additionalTags    optional tags
     */
    public AsyncAction(ViewT[] views, Class<?>[] actionTypes, String regMethodName, String[] actionMethodNames, Tag1T tag1, Tag2T tag2,
                       Object... additionalTags) {
        List<AsyncAction.ActionBinding<ViewT>> bindings = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(views)) {
            for (ViewT view : views) {
                ActionBinding<ViewT> binding = new ActionBinding<>();
                binding.view = view;
                for (Class<?> actionType : actionTypes) {
                    binding.registrations.add(new MutableTriple<Class<?>, String, String[]>(actionType, regMethodName, actionMethodNames));
                }

                bindings.add(binding);
            }
        } else {
            ActionBinding<ViewT> binding = new ActionBinding<>();
            for (Class<?> actionType : actionTypes) {
                binding.registrations.add(new MutableTriple<Class<?>, String, String[]>(actionType, regMethodName, actionMethodNames));
            }
            bindings.add(binding);
        }

        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tags = additionalTags;

        this.bindings = bindings;

        registerAction();
    }

    @Override
    public void registerAction() {
        ClassLoader cl = this.getClass().getClassLoader();

        for (ActionBinding<ViewT> binding : bindings) {
            Class<?>[] actions = new Class<?>[binding.registrations.size()];
            for (int i = 0; i < actions.length; i++) {
                actions[i] = binding.registrations.get(i).getLeft();
            }
            binding.actionHandler = Proxy.newProxyInstance(cl, actions, this);
        }

        for (ActionBinding<ViewT> actionBinding : bindings) {
            if (actionBinding.view != null) {
                for (Triple<Class<?>, String, String[]> reg : actionBinding.registrations) {
                    if (StringUtils.isNotEmpty(reg.getMiddle())) {
                        try {
                            Method regMethod = actionBinding.view.getClass().getMethod(reg.getMiddle(), reg.getLeft());
                            if (regMethod != null) {
                                regMethod.invoke(actionBinding.view, actionBinding.actionHandler);
                            }
                        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                            // if (BuildConfig.DEBUG) {
                            // // Log.v("ActionFramework", e.getClass().getName() + " for " + type.getName());
                            // }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerAction(List<ActionBinding<ViewT>> bindings) {
        ClassLoader cl = this.getClass().getClassLoader();
        for (ActionBinding<ViewT> binding : bindings) {
            Class<?>[] actions = new Class<?>[binding.registrations.size()];
            for (int i = 0; i < actions.length; i++) {
                actions[i] = binding.registrations.get(i).getLeft();
            }
            binding.actionHandler = Proxy.newProxyInstance(cl, actions, this);
        }

        for (ActionBinding<ViewT> actionBinding : bindings) {
            if (actionBinding.view != null) {
                for (Triple<Class<?>, String, String[]> reg : actionBinding.registrations) {
                    if (StringUtils.isNotEmpty(reg.getMiddle())) {
                        try {
                            Method regMethod = actionBinding.view.getClass().getMethod(reg.getMiddle(), reg.getLeft());
                            if (regMethod != null) {
                                regMethod.invoke(actionBinding.view, actionBinding.actionHandler);
                            }
                        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                            // if (BuildConfig.DEBUG) {
                            // // Log.v("ActionFramework", e.getClass().getName() + " for " + type.getName());
                            // }
                        }
                    }
                }
            }
        }
        this.bindings = bindings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <HandlerT> HandlerT getHandlerImpl(Class<HandlerT> clazz) {
        HandlerT result = null;
        for (ActionBinding<ViewT> binding : bindings) {
            if (clazz.isInstance(binding.actionHandler)) {
                result = (HandlerT) binding.actionHandler;
                break;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <HandlerT> HandlerT getHandlerImpl(View view, Class<HandlerT> clazz) {
        HandlerT result = null;
        for (ActionBinding<ViewT> binding : bindings) {
            if (binding.view == view && clazz.isInstance(binding.actionHandler)) {
                result = (HandlerT) binding.actionHandler;
                break;
            }
        }
        return result;
    }

    @Override
    public void replaceViewTargets(ViewT... views) {
        for (int i = 0; i < views.length; i++) {
            bindings.get(i).view = views[i];
        }
    }

    @Override
    public List<ViewT> getViewTargets() {
        List<ViewT> targets = new ArrayList<>();
        for (ActionBinding<ViewT> binding : bindings) {
            if (binding.view != null) {
                targets.add(binding.view);
            }
        }
        return targets;
    }

    @Override
    public synchronized void setRunWorkThread(boolean run) {
        this.runWorkThread = run;
    }

    @Override
    public void skipWorkThreadOnce() {
        skipWorkThread = true;
    }

    @Override
    public void invokeSelf(Object... methodArgs) {
        if (bindings.get(0).actionHandler instanceof IGenericAction) {
            ((IGenericAction) bindings.get(0).actionHandler).invokeAction(methodArgs);
        }
    }

    @Override
    public void invokeSelfInUiThread(Object... methodArgs) {
        for (ActionBinding<ViewT> binding : bindings) {
            if (binding.actionHandler instanceof IGenericAction) {
                Runnable runner = new Runnable() {

                    private Object[] methodArgs;
                    private ActionBinding<ViewT> binding;

                    public Runnable init(Object[] methodArgs, ActionBinding<ViewT> binding) {
                        this.methodArgs = methodArgs;
                        this.binding = binding;
                        return null;
                    }

                    @Override
                    public void run() {
                        ((IGenericAction) binding.actionHandler).invokeAction(methodArgs);
                    }
                }.init(methodArgs, binding);
                if (binding.view != null) {
                    binding.view.post(runner);
                } else {
                    new Handler(Looper.getMainLooper()).post(runner);
                }
                break;
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        String methodName = method.getName();
        for (ActionBinding<ViewT> binding : bindings) {
            if (proxy == binding.actionHandler) {
                if (binding.hasInvokeLimitations) {
                    boolean found = false;
                    for (Triple<Class<?>, String, String[]> reg : binding.registrations) {
                        if (ArrayUtils.isNotEmpty(reg.getRight())) {
                            for (String methodLimit : reg.getRight()) {
                                if (StringUtils.equals(methodName, methodLimit)) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!found) {
                        // if (BuildConfig.DEBUG) {
                        // // Log.e("Proxy Action Library", "No method found: " + methodName);
                        // }
                        return null;
                    }
                }

                result = onActionPrepare(methodName, args, tag1, tag2, tags);
                if (runWorkThread && !skipWorkThread) {
                    IFinishedHandler<ResultT> handler = new IFinishedHandler<ResultT>() {

                        private ViewT v;
                        private String methodName;
                        private Object[] methodArgs;

                        /**
                         * Initializer method to allow a custom constructor workaround.
                         *
                         * @param v the given view
                         * @param methodName the invoked method name
                         * @param methodArgs given method arguments
                         * @return {@code this}
                         */
                        private IFinishedHandler<ResultT> init(ViewT v, String methodName, Object[] methodArgs) {
                            this.v = v;
                            this.methodName = methodName;
                            this.methodArgs = methodArgs;
                            return this;
                        }

                        @Override
                        public void onFinished(ResultT result) {
                            Runnable runner = new Runnable() {
                                private ResultT result;

                                public Runnable init(ResultT result) {
                                    this.result = result;
                                    return this;
                                }

                                @Override
                                public void run() {
                                    AsyncAction.this.onActionAfterWork(methodName, methodArgs, result, tag1, tag2, tags);
                                }
                            }.init(result);
                            if (v != null) {
                                v.post(runner);
                            } else {
                                new Handler(Looper.getMainLooper()).post(runner);
                            }
                        }
                    }.init(binding.view, methodName, args);
                    ActionThread action = new ActionThread(handler, methodName, args);
                    actionThreads.add(action);
                    action.start();
                    break;
                }
            }
        }
        skipWorkThread = false;
        return result;
    }

    @Override
    public void cancelActions() {
        for (ActionThread action : actionThreads) {
            action.setRun(false);
        }
        for (ActionBinding<ViewT> binding : bindings) {
            binding.actionHandler = null;
            binding.view = null;
            binding.actionHandler = null;
            for (Triple<Class<?>, String, String[]> reg : binding.registrations) {
                MutableTriple<Class<?>, String, String[]> triple = (MutableTriple<Class<?>, String, String[]>) reg;
                triple.setRight(null);
            }
            binding.registrations = null;
        }
        bindings = null;
        tag1 = null;
        tag2 = null;
        tags = null;
    }

    /**
     * Thread doing the actual work.
     *
     * @author Alexander Dridiger
     */
    private class ActionThread extends BaseRunThread {

        private IFinishedHandler<ResultT> handler;
        private String methodName;
        private Object[] methodArgs;

        public ActionThread(IFinishedHandler<ResultT> handler, String methodName, Object[] methodArgs) {
            this.handler = handler;
            this.methodName = methodName;
            this.methodArgs = methodArgs;
        }

        @Override
        public void run() {
            // Moves the current Thread into the background
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            ResultT result = null;
            if (isRunning()) {
                result = onActionDoWork(methodName, methodArgs, tag1, tag2, tags);
            }
            if (isRunning()) {
                handler.onFinished(result);
            }
            AsyncAction.this.actionThreads.remove(this);
        }
    }

    /**
     * Helper method that strips arguments from a given {@link java.lang.reflect.Array}. Most useful when working with {@link com
     * .drisoftie.action.async.IGenericAction}s.
     *
     * @param methodArgs arguments array to strip from
     * @return the stripped arguments
     */
    public Object[] stripMethodArgs(Object[] methodArgs) {
        if (methodArgs != null && methodArgs.length > 0 && methodArgs[0] instanceof Object[]) {
            return (Object[]) methodArgs[0];
        }
        return null;
    }

    /**
     * Representing a binding with the corresponding {@link ViewT} (<b>optional</b>), it's action handler implementing the action
     * callback(s). Each registration for the action callback consists of the concrete class, the registration method and a list of
     * <b>optional</b> accepted callback methods, used as a filter what callback methods are accepted.
     *
     * @param <ViewT> generic parameter for the view type to bind an action handler
     */
    protected static class ActionBinding<ViewT> {
        ViewT view;
        Object actionHandler;
        List<Triple<Class<?>, String, String[]>> registrations = new ArrayList<>();
        boolean hasInvokeLimitations = false;

        /**
         * Setter for the view.
         *
         * @param view the targeted view
         * @return {@code this}
         */
        public ActionBinding<ViewT> setView(ViewT view) {
            this.view = view;
            return this;
        }
    }
}