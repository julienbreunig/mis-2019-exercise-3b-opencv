package com.example.exercise_3b;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "MainActivity";

    private Mat matrixRGBA;
    private CascadeClassifier cascadeClassifier;
    private CameraBridgeViewBase mOpenCvCameraView;

    // code was given in the project
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    // source: https://www.mirkosertic.de/blog/2013/07/realtime-face-detection-on-android-using-opencv/
                    // shortened the source version since the initAssetFile method was given in the project
                    cascadeClassifier = new CascadeClassifier(initAssetFile("haarcascade_mcs_nose.xml"));
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    // code was given in the project
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created.*/
    // code was given in the project
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        // source: https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // before opening the CameraBridge, we need the Camera Permission on newer Android versions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x123);
        } else {
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_java_surface_view);
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
    }

    // code was given in the project
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    // code was given in the project
    // opencv_version was changed to implemented version
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    // code was given in the project
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    // source: https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c
    public void onCameraViewStarted(int width, int height) {
        matrixRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    // source: https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c
    public void onCameraViewStopped() {
        matrixRGBA.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // source: https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c
        // this method returns a RGBA matrix with frame
        matrixRGBA = inputFrame.rgba();
        // this method returns a single channel gray scale matrix with frame
        Mat grayscaleImage = inputFrame.gray();

        // create a MatOfRect that stores the detected noses
        MatOfRect noses = new MatOfRect();

        // source: https://www.mirkosertic.de/blog/2013/07/realtime-face-detection-on-android-using-opencv/
        // use the cascadeClassifier to detect noses and stores them in noses
        if (cascadeClassifier != null) {
            // source: https://stackoverflow.com/questions/20801015/recommended-values-for-opencv-detectmultiscale-parameters
            // scaleFactor – Parameter specifying how much the image size is reduced at each image scale
            // minNeighbors – Parameter specifying how many neighbors each candidate rectangle should have to retain it.
            // minSize – Minimum possible object size. Objects smaller than that are ignored.
            // maxSize – Maximum possible object size. Objects bigger than this are ignored.
            cascadeClassifier.detectMultiScale(grayscaleImage, noses, 1.05, 7, 0,
                    new Size(50, 60), new Size(150,200));
        }

        // put the detected noses in an array of rectangles
        Rect[] nosesArray = noses.toArray();

        // iterate through the array of noses and draw a red filled circle for each one
        for (int i = 0; i <nosesArray.length; i++)
            createRedCircle(matrixRGBA, findCenterOfRectangle(nosesArray[i].tl(),nosesArray[i].br()), calculateNoseSize(nosesArray[i].tl(),nosesArray[i].br()));

        // clone the RGBA matrix as preparation for the conversion to RGB
        Mat temporaryMatrix = matrixRGBA.clone();

        // converts an image from one color space to another.
        Imgproc.cvtColor(temporaryMatrix, matrixRGBA, Imgproc.COLOR_RGBA2RGB);

        return matrixRGBA;
    }

    // code was given in the project
    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getPath();
    }

    /**
     * takes a matrix of an image, a center point and the desired
     * width and draws a red filled circle onto the image at the
     * given center point
     */
    public void createRedCircle(Mat image, Point center, int width )
    {
        Imgproc.circle( image, center, width, new Scalar( 255, 0, 0, 0), -1);
    }

    /**
     * takes the top left and bottom right point of a
     * rectangle and returns the center point
     */
    public Point findCenterOfRectangle(Point tl, Point br)
    {
        Point center = new Point (tl.x+(br.x-tl.x)/2,tl.y+(br.y-tl.y)/2);

        return center;
    }

    /**
     * takes the top left and bottom right point of a
     * nose rectangle and returns fraction of the width
     */
    public int calculateNoseSize (Point tl, Point br)
    {
        int noseSize = (int) (br.y-tl.y)/2;

        return noseSize;
    }

}
