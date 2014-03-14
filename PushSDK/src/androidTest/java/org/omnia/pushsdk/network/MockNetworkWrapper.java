/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnia.pushsdk.network;

import android.content.Context;

import com.xtreme.network.INetworkRequestLauncher;
import com.xtreme.network.MockNetworkRequestLauncher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockNetworkWrapper implements NetworkWrapper {

    private INetworkRequestLauncher networkRequestLauncher;
    private final boolean isNetworkAvailable;

    public MockNetworkWrapper() {
        isNetworkAvailable = true;
    }

    public MockNetworkWrapper(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
    }

    @Override
    public INetworkRequestLauncher getNetworkRequestLauncher() {
        if (networkRequestLauncher == null) {
            networkRequestLauncher = new MockNetworkRequestLauncher();
        }
        return networkRequestLauncher;
    }

    @Override
    public boolean isNetworkAvailable(Context context) {
        return isNetworkAvailable;
    }

    @Override
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return new MockHttpURLConnection(url);
    }
}
