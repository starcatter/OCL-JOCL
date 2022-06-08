package pl.edu.uksw.amap.ocl_jocl;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clFinish;
import static org.jocl.CL.clFlush;
import static org.jocl.CL.clSetKernelArg;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import java.nio.ByteBuffer;

class AnalyzerKernel extends OclKernelWrapper {

    public AnalyzerKernel(OclContextWrapper contextWrapper, String kernelSource) {
        super(contextWrapper, kernelSource);
    }

    public ByteBuffer processImage(ByteBuffer image, int srcWidth, int srcHeight, int outWidth, int outHeight) {
        int srcCapacity = srcWidth * srcHeight * 4; // RGBA8888
        int outCapacity = outWidth * outHeight * 4; // RGBA8888

        // Allocate the memory objects for the input- and output data
        cl_mem srcMemory = clCreateBuffer(contextWrapper.getContext(),
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * srcCapacity, Pointer.to(image), null);

        ByteBuffer outBuffer = ByteBuffer.allocate(outCapacity);
        cl_mem outMemory = clCreateBuffer(contextWrapper.getContext(),
                CL_MEM_READ_WRITE,
                Sizeof.cl_char * outCapacity, null, null);

        // Set the arguments for the kernel
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemory));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(outMemory));

        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{srcWidth}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{srcHeight}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{outWidth}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{outHeight}));

        // Execute the kernel
        clEnqueueNDRangeKernel(contextWrapper.getCommandQueue(), kernel, 1, null,
                new long[] {srcCapacity}, null, 0, null, null);

        // clFinish(contextWrapper.getCommandQueue());

        // Read the output data
        clEnqueueReadBuffer(contextWrapper.getCommandQueue(), outMemory, CL_TRUE, 0,
                outCapacity * Sizeof.cl_char, Pointer.to(outBuffer), 0, null, null);

        // free GPU resources
        CL.clReleaseMemObject(srcMemory);
        CL.clReleaseMemObject(outMemory);

        return outBuffer;
    }
}
