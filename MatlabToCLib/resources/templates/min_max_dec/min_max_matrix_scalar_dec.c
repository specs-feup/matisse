	
	int i;
	<MM_TEMP_TYPE> temp[<MM_LENGTH>];
	
	/* iterate over the matrixes and save the correct element on each position */
	for( i=0 ; i<<MM_LENGTH> ; i++ )
		if( matrix[i] <MM_OPERATOR> scalar )
		
			temp[i] = matrix[i];
		else
		
			temp[i] = scalar;
			
	<MM_COPY_CALL>(temp, max);
	
	return max;
