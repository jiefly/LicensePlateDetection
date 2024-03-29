package com.gao.jiefly.licenseplatedetection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;
import static org.opencv.imgproc.Imgproc.CV_SHAPE_RECT;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.morphologyEx;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String DIR_PATH = "sdcard/store";
    private static final int GET_PICTURE = 0;
    @InjectView(R.id.btn4)
    Button mBtn4;
    @InjectView(R.id.iv_1)
    ImageView mIv1;
    @InjectView(R.id.iv_2)
    ImageView mIv2;
    @InjectView(R.id.iv_3)
    ImageView mIv3;
    @InjectView(R.id.iv_4)
    ImageView mIv4;
    @InjectView(R.id.iv_5)
    ImageView mIv5;
    @InjectView(R.id.iv_6)
    ImageView mIv6;
    @InjectView(R.id.iv_7)
    ImageView mIv7;
    @InjectView(R.id.iv_8)
    ImageView mIv8;


    private boolean isFirstResume = false;

    private Bitmap srcBitmap;
    private Bitmap dstBitmap;
    private Bitmap saveBitmap;

    Mat srcMat;
    Mat dstMat;
    Mat grayMat;
    Mat saveMat;

    Mat hsvMat;

    private int value = 0;
    @InjectView(R.id.iv_done1)
    ImageView mIvDone1;
    @InjectView(R.id.iv_src)
    ImageView mIvSrc;
    @InjectView(R.id.iv_done)
    ImageView mIvDone;
    @InjectView(R.id.iv_done2)
    ImageView mIvDone2;
    /*    @InjectView(R.id.iv_done3)
        ImageView mIvDone3;
        @InjectView(R.id.iv_done4)
        ImageView mIvDone4;*/
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
        hsvMat = new Mat();
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

    @OnClick({R.id.btnGetImg, R.id.btn2, R.id.btn3, R.id.btn4})
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
                detechColor("test");
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
                    dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Utils.bitmapToMat(srcBitmap, srcMat);
                    Gray(srcMat, grayMat);
                    dstMat = grayMat;
                    //deal(paths.get(i));
                    detechColor(paths.get(i));
                }
                LogE("end:" + SystemClock.currentThreadTimeMillis() + "\n花费时间：" + (SystemClock.currentThreadTimeMillis() - currentTime));
                break;
            case R.id.btn4:
//                图像垂直投影
                Bitmap testBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.newtest);
                Mat testMat = new Mat();
                //Bitmap bitmap = testBitmap.copy(Bitmap.Config.RGB_565,true);
                Utils.bitmapToMat(testBitmap, testMat);

//              先获取水平方向投影，用来切除上下多余的部分
                Map<String, int[]> result = Util.Projection(testMat, 0);

                //mIvDone4.setImageBitmap(getBitmapFromMat(testMat));
//                StringBuilder stringBuilder = newtest StringBuilder();

                int[] resultH = result.get("H");
               /* for (int i=0;i<resultV.length;i++){
                   stringBuilder.append(resultV[i]).append(",");
                }*/
//                将水平和垂直方向的投影在Bitmap上显示出来
//                LogE(stringBuilder.toString());
//                LogE(resultV.length+"<=====>"+resultH.length);
                Bitmap bitmapH = getBitmapFromMat(testMat);
                Canvas canvas = new Canvas(bitmapH);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1);
//                int width = resultV.length;
                int height = resultH.length;
                Path path = new Path();
                //canvas.drawBitmap(bitmap,0,0,paint);
                /*for (int i =0;i<width;i++){
                    path.reset();
                    path.moveTo(i,0);
                    path.lineTo(i,resultV[i]);
                    canvas.drawPath(path,paint);
                }*/
                for (int i =0;i<height;i++){
                    path.reset();
                    path.moveTo(0,i);
                    path.lineTo(resultH[i],i);
                    canvas.drawPath(path,paint);
                }
                mIvSrc.setImageBitmap(bitmapH);
                int minH = 1000;
                for (int x:resultH){
                    if (x<minH){
                        minH = x;
                    }
                }
                LogE("minH:"+minH);
//                这是切割好上下边之后的mat
                Mat resultMat = Util.cutH(testMat, resultH, testMat.cols()/5);
                Bitmap afterBitmap = getBitmapFromMat(resultMat);
                Imgproc.cvtColor(testMat, grayMat, COLOR_RGB2GRAY);
                Imgproc.threshold(grayMat, grayMat, 100, 255, Imgproc.THRESH_OTSU);
                mIvDone2.setImageBitmap(getBitmapFromMat(grayMat));
