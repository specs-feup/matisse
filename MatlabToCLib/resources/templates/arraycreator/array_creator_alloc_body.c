// new_matrix(int dim1, int dim2..., elementType value, tensor** t)
   int* shape;
   int dims;
   
   shape = (int[<NUM_DIMS>]){<DIM_NAMES>};
   dims = <NUM_DIMS>;
 
   <CALL_NEW>(shape, dims, t);
   <CALL_SET_MATRIX>(*t, value);
   
   return *t;
