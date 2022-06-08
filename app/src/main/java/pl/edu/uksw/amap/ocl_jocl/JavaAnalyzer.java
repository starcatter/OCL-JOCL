package pl.edu.uksw.amap.ocl_jocl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class JavaAnalyzer implements ImageAnalysis.Analyzer {
    private final TextView sampleText;
    private final ImageView imageView;

    public JavaAnalyzer(TextView sampleText, ImageView imageView) {
        this.sampleText = sampleText;
        this.imageView = imageView;
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        // get raw image buffer
        ByteBuffer inputImageBuffer = planes[0].getBuffer();

        // create bitmap from buffer
        Bitmap bitmapImage = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
        bitmapImage.copyPixelsFromBuffer(inputImageBuffer);

        // call imageProxy.close() ASAP, have to get transformMatrix before that
        Matrix transformMatrix = imageProxy.getImageInfo().getSensorToBufferTransformMatrix();
        transformMatrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

        imageProxy.close();

        // resize the bitmap to match imageview
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, imageView.getHeight(), imageView.getWidth(), true);

        // transform the bitmap to match camera orientation
        Bitmap finalBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, imageView.getHeight(), imageView.getWidth(), transformMatrix, true);

        // enqueue imageView update
        imageView.post(() -> imageView.setImageBitmap(finalBitmap));

        // enqueue text field update
        sampleText.post(() -> sampleText.setText(String.format(java.util.Locale.US, "[%d x %s] -> [%d x %d]", bitmapImage.getWidth(), bitmapImage.getHeight(), imageView.getWidth(), imageView.getHeight())));
    }
}
