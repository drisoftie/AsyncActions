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
package com.drisoftie.action.async.thread;

/**
 * A special {@link BaseRunThread} which has a running loop inside it.
 *
 * @author Alexander Dridiger
 */
public abstract class BaseLoopThread extends BaseRunThread {

    /**
     * Does work <b>before</b> the running loop is entered.
     */
    public abstract void runBeforeLoop();

    /**
     * Does work <b>inside</b> the running loop.
     */
    public abstract void runInLoop();

    /**
     * Does work <b>after</b> the loop was exited.
     */
    public abstract void runAfterLoop();

    @Override
    public final void run() {
        runBeforeLoop();
        while (isRunning()) {
            runInLoop();
        }
        runAfterLoop();
    }
}
