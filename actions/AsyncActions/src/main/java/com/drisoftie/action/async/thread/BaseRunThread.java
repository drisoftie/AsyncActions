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
package com.drisoftie.action.async.thread;

/**
 * A basic {@link Thread} which has a {@code run} indicator to enable or stop it.
 *
 * @author Alexander Dridiger
 */
public class BaseRunThread extends Thread {

    private boolean run = true;

    /**
     * Asks if the current {@link Thread} is running or not.
     *
     * @return if running
     */
    public boolean isRunning() {
        return run;
    }

    /**
     * Set the current {@link Thread} to remain running or not.
     *
     * @param run indicator
     */
    public void setRun(boolean run) {
        this.run = run;
    }
}
