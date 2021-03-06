<p align="center" >
  <img src="https://avatars2.githubusercontent.com/u/11334935?v=3&s=200" alt="Donky Networks LTD" title="Donky Network SDK">
</p>

# DonkySDK-Android-Modular (V2.7.0.3)

The Android Modular SDK is a kit for adding push notifications and rich content services to your application. For detailed documentation, tutorials and guides, visit our [online documentation](http://docs.mobiledonky.com/docs/set-up-guide).

## Requirements

<ul>
<li>Android 4.0+</li>
</ul>

## Author

<ul>
<li>Donky Networks Ltd, sdk@mobiledonky.com</li>
</ul>

## License

<ul>
<li>Donky-Core-SDK is available under the MIT license. See the LICENSE file for more info.</li>
</ul>

##Support

<ul>
<li>Please contact sdk@mobiledonky.com if you have any issues with integrating or using this SDK.</li>
</ul>

## Donky Core SDK

<ul>
<li>Donky Core SDK (Requried for all implementations of the Donky SDK.)</li>
</ul>

## Modules

<ul>
<li>Core Analytics</li>
<li>Assets</li>
<li>Automation</li>
<li>Sequencing</li>
<li>SignalR</li>
<li>Location</li>
<li>Push</li>
<li>Rich Messaging Logic</li>
<li>Rich Messaging Inbox UI</li>
<li>Common Messaging (Logic + UI)</li>
</ul>

## Core Module dependencies

<ul>
<li>com.google.android.gms:play-services-gcm</li>
<li>com.squareup.retrofit:retrofit (http://square.github.io/retrofit/)</li>
<li>com.squareup.okhttp:okhttp (http://square.github.io/okhttp/)</li>
<li>com.google.code.gson (https://code.google.com/p/google-gson/)</li>
</ul>

# Donky-Core-SDK

Provides the basics of any Donky integration into your apps.  The Core SDK is responsible for handling:

<ul>
<li>Registration onto the Donky Network</li>
<li>Control of User and Device registration details</li>
<li>Sending and receiving content</li>
</ul>

## Source

You can find the source code of Donky Core in [*Donky/core*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/core) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/register-users-android).

Download via Gradle

```shell
    compile 'net.donky:donky-core:2.7.0.3'
```

### Standard Mode 

Initialise this module in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
			
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<",
			new DonkyListener() /*set callback handlers*/);
			
    }
  
}
```

### Authenticated Mode

You can delegate authentication responsibility to another trusted system that can issue a JWT containing the required information. In order to be able to register to Donky Network in a secured way you need to provide a callback that will respond to authentication challenges and provide a valid JWT token.

```java
class Authenticator implements DonkyAuthenticator {

  @Override
  public void onAuthenticationChallenge(DonkyAuthClient authClient,
                                        ChallengeOptions options) {
    
    /**
     * Obtain JWT token based on a given nonce and return the value to
     * authClient instance. At this point the user authentication process will 
     * be able to continue automatically.
     * options.getNonce() for nonce to be included in token
     * options.getForUserId() expected user id when re-authenticating
     */
     authClient.authenticateWithToken("TEST_TOKEN");
   }

}
```

Initialise this module in onCreate method of your application class with an instance of the DonkyAuthenticator class you created.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<",
          new Authenticator() /* extends DonkyAuthenticator */, 
          new DonkyListener() /*set callback handlers*/);
    }
  
}
```

For more details on [authentication](https://dash.readme.io/project/donky/docs/authentication-overview) please visit [documaentation page](http://docs.mobiledonky.com/docs/android-auth)

# Donky-Push

Use the Push module to enable your application to receive Push messages.

## Source

You can find the source code of Donky Core in [*Donky/push*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/push) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/android-push).

Download via Gradle

```shell
    compile 'net.donky:donky-module-push:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
		DonkyPush.initialiseDonkyPush(this, true,
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
			new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-RichMessage-Logic

Use the Rich Message  module to enable your application to receive rich content messages.

## Source

You can find the source code of Donky Rich Logic in [*Donky/richlogic*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/richlogic) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/android-rich).

Download via Gradle

```shell
    compile 'net.donky:donky-module-rich-logic:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyRichLogic.initialiseDonkyRich(this,
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-RichMessage-Inbox-UI

Use the Rich Messaging Inbox module to enable your application to disply rich messaging inbox and rich messages.

## Source

