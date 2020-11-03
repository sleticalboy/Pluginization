package com.sleticalboy.pluginization.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 20-11-3.
 *
 * @author binli
 */
public final class IOs {

    public static void copy(String source, String destination) throws IOException {
        copy(new FileInputStream(source), new FileOutputStream(destination));
    }

    public static void copy(String source, File destination) throws IOException {
        copy(new FileInputStream(source), new FileOutputStream(destination));
    }

    public static void copy(File source, String destination) throws IOException {
        copy(new FileInputStream(source), new FileOutputStream(destination));
    }

    public static void copy(File source, File destination) throws IOException {
        copy(new FileInputStream(source), new FileOutputStream(destination));
    }

    public static void copy(InputStream source, String destination) throws IOException {
        copy(source, new FileOutputStream(destination));
    }

    public static void copy(InputStream source, File destination) throws IOException {
        copy(source, new FileOutputStream(destination));
    }

    public static void copy(InputStream source, OutputStream destination) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(source);
        BufferedOutputStream bos = new BufferedOutputStream(destination);
        byte[] buffer = new byte[8 * 1024];
        int len;
        while ((len = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }

    public static File file(String file) throws IOException {
        return file(new File(file));
    }

    public static File file(File file) throws IOException {
        if (file == null) {
            throw new IOException("file is null.");
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return file;
        }
        if (!parent.exists() && parent.mkdirs()) {
            Log.d("IOs", "create dirs: " + parent);
        }
        if (!file.exists() && file.createNewFile()) {
            Log.d("IOs", "create file: " + file);
        }
        return file;
    }
}
