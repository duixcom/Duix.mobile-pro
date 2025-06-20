package ai.guiji.duix.sdk.client.pro.demo.audio;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

@SuppressLint("MissingPermission")
public class AudioRecordCore {

    private AudioRecord mRecorder;   //录音器

    private final int sampleRate = 16000;   //音频采样率
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;  // 单通道
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //音频录制格式，默认为PCM16Bit
    private final int bufferSize = 1920;

    AudioRecordListener audioRecordListener;
    boolean mRecording;

    public AudioRecordCore(AudioRecordListener audioRecordListener) {
        this.audioRecordListener = audioRecordListener;
    }

    public void startRecord() {
        if (mRecording) {
            if (mRecorder != null) {
                mRecorder.stop();
            }
        }
        mRecording = true;
        new Thread(() -> {
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, channelConfig,
                    audioFormat, bufferSize);//初始化录音器
            int sessionId = mRecorder.getAudioSessionId();
            AcousticEchoCanceler acousticEchoCanceler = AcousticEchoCanceler.create(sessionId);
            if (acousticEchoCanceler != null){
                int res = acousticEchoCanceler.setEnabled(true);
                if (res != AudioEffect.SUCCESS){
                    Log.e("AudioRecordCore", "AcousticEchoCanceler set failed: " + res);
                }
            }
            NoiseSuppressor noiseSuppressor = NoiseSuppressor.create(sessionId);
            if (noiseSuppressor != null) {
                int res = noiseSuppressor.setEnabled(true);
                if (res != AudioEffect.SUCCESS) {
                    Log.e("AudioRecordCore", "NoiseSuppressor set failed: " + res);
                }
            }
            mRecorder.startRecording();
            audioStep();
        }).start();
    }

    private void audioStep() {
        byte[] data = new byte[bufferSize];
        while (mRecording) {
            int length = mRecorder.read(data, 0, bufferSize);//读入数据
            if (length > 0 && audioRecordListener != null) {
                audioRecordListener.onAudioData(data);
            }
        }
    }

    public void stopRecord() {
        mRecording = false;
        if (mRecorder != null && mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mRecorder.stop();
        }
    }

    public void release() {
        mRecording = false;
        if (mRecorder != null) {
            if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                mRecorder.stop();
            }
            mRecorder.release();
            mRecorder = null;
        }
    }

    public interface AudioRecordListener {

        // 音频数据
        void onAudioData(byte[] buffer);

    }


}
