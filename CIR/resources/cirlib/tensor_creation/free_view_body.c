
	/* If already null, return */
	if(*t == NULL) {
		return;
	}

	/* Free the shape data */
	free((*t)-><TENSOR_SHAPE>);
	(*t)-><TENSOR_SHAPE> = NULL;
	
	/* Free the tensor itself */
	free(*t);
	
	/* Set the pointer to null */
	*t = NULL;