You can find the source code of Donky Rich Inbox UI in [*Donky/richui*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/richui) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/rich-prebuilt-inbox).

Download via Gradle

```shell
    compile 'net.donky:donky-module-rich-ui-inbox:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyRichInboxUI.initialiseDonkyRich(this,
      		new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

Open Inbox
```java
Intent intent = new Intent(this, RichInboxAndMessageActivityWithToolbar.class);
startActivity(intent);
```

# Donky-Analytics

The analytics module provides key metrics around app launches, app session times.

## Source

You can find the source code of Donky Analytics in [*Donky/analytics*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/analytics) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/android-analytics-automation).

Download via Gradle

```shell
    compile 'net.donky:donky-module-analytics:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyAnalytics.initialiseAnalytics(this,
        	new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-Automation

The automation layer adds the ability to invoke server defined behaviours.

## Source

You can find the source code of Donky Automation in [*Donky/automation*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/automation) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/android-analytics-automation).

Download via Gradle

```shell
    compile 'net.donky:donky-module-automation:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyAutomation.initialiseDonkyAutomation(this, 
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-Assets

The automation layer adds the ability to invoke server defined behaviours.

## Source

You can find the source code of Donky Automation in [*Donky/assets*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/assets) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/android-assets).

Download via Gradle

```shell
    compile 'net.donky:donky-module-automation:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyAssets.initialiseDonkyAssets(this, 
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-Location

The location module adds the ability to track device location locally, and communicate the information over the network to other devices or back to Donky for analytics

## Source

You can find the source code of Donky Location in [*Donky/location*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/location) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/android-core-location).

Download via Gradle

```shell
    compile 'net.donky:donky-module-location:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkyLocation.initialiseDonkyLocation(this, 
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-SignalR

Donky SignalR library integrated into your Donky Project will use websocets to syncronise with the Donky Network when the Application is foregrounded.

## Source

You can find the source code of Donky SignalR in [*Donky/signalr*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/signalr) Android Studio project.

You can use 'File->New->Import Module' option to easily include the source code to your project.
This will automatcally add sources for following libraries:
  
- Customised version of [SignalR](https://github.com/SignalR/java-client) Java client library which you can find [here](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/signalr-client-sdk)
  
- Customised version of [SignalR](https://github.com/SignalR/java-client) Android library which you can find [here](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/signalr-client-sdk-android)

Additional maven dependency is 

- [TooTallNate/Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) library.
  
## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide) and for details how to use this module [here](http://docs.mobiledonky.com/docs/set-up-guide).

Initialise this module before initailising Donky Core in onCreate method of your application class.

Add the following to gradle.build dependencies

```shell
    compile 'net.donky:donky-module-signalr:2.7.0.3'
```

You will also need to include for TooTallNate/Java-WebSocket library:

```shell
repositories {
        jcenter()
        maven {
            url 'http://clojars.org/repo'
        }
    }
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
      
        DonkySignalR.initialiseDonkySignalR(this,
        	new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

# Donky-Sequencing

Use of this module allows you to perform multiple calls to some account controller methods without needing to implement call backs or worry about sequencing when changing local and network state.

## Source

You can find the source code of Donky Core in [*Donky/sequencing*](https://github.com/Donky-Network/DonkySDK-Android-Modular/tree/master/src/Donky/sequencing) Android Studio project.
You can use 'File->New->Import Module' option to easily include the source code to your project.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/set-up-guide).

Download via Gradle

```shell
    compile 'net.donky:donky-module-sequencing:2.7.0.3'
```

Initialise this module before initailising Donky Core in onCreate method of your application class.

```java
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
		
        DonkySequencing.initialiseDonkySequencing(this, 
			new DonkyListener() /*set callback handlers*/);
      
		DonkyCore.initialiseDonkySDK(this,">>ENTER API KEY HERE<<", 
        	new DonkyListener() /*set callback handlers*/);
    		
	}
}
```

DonkySequenceAccountController class in this module overides the following methods

```java
public void updateRegistrationDetails(UserDetails , DeviceDetails, DonkySequenceListener)
```

```java
public void updateUserDetails(UserDetails , DonkySequenceListener)
```

```java
public void updateDeviceDetails(DeviceDetails , DonkySequenceListener)
```

```java
public void updateTags(List<TagDescription>, DonkySequenceListener)
```

```java
public void setAdditionalProperties(TreeMap<String, String>, DonkySequenceListener)
```