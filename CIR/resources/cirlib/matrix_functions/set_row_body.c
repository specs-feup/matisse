/* void set_row(matrix m, int offset, int elements, matrix values) */
	int i;
	int valuesElements = <NUMELS_VALUES_CALL>;
	
	/* If only one element, set all values to that element */
	if(valuesElements == 1) {
		for(i=0; i<elements; i++) {
			<SET_M_IOFFSET_VALUES0>;
		}
		
		return;
	}
	
	/* Check if number of elements is the same */
	if(elements != valuesElements) {
		printf("In an assignment  A(I) = B, the number of elements in B and I must be the same.\n");
		exit(EXIT_FAILURE);
	}
	
	for(i = 0; i<elements; i++) {
		<SET_M_IOFFSET_VALUESI>;
	}
