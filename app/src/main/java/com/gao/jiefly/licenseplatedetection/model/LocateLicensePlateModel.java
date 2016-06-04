package com.gao.jiefly.licenseplatedetection.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.gao.jiefly.licenseplatedetection.bean.CarPictureBean;
import com.gao.jiefly.licenseplatedetection.bean.LicensePlateBean;

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

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.findContours;
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

    public Mat getHsvMat() {
        return hsvMat;
    }

    Mat hsvMat = new Mat();

    public Mat getResultMat() {
        return resultMat;
    }

    Mat resultMat = new Mat();

    public Mat getGuassMat() {
        return guassMat;
    }

    Mat guassMat = new Mat();
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

    /*
    * 通过颜色定位车牌
    * */
    @Override
    public List<LicensePlateBean> locateLicensePlateByColor(CarPictureBean carPictureBean) {
        if (picBitmap == null) {
            Log.e(TAG, "Picture bitmap can not be null!!!");
            return null;
        }
        Utils.bitmapToMat(carPictureBean.getPicBitmap(),srcMat);

//        将含有车牌的照片从rgb色域转为hsv色域
        hsvMat = new Mat(srcMat.width(),srcMat.height(), CvType.CV_8UC3);
        ChangeToHSV(srcMat,hsvMat);
//        检测照片中的蓝色色域的区域
        Mat mat = findColor(hsvMat,grayMat);
        hsvMat = dilateAndErode(mat,3,1,2);

        findContours(hsvMat,contours,new Mat(),Imgproc.RETR_LIST, CHAIN_APPROX_SIMPLE);
        drawContours(hsvMat,contours,-1,new Scalar(255,0,0));
        filterLicensePlates(contours);
//        resultMat = mat;
//        从蓝色色域的区域检测出外接矩为矩形的区域，将至作为初步的车牌候选区
//        findLicensePlates(contours,mat);
//        从这些候选区当中过滤掉宽高比不符，以及size过于小的区域
//        contours = filterLicensePlates(contours);
        return getLicensePlateFormContours(contours);
    }

    /*
    * 找出hsvMat中的蓝色区域，并将其他区域设为0
    * */
    private Mat findColor(Mat hsvMat,Mat dstMat) {
        Scalar lowerThreshold = new Scalar(75, 90, 90);
        Scalar upperThreshold = new Scalar(140, 255, 255);
        Core.inRange(hsvMat, lowerThreshold, upperThreshold, dstMat);
        return dstMat;
    }
    /*
    * 将rgb色域转换为hsv色域
    * */
    private void ChangeToHSV(Mat srcMat,Mat hsvMat) {
        Imgproc.cvtColor(srcMat, hsvMat, COLOR_RGB2HSV);
    }

    /*
    * 通过车牌的形态特征定位车牌
    * */
    @Override
    public List<LicensePlateBean> locateLicensePlateByShape(CarPictureBean carPictureBean) {
        if (picBitmap == null) {
            Log.e(TAG, "Picture bitmap can not be null!!!");
            return null;
        }
        guassMat = Guass(picBitmap);
        resultMat = srcMat.clone();
        grayMat = ChangeToGray(srcMat);
        sobelDeal(grayMat);
        binDeal(sobelMat);
        morphologyExDeal(binMat);
//        morphologyExMat = dilateAndErode(binMat,20,1,1);
        findLicensePlates(contours,morphologyExMat);

        contours = filterLicensePlates(contours);
//        srcMat = doAffineTransform(srcMat);
        return getLicensePlateFormContours(contours);
    }

    private List<LicensePlateBean> getLicensePlateFormContours(List<MatOfPoint> contours) {
        for (MatOfPoint matOfPoint : contours) {
            Rect rect = Imgproc.boundingRect(matOfPoint);
            Log.e(TAG,rect.width+"---->"+rect.height+"原始图片大小："+srcMat.cols()+"----->"+srcMat.rows());
            Mat result = new Mat(srcMat, rect);
            Log.i(TAG, result.size() + "");
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
                Rect rect = Imgproc.boundingRect(matOfPoint);
                /*int x, y, w, h;
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;
                Imgproc.rectangle(resultMat, new Point(x, y), new Point(x + w, y + h), new Scalar(255, 255, 0), 2);*/
                float r = (float) rect.size().width / (float) rect.size().height;
                double angle = rotatedRect.angle;
                if (r > 3.1 && r < 4) {
//                    if (angle - 50 < 0 && angle + 50 > 0) {
                        for (int i = 0; i < 4; i++) {
                            line(resultMat, points[i], points[(i + 1) % 4], new Scalar(255, 0, 0));
                            result.add(matOfPoint);
                        }
//                    }
                }
                /*for (int i = 0; i < 4; i++) {
//                    line(resultMat, points[i], points[(i + 1) % 4], new Scalar(255, 0, 0));
                    result.add(matOfPoint);
                }*/
                Log.e(TAG, "angle:" + rotatedRect.angle + "r:" + r);
            }
        }
        Log.e(TAG, "识别结果：" + result.size());
        return result;
    }

    /*
    * 初步寻找出，符合车牌形状的候选区
    * */
    private void findLicensePlates(List<MatOfPoint> contours,Mat mat) {
        Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_CCOMP, CHAIN_APPROX_NONE);

        drawContours(resultMat,contours,-1,new Scalar(0,255,0),2);
    }

    /*
    * 功能：对mat进行闭操作，将车牌中的空洞联通起来
    * */
    private void morphologyExDeal(Mat binMat) {
//        Imgproc.morphologyEx(binMat, morphologyExMat, Imgproc.MORPH_CLOSE, kernel, anchor, iterations);
        Mat kernellll = getStructuringElement(Imgproc.MORPH_RECT,new Size(17,3));
        Imgproc.morphologyEx(binMat,morphologyExMat,MORPH_CLOSE,kernellll);
//        return morphologyExMat;
//        morphologyExMat = dilateAndErode(binMat,7,1,2);
    }

    private Mat dilateAndErode(Mat srcMat, int kernelSize, int point, int times) {
        Mat kenrelX = getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(kernelSize, 1));
        Point X = new Point(-point, 0);
        Mat kenrelY = getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(1, kernelSize));
        Point Y = new Point(0, -point);
        Mat dilatMat = new Mat();
        //x方向膨胀把数字连通
        dilate(srcMat, dilatMat, kenrelX, X, times);
        //x方向腐蚀去除碎片
        erode(dilatMat, dilatMat, kenrelX, X, 2 * times);
        //x方向膨胀回复形态
        dilate(dilatMat, dilatMat, kenrelX, X, times);
        //y方向腐蚀去碎片
        erode(dilatMat, dilatMat, kenrelY, Y, times/2);
        //y方向膨胀回复形态
        dilate(dilatMat, dilatMat, kenrelY, Y, times);

        return dilatMat;
    }

    /*
    * 功能：对Mat进行二值化处理
    * */
    private void binDeal(Mat sobelMat) {
        Imgproc.threshold(sobelMat, binMat, thresh, maxValue, type);
    }

    /*
    * 功能：通过sobel算子进行边缘提取
    * */
    private void sobelDeal(Mat grayMat) {
  /*      Mat gradX = new Mat();
        Mat gradY = new Mat();
        Mat absGradX = new Mat();
        Mat absGradY = new Mat();
//        X
        Imgproc.Sobel(grayMat,gradX,CvType.CV_8U,1,0,3,scale,delate,Core.BORDER_DEFAULT);
        Core.convertScaleAbs(gradX,absGradX);
//        Y
        Imgproc.Sobel(grayMat,gradY,CvType.CV_8U,0,1,3,scale,delate,Core.BORDER_DEFAULT);
        Core.convertScaleAbs(gradY,absGradY);

        Core.addWeighted(absGradX,1,absGradY,0,0,sobelMat);*/


        Imgproc.Sobel(grayMat, sobelMat, ddepth, dx, dy, kSize, scale, delate, Core.BORDER_DEFAULT);
//        Imgproc.Sobel(grayMat, sobelMat,CvType.CV_8U, 1, 0, 3, 1, 0.3, Core.BORDER_DEFAULT);
    }
    /*
    * 首先对图片进行高斯滤波，滤除一些杂乱的信息
    * */

    private Mat Guass(Bitmap picBitmap){
        Utils.bitmapToMat(picBitmap,srcMat);
        Mat guassMat = new Mat();
        Imgproc.GaussianBlur(srcMat,guassMat,gaussianBlurSize,Core.BORDER_DEFAULT);
        return guassMat;
    }
    /*
    * 功能：将彩色bitmap转为灰色Mat
    * 参数picBitmap：待转换的bitmap
    * 返回值：转换好的灰色Mat
    * */
    private Mat ChangeToGray(Mat srcMat) {
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        return grayMat;
    }

    /*
    * 对检测出来的车牌进行仿射变换，修复车牌的角度
    * */
    private Mat doAffineTransform(Mat resultMat) {
        List<Point> srcPoints = new ArrayList<>();
        List<Point> dstPoints = new ArrayList<>();

        srcPoints.add(new Point(0, 0));
        srcPoints.add(new Point(resultMat.cols() - 1, 0));
        srcPoints.add(new Point(0, resultMat.rows() - 1));

        dstPoints.add(new Point(resultMat.cols() * 0.0, resultMat.rows() * 0.33));
        dstPoints.add(new Point(resultMat.cols() * 0.65, resultMat.rows() * 0.35));
        dstPoints.add(new Point(resultMat.cols() * 0.15, resultMat.rows() * 0.6));

        MatOfPoint2f src = new MatOfPoint2f();
        src.fromList(srcPoints);
        MatOfPoint2f dst = new MatOfPoint2f();
        dst.fromList(dstPoints);

        Mat tranMat;
        tranMat = Imgproc.getAffineTransform(src, dst);

        Imgproc.warpAffine(resultMat, resultMat, tranMat, resultMat.size());

        /*Point center = new Point( tranMat.cols()/2, tranMat.rows()/2 );
        double angle = -30.0;
        double scale = 0.8;
        Mat rotMat = getRotationMatrix2D( center, angle, scale );
        Imgproc.warpAffine(tranMat,tranMat,rotMat,tranMat.size());
*/
        return tranMat;

    }
}
