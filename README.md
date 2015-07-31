<p align="center" >
  <img src="https://avatars2.githubusercontent.com/u/11334935?v=3&s=200" alt="Donky Networks LTD" title="Donky Network SDK">
</p>

# DonkySDK-Android-Modular

The modular SDK exposes all of the network functionality in a way that means developers can consume only the pieces they need in order to:
<ul>
<li>Send custom notifications</li>
<li>Use Donky's messaging features</li>
<li>Automate actions and messaging</li>
<li>Track in app analytics</li>
<li>Build complex M2M, A2P and P2P applications</li>
</ul>

Using Donky as a data network allows developers to focus on writing the apps code rather than having to worry about building a reliable and secure network. The complexities of transferring data across the internet are taken care of, allowing developers to just build their application.

## Requirements

<ul>
<li>Android 4.0+</li>
</ul>

## Author

Donky Networks Ltd, sdk@mobiledonky.com

## License

Donky-Core-SDK is available under the MIT license. See the LICENSE file for more info.

## Donky Core SDK

<ul>
<li>Donky Core SDK (Requried for all implementations of the Donky SDK.</li>
</ul>

## Modules

<ul>
<li>Simple Push Module (Logic + UI)</li>
<li>Rich Messaging Modules (Logic + Inbox + Pop-Up)</li>
<li>Common Messaging Module (Logic + UI)</li>
<li>Core Analytics Module</li>
<li>Automation Module</li>
</ul>

## Third Party Dependencies

<ul>
<li>com.android.support:appcompat-v7:22.0.0</li>
<li>com.google.android.gms:play-services-gcm</li>
<li>com.squareup.retrofit:retrofit (http://square.github.io/retrofit/)</li>
<li>com.squareup.okhttp:okhttp (http://square.github.io/okhttp/)</li>
<li>com.google.code.gson (https://code.google.com/p/google-gson/)</li>
</ul>

# Donky-Core-SDK

Provides the basics of any Donkyintegration into your apps.  The Core SDK is responsible for handling:

<ul>
<li>Registration onto the Donky Network</li>
<li>Control of User and Device registration details</li>
<li>Sending and receiving content</li>
</ul>

## Usage

Only add this to your Project if this is the only part of the SDK you are going to use. 

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancy (jCenter repo)

```shell
    compile 'net.donky:donky-core:2.2.0.0'
```

# Donky-SimplePush-Logic

Use the Simple Push module to enable your application to receive Simple Push messages.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancies (jCenter repo)

```shell
    compile 'net.donky:donky-module-push-logic:2.2.0.0'
```

# Donky-SimplePush-UI

Use the Simple Push module to enable your application to display Simple Push messages.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancies (jCenter repo)

```shell
    compile 'net.donky:donky-module-push-ui:2.2.0.0'
```

# Donky-RichMessage-Logic

Use the Rich Message  module to enable your application to receive rich content messages.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancy (jCenter repo)

```shell
    compile 'net.donky:donky-module-rich-logic:2.2.0.0'
```

# Donky-RichMessage-Inbox

Use the Rich Messaging Inbox module to enable your application to disply rich messaging inbox and rich messages.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancy (jCenter repo)

```shell
    compile 'net.donky:donky-module-rich-ui-inbox:2.2.0.0'
```

# Donky-RichMessage-PopUp

Use the Rich Message module to enable your application to disply rich content messages.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancy (jCenter repo)

```shell
    compile 'net.donky:donky-module-rich-ui-popup:2.2.0.0'
```

# Donky-Analytics

The analytics module provides key metrics around app launches, app session times.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancy (jCenter repo)

```shell
    compile 'net.donky:donky-module-analytics:2.2.0.0'
```

# Donky-Automation

The automation layer adds the ability to invoke server defined behaviours.

## Usage

To read more about how to get started please see [here](http://docs.mobiledonky.com/docs/start-here).

If you are using Gradle just add the following dependancy (jCenter repo)

```shell
    compile 'net.donky:donky-module-automation:2.2.0.0'
```