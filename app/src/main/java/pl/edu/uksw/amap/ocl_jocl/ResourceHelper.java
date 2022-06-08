package pl.edu.uksw.amap.ocl_jocl;

import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;

public class ResourceHelper {
    private static String readResourceString(Resources resources, int resourceId) {
        InputStream ins = resources.openRawResource(resourceId);
        try {
            byte[] b = new byte[ins.available()];
            ins.read(b);
            return new String(b);
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource id:" + resourceId, e);
        }
    }
}
