# Duix Mobile Pro

This document explains how to use the Duix Mobile Pro SDK provided by the Silicon-based Digital Human Service, including installation, key interfaces, and code examples.

## 1. Adding Dependencies
Introduce the aar package: `duix_client_sdk_pro_release_${version}.aar`  
Create a `libs` directory in the app folder and place the aar package inside.  
Add the following configuration to `build.gradle`:

```
dependencies {
    implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')   // Load aar packages from the libs folder

    implementation 'com.auth0:java-jwt:3.18.1'                  // (Required), signature tool
    implementation 'org.java-websocket:Java-WebSocket:1.5.1'    // (Optional), required for LiveKit interaction mode
}
```

## 2. Proguard Obfuscation Configuration

Add the following configuration to the project's `proguard-rules.pro` file:

```
-keep class ai.guiji.duix.DuixNcnn {*; }
```

## 3. Quick Start
Integrate the digital human module code in the Activity. If microphone usage is required, ensure microphone permissions are dynamically obtained before startup.

```kotlin
class PlayActivity : BaseActivity() {

    companion object {
        const val GL_CONTEXT_VERSION = 2
    }

    private lateinit var modelPath: String

    private lateinit var binding: ActivityPlayBinding
    private var player: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...

        val renderer =
            DUIXRenderer(binding.glTextureView)
        binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
        binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.glTextureView.isOpaque = false           // Transparent
        binding.glTextureView.setRenderer(renderer)
        binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // Must be called after setting the Renderer

        player = Player(mContext, renderer, playerCallback)
        player?.init(modelPath)

        binding.btnPlayAudio.setOnClickListener {
            val thread = Thread {
                player?.startPush()
                val inputStream = assets.open("pcm/3.pcm")
                val buffer = ByteArray(1280)
                while (inputStream.read(buffer) != -1){
                    val data = buffer.clone()
                    player?.pushPcm(data)
                }
                player?.stopPush()
            }
            thread.start()
        }
        binding.btnMotion.setOnClickListener {
            player?.requireMotion("Greeting")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    private val playerCallback = object : Player.Callback {
        override fun onInitSuccess(modelInfo: String) {
            runOnUiThread {
                Log.e(TAG, "onInitSuccess modelInfo: $modelInfo")
                Toast.makeText(mContext, "Init success", Toast.LENGTH_SHORT).show()
                binding.btnPlayAudio.isEnabled = true
                binding.btnMotion.isEnabled = true
            }
        }

        override fun onInitError(code: Int, subCode: Int, msg: String?) {
            runOnUiThread {
                Toast.makeText(mContext, "Init error $code $subCode, $msg", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        override fun onPlayStart() {
            Log.e(TAG, "onPlayStart")
        }

        override fun onPlayEnd() {
            Log.e(TAG, "onPlayEnd")
        }

        override fun onPlayError(code: Int, msg: String?) {
            Log.e(TAG, "onPlayError code: $code msg: $msg")
        }

        override fun onMotionStart(name: String?) {
            Log.e(TAG, "onMotionStart name: $name")
        }

        override fun onMotionComplete(name: String?) {
            Log.e(TAG, "onMotionComplete name: $name")
        }
    }
}
```

## 4. SDK Rendering Function Calls and API Description

### 4.1. Initializing the Service

`init` interface definition:  
`ai.guiji.duix.sdk.client.pro.VirtualFactory`

Initialize the module using the `appId` and `appSecret` provided in the platform account.

```
/**
 * Initialization using the default working path (/sdcard/Android/data/package_name/files/duix/)
 */
int init(Context context, String appId, String appKey);

/**
 * Custom working path initialization
 */
int init(String appId, String appKey, String workPath)
```

Parameter description:

| Parameter   | Type      | Description               |
|------------|----------|--------------------------|
| context    | Context  | Non-null, App context     |
| appId      | String   | Non-null, platform-generated appId |
| appKey     | String   | Non-null, platform-generated appKey |
| workPath   | String   | Custom working path       |

Return parameter description:

| Value  | Description          |  
|-------|---------------------|
| 0     | Initialization successful |
| -1000 | Failed to set file path |
| -1001 | Context is null      |
| -1002 | appId is null       |
| -1003 | appKey is null      |

Reference demo App instance:  
`ai.guiji.duix.sdk.client.pro.demo.ui.activity.MainActivity`  
For debugging convenience, the demo sets `appId` in `MainActivity`. In practice, SDK initialization can also be performed in `Application`.

```kotlin
VirtualFactory.init(mContext, mAppId, mAppKey)
```

### 4.2. Model and Configuration File Download
Function definition:  
`ai.guiji.duix.sdk.client.pro.VirtualModelUtil`

#### (4.2.1) Basic Configuration File Check

Function definition:  
`ai.guiji.duix.sdk.client.pro.VirtualModelUtil`

