package com.gao.jiefly.licenseplatedetection.listener;

import com.gao.jiefly.licenseplatedetection.bean.CharacterBean;

import java.util.List;

/**
 * Created by jiefly on 2016/6/5.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public interface CharacterSegmentationListener {
    void onSuccess(List<CharacterBean> results);
    void onFailed();
}
