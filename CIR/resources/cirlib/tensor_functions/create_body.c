/*  create(int index_1, int index_2... tensor_d** t) */


	int TRIES = 2;
	int i;
	int length;
	int dims;
	
	/* If matrix is already allocated, free it */
	if(*t != NULL) {
	
		<CALL_FREE>;
		*t = NULL;
	}

	/* Create the tensor and return it. */	
	for(i=0; i<TRIES; i++) {
		*t = (<TENSOR_STRUCT>*) malloc(sizeof(<TENSOR_STRUCT>));
		if (*t != NULL) {
			break;
		}
	}

	if (*t == NULL) {
		printf("ERROR: Could not allocate memory for the matrix structure\n");
		exit(EXIT_FAILURE);
	}

	/* Calculate the length of the linearized version of the tensor. */
	length = <LENGTH_CALC>;
	dims = <DIMS>;


	if(length == 0) {
		(*t)->data = NULL;
	} else {
		(*t)->data = (<DATA_TYPE>*) <CUSTOM_DATA_ALLOCATOR>[[sizeof(<DATA_TYPE>) * length]];
		if((*t)->data == NULL) {
			printf("ERROR: Could not allocate memory for the matrix elements (%d elements)\n", length);
			exit(EXIT_FAILURE);
		}
	}
	

	(*t)->length = length;

	(*t)->shape = (int*) malloc(sizeof(int) * dims);
	if((*t)->shape == NULL) {
		printf("ERROR: Could not allocated memory for the matrix shape\n");
		exit(EXIT_FAILURE);
	}
	
<SHAPE_ASSIGN>
while (dims > 2 && (*t)->shape[dims - 1] == 1) {
	--dims;
}

<INITIALIZE_DATA>

	(*t)->dims = dims;

	/* Data is owned by the tensor, since it allocated it */
	(*t)->owns_data = 1;
	
	return *t;
