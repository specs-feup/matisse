
	/* If already null, return */
	if(*t == NULL) {
		return;
	}

	/* Free the values, if tensor owns the data */
	if((*t)-><TENSOR_OWNS_DATA>) {
		<FREE_DATA_FUNCTION>[[(*t)-><TENSOR_DATA>]];
		(*t)-><TENSOR_DATA> = NULL;
	}
	
	/* Free the shape data */
	free((*t)-><TENSOR_SHAPE>);
	(*t)-><TENSOR_SHAPE> = NULL;
	
	/* Free the tensor itself */
	free(*t);
	
	/* Set the pointer to null */
	*t = NULL;
