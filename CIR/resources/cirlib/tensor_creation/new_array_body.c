/* new_row(int dim1, int dim2..., tensor** t) */
	/* int* shape; */
	int shape[<NUM_DIMS>];
	int dims;

	<SHAPE_INIT>
/*	shape = (int[<NUM_DIMS>]){<DIM_NAMES>}; */
	dims = <NUM_DIMS>;
	
	return <CALL_NEW_HELPER>(shape,  dims, t);	
