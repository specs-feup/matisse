
	int i;
	<MM_MAX_TYPE> max;

	/* assign the first element of the input to the output */
	//max = vector[0];
	max = <MM_VECTOR_GET_0>;
			
	/* test the rest of the values against this one */
	for( i=1 ; i<<MM_LENGTH> ; i++ ){
		
		if( <MM_VECTOR_GET_I> <MM_OPERATOR> max ){
			max = <MM_VECTOR_GET_I>;
		}
	}

	return max;
