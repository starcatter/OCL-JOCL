package pl.edu.uksw.amap.ocl_jocl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import pl.edu.uksw.amap.ocl_jocl.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private OclContextWrapper ocl;

    private String readResourceString(int resourceId){
        InputStream ins = getResources().openRawResource(resourceId);
        try {
            byte[] b = new byte[ins.available()];
            ins.read(b);
            return new String(b);
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource id:"+resourceId, e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocl = new OclContextWrapper();
        ocl.initCL();

        runMultiplyKernel();

        ocl.shutdownCL();
    }

    private void runMultiplyKernel() {
        // create kernel from source
        String kernelSource = readResourceString(R.raw.multiply);
        MultiplyKernel kernel = new MultiplyKernel(ocl, kernelSource);

        // Create input- and output data
        int n = 3;

        float[] srcArrayA = new float[n];
        float[] srcArrayB = new float[n];
        float[] dstArray = new float[n];

        for (int i=0; i<n; i++)
        {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        }

        // run kernel
        kernel.multiply(srcArrayA, srcArrayB, dstArray);

        // cleanup
        kernel.free();

        // format output string
        StringBuilder resultString = new StringBuilder();
        for (int i = 0; i < n; i++) {
            resultString.append(String.format("%s * %s = %s\n", srcArrayA[i], srcArrayB[i], dstArray[i]));
        }

        binding.sampleText.setText(resultString);
    }

}