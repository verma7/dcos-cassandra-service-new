/*
 * Copyright 2015 Mesosphere
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mesosphere.dcos.cassandra.common.client;

/**
 * Created by kowens on 2/12/16.
 */
public class ExecutorClientException extends Exception {
    public ExecutorClientException() {
    }

    public ExecutorClientException(Throwable cause) {
        super(cause);
    }

    public ExecutorClientException(String message) {
        super(message);
    }

    public ExecutorClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
