/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

public interface PCFPushUnregisterDeviceListener {
    void onPCFPushUnregisterDeviceSuccess();
    void onPCFPushUnregisterDeviceFailed(String reason);
}
