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

import android.view.View;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder Chain Pattern (also known as Fluent Pattern) implementation to get an {@link AsyncAction}.
 * Facilitates creation of an {@link AsyncAction} in a cascade like way.
 * <p/>
 * Use this class in the following way:
 * <p/>
 * <p/>
 * <pre>
 * {@code
 *  new ActionBuilder<View>().with(view)                      // binding a view
 *      .reg(OnClickListener.class, RegActionMethod.SET_ONCLICK) // register an on click callback on the view
 *      .invokeOnly(ActionMethod.ON_CLICK)                       // allow only the onClick method to be
 *                                                               // invoked
 *      .pack(new AsyncAction<View, Void, Void, Void>()          // pack everything inside a new action
 * }
 * </pre>
 *
 * @author Alexander Dridiger
 */
public class ActionBuilder<ViewT extends View> {

    /**
     * List of all bindings.
     */
    private List<AsyncAction.ActionBinding<ViewT>> bindings = new ArrayList<>();

    /**
     * Building chain starts registration with <b>optional</b> {@link ViewT}s to bind something to. <b>{@code null}</b> passing is
     * allowed for not targeting any views.
     *
     * @param views optional view references
     * @return further build options
     */
    public TargetBuilder with(ViewT... views) {
        BuilderWrapper wrapper = new BuilderWrapper();
        wrapper.currentTargetBuilder = new TargetBuilder(wrapper, views);
        return wrapper.currentTargetBuilder;
    }

    /**
     * Sets next steps for build options. Facilitates cascaded building.
     */
    public class BuilderWrapper {

        /**
         * The current wrapped builder.
         */
        private TargetBuilder currentTargetBuilder;

        /**
         * Further views can be registered with separate bindings.
         *
         * @param views other views
         * @return further build options
         */
        public TargetBuilder and(ViewT... views) {
            currentTargetBuilder = new TargetBuilder(this, views);
            return currentTargetBuilder;
        }

        /**
         * Pack all registered bindings into an action.
         *
         * @param action    the action to pack everything into
         * @param <ResultT> the type of the result
         * @param <Tag1T>   the type of the first tag
         * @param <Tag2T>   the type of the second tag
         * @return the new packed action, ready to use
         */
        public <ResultT, Tag1T, Tag2T> AsyncAction<ViewT, ResultT, Tag1T, Tag2T> pack(AsyncAction<ViewT, ResultT, Tag1T, Tag2T> action) {
            action.registerAction(bindings);
            bindings = null;
            return action;
        }
    }

    /**
     * Builder for registering a target to its bindings. Sets next steps for build options. Facilitates cascaded building.
     */
    public class TargetBuilder {

        /**
         * The initiating builder.
         */
        protected BuilderWrapper builder;
        /**
         * Cached bindings to use when ready.
         */
        protected List<AsyncAction.ActionBinding<ViewT>> subBindings = new ArrayList<>();

        /**
         * Default constructor.
         */
        public TargetBuilder() {
        }

        /**
         * Setting initiating builder and targeted views.
         *
         * @param builder the builder
         * @param targets the view targets
         */
        public TargetBuilder(BuilderWrapper builder, ViewT... targets) {
            this.builder = builder;
            init(targets);
        }

        /**
         * Setter for a builder.
         *
         * @param builder the builder to set
         * @return the builder
         */
        protected TargetBuilder setBuilder(BuilderWrapper builder) {
            this.builder = builder;
            return this;
        }

        /**
         * Initializing necessary bindings for the given views.
         *
         * @param targets targets to bind
         */
        protected void init(ViewT... targets) {
            if (ArrayUtils.isEmpty(targets)) {
                subBindings.add(new AsyncAction.ActionBinding<ViewT>());
            } else {
                for (ViewT target : targets) {
                    subBindings.add(new AsyncAction.ActionBinding<ViewT>().setView(target));
                }
            }
        }

        /**
         * Setting a registration with a handler class by the given method.
         *
         * @param clazz     the class of the handler
         * @param regMethod registration method for the handler
         * @return the next builder in the chain
         */
        public TargetFinishBuilder reg(Class<?> clazz, String regMethod) {
            for (AsyncAction.ActionBinding<ViewT> binding : subBindings) {
                binding.registrations.add(new MutableTriple<Class<?>, String, String[]>(clazz, regMethod, null));
            }
            return new TargetFinishBuilder().setBuilder(builder).setBindings(subBindings);
        }

        /**
         * Helper method for registrations.
         *
         * @param clazz     the class of the handler
         * @param regMethod registration method for the handler
         * @return the next builder in the chain
         */
        public TargetFinishBuilder reg(Class<?> clazz, RegActionMethod regMethod) {
            for (AsyncAction.ActionBinding<ViewT> binding : subBindings) {
                binding.registrations.add(new MutableTriple<Class<?>, String, String[]>(clazz, regMethod.method(), null));
            }
            return new TargetFinishBuilder().setBuilder(builder).setBindings(subBindings);
        }

        /**
         * Setting a registration with multiple handler classes by the given method.
         *
         * @param classes   classes of the handlers
         * @param regMethod registration method for the handler
         * @return the next builder in the chain
         */
        public TargetFinishBuilder reg(Class<?>[] classes, RegActionMethod regMethod) {
            for (AsyncAction.ActionBinding<ViewT> binding : subBindings) {
                for (Class<?> clazz : classes) {
                    binding.registrations.add(new MutableTriple<Class<?>, String, String[]>(clazz, regMethod.method(), null));
                }
            }
            return new TargetFinishBuilder().setBuilder(builder).setBindings(subBindings);
        }

        /**
         * Setting a registration with {@link org.apache.commons.lang3.tuple.Pair}s of multiple handler classes paired with the given
         * methods.
         *
         * @param classRegPairs the pairs
         * @return the next builder in the chain
         */
        public TargetFinishBuilder reg(Pair<Class<?>, RegActionMethod>... classRegPairs) {
            for (AsyncAction.ActionBinding<ViewT> binding : subBindings) {
                for (Pair<Class<?>, RegActionMethod> pair : classRegPairs) {
                    binding.registrations.add(
                            new MutableTriple<Class<?>, String, String[]>(
                                    pair.getLeft(), pair.getRight().method(), null));
                }
            }
            return new TargetFinishBuilder().setBuilder(builder).setBindings(subBindings);
        }
    }

    /**
     * Builder for registering a target to its bindings. Sets next steps for build options. Can be seen as the last step in the chain.
     * Facilitates cascaded building.
     */
    public class TargetFinishBuilder extends TargetBuilder {

        /**
         * Default constructor.
         */
        public TargetFinishBuilder() {
            super();
        }

        /**
         * Second needed constructor.
         */
        public TargetFinishBuilder(BuilderWrapper builder, ViewT... targets) {
            super(builder, targets);
        }

        @Override
        protected TargetFinishBuilder setBuilder(BuilderWrapper builder) {
            this.builder = builder;
            return this;
        }

        /**
         * Setter for bindings.
         *
         * @param bindings the bindings to set
         * @return next builder in the chain
         */
        protected TargetFinishBuilder setBindings(List<AsyncAction.ActionBinding<ViewT>> bindings) {
            subBindings = bindings;
            return this;
        }

        /**
         * Beginning a new registration chain with new target views.
         *
         * @param views new views
         * @return new builder for the targets
         */
        public TargetBuilder and(ViewT... views) {
            builder.currentTargetBuilder = new TargetBuilder(builder, views);
            subBindings = null;
            return builder.currentTargetBuilder;
        }

        /**
         * Filter method to allow only the listed methods to be invoked. Ends the current registration process and give the option to
         * start a new one or pack it all together.
         *
         * @param actionMethods the allowed methods
         * @return next builder in the chain
         */
        public BuilderWrapper invokeOnly(ActionMethod... actionMethods) {
            if (ArrayUtils.isNotEmpty(actionMethods)) {
                String[] methods = new String[actionMethods.length];
                for (int i = 0; i < methods.length; i++) {
                    methods[i] = actionMethods[i].method();
                }
                for (AsyncAction.ActionBinding<ViewT> binding : subBindings) {
                    for (Triple<Class<?>, String, String[]> tuple : binding.registrations) {
                        ((MutableTriple<Class<?>, String, String[]>) tuple).setRight(methods);
                    }
                    binding.hasInvokeLimitations = true;
                }
            }
            bindings.addAll(subBindings);
            subBindings = null;
            return builder;
        }

        /**
         * Pack all registered bindings into an action.
         *
         * @param action    the action to pack everything into
         * @param <ResultT> the type of the result
         * @param <Tag1T>   the type of the first tag
         * @param <Tag2T>   the type of the second tag
         * @return the new packed action, ready to use
         */
        public <ResultT, Tag1T, Tag2T> AsyncAction<ViewT, ResultT, Tag1T, Tag2T> pack(AsyncAction<ViewT, ResultT, Tag1T, Tag2T> action) {
            bindings.addAll(subBindings);
            action.registerAction(bindings);
            bindings = null;
            subBindings = null;
            return action;
        }
    }
}