```
/**
 * Returns whether the basic configuration download is complete
 */
boolean checkBaseConfig();
```

#### (4.2.2) Model File Check

Function definition:  
`ai.guiji.duix.sdk.client.pro.VirtualModelUtil`

```
/**
 * Returns whether the model file download is complete
 */
boolean checkModel(String name)
```

Parameter description:

| Parameter | Type     | Description                          |
|----------|--------|-------------------------------------|
| name     | String | Download URL or name of the model file |

#### (4.2.3) Basic Configuration File Download

Function definition:  
`ai.guiji.duix.sdk.client.pro.VirtualModelUtil`

```
/**
 * Starts the basic configuration file download
 */
void baseConfigDownload(Context context, String url, ModelDownloadCallback callback)
```

Parameter description:

| Parameter   | Type                         | Description          |
|------------|----------------------------|---------------------|
| context    | Context                    | App context         |
| url        | String                     | Download URL of the configuration file |
| callback   | ModelDownloadCallback      | Callback interface for configuration download |

`ModelDownloadCallback` interface definition:  
`ai.guiji.duix.sdk.client.pro.callback.ModelDownloadCallback`

```
/**
 * Download completion event
 * @param url Download URL
 * @param dir Save path of the model folder
 */
void onDownloadComplete(String url, File dir)

/**
 * Download failure event
 * @param url Download URL
 * @param code Error code
 * @param msg Error message
 */
void onDownloadFail(String url, int code, String msg)

/**
 * Download progress
 * @param url Download URL
 * @param current Current download progress in bytes
 * @param total Total size of the zip file to download
 */
void onDownloadProgress(String url, long current, long total)

/**
 * Zip file extraction progress
 * @param url Download URL
 * @param Current extraction progress in bytes
 * @param total Total size of the zip file to extract
 */
void onUnzipProgress(String url, long current, long total)
```

#### (4.2.4) Model File Download

Function definition:  
`ai.guiji.duix.sdk.client.pro.VirtualModelUtil`

```
/**
 * Starts the model file download
 */
void modelDownload(Context context, String modelUrl, ModelDownloadCallback callback)
```

Parameter description:

| Parameter   | Type                         | Description          |
|------------|----------------------------|---------------------|
| context    | Context                    | App context         |
| modelUrl   | String                     | Download URL of the model file |
| callback   | ModelDownloadCallback      | Callback interface for model download |

### 4.3. Creating a Session and Starting the Digital Human

Function definition:  
`ai.guiji.duix.sdk.client.pro.Player`

```
/**
 * @param sink Rendering control interface
 * @param callback Event callback
 */
Player(Context context, RenderSink sink, Callback callback) 

void init(String modelUrl)
```

`Callback` definition:
```
public interface Callback {

        void onInitSuccess(String modelInfo);           // Initialization complete

        void onInitError(int code, int subCode, String msg);    // Initialization error

        void onPlayStart();                             // Audio playback started

        void onPlayEnd();                               // Audio playback completed

        void onPlayError(int code, String msg);         // Playback error

        void onMotionStart(String name);                // Motion playback started

        void onMotionComplete(String name);             // Motion playback completed
       
        /**
        * Frame rendering calculation statistics
        * @param resultCode Whether frame data is returned normally (<0 indicates abnormal frame data)
        * @param isLip      Whether lip-sync is calculated
        * @param useTime    Time consumed for frame calculation
        */
        default void onRenderReport(int resultCode, boolean isLip, long useTime){}  
    }
```

**Callbacks are issued in the rendering thread. Avoid time-consuming operations in callbacks.**

`onInitError` `code` values:

| Value  | Description       |
|-------|------------------|
| -1000 | Authorization error |
| -1001 | Model loading error |

Call example:
```
val renderer =
            DUIXRenderer(binding.glTextureView)
binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
binding.glTextureView.isOpaque = false           // Transparent
binding.glTextureView.setRenderer(renderer)
binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // Must be called after setting the Renderer
player = Player(mContext, renderer, playerCallback)
player?.init(modelPath)
```

### 4.4. Playing Audio Streams

Requires PCM data with a 16k sample rate, single channel, and 16-bit depth. After calling `startPush()`, continuously call `pushPcm(data)` to push data. Avoid excessively long pushes. Call `stopPush()` to end the push after a speech segment is completed.

Function definition:  
`ai.guiji.duix.sdk.client.pro.Player`

```
public void startPush()

public void pushPcm(byte[] buffer)

public void stopPush()
```

Call example:
```
val thread = Thread {
                player?.startPush()
                val inputStream = assets.open("pcm/3.pcm")
                val buffer = ByteArray(1280)
                while (inputStream.read(buffer) != -1){
                    val data = buffer.clone()
                    player?.pushPcm(data)
                }
                player?.stopPush()
            }
thread.start()
```

### 4.5. Digital Human Motion Playback

