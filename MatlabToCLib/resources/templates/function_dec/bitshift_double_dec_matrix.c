int abs_<SECOND_INPUT>;
int i;


// Check the whole matrix to see if
for ( i=0 ; i<<MATRIX_LENGTH> ; i++ ){
	
	// The input is not an integer 
	if( !<IS_INTEGER_CALL>(<FIRST_INPUT>[i]) ){
		printf("Double inputs must have integer values in the range of ASSUMEDTYPE.");
		exit(EXIT_FAILURE);
	}
	
	// The input is not in range 
	if( !<IN_RANGE_CALL>(<FIRST_INPUT>[i]) ){
		printf("Double inputs must have integer values in the range of ASSUMEDTYPE.");
		exit(EXIT_FAILURE);
	}	
}

// Perform the shift operations
if( <SECOND_INPUT><0 )
{
	abs_<SECOND_INPUT> = <SECOND_INPUT> * -1;
	for( i=0 ; i<<MATRIX_LENGTH> ; i++ )
	{
		<OUTPUT>[i] = ((<TYPE>)<FIRST_INPUT>[i]) >> abs_<SECOND_INPUT>;
	}
}
else
{
	for( i=0 ; i<<MATRIX_LENGTH> ; i++ )
	{
		<OUTPUT>[i] = ((<TYPE>)<FIRST_INPUT>[i]) << <SECOND_INPUT>;
	}
}

return <OUTPUT>;
