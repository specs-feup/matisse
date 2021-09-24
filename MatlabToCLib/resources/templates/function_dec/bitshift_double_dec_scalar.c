int abs_<SECOND_INPUT>;
int <OUTPUT>;

// Check if the input is not an integer 
if( !<IS_INTEGER_CALL>(<FIRST_INPUT>) ){
	printf("Double inputs must have integer values in the range of ASSUMEDTYPE.");
	exit(EXIT_FAILURE);
}

// Check if the input is not in range 
if( !<IN_RANGE_CALL>(<FIRST_INPUT>) ){
	printf("Double inputs must have integer values in the range of ASSUMEDTYPE.");
	exit(EXIT_FAILURE);
}

// Perform the shift operation
if( <SECOND_INPUT> < 0 )
{
	abs_<SECOND_INPUT> = <SECOND_INPUT> * -1;
	<OUTPUT> = (<TYPE>)<FIRST_INPUT> >> abs_<SECOND_INPUT>;
}
else
{
	<OUTPUT> = (<TYPE>)<FIRST_INPUT> << <SECOND_INPUT>;
}

return <OUTPUT>;