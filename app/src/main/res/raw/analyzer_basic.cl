__kernel void sampleKernel(
   __global const char *inputImage,
   __global char *outputImage,
   __private const unsigned int width,
   __private const unsigned int height,
   __private const unsigned int width_out,
   __private const unsigned int height_out
) {
   int gid = get_global_id(0);
   const char value = inputImage[gid];
   const int pixel = gid / 4;
   const int component = gid % 4;

   // source buffer index to x/y
   const int source_column = pixel % width;
   const int source_row = pixel / width;

   // rotate
   const int target_column = height-source_row;
   const int target_row = source_column;

   // scale
   const int target_column_scaled = width>width_out
      ? (float)target_column * ((float)width_out/(float)width)
      : target_column;

   const int target_row_scaled = height > height_out
      ? (float)target_row * ((float)height_out/(float)height)
      : target_row;

   // calcualte target buffer index
   const int target_pixel = (target_row_scaled*width_out) + target_column_scaled;
   const int target_index = target_pixel*4 + component;

   outputImage[target_index] = component == 4 ? 255 : value;
}
