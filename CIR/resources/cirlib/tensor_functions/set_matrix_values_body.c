/* set_matrix_values(tensor* t, elementType value) */

	int i;
	
	/* Set the values inside the tensor */
	for(i = 0; i<t-><TENSOR_LENGTH>; i = i+1)
   {
      t-><TENSOR_DATA>[i] = value;
   }
