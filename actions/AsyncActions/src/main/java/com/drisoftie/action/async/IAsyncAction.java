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

import java.util.List;

/**
 * A generic action interface.
 *
 * @author Alexander Dridiger
 */
public interface IAsyncAction<ViewT> {

    /**
     * Cancels all running actions.
     */
    void cancelActions();

    /**
     * Returns the {@link ViewT} targets this action is bound to.
     *
     * @return all targets
     */
    List<ViewT> getViewTargets();
}