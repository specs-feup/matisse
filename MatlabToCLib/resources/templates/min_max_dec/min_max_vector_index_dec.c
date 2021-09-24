
	int i;

	/* assign the first element of the input to the output */
	*max = vector[0];
	*index = 0;
			
	/* test the rest of the values against this one */
	for( i=1 ; i<<MM_LENGTH> ; i++ ){
		if( vector[i] <MM_OPERATOR> *max ){
		
			*max = vector[i];
			*index = i;
		}
	}
	// index ajusted to match matlab
	*index += 1;
