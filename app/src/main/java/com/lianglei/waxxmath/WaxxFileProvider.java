package com.lianglei.waxxmath;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 最小化 FileProvider：仅暴露 /sdcard/waxx 目录下的文件供系统分享，
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
                .encodedPath(file.getName())
                .build();
    }

    private File resolve(Uri uri) throws FileNotFoundException {
        String name = uri.getLastPathSegment();
        if (name == null) throw new FileNotFoundException("bad uri");
        File f = new File(android.os.Environment.getExternalStorageDirectory(), "waxx/" + name);
        if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
        return f;
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