//              获取垂直方向投影，用于分割字符
                result = Util.Projection(resultMat, 1);
                int[] resultV = result.get("V");
                int minV = 1000;
                for (int x:resultV){
                    if (x<minV)
                        minV = x;
                }
                LogE(resultV.length + "");
                Canvas canvasH = new Canvas(afterBitmap);
                Paint paintH = new Paint();
                paintH.setColor(Color.RED);
                paintH.setStyle(Paint.Style.STROKE);
                paintH.setStrokeWidth(1);
                int width = resultV.length;
                int heightH = resultH.length;
                Path pathH = new Path();
                //canvas.drawBitmap(bitmap,0,0,paint);
                for (int i = 0; i < width; i++) {
                    pathH.reset();
                    pathH.moveTo(i, 0);
                    pathH.lineTo(i, resultV[i]);
                    canvasH.drawPath(pathH, paintH);
                }
                mIvDone.setImageBitmap(afterBitmap);
                LogE("minV:"+minV);
                List<Mat> charMats = Util.cutV(resultMat, resultV, minV+height / 100+1, 8);
                LogE("检测到的字符数目：" + charMats.size());
                switch (charMats.size()) {
                    case 8:
                        mIv8.setImageBitmap(getBitmapFromMat(charMats.get(7)));
                    case 7:
                        mIv7.setImageBitmap(getBitmapFromMat(charMats.get(6)));
                    case 6:
                        mIv6.setImageBitmap(getBitmapFromMat(charMats.get(5)));
                    case 5:
                        mIv5.setImageBitmap(getBitmapFromMat(charMats.get(4)));
                    case 4:
                        mIv4.setImageBitmap(getBitmapFromMat(charMats.get(3)));
                    case 3:
                        mIv3.setImageBitmap(getBitmapFromMat(charMats.get(2)));
                    case 2:
                        mIv2.setImageBitmap(getBitmapFromMat(charMats.get(1)));
                    case 1:
                        mIv1.setImageBitmap(getBitmapFromMat(charMats.get(0)));
                }
        }
    }


    private void detechColor(String fileName) {
        Imgproc.cvtColor(srcMat, hsvMat, COLOR_RGB2HSV);
        Scalar lowerThreshold = new Scalar(75, 90, 90);
        Scalar upperThreshold = new Scalar(140, 255, 255);
        Core.inRange(hsvMat, lowerThreshold, upperThreshold, dstMat);

        Bitmap sobelBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(dstMat, sobelBitmap);
        mIvDone1.setImageBitmap(sobelBitmap);
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.dilate(hsvMat, hsvMat, new Mat());
        Imgproc.findContours(dstMat, contours, new Mat(), RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> newMat = new ArrayList<>();
        filterContours(newMat, contours);
        saveResult(Util.getMaxMatOfPoint(newMat), fileName);
    }

    private void saveResult(MatOfPoint maxContours, String fileName) {
        if (maxContours != null) {
            Rect rect = Imgproc.boundingRect(maxContours);
//            int x, y, w, h;
//            x = rect.x;
//            y = rect.y;
//            w = rect.width;
//            h = rect.height;
            //Imgproc.rectangle(srcMat, newtest Point(x, y), newtest Point(x + w, y + h), newtest Scalar(0, 255, 0), 2);
            saveMat = new Mat(rect.size(), CvType.CV_8UC1);
            Mat m = new Mat(srcMat, rect);
            Core.copyMakeBorder(m, saveMat, 0, 0, 0, 0, Core.BORDER_DEFAULT);
            saveBitmap = Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(m, saveBitmap);
            Util.saveBitmap(fileName, saveBitmap);
        }
    }

    /*
    * 测试各个步骤是否正确
    * */
    private void deal(String fileName) {
        if (fileName == null)
            fileName = "default";
        //MedianBlur(grayMat, dstMat, 2);
        GaussianBlur(srcMat, dstMat, new Size(5, 5), Core.BORDER_DEFAULT);
        Gray(dstMat, grayMat);
        Mat sobelMat = new Mat();
        //边缘提取
        //Imgproc.Canny(dstMat, contours, 100, 150, 3, false);
        Imgproc.Sobel(grayMat, sobelMat, CvType.CV_8U, 1, 0, 3, 2, 0, Core.BORDER_DEFAULT);
        Mat dstCon = new Mat();
        Bitmap sobelBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(sobelMat, sobelBitmap);
        mIvDone1.setImageBitmap(sobelBitmap);
        //边缘二值化
        Imgproc.threshold(sobelMat, dstCon, 50, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        Bitmap binaryBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(dstCon, binaryBitmap);
        // mIvDone4.setImageBitmap(binaryBitmap);
        Mat dilatMat = dilateAndErode(dstCon, 3, -1, 2);
        Mat exMat = new Mat();
        Mat kernel = getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
        //闭操作
        morphologyEx(dstCon, exMat, MORPH_CLOSE, kernel, new Point(-1, -1), 2);


        Bitmap dilateBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(exMat, dilateBitmap);
        mIvDone2.setImageBitmap(dilateBitmap);

        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(exMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, CHAIN_APPROX_NONE);
        List<MatOfPoint> newMatList = new ArrayList<>();
        //初步定位车牌
        filterContours(newMatList, contours);
       /* if (newMatList.size()==1){
            LogE("多余一个车牌候选区："+fileName);
            advanceFilter(newMatList, fileName);
        }
        for (int i = 0; i < newMatList.size(); i++) {
            //drawContours(srcMat,newMatList,i,newtest Scalar(255,0,0),2);
        }*/
        //对车牌进行精定位


        MatOfPoint maxContours = Util.getMaxMatOfPoint(newMatList);
        saveResult(maxContours, fileName);
        Utils.matToBitmap(srcMat, dstBitmap);
        // mIvDone3.setImageBitmap(dstBitmap);
    }

    private void advanceFilter(List<MatOfPoint> newMatList, String fileName) {
        for (int i = 0; i < newMatList.size(); i++) {
            saveResult(newMatList.get(i), i + fileName);
        }
    }

    private Mat dilateAndErode(Mat dstCon, int kernelSize, int point, int times) {
        Mat kenrelX = getStructuringElement(CV_SHAPE_RECT,
                new Size(kernelSize, 1));
        Point X = new Point(-point, 0);
        Mat kenrelY = getStructuringElement(CV_SHAPE_RECT,
                new Size(1, kernelSize));
        Point Y = new Point(0, -point);
        Mat dilatMat = new Mat();
        //x方向膨胀把数字连通
        dilate(dstCon, dilatMat, kenrelX, X, times);
        //x方向腐蚀去除碎片
        erode(dilatMat, dilatMat, kenrelX, X, 2 * times);
        //x方向膨胀回复形态
        dilate(dilatMat, dilatMat, kenrelX, X, times);
        //y方向腐蚀去碎片
        erode(dilatMat, dilatMat, kenrelY, Y, times / 2);
        //y方向膨胀回复形态
        dilate(dilatMat, dilatMat, kenrelY, Y, times);

        return dilatMat;
    }

    //车牌粗定位
    //满足大小以及宽高比
    private void filterContours(List<MatOfPoint> newMatList, List<MatOfPoint> contours) {
        //筛选合适的候选车牌区
        for (MatOfPoint matOfPoint : contours) {
            if (Imgproc.contourArea(matOfPoint) > 500) {
                LogE(matOfPoint.size().area() + "面积");
                //Rect rect = Imgproc.boundingRect(matOfPoint);
                RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(matOfPoint.toArray()));
                Point[] points = new Point[4];
                rotatedRect.points(points);

                float r = (float) rotatedRect.size.width / (float) rotatedRect.size.height;
                double angle = rotatedRect.angle;
                if (r > 2 && r < 4) {
                    /*if (angle - 40 < 0 && angle + 40 > 0) {

                    }*/
                    for (int i = 0; i < 4; i++) {
                        //line(image, vertices[i], vertices[(i + 1) % 4], Scalar(0, 255, 0));//四个角点连成线，最终形成旋转的矩形。
                        line(srcMat, points[i], points[(i + 1) % 4], new Scalar(255, 0, 0));
                        newMatList.add(matOfPoint);
                    }
                }
                LogE("angle:" + rotatedRect.angle + "r:" + r);

                /*(double) (rect.width / rect.height) > 2.5 || (double) (rect.width / rect.height) < 4.0*/
                /*if (rotatedRect.angle) {
                    newMatList.add(matOfPoint);
                }*/
            }
        }
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

    private Bitmap getBitmapFromMat(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
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
