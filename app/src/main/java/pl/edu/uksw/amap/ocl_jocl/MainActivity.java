package pl.edu.uksw.amap.ocl_jocl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import pl.edu.uksw.amap.ocl_jocl.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private OclContextWrapper ocl;

    private String readResourceString(int resourceId) {
        InputStream ins = getResources().openRawResource(resourceId);
        try {
            byte[] b = new byte[ins.available()];
            ins.read(b);
            return new String(b);
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource id:" + resourceId, e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocl = new OclContextWrapper();
        ocl.initCL();

        //runMultiplyKernel();
        runMatrixKernel();

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

        for (int i = 0; i < n; i++) {
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


    private void runMatrixKernel() {
        // create kernel from source
        String kernelSource = readResourceString(R.raw.matrix2d);
        Matrix2DKernel kernel = new Matrix2DKernel(ocl, kernelSource);

        // For matrix multiplication, the number of columns in the first matrix must be equal to the
        // number of rows in the second matrix. The resulting matrix, known as the matrix product,
        // has the number of rows of the first and the number of columns of the second matrix.
        // C=AB
        // matrix A is m*n
        // matrix B is n*p
        // matrix C is m*p
        // m == p

        // Create input- and output data
        int m = 3;
        int n = 2;
        int p = 3;

        float[] srcArrayA = new float[m * n];
        float[] srcArrayB = new float[n * p];
        float[] dstArray = new float[m * p];

        for (int i = 0; i < n*p; i++) {
            srcArrayA[i] = i+1;
            srcArrayB[i] = i+1;
        }

        // run kernel
        kernel.multiplyMatrix(srcArrayA, srcArrayB, dstArray, m, n, p);

        // cleanup
        kernel.free();

        // format output string
        StringBuilder resultString = new StringBuilder();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                resultString.append("[").append(srcArrayA[j*m + i]).append("] ");
            }
            resultString.append("\n");
        }

        resultString.append("*\n");

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                resultString.append("[").append(srcArrayB[j*n + i]).append("] ");
            }
            resultString.append("\n");
        }

        resultString.append("=\n");

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                resultString.append("[").append(dstArray[j*m + i]).append("] ");
            }
            resultString.append("\n");
        }

        binding.sampleText.setText(resultString);
    }

}