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

import android.content.Context;
import android.os.Build;
import android.util.Base64;

import com.google.gson.Gson;
import com.pivotal.mss.common.backend.ApiRequestImpl;
import com.pivotal.mss.common.network.NetworkWrapper;
import com.pivotal.mss.common.util.Logger;
import com.pivotal.mss.pushsdk.RegistrationParameters;
import com.pivotal.mss.pushsdk.model.api.BackEndApiRegistrationRequestData;
import com.pivotal.mss.pushsdk.model.api.BackEndApiRegistrationResponseData;
import com.pivotal.mss.pushsdk.util.Const;
import com.pivotal.mss.pushsdk.util.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * API request for registering a device with the Pivotal Mobile Services Suite back-end server.
 */
public class BackEndRegistrationApiRequestImpl extends ApiRequestImpl implements BackEndRegistrationApiRequest {

    private Context context;

    public BackEndRegistrationApiRequestImpl(Context context, NetworkWrapper networkWrapper) {
        super(networkWrapper);
        verifyArguments(context);
        saveArguments(context);
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context) {
        this.context = context;
    }

    @Override
    public void startNewDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        verifyNewRegistrationArguments(gcmDeviceRegistrationId, parameters, listener);
        handleRequest(gcmDeviceRegistrationId, null, parameters, listener, false);
    }

    @Override
    public void startUpdateDeviceRegistration(String gcmDeviceRegistrationId, String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        verifyUpdateRegistrationArguments(gcmDeviceRegistrationId, backEndDeviceRegistrationId, parameters, listener);
        handleRequest(gcmDeviceRegistrationId, backEndDeviceRegistrationId, parameters, listener, true);
    }

    private void verifyNewRegistrationArguments(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        if (gcmDeviceRegistrationId == null) {
            throw new IllegalArgumentException("gcmDeviceRegistrationId may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void verifyUpdateRegistrationArguments(String gcmDeviceRegistrationId, String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        verifyNewRegistrationArguments(gcmDeviceRegistrationId, parameters, listener);
        if (backEndDeviceRegistrationId == null) {
            throw new IllegalArgumentException("backEndDeviceRegistrationId may not be null");
        }
    }

    private void handleRequest(String gcmDeviceRegistrationId, String previousBackEndDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener, boolean isUpdate) {
        OutputStream outputStream = null;
        try {
            final URL url = getURL(isUpdate, previousBackEndDeviceRegistrationId, parameters);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setDoOutput(true); // indicate "POST" request
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod(getRequestMethod(isUpdate));
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", getBasicAuthorizationValue(parameters));
            urlConnection.connect();

            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            final String requestBodyData = getRequestBodyData(gcmDeviceRegistrationId, parameters, isUpdate);
            Logger.v("Making network request to register this device with the back-end server: " + requestBodyData);
            writeOutput(requestBodyData, outputStream);

            final int statusCode = urlConnection.getResponseCode();

            final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            final String responseString = readInput(inputStream);

            urlConnection.disconnect();

            onSuccessfulNetworkRequest(statusCode, responseString, listener);

        } catch (Exception e) {
            Logger.ex("Back-end device registration attempt failed", e);
            listener.onBackEndRegistrationFailed(e.getLocalizedMessage());
        }

        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private URL getURL(boolean isUpdate, String previousBackEndDeviceRegistrationId, RegistrationParameters parameters) throws MalformedURLException {
        if (isUpdate) {
            return new URL(parameters.getBaseServerUrl(), Const.BACKEND_REGISTRATION_REQUEST_ENDPOINT + "/" +  previousBackEndDeviceRegistrationId);
        } else {
            return new URL(parameters.getBaseServerUrl(), Const.BACKEND_REGISTRATION_REQUEST_ENDPOINT);
        }
    }

    private String getRequestMethod(boolean isUpdate) {
        if (isUpdate) {
            return "PUT";
        } else {
            return "POST";
        }
    }

    public void onSuccessfulNetworkRequest(int statusCode, String responseString, final BackEndRegistrationListener listener) {

        if (isFailureStatusCode(statusCode)) {
            Logger.e("Back-end server registration failed: server returned HTTP status " + statusCode);
            listener.onBackEndRegistrationFailed("Back-end server returned HTTP status " + statusCode);
            return;
        }

        if (responseString == null) {
            Logger.e("Back-end server registration failed: server response empty");
            listener.onBackEndRegistrationFailed("Back-end server response empty");
            return;
        }

        final Gson gson = new Gson();
        final BackEndApiRegistrationResponseData responseData;
        try {
            responseData = gson.fromJson(responseString, BackEndApiRegistrationResponseData.class);
            if (responseData == null) {
                throw new Exception("unable to parse server response");
            }
        } catch (Exception e) {
            Logger.e("Back-end server registration failed: " + e.getLocalizedMessage());
            listener.onBackEndRegistrationFailed(e.getLocalizedMessage());
            return;
        }

        final String deviceUuid = responseData.getDeviceUuid();
        if (deviceUuid == null || deviceUuid.isEmpty()) {
            Logger.e("Back-end server registration failed: did not return device_uuid");
            listener.onBackEndRegistrationFailed("Back-end server did not return device_uuid");
            return;
        }

        Util.saveIdToFilesystem(context, deviceUuid, "device_uuid");

        Logger.i("Back-end Server registration succeeded.");
        listener.onBackEndRegistrationSuccess(deviceUuid);
    }

    private String getRequestBodyData(String deviceRegistrationId, RegistrationParameters parameters, boolean isUpdate) {
        final BackEndApiRegistrationRequestData data = getBackEndApiRegistrationRequestData(deviceRegistrationId, parameters, isUpdate);
        final Gson gson = new Gson();
        return gson.toJson(data);
    }

    private BackEndApiRegistrationRequestData getBackEndApiRegistrationRequestData(String deviceRegistrationId, RegistrationParameters parameters, boolean isUpdate) {
        final BackEndApiRegistrationRequestData data = new BackEndApiRegistrationRequestData();
        if (parameters.getDeviceAlias() == null) {
            data.setDeviceAlias("");
        } else {
            data.setDeviceAlias(parameters.getDeviceAlias());
        }
        data.setDeviceModel(Build.MODEL);
        data.setDeviceManufacturer(Build.MANUFACTURER);
        if (!isUpdate) {
            data.setOs("android");
        }
        data.setOsVersion(Build.VERSION.RELEASE);
        data.setRegistrationToken(deviceRegistrationId);
        return data;
    }

    public static String getBasicAuthorizationValue(RegistrationParameters parameters) {
        final String stringToEncode = parameters.getVariantUuid() + ":" + parameters.getVariantSecret();
        return "Basic  " + Base64.encodeToString(stringToEncode.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
    }

    @Override
    public BackEndRegistrationApiRequest copy() {
        return new BackEndRegistrationApiRequestImpl(context, networkWrapper);
    }
}