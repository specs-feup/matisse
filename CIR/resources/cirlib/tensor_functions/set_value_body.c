/* set_matrix_value(tensor* t, int index_1, int index_2..., ? value) */

	int index;
	
	/* Get index */
	index = <SUB2IDX_CALL>;
	
	/* Check if the index is inside the matrix range */
	if(index >= t-><TENSOR_LENGTH>) {
		printf("ERROR (<FUNCTION_NAME>): Trying to access position %d in matrix with %d elements\n", index, t-><TENSOR_LENGTH>);
		exit(EXIT_FAILURE);
	}
	
	/* Set the value */
	<ASSIGNMENT>;
