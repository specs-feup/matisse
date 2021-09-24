/*new_array_view(tensor* t, int offset, int length, tensor** view) */
	int* shape = (int[2]){1, length};
	int dims = 2;
   
	int i;
	int copy = <IS_COPY>;
	<DATA_TYPE>* temp_pointer;
	
	int isColumn = 0;
	int temp_int;

	// Check if matrix is column vector

	if(t->dims == 2 && t->shape[1] == 1) {
		isColumn = 1;
	}
	
	/* Check if given matrix exists */
	if(t == NULL) {
		printf("ERROR: Given matrix is NULL\n");
	    exit(EXIT_FAILURE);
	}
	
	/* Check if given view is not null. Copy values to view in that case. */
	if(*view != NULL) {
	
		/* If view size different, reallocate memory */
		if((*view)->length != length) {
			if(copy) {
				/* TODO: Realloc could bring any benefit? It complicates logic though, can only realloc if view owns data*/
				/* temp_pointer = (<DATA_TYPE>*) realloc((*view)->data, sizeof(<DATA_TYPE>) * length); */
				temp_pointer = (<DATA_TYPE>*) malloc(sizeof(<DATA_TYPE>) * length);
				if (temp_pointer == NULL) {
					printf("ERROR: Could not allocate the number of elements in view from %d to %d.\n", (*view)->length, length);
					exit(EXIT_FAILURE);
				}

				// Free previous data if view owns it
				if((*view)->owns_data) {
					free((*view)->data);
				}
				
				// Update view
				(*view)->data = temp_pointer;
				// Set data as owned
				(*view)->owns_data = 1;
			}
			
			
			(*view)->length = length;
			if(isColumn) {
				(*view)->shape[0] = length;
				(*view)->shape[1] = 1;
			} else {
				(*view)->shape[0] = 1;
				(*view)->shape[1] = length;
			}
		}

		/* Copy values. This assumes that a view once create as a copy, will always be used as a copy. Otherwise we might need to allocate memory, in case it does not own the data */
		if(copy) {
			if(!(*view)->owns_data) {
				printf("ERROR: Trying to copy elements to a view which does not own the data.\n");
				exit(EXIT_FAILURE);
			}
		
			for(i=0; i<length; i++) {
				(*view)-><TENSOR_DATA>[i] = t-><TENSOR_DATA>[i + offset];
			}
		} 
		/* Reassign pointer, free data is owned by view */
		else {
			if((*view)->owns_data) {
				free((*view)->data);
			}
			//free((*view)-><TENSOR_DATA>);
			(*view)-><TENSOR_DATA> = t-><TENSOR_DATA> + offset;
			(*view)->owns_data = 0;
		}

		// Sanity check
		if(!(*view)->owns_data) {
			if(((*view)->data + (*view)->length) > (t->data + t->length)) {
				printf("ERROR (get_view): View data, from existing view, overflows original data.\nView end: %p; Original data end: %p", ((*view)->data + (*view)->length - 1), (t->data + t->length - 1));
				exit(1);
			}
		}
		
		return view;
	}

	/* Create the tensor for the view and return it. */
	*view = (<TENSOR_STRUCT>*) malloc(sizeof(<TENSOR_STRUCT>));

	if (*view == NULL) {
       printf("ERROR: Could not allocate memory for the view structure\n");
	   exit(EXIT_FAILURE);
	}

	// Correct shape if matrix is column vector
	if(isColumn) {
		temp_int = shape[0];
		shape[0] = shape[1];
		shape[1] = temp_int;
	}
	
	if(length == 0) {
		(*view)-><TENSOR_DATA> = NULL;
	} else {
		/* Allocate memory and copy values */
		if(copy) {
			(*view)->owns_data = 1;
			(*view)-><TENSOR_DATA> = (<DATA_TYPE>*) malloc(sizeof(<DATA_TYPE>) * length);
			for(i=0; i<length; i++) {
				(*view)-><TENSOR_DATA>[i] = t-><TENSOR_DATA>[i + offset];
			}
		} 
		/* Assign a pointer to given tensor's data, plus an offset */
		else {
			(*view)->owns_data = 0;
			(*view)-><TENSOR_DATA> = t-><TENSOR_DATA> + offset;
		}
	
		
	}
	

	(*view)-><TENSOR_LENGTH> = length;


	(*view)-><TENSOR_SHAPE> = (int*) malloc(sizeof(int) * dims);
	if((*view)-><TENSOR_SHAPE> == NULL) {
		printf("ERROR: Could not allocated memory for the view shape\n");
		exit(EXIT_FAILURE);
    }	
	
	for (i = 0; i < dims; ++i) {
		(*view)-><TENSOR_SHAPE>[i] = shape[i];
	}

	(*view)-><TENSOR_DIMS> = dims;

	// Sanity check
	if(!(*view)->owns_data) {
		if(((*view)->data + (*view)->length) > (t->data + t->length)) {
			printf("ERROR (get_view): View data overflows original data.\nView end: %p; Original data end: %p", ((*view)->data + (*view)->length - 1), (t->data + t->length - 1));
			exit(1);
		}
	}
	
	return view;
