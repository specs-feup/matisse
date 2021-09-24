/* new_row(int* shape, int dims, tensor** t) */

	int TRIES = 2;
	int i;
	int length;
	int sameShape;
	int* previous_shape = NULL;
	<DATA_TYPE>* previous_data = NULL;
	
	/* Check if matrix is already allocated */
	if(*t != NULL) {
	
		/* If shape is the same, return current matrix, */
		/* even if it does not own the data (so it can implement window-writing) */
		sameShape = <CALL_IS_SAME_SHAPE>(*t, shape, dims);	
		//if(sameShape && (*t)->owns_data) {
		if(sameShape) {
			return *t;
		} 		
		
		/* Save pointers to previous shape and data */
		/* Only free data if tensor owns it */
		if((*t)->owns_data) {
			previous_data = (*t)-><TENSOR_DATA>;
		}
		
		previous_shape = (*t)-><TENSOR_SHAPE>;		
	}

	/* Create the tensor and return it. */
	free(*t);
	
	for(i=0; i<TRIES; i++) {
		*t = (<TENSOR_STRUCT>*) malloc(sizeof(<TENSOR_STRUCT>));
		if (*t != NULL) {
			break;
		}
	}
	/**t = (<TENSOR_STRUCT>*) malloc(sizeof(<TENSOR_STRUCT>)); */

	if (*t == NULL) {
       printf("ERROR: Could not allocate memory for the matrix structure\n");
	   exit(EXIT_FAILURE);
	}

	/* Calculate the length of the linearized version of the tensor. */
	length = 1;
	for (i = 0; i < dims; i++) {
		length *= shape[i];
	}

	<FREE_DATA_FUNCTION>[[previous_data]];
	if(length == 0) {
		(*t)-><TENSOR_DATA> = NULL;
	} else {
		(*t)-><TENSOR_DATA> = (<DATA_TYPE>*) <CUSTOM_DATA_ALLOCATOR>[[sizeof(<DATA_TYPE>) * length]];
		if((*t)-><TENSOR_DATA> == NULL) {
			printf("ERROR: Could not allocate memory for the matrix elements (%d elements)\n", length);
			exit(EXIT_FAILURE);
        }
	}
	

	(*t)-><TENSOR_LENGTH> = length;

	free(previous_shape);
	(*t)-><TENSOR_SHAPE> = (int*) malloc(sizeof(int) * dims);
	if((*t)-><TENSOR_SHAPE> == NULL) {
		printf("ERROR: Could not allocated memory for the matrix shape\n");
		exit(EXIT_FAILURE);
    }	
	
	for (i = 0; i < dims; ++i) {
		(*t)-><TENSOR_SHAPE>[i] = shape[i];
	}

	while (dims > 2 && shape[dims - 1] == 1) {
		--dims;
	}

<INITIALIZE_DATA>

	(*t)-><TENSOR_DIMS> = dims;

	/* Data is owned by the tensor, since it allocated it */
	(*t)-><TENSOR_OWNS_DATA> = 1;
	
	return *t;
