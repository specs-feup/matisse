/* tensor* t */
	
	/* Determine the maximum dimension */
	int maxDim = 0;
	int i;
	int currentDim;
	
	for(i=0; i<t-><TENSOR_DIMS>; i++) {
		currentDim = t-><TENSOR_SHAPE>[i];
		
		/* If empty matrix, return 0 */
		if(currentDim == 0) {
			return 0;
		}
		
		if(currentDim > maxDim) {
			maxDim = currentDim;
		}
	}
	
	return maxDim;
