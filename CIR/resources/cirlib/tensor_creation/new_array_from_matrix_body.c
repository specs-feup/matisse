/*tensor_d** new_array_d(tensor_i* shape, tensor_d** t) */
	int dims = <CALL_NUMEL_SHAPE>;
	int* newShape = malloc(sizeof(int) * dims);
	int i;

	for(i=0;i<dims;i++) {
		newShape[i] = <CALL_GET_I>;

	}

	<CALL_NEW_HELPER>(newShape,  dims, t);
	free(newShape);
	return *t;
