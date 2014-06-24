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

package com.pivotal.mss.pushsdk.backend;

import com.pivotal.mss.pushsdk.RegistrationParameters;

public class FakeBackEndRegistrationApiRequest implements BackEndRegistrationApiRequest {

    private final FakeBackEndRegistrationApiRequest originatingRequest;
    private final String backEndDeviceRegistrationIdFromServer;
    private final boolean willBeSuccessfulRequest;
    private boolean wasRegisterCalled = false;
    private boolean isNewRegistration;
    private boolean isUpdateRegistration;

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationIdFromServer) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationIdFromServer = backEndDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = true;
    }

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationIdFromServer, boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationIdFromServer = backEndDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakeBackEndRegistrationApiRequest(FakeBackEndRegistrationApiRequest originatingRequest, String backEndDeviceRegistrationIdFromServer, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.backEndDeviceRegistrationIdFromServer = backEndDeviceRegistrationIdFromServer;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startNewDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        wasRegisterCalled = true;
        isNewRegistration = true;

        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
            originatingRequest.isNewRegistration = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndRegistrationSuccess(backEndDeviceRegistrationIdFromServer);
        } else {
            listener.onBackEndRegistrationFailed("Fake back-end new registration failed fakely");
        }
    }

    @Override
    public void startUpdateDeviceRegistration(String gcmDeviceRegistrationId, String previousBackEndDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        wasRegisterCalled = true;
        isUpdateRegistration = true;

        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
            originatingRequest.isUpdateRegistration = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndRegistrationSuccess(backEndDeviceRegistrationIdFromServer);
        } else {
            listener.onBackEndRegistrationFailed("Fake back-end update registration failed fakely");
        }
    }

    @Override
    public BackEndRegistrationApiRequest copy() {
        return new FakeBackEndRegistrationApiRequest(this, backEndDeviceRegistrationIdFromServer, willBeSuccessfulRequest);
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }

    public boolean isNewRegistration() {
        return isNewRegistration;
    }

    public boolean isUpdateRegistration() {
        return isUpdateRegistration;
    }
}