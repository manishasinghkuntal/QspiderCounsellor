package com.counsellor.Qspider;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.counsellor.Qspider.helper.APIClient;
import com.counsellor.Qspider.helper.APIInterface;
import com.counsellor.Qspider.home.RecordingData;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.logging.Handler;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

public class TService extends AccessibilityService {
    MediaRecorder recorder;
    File audiofile;
    String name, phonenumber;
    String audio_format;
    public String Audio_Type;
    int audioSource;
    Context context;
    private Handler handler;
    Timer timer;
    Boolean offHook = false, ringing = false;
    Toast toast;
    Boolean isOffHook = false;
    private boolean recordstarted = false;
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    String mobileNumer;
    private CallBr br_call;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (ActivityCompat.checkSelfPermission(this, READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, READ_PHONE_NUMBERS) ==
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            mobileNumer = mPhoneNumber;
            return;
        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

        // if(terminate != null) {
        // stopSelf();
        // }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {


        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");

        super.onDestroy();
    }

    public class CallBr extends BroadcastReceiver {
        Bundle bundle;
        String state;
        String inCall, outCall;
        public boolean wasRinging = false;
        String callType;


        @Override
        public void onReceive(Context context, Intent intent) {
            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/TestRecordingData1");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }
            if (intent.getAction().equals(ACTION_IN)) {

                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        wasRinging = true;
                        Toast.makeText(context, "IN : " + inCall, Toast.LENGTH_LONG).show();
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging == true) {

                            callType = "1";
                            Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();
                            String file_name = "IncomingRecord";
                            try {
                                audiofile = File.createTempFile(file_name, ".mp4", sampleDir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                            recorder = new MediaRecorder();
//                          recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            recorder.setOutputFile(audiofile.getAbsolutePath());

                            try {
                                recorder.prepare();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            recorder.start();
                            recordstarted = true;
                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        Toast.makeText(context, "REJECT || DISCO", Toast.LENGTH_LONG).show();

                        if (recordstarted) {
                            recorder.stop();

                            File audioVoice = new File(audiofile.getAbsolutePath());
                            loadRecording(context, audioVoice, "7000366872", callType);


                            recordstarted = false;
                        }
                    }
                }
            } else if (intent.getAction().equals(ACTION_OUT)) {
                callType = "1";

                //   if ((bundle = intent.getExtras()) != null) {
                //   outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                // Toast.makeText(context, "OUT : " + "700", Toast.LENGTH_LONG).show();
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
                //  state = bundle.getString(TelephonyManager.EXTRA_STATE);
                Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();
                String file_name = "OutGoingRecord";
                try {
                    audiofile = File.createTempFile(file_name, ".mp4", sampleDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                recorder = new MediaRecorder();
//                          recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setOutputFile(audiofile.getAbsolutePath());

                try {
                    recorder.prepare();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recorder.start();
                recordstarted = true;

                //  }
            }
        }

        public void loadRecording(Context context, File file, String number, String callType) {

//            CallRecordDatabase callRecordDatabase = CallRecordDatabase.getInstance(context);
//            callRecordDatabase.callRecordDao();
            //   File file = new File(recordings.get(i).getAudio());
            String createdDate = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss").format(new Date());

            RequestBody reqFile = RequestBody.create(MediaType.parse("audio/mp4"),
                    file);
            // MultipartBody.Part is used to send also the actual file name
            RequestBody requestNumber =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), "7000366872");
            // add another part within the multipart request
            RequestBody requestTime =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), createdDate);
            // add another part within the multipart request
            RequestBody requestDuration =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), "2:15");

            RequestBody requestCallType =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), callType);
            // add another part within the multipart request
            RequestBody requestMainStatus =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), "2");
            // add another part within the multipart request
            RequestBody requestSubStatus =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), "32");

            RequestBody requestComment =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), "comment");
            APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
            Call<RecordingData> call = apiInterface.PostRecording(requestNumber,
                    requestTime, requestDuration, requestCallType, requestMainStatus, requestSubStatus, requestComment, reqFile);
            call.enqueue(new Callback<RecordingData>() {
                @Override
                public void onResponse(Call<RecordingData> call, Response<RecordingData> response) {
                    Log.e("RecordingData", response.code() + "" + "Msg:" + response.body().getAudio());
                    Log.e("RecordingData", "Msg: " + new Gson().toJson(response.body()));
                    if (response.isSuccessful() || response.code() == 201) {
                        //  CallRecordModel callRecordModel = new CallRecordModel(0, "9988998899", "1:25", "2:45", "0", "1", "2", "comment", "0");


                    }


                    //finally we are setting the list to our MutableLiveData
                }

                @Override
                public void onFailure(Call<RecordingData> call, Throwable t) {
                    Log.e("RecordingsError", "Recordings error:" + t.getMessage().toString());

                }
            });



            /*
             * The method is doing nothing but only generating
             * a simple notification
             * If you are confused about it
             * you should check the Android Notification Tutorial
             * */
        }

    }
}
