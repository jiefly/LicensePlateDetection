package com.gao.jiefly.licenseplatedetection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.CV_SHAPE_RECT;
import static org.opencv.imgproc.Imgproc.RETR_CCOMP;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String DIR_PATH = "sdcard/store";
    private static final int GET_PICTURE = 0;


    private boolean isFirstResume = false;

    private Bitmap srcBitmap;
    private Bitmap dstBitmap;
    private Bitmap saveBitmap;

    Mat srcMat;
    Mat dstMat;
    Mat grayMat;
    Mat saveMat;

    private int value = 0;
    @InjectView(R.id.iv_done1)
    ImageView mIvDone1;
    @InjectView(R.id.iv_src)
    ImageView mIvSrc;
    @InjectView(R.id.iv_done)
    ImageView mIvDone;
    @InjectView(R.id.iv_done2)
    ImageView mIvDone2;
    @InjectView(R.id.iv_done3)
    ImageView mIvDone3;
    @InjectView(R.id.iv_done4)
    ImageView mIvDone4;
    @InjectView(R.id.btnGetImg)
    Button mBtnGetImg;
    @InjectView(R.id.btn2)
    Button mBtn2;
    @InjectView(R.id.btn3)
    Button mBtn3;
    BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    LogI("opencv load success");
                    initMat();
                    break;
                case BaseLoaderCallback.INIT_FAILED:
                    LogE("opencv init failed!!!");
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

    }

    private void initMat() {
        srcMat = new Mat();
        dstMat = new Mat();
        grayMat = new Mat();
        saveMat = new Mat();
    }

    /*
    * srcMat:处理的源Mat
    * dstMat:处理后的结果
    * 功能：将srcMat灰度化
    * */
    private void Gray(Mat srcMat1, Mat dstMat1) {
        Imgproc.cvtColor(srcMat1, dstMat1, Imgproc.COLOR_RGB2GRAY);
    }

    /*功能：中值滤波
     *srcMat:处理的源Mat
     * dstMat：处理后的结果
     * ksize:窗口大小
     * */
    private void MedianBlur(Mat srcMat1, Mat dstMat1, int ksize) {
        Imgproc.medianBlur(srcMat1, dstMat1, ksize);
    }

    @OnClick({R.id.btnGetImg, R.id.btn2, R.id.btn3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGetImg:
                Intent intentGetPic = new Intent("android.intent.action.GET_CONTENT");
                intentGetPic.setType("image/*");
                intentGetPic.putExtra("crop", true);
                intentGetPic.putExtra("scale", true);
                // intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intentGetPic, GET_PICTURE);
                break;
            case R.id.btn2:
                deal(null);
                break;
            case R.id.btn3:
                final Vector<String> paths = Util.GetImageFileName(DIR_PATH);
               /* List<String> paths = Util.getPicPath("sdcard/store",this);*/
                double currentTime;
                currentTime = SystemClock.currentThreadTimeMillis();
                LogE("start:" + currentTime);
                for (int i = 0; i < paths.size(); i++) {
                    LogI(paths.get(i));
                    File file = new File(DIR_PATH + "/" + paths.get(i));
                    srcBitmap = BitmapFactory.decodeFile(String.valueOf(file));
                    dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                    Utils.bitmapToMat(srcBitmap, srcMat);
                    Gray(srcMat, grayMat);
                    dstMat = grayMat;
                    deal(paths.get(i));
                }
                LogE("end:" + SystemClock.currentThreadTimeMillis() + "\n花费时间：" + (SystemClock.currentThreadTimeMillis() - currentTime));
                break;
        }
    }

    /*
    * 测试各个步骤是否正确
    * */
    private void deal(String fileName) {

        MedianBlur(grayMat, dstMat, 3);

        Mat sobelMat = new Mat();
        //边缘提取
        //Imgproc.Canny(dstMat, contours, 100, 150, 3, false);
        Imgproc.Sobel(dstMat, sobelMat, CvType.CV_8U, 2, 0, 3, 0.4, 0);
        Mat dstCon = new Mat();
        Bitmap sobelBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(sobelMat, sobelBitmap);
        mIvDone1.setImageBitmap(sobelBitmap);
        //边缘二值化
        Imgproc.threshold(sobelMat, dstCon, 0, 255, Imgproc.THRESH_OTSU);
        //Mat exMat = new Mat();
        //Mat kernel = getStructuringElement(CV_SHAPE_RECT,new Size(3,3));
        //闭操作
        //morphologyEx(dstCon,exMat,MORPH_CLOSE,kernel,new Point(1,1),1);

        //brush(dstCon);

        Mat kenrelX = getStructuringElement(CV_SHAPE_RECT,
                new Size(10, 1));
        Point X = new Point(-1, 0);
        Mat kenrelY = getStructuringElement(CV_SHAPE_RECT,
                new Size(1, 10));
        Point Y = new Point(0, -1);
        Mat dilatMat = new Mat();
        //x方向膨胀把数字连通
        dilate(dstCon, dilatMat, kenrelX, X, 2);
        //x方向腐蚀去除碎片
        erode(dilatMat, dilatMat, kenrelX, X, 4);
        //x方向膨胀回复形态
        dilate(dilatMat, dilatMat, kenrelX, X, 2);
        //y方向腐蚀去碎片
        erode(dilatMat, dilatMat, kenrelY, Y, 1);
        //y方向膨胀回复形态
        dilate(dilatMat, dilatMat, kenrelY, Y, 2);

        Bitmap dilateBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dilatMat, dilateBitmap);
        mIvDone2.setImageBitmap(dilateBitmap);

        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(dilatMat, contours, new Mat(), RETR_CCOMP, CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> newMatList = new ArrayList<>();
        //筛选合适的候选车牌区
        for (MatOfPoint matOfPoint : contours) {
            if (Imgproc.contourArea(matOfPoint) > 200) {
                Rect rect = Imgproc.boundingRect(matOfPoint);
                if ((double) (rect.width / rect.height) > 2.5 || (double) (rect.width / rect.height) < 3.5) {
                   newMatList.add(matOfPoint);
                }
            }
        }
        MatOfPoint maxContours = Util.getMaxMatOfPoint(newMatList);
        if (maxContours!=null){
            Rect rect = Imgproc.boundingRect(maxContours);
            int x, y, w, h;
            x = rect.x;
            y = rect.y;
            w = rect.width;
            h = rect.height;
            Imgproc.rectangle(srcMat, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 2);
            saveMat = new Mat(rect.size(), CvType.CV_8UC4);
            Mat m = new Mat(srcMat, rect);
            Core.copyMakeBorder(m, saveMat, 0, 0, 0, 0, Core.BORDER_DEFAULT);
            saveBitmap = Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(m, saveBitmap);
            Util.saveBitmap(fileName, saveBitmap);
        }
        /*for (MatOfPoint matOfPoint : contours) {
            //LogE("宽高比："+(double)matOfPoint.width() / matOfPoint.height()+"rows:"+matOfPoint.rows()+"cols:"+matOfPoint.cols());
            //车牌的宽高比范围为2.5-3.5
            //&& (matOfPoint.width() / matOfPoint.height() > 2.5)
            // && (matOfPoint.width() / matOfPoint.height() < 3.5)
            if (Imgproc.contourArea(matOfPoint) > 100) {
                Rect rect = Imgproc.boundingRect(matOfPoint);
                int x, y, w, h;
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;
                if ((double) (rect.width / rect.height) > 2.5 && (double) (rect.width / rect.height) < 4.5) {
                    Imgproc.rectangle(srcMat, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 2);
                    saveMat = new Mat(rect.size(), CvType.CV_8UC4);
                    Mat m = new Mat(srcMat, rect);
                    Core.copyMakeBorder(m, saveMat, 0, 0, 0, 0, Core.BORDER_DEFAULT);
                    saveBitmap = Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(m, saveBitmap);
                    Util.saveBitmap(fileName, saveBitmap);
                }

                //circle(srcMat, new Point(x + w / 2, y + h / 2), 3, new Scalar(255, 0, 0), 2);

            }
        }*/

        /*Utils.matToBitmap(srcMat, dstBitmap);
        mIvDone3.setImageBitmap(dstBitmap);*/
    }

    private void brush(Mat srcMat) {
        LogE("row size:height" + srcMat.height() + "weight" + srcMat.width());
        for (int i = 0; i < srcMat.height(); i++) {
            for (int j = 0; j < srcMat.width(); j++) {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_PICTURE:
                    try {
                        srcBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                        mIvSrc.setImageBitmap(srcBitmap);
                        dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                        Utils.bitmapToMat(srcBitmap, srcMat);
                        Gray(srcMat, grayMat);
                        dstMat = grayMat;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        LogE("打开图片失败");
                    }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstResume) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, getApplicationContext(), mBaseLoaderCallback);
        }
        isFirstResume = true;
        LogI("onResume sucess load OpenCV...");
    }

    private void LogE(String string) {
        Log.e(TAG, string);
    }

    private void LogI(String string) {
        Log.i(TAG, string);
    }
}
