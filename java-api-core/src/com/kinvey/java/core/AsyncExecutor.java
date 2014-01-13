/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java.core;

/**
 * This interface defines an async implementation that can be notified.  As async execution occurs in a background thread,
 * this interface provides a mechanism for notifying the calling thread.
 *
 * @author edwardf
 */
public interface AsyncExecutor<T> {

    /**
     * An event has occured in a background thread that needs to be delegated to the calling thread.
     *
     * @param object the message
     */
    public void notify(T object);



}
