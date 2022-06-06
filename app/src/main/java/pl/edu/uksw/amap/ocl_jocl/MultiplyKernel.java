package pl.edu.uksw.amap.ocl_jocl;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clSetKernelArg;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

class MultiplyKernel extends OclKernelWrapper {

    public MultiplyKernel(OclContextWrapper contextWrapper, String kernelSource) {
        super(contextWrapper, kernelSource);
    }

    public void multiply(float srcArrayA[], float srcArrayB[], float dstArray[]) {
        int n = dstArray.length;

        // Allocate the memory objects for the input- and output data
        cl_mem srcMemA = clCreateBuffer(contextWrapper.getContext(),
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, Pointer.to(srcArrayA), null);
        cl_mem srcMemB = clCreateBuffer(contextWrapper.getContext(),
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, Pointer.to(srcArrayB), null);
        cl_mem dstMem = clCreateBuffer(contextWrapper.getContext(),
                CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);

        // Set the arguments for the kernel
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemA));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemB));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(dstMem));


        // Execute the kernel
        clEnqueueNDRangeKernel(contextWrapper.getCommandQueue(), kernel, 1, null,
                new long[]{n}, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(contextWrapper.getCommandQueue(), dstMem, CL_TRUE, 0,
                n * Sizeof.cl_float, Pointer.to(dstArray), 0, null, null);

        // Release memory objects
        CL.clReleaseMemObject(srcMemA);
        CL.clReleaseMemObject(srcMemB);
        CL.clReleaseMemObject(dstMem);
    }
}
