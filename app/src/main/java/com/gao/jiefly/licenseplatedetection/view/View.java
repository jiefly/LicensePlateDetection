package com.gao.jiefly.licenseplatedetection.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.gao.jiefly.licenseplatedetection.R;
import com.gao.jiefly.licenseplatedetection.presenter.Presenter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.FileNotFoundException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class View extends Activity implements IView {

    private static final int GET_CAR_PIC = 0;
    private static final int GET_LICENSE_PLATE = 1;
    @InjectView(R.id.ivSrc)
    ImageView mIvSrc;
    @InjectView(R.id.ivSobel)
    ImageView mIvSobel;
    @InjectView(R.id.ivMorphologyEx)
    ImageView mIvMorphologyEx;
    @InjectView(R.id.ivResult)
    ImageView mIvResult;
    @InjectView(R.id.ivGuass)
    ImageView mIvGuass;
    @InjectView(R.id.ivGray)
    ImageView mIvGray;
    @InjectView(R.id.ivBin)
    ImageView mIvBin;
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
    @InjectView(R.id.btnLocate)
    Button mBtnLocate;
    @InjectView(R.id.btnSegmentation)
    Button mBtnSegmentation;
    @InjectView(R.id.btnGetPic)
    Button mBtnGetPic;
    @InjectView(R.id.btnGetLicensePlate)
    Button mBtnGetLicensePlate;


    private Presenter mPresenter;

    private boolean isFirstResume = false;
    private static final String TAG = "View";
    Intent intentGetPic;

    BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "opencv load success");
                    break;
                case BaseLoaderCallback.INIT_FAILED:
                    Log.e(TAG, "opencv init failed!!!");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        ButterKnife.inject(this);
        mPresenter = new Presenter(this);
        intentGetPic = new Intent("android.intent.action.GET_CONTENT");
        intentGetPic.setType("image/*");
        intentGetPic.putExtra("crop", true);
        intentGetPic.putExtra("scale", true);
    }


    @Override
    public void showLicensePlate(List<Bitmap> bitmaps) {
        switch (bitmaps.size()) {
            case 7:
                //        检测结果
                mIvResult.setImageBitmap(bitmaps.get(6));
            case 6:
                //        闭操作
                mIvMorphologyEx.setImageBitmap(bitmaps.get(5));
            case 5:
                //        二值化
                mIvBin.setImageBitmap(bitmaps.get(4));
            case 4:
                //        sobel边缘提取
                mIvSobel.setImageBitmap(bitmaps.get(3));
            case 3:
                //        灰度化
                mIvGray.setImageBitmap(bitmaps.get(2));
            case 2:
                //        高斯滤波
                mIvGuass.setImageBitmap(bitmaps.get(1));
            case 1:
                //        原图
                mIvSrc.setImageBitmap(bitmaps.get(0));
        }
    }

    @Override
    public void showCharacters(List<Bitmap> characters) {
        switch (characters.size()) {
            case 8:
                mIv8.setImageBitmap(characters.get(7));
            case 7:
                mIv7.setImageBitmap(characters.get(6));
            case 6:
                mIv6.setImageBitmap(characters.get(5));
            case 5:
                mIv5.setImageBitmap(characters.get(4));
            case 4:
                mIv4.setImageBitmap(characters.get(3));
            case 3:
                mIv3.setImageBitmap(characters.get(2));
            case 2:
                mIv2.setImageBitmap(characters.get(1));
            case 1:
                mIv1.setImageBitmap(characters.get(0));
        }
    }

    @OnClick({R.id.btnLocate, R.id.btnSegmentation, R.id.btnGetPic, R.id.btnGetLicensePlate})
    public void onClick(android.view.View view) {
        switch (view.getId()) {
            case R.id.btnLocate:
                mPresenter.showLocateResult(1);
                break;
            case R.id.btnSegmentation:
                mPresenter.showCharacters();
                break;
            case R.id.btnGetPic:
                startActivityForResult(intentGetPic, GET_CAR_PIC);
                break;
            case R.id.btnGetLicensePlate:
                startActivityForResult(intentGetPic, GET_LICENSE_PLATE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_CAR_PIC:
                    try {
                        mPresenter.setCarPicture(BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData())));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case GET_LICENSE_PLATE:
                    try {
                        mPresenter.setLicensePlate(BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData())));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
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
        Log.i(TAG, "onResume sucess load OpenCV...");
    }
}
