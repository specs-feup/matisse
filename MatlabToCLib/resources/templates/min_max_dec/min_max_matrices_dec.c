
	int i;
	<MM_TEMP_TYPE> temp[<MM_LENGTH>];

	/* iterate over the matrixes and save the correct element on each position */
	for( i=0 ; i<<MM_LENGTH> ; i++ ){
		if( matrix1[i] <MM_OPERATOR> matrix2[i] )
		
			temp[i] = matrix1[i];
		else
		
			temp[i] = matrix2[i];
	}

	<MM_COPY_CALL>(temp, max);

	return max;
