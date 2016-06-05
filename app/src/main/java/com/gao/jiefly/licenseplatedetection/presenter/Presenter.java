package com.gao.jiefly.licenseplatedetection.presenter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gao.jiefly.licenseplatedetection.bean.CarPictureBean;
import com.gao.jiefly.licenseplatedetection.bean.CharacterBean;
import com.gao.jiefly.licenseplatedetection.bean.LicensePlateBean;
import com.gao.jiefly.licenseplatedetection.listener.CharacterSegmentationListener;
import com.gao.jiefly.licenseplatedetection.model.CharactersSegmentationModel;
import com.gao.jiefly.licenseplatedetection.model.IcharactersSegmentationModel;
import com.gao.jiefly.licenseplatedetection.model.LocateLicensePlateModel;
import com.gao.jiefly.licenseplatedetection.view.IView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class Presenter {
    private IcharactersSegmentationModel mSegmentationModel;
    public LocateLicensePlateModel mLicensePlateModel;
    private CarPictureBean mCarPictureBean;
    private LicensePlateBean mLicensePlateBean;
    private IView mIView;
    private static final int SEGMENT_OK = 1;
    private static final int SEGMENT_FAILED = 0;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEGMENT_OK:
                    mIView.showCharacters(characters);
                    characters.clear();
                    break;
                case SEGMENT_FAILED:
                    mIView.showToast("分割字符失败··· ");
                    break;
            }
        }
    };
    List<Bitmap> characters = new ArrayList<>();
    public Presenter(IView IView) {
        mIView = IView;
    }

    /*
    * 获取定位的车牌候选集合
    * */
    public List<LicensePlateBean> LocateLicensePlate(CarPictureBean carPictureBean,int type) {
        mLicensePlateModel = new LocateLicensePlateModel(carPictureBean);
        switch (type){
//            byShape
            case 0:
                return mLicensePlateModel.locateLicensePlateByShape(carPictureBean);
//            byColor
            case 1:
                return mLicensePlateModel.locateLicensePlateByColor(carPictureBean);
        }
        return null;
    }


    /*
    * 设置待检测的图片
    * */
    public void setCarPicture(Bitmap carBitmap) {
        mCarPictureBean = new CarPictureBean("测试", carBitmap);
        mLicensePlateModel = new LocateLicensePlateModel(mCarPictureBean);
    }

    /*
    * 设置带分割的车牌
    * */
    public void setLicensePlate(Bitmap licensePlate) {
        Mat licensePlateMat = new Mat();
        Utils.bitmapToMat(licensePlate, licensePlateMat);
        mLicensePlateBean = new LicensePlateBean(licensePlateMat);
    }

    /*
    * 显示检测结果
    * */
    public void showLocateResult(int type) {
        List<LicensePlateBean> licensePlateBeanList = new ArrayList<>();
        licensePlateBeanList = LocateLicensePlate(mCarPictureBean,type);
        Bitmap srcBitmap = mCarPictureBean.getPicBitmap();
        List<Bitmap> bitmapList = new ArrayList<>();
        switch (type){
            case 0:
                Bitmap guassBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap sobelBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap binBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap ExBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(mLicensePlateModel.getSobelMat(), sobelBitmap);
                Utils.matToBitmap(mLicensePlateModel.getMorphologyExMat(), ExBitmap);
                Utils.matToBitmap(mLicensePlateModel.getBinMat(), binBitmap);
                Utils.matToBitmap(mLicensePlateModel.getBinMat(),binBitmap);
                Utils.matToBitmap(mLicensePlateModel.getGuassMat(),guassBitmap);
                Utils.matToBitmap(mLicensePlateModel.getGrayMat(),grayBitmap);
                Utils.matToBitmap(mLicensePlateModel.getResultMat(),resultBitmap);
                bitmapList.add(srcBitmap);
                bitmapList.add(guassBitmap);
                bitmapList.add(grayBitmap);
                bitmapList.add(sobelBitmap);
                bitmapList.add(binBitmap);
                bitmapList.add(ExBitmap);
                bitmapList.add(resultBitmap);
                break;
            case 1:
                Bitmap hsvBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap hsvResultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(mLicensePlateModel.getHsvMat(),hsvBitmap);
//                Utils.matToBitmap(mLicensePlateModel.getResultMat(),hsvResultBitmap);
                Utils.matToBitmap(mLicensePlateModel.getGrayMat(),bitmap);
                bitmapList.add(srcBitmap);
                bitmapList.add(hsvBitmap);
                bitmapList.add(bitmap);
//                bitmapList.add(hsvResultBitmap);
                break;
        }
        /*Bitmap testBitmap = Bitmap.createBitmap(mLicensePlateModel.getSrcMat().width(),mLicensePlateModel.getSrcMat().height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mLicensePlateModel.getSrcMat(),testBitmap);
        srcBitmap = testBitmap*/;

        if (licensePlateBeanList.size() > 0)
            mLicensePlateBean = licensePlateBeanList.get(0);
//        识别面积最大的车牌
        for (LicensePlateBean licensePlateBean:licensePlateBeanList){
            if (mLicensePlateBean.getSrcMat().width()*mLicensePlateBean.getSrcMat().height()<licensePlateBean.getSrcMat().width()*licensePlateBean.getSrcMat().height()){
                mLicensePlateBean = licensePlateBean;
            }
        }
        mIView.showLicensePlate(bitmapList);
    }

    /*
    * 显示分割结果
    * */
    public void showCharacters() {
        mSegmentationModel = new CharactersSegmentationModel(mLicensePlateBean);
        mSegmentationModel.setListener(new CharacterSegmentationListener() {
            @Override
            public void onSuccess(List<CharacterBean> results) {
                Log.e("jiefly","success---segmentation");
                for (CharacterBean characterBean : results) {
                    characters.add(getBitmapFromMat(characterBean.getSrcMat()));
                }
                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void onFailed() {
                mHandler.sendEmptyMessage(0);
                Log.e("jiefly","failed---segmentation");
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSegmentationModel.getCharacter(10, 8);
            }
        }).start();

    }

    private Bitmap getBitmapFromMat(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

}
