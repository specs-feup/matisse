/* tensor* input_matrix, tensor** transposed_matrix */

	<TENSOR_STRUCT>* temp = NULL;
	int i;
	int j;
	int dim1, dim2;

	if (input_matrix-><TENSOR_DIMS> != 2) {
		printf("Transpose on ND array is not defined. Array has %d dimensions.\n", input_matrix-><TENSOR_DIMS>);
		for (i = 0; i < input_matrix->dims; ++i) {
			printf("%d\n", input_matrix-><TENSOR_SHAPE>[i]);
		}
		exit(EXIT_FAILURE);
	}

	/* If input and output are the same, allocate matrix to temporary array */
	if (input_matrix == *transposed_matrix) {
		<CALL_NEW_ARRAY>(input_matrix-><TENSOR_SHAPE>[1], input_matrix-><TENSOR_SHAPE>[0], &temp);
	}
	/* Otherwise, allocate to output matrix and work on temporary array */
	else {
		<CALL_NEW_ARRAY>(input_matrix-><TENSOR_SHAPE>[1], input_matrix-><TENSOR_SHAPE>[0],
				transposed_matrix);
		temp = *transposed_matrix;
	}

	/* Get dimensions */
	dim1 = <CALL_DIM_SIZE>(input_matrix, 0);
	dim2 = <CALL_DIM_SIZE>(input_matrix, 1);

	for (i = 0; i < dim1; i++) {
		for (j = 0; j < dim2; j++) {
			<FULLCALL_SET>;
		}
	}

	<CALL_COPY>(temp, transposed_matrix);

	return *transposed_matrix;
