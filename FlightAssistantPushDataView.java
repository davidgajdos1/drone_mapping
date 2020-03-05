package com.dji.sdk.sample.demo.flightcontroller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.BaseCameraView;
import com.dji.sdk.sample.internal.view.BaseThreeBtnView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.ObstacleDetectionSector;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.flightcontroller.VisionSensorPosition;
import dji.common.util.CommonCallbacks;
import dji.internal.view.VideoSurfaceView;
import dji.midware.data.model.P3.DataEyeGetPushEasySelfCalibration;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;


/**
 * Class that retrieves the push data for Intelligent Flight Assistant
 */
public class FlightAssistantPushDataView extends BaseThreeBtnView {

    public FlightAssistantPushDataView(Context context) {
        super(context);
    }
    public static final int STATUS_CODE = 0;
    Handler handler = new Handler(Looper.getMainLooper());
    TextureView mVideoSurface = (TextureView) findViewById(R.id.texture_video_previewer_surface);
    public StringBuilder stringBuilderTail;
    public StringBuilder stringBuilderNose;
    public Boolean isSending = false;
    public Socket[] sock;
    public Thread thread;
    public String IP = "192.168.1.22";


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Button button = (Button)findViewById(R.id.btn_middle);
        button.setOnClickListener(this);
        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            FlightController flightController =
                ((Aircraft) DJISampleApplication.getProductInstance()).getFlightController();


