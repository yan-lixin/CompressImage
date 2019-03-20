package com.compressimage.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.compressimage.CompressImageManager;
import com.compressimage.bean.Photo;
import com.compressimage.config.CompressConfig;
import com.compressimage.listener.CompressImage;
import com.compressimage.utils.CachePathUtils;
import com.compressimage.utils.CommonUtils;
import com.compressimage.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompressImage.CompressListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CompressConfig mCompressConfig;
    private ProgressDialog mProgressDialog;
    private String mCameraCachePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(permissions[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(permissions, 200);
            }
        }

        mCompressConfig = CompressConfig.builder()
                .setUnCompressMinPixel(1000) // 最小像素不压缩，默认值：1000
                .setUnCompressNormalPixel(2000) // 标准像素不压缩，默认值：2000
                .setMaxPixel(1000) // 长或宽不超过的最大像素 (单位px)，默认值：1200
                .setMaxSize(100 * 1024) // 压缩到的最大大小 (单位B)，默认值：200 * 1024 = 200KB
                .enablePixelCompress(true) // 是否启用像素压缩，默认值：true
                .enableQualityCompress(true) // 是否启用质量压缩，默认值：true
                .enableReserveRaw(true) // 是否保留源文件，默认值：true
                .setCacheDir("") // 压缩后缓存图片路径，默认值：Constants.COMPRESS_CACHE
                .setShowCompressDialog(true) // 是否显示压缩进度条，默认值：false
                .create();

        initListener();
    }

    private void initListener() {
        findViewById(R.id.cameraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera();
            }
        });

        findViewById(R.id.albumSelectionBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                albumSelection();
            }
        });

        findViewById(R.id.moreCompressBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compressMore();
            }
        });
    }

    private void camera() {
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else  {
            outputUri = Uri.fromFile(file);
        }
        mCameraCachePath = file.getAbsolutePath();
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }

    private void albumSelection() {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.CAMERA_CODE) {
                preCompress(mCameraCachePath);
            } else if (requestCode == Constants.ALBUM_CODE) {
                if (data != null) {
                    Uri uri = data.getData();
                    String path = UriParseUtils.getPath(this, uri);
                    preCompress(path);
                }
            }
        }
    }

    /**
     * 压缩效果 1.3 MB --> 90 KB
     */
    private void compressMore() {
        List<Photo> photos = new ArrayList<>();
        photos.add(new Photo("/storage/emulated/0/Camera/img1.jpg"));
        photos.add(new Photo("/storage/emulated/0/Camera/img2.jpg"));
        photos.add(new Photo("/storage/emulated/0/Camera/img3.jpg"));
        compress(photos);
    }

    private void preCompress(String photoPath) {
        List<Photo> photos = new ArrayList<>();
        photos.add(new Photo(photoPath));
        if (!photos.isEmpty()) {
            compress(photos);
        }
    }

    /**
     * 开始压缩
     * @param photos
     */
    private void compress(List<Photo> photos) {
        if (mCompressConfig.isShowCompressDialog()) {
            mProgressDialog = CommonUtils.showProgressDialog(this, "压缩中---");
        }
        CompressImageManager.build(this, mCompressConfig, photos, this).compress();
    }

    @Override
    public void onCompressSuccess(List<Photo> images) {
        Toast.makeText(this, "压缩成功", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null && mProgressDialog.isShowing() && !isFinishing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onCompressFailed(List<Photo> images, String error) {
        Log.e(TAG, error);
        if (mProgressDialog != null && mProgressDialog.isShowing() && !isFinishing()) {
            mProgressDialog.dismiss();
        }
    }
}
