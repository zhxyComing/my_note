package com.app.xuzheng.mynote.Utils;

/**
 * Created by xuzheng on 2017/8/2.
 * <p>
 * Java downloadHelper类 用于下载新版app
 * <p>
 * ★ 阅读 学习 记忆
 * <p>
 * 使用方法：
 * new DownLoaderHelper.Builder(context).setPath(getStorePath(context)+"/pms.apk")
 * .setProgressListener(listener).setUrl(TextUtils.isEmpty(url)?"https://spapk.qunarzz.com/pms/quhuhu_pms.apk":url).create().downLoad();
 * <p>
 * 由于现在没有一个后台提供验证升级接口，来调起升级，所以升级这块暂时不做
 * Bmob可以解决这个问题，但是暂时不用
 * 现在直接使用appStore的验证升级方式，app内不做验证升级
 */

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoaderHelper {
    private Context mContext;
    private DownLoaderHelper.Builder builder;

    private DownLoaderHelper(Context context, DownLoaderHelper.Builder builder) {
        this.mContext = context;
        this.builder = builder;
    }

    public void downLoad() {
        DownLoaderHelper.DownThread thread = new DownLoaderHelper.DownThread();
        thread.start();
    }

    public long downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {
        int downloadCount = 0;
        byte currentSize = 0;
        long totalSize = 0L;
        boolean updateTotalSize = false;
        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            if (this.builder.getProgressListener() != null) {
                this.builder.getProgressListener().begin();
            }

            URL url = new URL(downloadUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
            if (currentSize > 0) {
                httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
            }

            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(20000);
            int updateTotalSize1 = httpConnection.getContentLength();
            if (httpConnection.getResponseCode() == 404 && this.builder.getProgressListener() != null) {
                this.builder.getProgressListener().error();
            }

            String totalKB = String.valueOf(updateTotalSize1 / 1024);
            is = httpConnection.getInputStream();
            fos = new FileOutputStream(saveFile, false);
            byte[] buffer = new byte[4096];
            boolean readsize = false;

            while (true) {
                do {
                    int readsize1;
                    if ((readsize1 = is.read(buffer)) <= 0) {
                        if (this.builder.getProgressListener() != null) {
                            if ("0".equals(totalKB)) {
                                this.builder.getProgressListener().error();
                            } else {
                                this.builder.getProgressListener().done(saveFile.getPath());
                            }

                            return totalSize;
                        }

                        return totalSize;
                    }

                    fos.write(buffer, 0, readsize1);
                    totalSize += (long) readsize1;
                }
                while (downloadCount != 0 && (int) (totalSize * 100L / (long) updateTotalSize1) - 2 <= downloadCount);

                downloadCount += 2;
                if (this.builder.getProgressListener() != null) {
                    this.builder.getProgressListener().progress((int) totalSize * 100 / updateTotalSize1);
                }
            }
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }

            if (is != null) {
                is.close();
            }

            if (fos != null) {
                fos.close();
            }

        }
    }

    public static class Builder {
        private String path;
        private String fileName;
        private Context mContext;
        private String url;
        private DownLoadProgressListener listener;

        public Builder(Context context) {
            this.mContext = context;
        }

        public DownLoaderHelper.Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public DownLoaderHelper.Builder setStoreName(String name) {
            this.fileName = name;
            return this;
        }

        public DownLoaderHelper.Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getUrl() {
            return this.url;
        }

        public DownLoaderHelper.Builder setProgressListener(DownLoadProgressListener listener) {
            this.listener = listener;
            return this;
        }

        public DownLoadProgressListener getProgressListener() {
            return this.listener;
        }

        public DownLoaderHelper create() {
            return new DownLoaderHelper(this.mContext, this);
        }
    }

    private class DownThread extends Thread {
        private DownThread() {
        }

        public void run() {
            super.run();
            File saveFile = new File(DownLoaderHelper.this.builder.path);

            try {
                DownLoaderHelper.this.downloadUpdateFile(DownLoaderHelper.this.builder.getUrl(), saveFile);
            } catch (Exception var3) {
                if (DownLoaderHelper.this.builder.getProgressListener() != null) {
                    DownLoaderHelper.this.builder.getProgressListener().error();
                }
            }
        }
    }
}

