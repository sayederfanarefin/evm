package info.sayederfanarefin.evm;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class home extends AppCompatActivity implements ArduinoListener {

    private Arduino arduino;
    private TextView textView;
    Button proceed;
    ImageView iamhurt;

    ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        textView = findViewById(R.id.status_text);
        proceed = (Button) findViewById(R.id.proceed);

        iamhurt = (ImageView) findViewById(R.id.iamhurt);

//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

        scrollView = (ScrollView) findViewById(R.id.asd);
        scrollView.fullScroll(View.FOCUS_DOWN);
        arduino = new Arduino(this);

        display("Please plug an Arduino via OTG.\nOn some devices you will have to enable OTG Storage in the phone's settings.\n\n");
    }

    @Override
    protected void onStart() {
        super.onStart();
        arduino.setArduinoListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arduino.unsetArduinoListener();
        arduino.close();
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        display("Arduino attached!");
        arduino.open(device);
    }

    @Override
    public void onArduinoDetached() {
        display("Arduino detached");
    }

    String globalString = "";
    byte[] bytesImage = new byte[10];
    Bitmap bmp;
    boolean flag = true;

    @Override
    public void onArduinoMessage(byte[] bytes) {

        String a = null;
        try {
            a = new String(bytes, "UTF-8");
            globalString += a;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(globalString.contains("Streaming image") && flag){

            byte[] bytesImageTemp = new byte[bytesImage.length + bytes.length ];
            for(int i =0; i < bytesImage.length; i++){
                bytesImageTemp[i] = bytesImage[i];
            }
            for(int i =0; i < bytes.length; i++){
                bytesImageTemp[bytesImage.length + i] = bytes[i];
            }
            bytesImage= new byte[bytesImageTemp.length];
            for(int i =0; i < bytesImageTemp.length; i++){
                bytesImage[i] = bytesImageTemp[i];
            }

            if(globalString.contains("Image taken")){
                flag = false;
                InputStream instream = new ByteArrayInputStream(bytesImage);
                bmp = BitmapFactory.decodeStream(instream);
               try{
                   iamhurt.setImageBitmap(bmp);
               }catch (Exception e){
                   display(e.getMessage());
               }
                // BufferedImage bImageFromConvert = ImageIO.read(in);
                display("==== done image ======");
            }

            display("==== receiving image ======");
            display("====global: " + globalString);
        }else{
            display("Received: " + a);
        
        }
    }

    @Override
    public void onArduinoOpened() {
        //String str = "Hello World !";
    }

    @Override
    public void onUsbPermissionDenied() {
        display("Permission denied... New attempt in 3 sec");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                arduino.reopen();
            }
        }, 3000);
    }

    Mat img2, img1;
    MatOfKeyPoint keypoints1, keypoints2;
    Mat descriptors1, descriptors2;

    public void display(final String message) {
        Log.v("===arduino output: " , message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(message + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    String a, b;
    String x, y;
    int z = 0;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    // Mat imageMat=new Mat();
                    img1 = Imgcodecs.imread(a);
                    img2 = Imgcodecs.imread(b);

                    keypoints1 = new MatOfKeyPoint();
                    keypoints2 = new MatOfKeyPoint();
                    descriptors1 = new Mat();
                    descriptors2 = new Mat();
                    //Definition of ORB keypoint detector and descriptor extractors
                    FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
                    DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

//Detect keypoints
                    detector.detect(img1, keypoints1);
                    detector.detect(img2, keypoints2);
//Extract descriptors
                    extractor.compute(img1, keypoints1, descriptors1);
                    extractor.compute(img2, keypoints2, descriptors2);

//Definition of descriptor matcher
                    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

//Match points of two images
                    MatOfDMatch matches = new MatOfDMatch();
                    matcher.match(descriptors1, descriptors2, matches);

                    List<DMatch> matchesList = matches.toList();
                    List<DMatch> matches_final = new ArrayList<DMatch>();
                    int DIST_LIMIT = 60;
                    for (int i = 0; i < matchesList.size(); i++)
                        if (matchesList.get(i).distance <= DIST_LIMIT) {
                            matches_final.add(matches.toList().get(i));
                        }// end if

                    MatOfDMatch matches_final_mat = new MatOfDMatch();
                    matches_final_mat.fromList(matches_final);

                    Integer good_mathces= matches_final.size();

                    /////end

                    Scalar RED = new Scalar(255, 0, 0);
                    Scalar GREEN = new Scalar(0, 255, 0);

                    Mat outputImg = new Mat();
                    MatOfByte drawnMatches = new MatOfByte();

                    Features2d.drawMatches(img1, keypoints1, img2, keypoints2,
                            matches_final_mat, outputImg, GREEN, RED, drawnMatches,
                            Features2d.NOT_DRAW_SINGLE_POINTS);

                    Bitmap imageMatched = Bitmap.createBitmap(outputImg.cols(),
                            outputImg.rows(), Bitmap.Config.RGB_565);

                    Utils.matToBitmap(outputImg, imageMatched);

                    Log.d("DISTFILTER", "GoodMathces:" + good_mathces+ "");

                    iamhurt.setImageBitmap(imageMatched);

                    Log.v("====================", "end"+String.valueOf(good_mathces));
                    //http://answers.opencv.org/question/181449/image-matching-using-opencv-orb-android/
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {

                Uri currImageURI = data.getData();
                if (z == 0) {
                    x = getRealPathFromURI(currImageURI);
                    z++;
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                } else {
                    y = getRealPathFromURI(currImageURI);
                    Log.v("===", x);
                    Log.v("===", y);
                    a=x;
                    b=y;
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
                    //a(x, y);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getRealPathFromURI(Uri contentUri) {

        Log.v("======", contentUri.toString());

        return ImageFilePath.getPath(getApplicationContext(), contentUri);
    }


    public void onResume() {
        super.onResume();
//        if (!OpenCVLoader.initDebug()) {
//            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
//        } else {
//            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }
    }

    private void callForImageStream(){
       // arduino.send(str.getBytes());

    }

}
