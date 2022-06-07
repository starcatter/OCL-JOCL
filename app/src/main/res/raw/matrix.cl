__kernel void
sampleKernel(__global const float *a,
             __global const float *b,
             __global float *c,
             int m,
             int n,
             int p
             )
{
    // For matrix multiplication, the number of columns in the first matrix must be equal to the
    // number of rows in the second matrix. The resulting matrix, known as the matrix product,
    // has the number of rows of the first and the number of columns of the second matrix.
    // C=AB
    // matrix A is m*n
    // matrix B is n*p
    // matrix C is m*p
    int gid = get_global_id(0);

    const int cX = gid % m;
    const int cY = gid / m;

    //C(x,y) = A(x,1)*B(1,y) + A(x,2)*B(2,y) + ...

    float cXY = 0;

    for(int i = 0; i<m; i++){
        // in A, cX = column, i = row
        // aXY = A(cX,i)
        const int aIndex = i * m + cX;
        const float aXY = a[aIndex];

        // in B, i = column, cY = row
        // bXY = A(i,cY)
        const int bIndex = cY * n + i;
        const float bXY = a[bIndex];

        cXY += aXY*bXY;
    }

    c[gid] = cXY;
}
