/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package donky.microsoft.aspnet.signalr.client.http.android;

import android.os.Build;
import donky.microsoft.aspnet.signalr.client.Logger;
import donky.microsoft.aspnet.signalr.client.Platform;
import donky.microsoft.aspnet.signalr.client.PlatformComponent;
import donky.microsoft.aspnet.signalr.client.http.HttpConnection;

public class AndroidPlatformComponent implements PlatformComponent {

    @Override
    public HttpConnection createHttpConnection(Logger logger) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            return new AndroidHttpConnection(logger);
        } else {
            return Platform.createDefaultHttpConnection(logger);
        }
    }

    @Override
    public String getOSName() {
        return "android";
    }

}
