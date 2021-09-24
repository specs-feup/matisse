/* void copy_d(double* input, double* output) */
   int i;
   int size = <NUM_ELEMENTS>;
   
	/* Verify if they are the same array */
	if(input == output) {
		return;
	}

	for(i = 0; i<size; i = i+1)
	{
		output[i] = input[i];
	}
