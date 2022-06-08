package pl.edu.uksw.amap.ocl_jocl;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pl.edu.uksw.amap.ocl_jocl.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private OclContextWrapper contextWrapper;

    // UI
    private TextView sampleText;
    private ImageView imageView;
    private Button button;

    // Executor for analyzer
    private Executor executor = Executors.newSingleThreadExecutor();
    private AnalyzerKernel analyzerKernel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // bind UI elements
        sampleText = binding.sampleText;
        imageView = binding.imageView;
        button = binding.button;

        // bind button action
        button.setOnClickListener(this::startCamera);

        // Request camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        contextWrapper = new OclContextWrapper();
        contextWrapper.initCL();

        String kernelSource = ResourceHelper.readResourceString(R.raw.analyzer_basic, this.getResources());
        analyzerKernel = new AnalyzerKernel(contextWrapper, kernelSource);
    }

    public void startCamera(View view){
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(executor, new OclAnalyzer(sampleText, imageView, analyzerKernel));

                // select back facing camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        button.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contextWrapper.shutdownCL();
    }
}