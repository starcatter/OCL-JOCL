package pl.edu.uksw.amap.ocl_jocl;

import org.jocl.cl_kernel;

abstract class OclKernelWrapper {
    protected final OclContextWrapper contextWrapper;
    protected cl_kernel kernel = null;

    public OclKernelWrapper(OclContextWrapper contextWrapper, String kernelSource) {
        this.contextWrapper = contextWrapper;
        kernel = contextWrapper.createKernel(kernelSource);
    }

    public void free() {
        contextWrapper.freeKernel(kernel);
    }
}