Motion playback requires the model to support motion tags. Model information can be viewed in the initialization success callback.

Function definition:  
`ai.guiji.duix.sdk.client.pro.Player`

```
/**
     * 
     * @param name Motion tag name
     * @param now Whether to play immediately.
     *            true: Interrupts all current motions and plays the motion immediately;
     *            false: Adds the motion to the playback queue, playing it at the next suitable point during a silent interval. This mode may require longer waiting times but ensures smoother transitions.
     */
public void requireMotion(String name, boolean now)
```

Call example:

```
player?.requireMotion("Greeting")
```

### 4.6. Volume Control

Volume control range: 0.0F~1.0F, where 0.0F is mute and 1.0F is maximum volume.

Function definition:  
`ai.guiji.duix.sdk.client.pro.Player`

```
public void setVolume(float volume)
```

Call example:

```
player?.setVolume(1.0F)
```

## 5. LiveKit Solution Call Instructions

The LiveKit solution integrates front-end rendering, back-end recognition, large-model Q&A, audio streaming, and other complete processes.

### 5.1. Initializing the Service (Same as SDK Standard Initialization)

### 5.2. Model and Configuration File Download (Same as SDK Standard Initialization)

### 5.3. Creating a Session and Starting the Digital Human

Function definition:  
`ai.guiji.duix.sdk.client.pro.Player`

```
/**
 * @param sink Rendering control interface
 * @param callback Event callback
 */
Player(Context context, RenderSink sink, Callback callback) 

/**
 * @param conversationId Backend session ID
 * @param modelUrl Model download URL or name of an already downloaded model
 */
void init(String conversationId, String modelUrl)
```

`Callback` definition:
```
public interface Callback {

        void onInitSuccess(String modelInfo);           // Initialization complete

        void onInitError(int code, int subCode, String msg);    // Initialization error

        void onPlayStart();                             // Audio playback started

        void onPlayEnd();                               // Audio playback completed

        void onPlayError(int code, String msg);         // Playback error

        void onMotionStart(String name);                // Motion playback started

        void onMotionComplete(String name);             // Motion playback completed

        // LiveKit callbacks
        default void onSpeakStart(){}                   // LiveKit starts pushing audio data
        default void onSpeakEnd(){}                     // LiveKit audio push completed
        default void onSpeakBuffer(ByteBuffer buffer){} // LiveKit pushes real-time PCM data
        default void onSpeakText(String text){}         // LiveKit pushes text corresponding to the audio
        default void onMotion(String motion){}          // LiveKit requests motion playback
        default void onAsrResult(String content, boolean end){}     // ASR recognition result of uploaded audio
        default void onLiveKitConnectError(int code, String msg){}  // LiveKit connection error
    }
```

`onInitError` `code` values:

| Value  | Description       |
|-------|------------------|
| -1000 | Authorization error |
| -1001 | Model loading error |

Call example:
```
val renderer =
            DUIXRenderer(binding.glTextureView)
binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
binding.glTextureView.isOpaque = false           // Transparent
binding.glTextureView.setRenderer(renderer)
binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // Must be called after setting the Renderer
player = Player(mContext, renderer, playerCallback)
player?.init(conversationId, modelPath)
```

### 5.4. Playing Audio Streams

Call the `startPush` function in the `onSpeakStart` callback to trigger audio playback.

```
override fun onSpeakStart() {
            runOnUiThread {
                player?.startPush()
            }
        }
```

Call the `pushPcm` function in the `onSpeakBuffer` callback to push PCM data to the player.

```
 override fun onSpeakBuffer(buffer: ByteBuffer) {
            runOnUiThread {
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                player?.pushPcm(data)
            }
        }
```

Call the `stopPush` function in the `onSpeakEnd` callback to signal the end of the push.

```
override fun onSpeakEnd() {
            runOnUiThread {
                player?.stopPush()
            }
        }
```

### 5.5. Digital Human Motion Playback

Call the `requireMotion` function in the `onMotion` callback to trigger motion playback.

```
override fun onMotion(motion: String) {
            runOnUiThread {
                player?.requireMotion(motion)
            }
        }
```

### 5.6. Backend Audio Text Echo

Display the text corresponding to the audio in the `onSpeakText` callback.

```
override fun onSpeakText(text: String) {
            runOnUiThread {
                // Display text content here
            }
        }
```

### 5.7. Speech Recognition Results

Display speech recognition results in the `onAsrResult` callback.

```
override fun onAsrResult(content: String, end: Boolean) {
            runOnUiThread {
                // Display speech recognition results here
            }
        }
```

### 5.8. Backend Connection Errors

The `onLiveKitConnectError` callback is triggered when a backend connection error occurs.

```
override fun onLiveKitConnectError(code: Int, msg: String?) {
            
        }
```

## 6. Notes

1. The pushed audio data format must be 16k sample rate, single channel, and 16-bit depth.
```