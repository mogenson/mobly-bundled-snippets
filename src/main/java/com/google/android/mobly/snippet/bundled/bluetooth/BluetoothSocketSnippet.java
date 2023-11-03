/*
 * Copyright (C) 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.mobly.snippet.bundled.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.util.Base64;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.mobly.snippet.Snippet;
import com.google.android.mobly.snippet.event.EventCache;
import com.google.android.mobly.snippet.rpc.Rpc;
import com.google.android.mobly.snippet.rpc.RpcMinSdk;
import com.google.android.mobly.snippet.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * Snippet class exposing Android APIs in BluetoothSocket.
 */
public class BluetoothSocketSnippet implements Snippet {

    private static class BluetoothSocketSnippetException extends Exception {

        private static final long serialVersionUID = 1;

        public BluetoothSocketSnippetException(String msg) {
            super(msg);
        }
    }

    private final Context context;
    private final EventCache eventCache;

    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public BluetoothSocketSnippet() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        eventCache = EventCache.getInstance();
    }

    @Rpc(description = "Create a client RFCOMM Bluetooth socket and connect to a server socket.")
    public void btCreateRfcommClientSocket(String deviceAddress, String uuid)
        throws JSONException, IOException {
        BluetoothDevice remoteDevice =
            BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

        bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(
            UUID.fromString(uuid));
        bluetoothSocket.connect();
    }

    @Rpc(description = "Write data to a Bluetooth socket.")
    public void btWriteSocket(String data)
        throws BluetoothSocketSnippet.BluetoothSocketSnippetException, IOException {
        if (bluetoothSocket == null) {
            throw new BluetoothSocketSnippet.BluetoothSocketSnippetException(
                "Bluetooth socket is not initialized.");
        }
        if (outputStream == null) {
            outputStream = bluetoothSocket.getOutputStream();
        }
        outputStream.write(Base64.decode(data, Base64.NO_WRAP));
        outputStream.flush();
    }

    @Override
    public void shutdown() throws IOException {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            bluetoothSocket.close();
        }
    }
}
