package es.ua.eps.cannyandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.google.android.material.slider.Slider;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private boolean filterOn = true;
    private int blurValue;
    private int gradientValue;
    private int angleValue;
    private static String TAG = "MainActivity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny;
    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    static {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        Slider blurSlider = findViewById(R.id.blurSlider);
        Slider gradientSlider = findViewById(R.id.gradientSlider);
        Slider angleSlider = findViewById(R.id.angleSlider);
        Button stopBtn = findViewById(R.id.stopBtn);

        blurSlider.addOnChangeListener((slider, value, fromUser) -> {
            blurValue = Math.round(value);
        });

        gradientSlider.addOnChangeListener((slider, value, fromUser) -> {
            gradientValue = Math.round(value);
        });

        angleSlider.addOnChangeListener((slider, value, fromUser) -> {
            angleValue = Math.round(value);
        });

        stopBtn.setOnClickListener(v -> {
            filterOn = !filterOn;
            if (filterOn) {
                stopBtn.setText("Stop");
            }
            else {
                stopBtn.setText("Start");
            }

        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV Loaded!!!!");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else {
            Log.i(TAG, "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallBack);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgGray = new Mat(height, width, CvType.CV_8UC1);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        Point center = new Point(mRgba.cols() / 2, mRgba.rows() / 2);
        double angle = angleValue-90;
        double scale = 1.0;
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, scale);
        Mat rotatedMat = new Mat();
        Imgproc.warpAffine(mRgba, rotatedMat, rotationMatrix, mRgba.size());

        Imgproc.cvtColor(rotatedMat, imgGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(imgGray, imgCanny, blurValue, gradientValue);

        if (filterOn) {
            return imgCanny;
        }
        else {
            return rotatedMat;
        }

    }

}