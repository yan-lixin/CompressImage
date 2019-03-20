package com.compressimage.listener;

import com.compressimage.bean.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片集合的压缩返回监听
 */
public interface CompressImage {

    // 开始压缩
    void compress();

    // 图片集合的压缩结果返回
    interface CompressListener {

        // 成功
        void onCompressSuccess(List<Photo> images);

        // 失败
        void onCompressFailed(List<Photo> images, String error);
    }
}
