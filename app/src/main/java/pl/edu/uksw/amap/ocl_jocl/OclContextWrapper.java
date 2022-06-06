package pl.edu.uksw.amap.ocl_jocl;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_GPU;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseProgram;

import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

class OclContextWrapper {
    protected cl_context context;
    protected cl_command_queue commandQueue;

    public void initCL() {
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
    }

    public void shutdownCL() {
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    public cl_kernel createKernel(String programSource) {
        // ---
        // Create compute kernel
        // ---

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);

        // No longer need the program object - kernel is ready for use
        clReleaseProgram(program);

        return kernel;
    }

    public void freeKernel(cl_kernel kernel) {
        clReleaseKernel(kernel);
    }

    public cl_context getContext() {
        return context;
    }

    public cl_command_queue getCommandQueue() {
        return commandQueue;
    }
}
