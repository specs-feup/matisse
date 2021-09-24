	
	/* Check if given index is not bigger than the tensor */
	if(index >= t-><TENSOR_DIMS>) {
		return 1;
	}

	return t-><TENSOR_SHAPE>[index];
