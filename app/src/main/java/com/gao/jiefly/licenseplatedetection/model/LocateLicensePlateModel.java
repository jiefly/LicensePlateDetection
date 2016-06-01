package com.gao.jiefly.licenseplatedetection.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.gao.jiefly.licenseplatedetection.bean.CarPictureBean;
import com.gao.jiefly.licenseplatedetection.bean.LicensePlateBean;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.CV_SHAPE_RECT;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.line;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class LocateLicensePlateModel implements ILocateLicensePlateModel {
    private static final String TAG = "LocateLicensePlateModel";
    private CarPictureBean carPicture;
    //    汽车照片名称
    private String picName;
    //    汽车照片地址
    private Bitmap picBitmap;
    //    高斯滤波参数 kSize
    private Size gaussianBlurSize;
    //    Sobel参数
    private int ddepth;
    private int dx;
    private int dy;
    private int kSize;
    private double scale;
    private double delate;

    //    二值化参数
    private int type;
    private int maxValue;
    private int thresh;

    //    闭操作参数
    private Mat kernel;
    private int iterations;
    private Point anchor;

    //    检测出的车牌候选
    private List<LicensePlateBean> results = new ArrayList<>();

    public List<LicensePlateBean> getResults() {
        return results;
    }

    public Mat getSrcMat() {
        return srcMat;
    }

    public Mat getGrayMat() {
        return grayMat;
    }

    public Mat getSobelMat() {
        return sobelMat;
    }

    public Mat getBinMat() {
        return binMat;
    }

    public Mat getMorphologyExMat() {
        return morphologyExMat;
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }

    Mat srcMat = new Mat();
    Mat grayMat = new Mat();
    Mat sobelMat = new Mat();
    Mat binMat = new Mat();
    Mat morphologyExMat = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();

    public LocateLicensePlateModel(CarPictureBean carPicture) {
        this.carPicture = carPicture;
        configuration(this.carPicture);
    }

    private void configuration(CarPictureBean carPicture) {
        picName = carPicture.getPicName();
        picBitmap = carPicture.getPicBitmap();
        gaussianBlurSize = carPicture.getGaussianBlurSize();
        ddepth = carPicture.getDdepth();
        dx = carPicture.getDx();
        dy = carPicture.getDy();
        kSize = carPicture.getkSize();
        scale = carPicture.getScale();
        delate = carPicture.getDelate();
        type = carPicture.getType();
        maxValue = carPicture.getMaxValue();
        thresh = carPicture.getThresh();
        kernel = carPicture.getKernel();
        iterations = carPicture.getIterations();
        anchor = carPicture.getAnchor();
    }


    @Override
    public List<LicensePlateBean> locateLicensePlateByColor(CarPictureBean carPictureBean) {
        return null;
    }

    @Override
    public List<LicensePlateBean> locateLicensePlateByShape(CarPictureBean carPictureBean) {
        if (picBitmap == null) {
            Log.e(TAG, "Picture bitmap can not be null!!!");
            return null;
        }
        grayMat = ChangeToGray(picBitmap);
        sobelMat = sobelDeal(grayMat);
        binMat = binDeal(sobelMat);
        morphologyExMat = morphologyExDeal(binMat);
        findLicensePlates(contours);
        contours = filterLicensePlates(contours);
        return getLicensePlateFormContours(contours);
    }

    private List<LicensePlateBean> getLicensePlateFormContours(List<MatOfPoint> contours) {
        for (MatOfPoint matOfPoint : contours) {
            Rect rect = Imgproc.boundingRect(matOfPoint);

            Mat result = new Mat(srcMat, rect);
            Log.i(TAG,result.size()+"");
            results.add(new LicensePlateBean(result));
        }
        return results;
    }

    /*
    * 进一步滤除不符合要求的候选区
    * */
    private List<MatOfPoint> filterLicensePlates(List<MatOfPoint> contours) {
        List<MatOfPoint> result = new ArrayList<>();
        for (MatOfPoint matOfPoint : contours) {
            if (Imgproc.contourArea(matOfPoint) > 500) {
                Log.e(TAG, matOfPoint.size().area() + "面积");
                RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(matOfPoint.toArray()));
                Point[] points = new Point[4];
                rotatedRect.points(points);

                float r = (float) rotatedRect.size.width / (float) rotatedRect.size.height;
                double angle = rotatedRect.angle;
                if (r > 2 && r < 4) {
                    if (angle - 50 < 0 && angle + 50 > 0) {
                        for (int i = 0; i < 4; i++) {
                            line(binMat, points[i], points[(i + 1) % 4], new Scalar(255, 0, 0));
                            result.add(matOfPoint);
                        }
                    }
                }
                Log.e(TAG, "angle:" + rotatedRect.angle + "r:" + r);
            }
        }
        Log.e(TAG,"识别结果："+result.size());
        return result;
    }

    /*
    * 初步寻找出，符合车牌形状的候选区
    * */
    private void findLicensePlates(List<MatOfPoint> contours) {
        Imgproc.findContours(morphologyExMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, CHAIN_APPROX_NONE);
    }

    /*
    * 功能：对mat进行闭操作，将车牌中的空洞联通起来
    * */
    private Mat morphologyExDeal(Mat binMat) {
        Imgproc.morphologyEx(binMat, morphologyExMat, Imgproc.MORPH_CLOSE, kernel, anchor, iterations);
        return morphologyExMat;
        /*return dilateAndErode(binMat,7,1,2);*/
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
    /*
    * 功能：对Mat进行二值化处理
    * */
    private Mat binDeal(Mat sobelMat) {
        Imgproc.threshold(sobelMat, binMat, thresh, maxValue, type);
        return binMat;
    }

    /*
    * 功能：通过sobel算子进行边缘提取
    * */
    private Mat sobelDeal(Mat grayMat) {
        Imgproc.Sobel(grayMat, sobelMat, ddepth, dx, dy, kSize, scale, delate, Core.BORDER_DEFAULT);
        return sobelMat;
    }

    /*
    * 功能：将彩色bitmap转为灰色Mat
    * 参数picBitmap：待转换的bitmap
    * 返回值：转换好的灰色Mat
    * */
    private Mat ChangeToGray(Bitmap picBitmap) {
        Utils.bitmapToMat(picBitmap, srcMat);
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        return grayMat;
    }
}