            FlightAssistant intelligentFlightAssistant = flightController.getFlightAssistant();
            if (intelligentFlightAssistant != null) {
                intelligentFlightAssistant.setVisionDetectionStateUpdatedCallback(new VisionDetectionState.Callback() {
                    @Override
                    public void onUpdate(@NonNull VisionDetectionState visionDetectionState) {

                        ObstacleDetectionSector[] visionDetectionSectorArray =
                                visionDetectionState.getDetectionSectors();

                        VisionSensorPosition sensorPosition =
                                visionDetectionState.getPosition();
                        if (sensorPosition.toString().equals("NOSE"))
                        {
                            stringBuilderNose = new StringBuilder();
                            float heading = flightController.getCompass().getHeading();
                            stringBuilderNose.append("Drone heading: ")
                                    .append(heading)
                                    .append("\n");

                            stringBuilderNose.append("Sensor position and status: ")
                                    .append(sensorPosition)
                                    .append(" , ")
                                    .append(visionDetectionState.isSensorBeingUsed())
                                    .append("\n")
                                    .append("Number of sectors:")
                                    .append(visionDetectionSectorArray.length)
                                    .append("\n");

                            for (int i = 0; i < visionDetectionSectorArray.length; i++) {
                                stringBuilderNose.append("Obstacle distance from sector " + i + " : ")
                                        .append(visionDetectionSectorArray[i].getObstacleDistanceInMeters())
                                        .append("\n");
                                visionDetectionSectorArray[i].getWarningLevel();
                            }
                        /*
                        for (ObstacleDetectionSector visionDetectionSector : visionDetectionSectorArray) {

                            visionDetectionSector.getObstacleDistanceInMeters();
                            visionDetectionSector.getWarningLevel();
                            stringBuilder.append("Obstacle distance: ")
                                        .append(visionDetectionSector.getObstacleDistanceInMeters())
                                        .append("\n");
                            stringBuilder.append("Distance warning: ")
                                        .append(visionDetectionSector.getWarningLevel())
                                        .append("\n");
                        }


                        /*
                        stringBuilder.append("WarningLevel: ")
                                    .append(visionDetectionState.getSystemWarning().name())
                                    .append("\n");
                        stringBuilder.append("Sensor state: ")
                                    .append(visionDetectionState.isSensorBeingUsed())
                                    .append("\n");
                        */
                            changeDescription(stringBuilderNose.toString());
                        }
                        else if (sensorPosition.toString().equals("TAIL"))
                        {
                        stringBuilderTail = new StringBuilder();
                        float heading = flightController.getCompass().getHeading();
                        stringBuilderTail.append("Drone heading: ")
                                .append(heading)
                                .append("\n");

                        stringBuilderTail.append("Sensor position and status: ")
                                .append(sensorPosition)
                                .append("\n")
                                .append(visionDetectionState.isSensorBeingUsed())
                                .append("\n")
                                .append("Number of sectors:")
                                .append(visionDetectionSectorArray.length)
                                .append("\n");

                        for (int i = 0; i < visionDetectionSectorArray.length; i++) {
                            stringBuilderTail.append("Obstacle distance from sector " + i + " : ")
                                    .append(visionDetectionSectorArray[i].getObstacleDistanceInMeters())
                                    .append("\n");
                            visionDetectionSectorArray[i].getWarningLevel();
                        }
                            //changeDescription(stringBuilderTail.toString());

                        }
                    }

                });
            }
        } else {
            Log.i(DJISampleApplication.TAG, "onAttachedToWindow FC NOT Available");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            FlightAssistant intelligentFlightAssistant = ((Aircraft) DJISampleApplication.getProductInstance()).getFlightController().getFlightAssistant();
            if(intelligentFlightAssistant != null) {
                intelligentFlightAssistant.setVisionDetectionStateUpdatedCallback(null);
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.btn_middle:
            {
                isSending = !isSending;
                if(isSending) startSending();
                break;
            }
        }
    }

    @Override
    protected int getDescriptionResourceId() {
        return R.string.intelligent_flight_assistant_description;
    }

    @Override
    protected void handleRightBtnClick() {

    }

    @Override
    protected void handleMiddleBtnClick() {

    }

    @Override
    protected int getMiddleBtnTextResourceId() {
        //return DISABLE;
        return R.string.fetch_media_view_fetch_thumbnail;

    }

    @Override
    protected int getRightBtnTextResourceId() {
        return DISABLE;
    }

    @Override
    protected int getLeftBtnTextResourceId() {
        return DISABLE;
    }

    @Override
    protected void handleLeftBtnClick() {
    }

    @Override
    public int getDescription() {
        return R.string.flight_controller_listview_intelligent_flight_assistant;
    }

    public void startSending()
    {
        new Thread( new Runnable() {
            @Override
            public void run()
            {
                do
                {
                    sendData();
                }
                while(isSending);
            }}).start();
    }

    public void sendData()
    {
        sock = new Socket[1];

        thread = new Thread(new Runnable()
        {

            @Override
            public void run() {
                try {
                    sock[0] = new Socket(IP, 1234); //Connects to IP address - enter your IP here
                    sock[0].setSoTimeout(180000);
                    //captureAction();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bmp = mVideoSurface.getBitmap();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] mybytearray = stream.toByteArray();

                    byte[] size = ByteBuffer.allocate(4).putInt(stream.size()).array();

                    /*
                    File directory = Environment.getExternalStorageDirectory(); //Gets information about a said directory on your device - currently downloads
                    File photoPath = new File(directory, "Download/IMG_20180822_165545.png"); //Define your image name I used png but other formats should also work - make sure to specify file extension on server
                    boolean exists = photoPath.exists();
                    if (!exists) changeDescription("File does not exist");
                    byte[] mybytearray = new byte[(int) photoPath.length()];
                    FileInputStream fis = new FileInputStream(photoPath);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(mybytearray, 0, mybytearray.length);
                     */


                    byte[] noseData = stringBuilderNose.toString().getBytes(Charset.forName("UTF-8"));
                    byte[] tailData = stringBuilderTail.toString().getBytes(Charset.forName("UTF-8"));

                    OutputStream os = sock[0].getOutputStream();
                    //System.out.println("Sending...");
                    os.write(size);
                    os.write(mybytearray, 0, mybytearray.length);

                    //os.write(noseData);
                    //os.write(tailData);

                    os.flush();
                    sock[0].close();
                    //changeDescription("Data sent sucessfully");
                    //ToastUtils.showToast("Data sent sucessfully");

                } catch (
                        Exception e) {
                    //changeDescription(e.toString());
                    e.printStackTrace();
                    //ToastUtils.showToast("ERROR: "+e.toString());
                    changeDescription(e.toString());
                }
            }
            });

        thread.start();

        try {
            thread.join();
        }
        catch (Exception e) {
            e.printStackTrace();
            showToast("ERROR: " + e.toString());
            changeDescription(e.toString());
        }

    }

    private void captureAction(){

        final Camera camera = DJISDKManager.getInstance().getProduct().getCamera();
        if (camera != null) {

            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            showToast("take photo: success");
                                        } else {
                                            showToast(djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }, 2000);
                    }
                }
            });
        }
    }
}






