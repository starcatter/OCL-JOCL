package pl.edu.uksw.amap.ocl_jocl;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Size;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class OclAnalyzer implements ImageAnalysis.Analyzer {
    private final TextView sampleText;
    private final ImageView imageView;
    private final AnalyzerKernel kernel;

    public OclAnalyzer(TextView sampleText, ImageView imageView, AnalyzerKernel kernel) {
        this.sampleText = sampleText;
        this.imageView = imageView;
        this.kernel = kernel;
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        // get raw image buffer
        ByteBuffer inputImageBuffer = planes[0].getBuffer();

        final int inWidth = imageProxy.getWidth();
        final int inHeight = imageProxy.getHeight();

        // get processed buffer
        ByteBuffer processedBuffer = kernel.processImage(inputImageBuffer, inWidth, inHeight, imageView.getWidth(), imageView.getHeight());

        imageProxy.close();

        // create bitmap from buffer
        Bitmap finalBitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        finalBitmap.copyPixelsFromBuffer(processedBuffer);

        // enqueue imageView update
        imageView.post(() -> imageView.setImageBitmap(finalBitmap));

        // enqueue text field update
        sampleText.post(() -> sampleText.setText(String.format(java.util.Locale.US, "[%d x %s] -> [%d x %d]", inWidth, inHeight, imageView.getWidth(), imageView.getHeight())));
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Nullable
    @Override
    public Size getTargetResolutionOverride() {
        return new Size(imageView.getWidth(), imageView.getHeight());
    }
}
