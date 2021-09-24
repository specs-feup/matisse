
	int i;

	if(t-><TENSOR_DIMS> == 0) {
		return;
	}
	
	printf("[%d", t-><TENSOR_SHAPE>[0]);
	for(i = 1; i< t-><TENSOR_DIMS>; i++) {
		printf(", %d", t-><TENSOR_SHAPE>[i]);
	}
	printf("]");
