package com.gao.jiefly.licenseplatedetection.model;

import com.gao.jiefly.licenseplatedetection.listener.CharacterSegmentationListener;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public interface IcharactersSegmentationModel {
    void getCharacter(int threshold, int num);
    void setListener(CharacterSegmentationListener listener);
}
