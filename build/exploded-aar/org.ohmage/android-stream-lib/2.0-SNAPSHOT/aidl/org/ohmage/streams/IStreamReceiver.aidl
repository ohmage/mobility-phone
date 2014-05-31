/*
 * Copyright (C) 2013 ohmage
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

package org.ohmage.streams;

oneway interface IStreamReceiver
{

    /**
     * Sends a stream point to ohmage to be uploaded.
     *
     * @param       streamId         Id of stream for observer
     * @param       streamVersion    version of stream
     * @param       metadata         metadata for point
     * @param       data             data for point
     */
    void sendStream (in String streamId, in int streamVersion, in String metadata, in String data);
}
