
	int exponent_int = (int) exponent;

	/* return the identity matrix if the exponent_int is 0 */
	if(exponent_int == 0){
		<MP_EYE_CALL>(power);
		return power;
	}
	
	/* copy the contents of the base to the power ( output ) */
	<MP_COPY_CALL>(base, power);
	
	/* create the accumulator and initialize
	it with the identity matrix */
	<MP_INPUT_NUM_TYPE> accumulator[<MP_LENGTH>];
	<MP_EYE_CALL>(accumulator);

	
	while(exponent_int!=1){

		/* if exponent_int is odd, save the current power in
		the accumulator and decrease the exponent_int by 1 */
		if(exponent_int % 2 != 0){
			
			<MP_MAT_MULT_CALL>(accumulator, power, accumulator);
			exponent_int--;
		}

		/* keep squaring the base */
		<MP_MAT_MULT_CALL>(power, power, power);
		exponent_int = exponent_int / 2;
	}

	<MP_MAT_MULT_CALL>(power, accumulator, power);
	
	return 	power;
