	int i;
	int result;
	
	/* Check if it has the same number of dimensions */
	if(t-><TENSOR_DIMS> != dims) {
		return 0;
	}

	/* As default, result is one */
	result = 1;
	
	/* Check if all dimensions of the tensor are the same */
	for(i=0; i<dims; i++) {
		if(t-><TENSOR_SHAPE>[i] != shape[i]) {
			result = 0;
		}
	}

	/* Return result */
	
	return result;