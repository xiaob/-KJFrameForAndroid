/*
 * Copyright (c) 2014, kymjs 张涛 (kymjs123@gmail.com).
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
package org.kymjs.aframe.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.kymjs.aframe.KJException;
import org.kymjs.aframe.bitmap.utils.BitmapCreate;
import org.kymjs.aframe.bitmap.utils.BitmapHelper;
import org.kymjs.aframe.bitmap.utils.BitmapOperateUtil;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class FileUtils {
    /**
     * 检测SD卡是否存在
     */
    public static boolean checkSDcard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    /**
     * 将文件保存到本地
     */
    public static void saveFileCache(byte[] fileData, String folderPath,
            String fileName) {
        File folder = new File(folderPath);
        folder.mkdirs();
        File file = new File(folderPath, fileName);
        ByteArrayInputStream is = new ByteArrayInputStream(fileData);
        OutputStream os = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
                os = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while (-1 != (len = is.read(buffer))) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            } catch (Exception e) {
                throw new KJException(FileUtils.class.getClass().getName(), e);
            } finally {
                closeIO(is, os);
            }
        }
    }

    /**
     * 从指定文件夹获取文件
     */
    public static File getSaveFile(String folder, String fileNmae) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsoluteFile()
                + File.separator
                + folder
                + File.separator
                + fileNmae);
        return file.exists() ? file : null;
    }

    /**
     * 获取文件夹路径
     */
    public static String getSavePath(String folderName) {
        return getSaveFolder(folderName).getAbsolutePath();
    }

    /**
     * 获取文件夹对象
     */
    public static File getSaveFolder(String folderName) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsoluteFile()
                + File.separator
                + folderName
                + File.separator);
        file.mkdirs();
        return file;
    }

    /**
     * 输入流转byte[]
     */
    public static final byte[] input2byte(InputStream inStream) {
        if (inStream == null)
            return null;
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    /**
     * 把uri转为File对象
     */
    public static File uri2File(Activity aty, Uri uri) {
        if (SystemTool.getSDKVersion() < 11) {
            // 在API11以下可以使用：managedQuery
            String[] proj = { MediaStore.Images.Media.DATA };
            @SuppressWarnings("deprecation")
            Cursor actualimagecursor = aty.managedQuery(uri, proj, null, null,
                    null);
            int actual_image_column_index = actualimagecursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor
                    .getString(actual_image_column_index);
            return new File(img_path);
        } else {
            // 在API11以上：要转为使用CursorLoader,并使用loadInBackground来返回
            String[] projection = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(aty, uri, projection, null,
                    null, null);
            Cursor cursor = loader.loadInBackground();
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return new File(cursor.getString(column_index));
        }
    }

    /**
     * 复制文件
     * 
     * @param from
     * @param to
     */
    public static void copyFile(File from, File to) {
        if (null == from || !from.exists()) {
            return;
        }
        if (null == to) {
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(from);
            if (!to.exists()) {
                to.createNewFile();
            }
            os = new FileOutputStream(to);

            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = is.read(buffer))) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            throw new KJException(FileUtils.class.getClass().getName(), e);
        } finally {
            closeIO(is, os);
        }
    }

    /**
     * 关闭流
     * 
     * @param closeables
     */
    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                throw new KJException(FileUtils.class.getClass().getName(), e);
            }
        }
    }

    /**
     * 图片写入文件
     * 
     * @param bitmap
     *            图片
     * @param filePath
     *            文件路径
     * @return 是否写入成功
     */
    public static boolean bitmapToFile(Bitmap bitmap, String filePath) {
        boolean isSuccess = false;
        if (bitmap == null)
            return isSuccess;
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filePath),
                    8 * 1024);
            isSuccess = bitmap.compress(CompressFormat.JPEG, 70, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeIO(out);
        }
        return isSuccess;
    }

    /**
     * 从文件中读取文本
     * 
     * @param filePath
     * @return
     */
    public static String readFile(String filePath) {
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
        } catch (Exception e) {
            throw new KJException(FileUtils.class.getName() + "readFile---->"
                    + filePath + " not found");
        }
        return inputStream2String(is);
    }

    /**
     * 从assets中读取文本
     * 
     * @param name
     * @return
     */
    public static String readFileFromAssets(Context context, String name) {
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(name);
        } catch (Exception e) {
            throw new KJException(FileUtils.class.getName()
                    + ".readFileFromAssets---->" + name + " not found");
        }
        return inputStream2String(is);

    }

    /**
     * 输入流转字符串
     * 
     * @param is
     * @return 一个流中的字符串
     */
    public static String inputStream2String(InputStream is) {
        if (null == is) {
            return null;
        }
        StringBuilder resultSb = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            resultSb = new StringBuilder();
            String len;
            while (null != (len = br.readLine())) {
                resultSb.append(len);
            }
        } catch (Exception ex) {
        } finally {
            closeIO(is);
        }
        return null == resultSb ? null : resultSb.toString();
    }

    /**
     * 纠正图片角度（有些相机拍照后相片会被系统旋转）
     * 
     * @param path
     *            图片路径
     */
    public static void correctPictureAngle(String path) {
        int angle = BitmapOperateUtil.readPictureDegree(path);
        if (angle != 0) {
            Bitmap image = BitmapHelper.rotate(angle,
                    BitmapCreate.bitmapFromFile(path, 1000, 1000));
            bitmapToFile(image, path);
        }
    }
}
