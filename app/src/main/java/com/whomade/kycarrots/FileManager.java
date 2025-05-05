package com.whomade.kycarrots;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileManager {

    private static FileManager sFileManager;
    private Context mContext;
    private static String LOG_TAG          = "temp";

    public static FileManager get(Context context) {
        if(sFileManager ==null){
            sFileManager = new FileManager(context);
        }
        return sFileManager;
    }

    public  static Uri photoUri;

    private FileManager(Context context){
        mContext = context;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("temp", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
