	int shape[<NUM_DIMS>];
	int dims;

	<SHAPE_INIT>
	dims = <NUM_DIMS>;
	
	<CALL_NEW_HELPER>(shape, dims, cellArray);
	return *cellArray;