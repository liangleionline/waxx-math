package com.lianglei.waxxmath;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 最小化 FileProvider：仅暴露 Download/waxx 目录下的 PDF 供系统分享（兼容旧版 /sdcard/waxx）。
 * 不引入 AndroidX。
 */
public class WaxxFileProvider extends ContentProvider {

    public static String authority() {
        return "com.lianglei.waxxmath.fileprovider";
    }

    public static Uri uriFor(File file) {
        return new Uri.Builder()
                .scheme("content")
                .authority(authority())
                .appendPath(file.getName())   // appendPath 会自动做百分号编码，中文名不会出错
                .build();
    }

    private File resolve(Uri uri) throws FileNotFoundException {
        String name = uri.getLastPathSegment();
        if (name == null) throw new FileNotFoundException("bad uri");
        // 当前版本保存在 Download/waxx；老版本在 /sdcard/waxx，做一次兼容
        File current = new File(
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "waxx"),
                name);
        if (current.exists()) return current;
        File legacy = new File(new File(Environment.getExternalStorageDirectory(), "waxx"), name);
        if (legacy.exists()) return legacy;
        throw new FileNotFoundException(current.getAbsolutePath());
    }

    @Override
    public boolean onCreate() { return true; }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f = resolve(uri);
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String sel, String[] sa, String sort) {
        try {
            File f = resolve(uri);
            MatrixCursor c = new MatrixCursor(new String[]{
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
            c.addRow(new Object[]{f.getName(), f.length()});
            return c;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        return "application/pdf";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(Uri uri, String s, String[] sa) { return 0; }

    @Override
    public int update(Uri uri, ContentValues values, String s, String[] sa) { return 0; }
}
