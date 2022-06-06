package pl.edu.uksw.amap.ocl_jocl;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_GPU;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseProgram;
import static org.jocl.CL.clSetKernelArg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import pl.edu.uksw.amap.ocl_jocl.databinding.ActivityMainBinding;

import org.jocl.CL;
import org.jocl.*;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initCL();
        runKernel();
        shutdownCL();
    }

    protected cl_context context;
    protected cl_command_queue commandQueue;
    protected cl_kernel kernel;

    private void initCL() {
        // Enable exceptions and subsequently omit error checks
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        if (numPlatforms == 0) {
            throw new RuntimeException("No OpenCL platforms available");
        }

        // Obtain a platform ID
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        if (numDevices == 0) {
            throw new RuntimeException("No devices with type " + CL.stringFor_cl_device_type(CL_DEVICE_TYPE_GPU) + " on platform " + 0);
        }

        // Obtain a device ID
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, numDevices, devices, null);
        cl_device_id device = devices[0];

        // Create a context for the selected device
        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);

        // ---
        // Create compute kernel
        // ---

        final String programSource =
                "__kernel void "+
                        "sampleKernel(__global const float *a,"+
                        "             __global const float *b,"+
                        "             __global float *c)"+
                        "{"+
                        "    int gid = get_global_id(0);"+
                        "    c[gid] = a[gid] * b[gid];"+
                        "}";

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context, 1, new String[]{ programSource }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, "sampleKernel", null);

        // No longer need the program object - kernel is ready for use
        clReleaseProgram(program);
    }

    private void shutdownCL() {
        clReleaseKernel(kernel);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    private void runKernel(){
        // Create input- and output data
        int n = 10;
        float srcArrayA[] = new float[n];
        float srcArrayB[] = new float[n];
        float dstArray[] = new float[n];
        for (int i=0; i<n; i++)
        {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        }

        // Allocate the memory objects for the input- and output data
        cl_mem srcMemA = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, Pointer.to(srcArrayA), null);
        cl_mem srcMemB = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, Pointer.to(srcArrayB), null);
        cl_mem dstMem = clCreateBuffer(context,
                CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);

        // Set the arguments for the kernel
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemA));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemB));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(dstMem));


        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                new long[] {n}, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, dstMem, CL_TRUE, 0,
                n * Sizeof.cl_float, Pointer.to(dstArray), 0, null, null);

        // Release memory objects
        CL.clReleaseMemObject(srcMemA);
        CL.clReleaseMemObject(srcMemB);
        CL.clReleaseMemObject(dstMem);

        // format output string
        StringBuilder resultString = new StringBuilder();
        for (int i = 0; i < n; i++) {
            resultString.append(String.format("%s * %s = %s\n", srcArrayA[i], srcArrayB[i], dstArray[i]));
        }
        System.out.println(resultString);
    }
}