/*
 * Copyright (c) 2014, KJFrameForAndroid 张涛 (kymjs123@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kymjs.aframe.bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.kymjs.aframe.KJLoger;
import org.kymjs.aframe.bitmap.utils.BitmapCreate;
import org.kymjs.aframe.utils.CipherUtils;
import org.kymjs.aframe.utils.FileUtils;
import org.kymjs.aframe.utils.StringUtils;

import android.graphics.Bitmap;

/**
 * 图片下载器：可以从网络或本地加载一张Bitmap并返回，你应该使用性能更优的替代类 {@link #DownloadWithLruCache}<br>
 * <b>说明</b> 采用模板方法模式设计的下载器， 同时本类也是一个具体模板类，实现具体的模板方法<br>
 * <b>创建时间</b> 2014-7-11
 * 
 * @author kymjs(kymjs123@gmail.com)
 * @version 1.0 <br>
 */
@Deprecated
public class Downloader implements I_ImageLoder {
    private KJBitmapConfig config;

    public Downloader(KJBitmapConfig config) {
        this.config = config;
    }

    /**
     * 图片加载器协议的接口方法
     */
    @Override
    public byte[] loadImage(String imagePath) {
        if (StringUtils.isEmpty(imagePath)) {
            return null;
        }
        byte[] img = null;
        if (!imagePath.trim().toLowerCase().startsWith("http")) { // 如果不是网络图片
            img = loadImgFromFile(imagePath);
        } else { // 网络图片：首先从本地缓存读取，如果本地没有，则重新从网络加载
            File file = FileUtils.getSaveFile(config.cachePath,
                    CipherUtils.md5(imagePath));
            if (file == null) { // 本地没有缓存
                img = loadImgFromNet(imagePath);
            } else {
                try {
                    img = FileUtils.input2byte(new FileInputStream(
                            file));
                    showLogIfOpen("download success, from be disk cache\n"
                            + imagePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            file = null;
        }
        return img;
    }

    /**
     * 从网络载入一张图片
     * 
     * @param imagePath
     *            图片的地址
     */
    private byte[] loadImgFromNet(final String imagePath) {
        byte[] data = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(imagePath);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(config.timeOut);
            con.setReadTimeout(config.timeOut * 2);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();
            data = FileUtils.input2byte(con.getInputStream());
            // 建立本地缓存
            if (config.openDiskCache) {
                FileUtils.saveFileCache(data,
                        FileUtils.getSavePath(config.cachePath),
                        CipherUtils.md5(imagePath));
            }
            showLogIfOpen("download success, from be net\n"
                    + imagePath);
        } catch (Exception e) {
            doFailureCallBack(imagePath, e);
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return data;
    }

    /**
     * 从本地载入一张图片
     * 
     * @param imagePath
     *            图片的地址
     */
    private byte[] loadImgFromFile(String imagePath) {
        byte[] data = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagePath);
            if (fis != null) {
                // 本地图片就不加入本地缓存了
                data = FileUtils.input2byte(fis);
                showLogIfOpen("download success, from be disk file\n"
                        + imagePath);
            }
        } catch (FileNotFoundException e) {
            doFailureCallBack(imagePath, e);
            e.printStackTrace();
        } finally {
            FileUtils.closeIO(fis);
        }
        return data;
    }

    /**
     * 从磁盘缓存读取一个Bitmap
     * 
     * @return The bitmap or null if not found
     */
    @Override
    public Bitmap getBitmapFromDisk(String key) {
        File file = FileUtils.getSaveFile(config.cachePath,
                CipherUtils.md5(key));
        if (file != null) {
            return BitmapCreate.bitmapFromFile(
                    file.getAbsolutePath(), config.width,
                    config.height);
        } else {
            return null;
        }
    }

    /**
     * 如果设置了回调，调用加载失败回调
     * 
     * @param imagePath
     *            加载失败的图片路径
     * @param e
     *            失败原因
     */
    private void doFailureCallBack(String imagePath, Exception e) {
        if (config.callBack != null) {
            config.callBack.imgLoadFailure(imagePath, e.getMessage());
        }
    }

    /**
     * 如果打开了log显示器，则显示log
     * 
     * @param imageUrl
     */
    private void showLogIfOpen(String log) {
        if (config.isDEBUG) {
            KJLoger.debugLog(getClass().getName(), log);
        }
    }
}
