	if( <MP_SECOND_INPUT><0 ){
		printf("This implementation can only work with positive values of <MP_SECOND_INPUT>.\n");
		exit(EXIT_FAILURE);
	}
	
	if( <MP_SECOND_INPUT>==0 ){
		double ret[<MP_LENGTH>];
		return <MP_EYE_CALL>(ret);
	}
	
	if( <MP_SECOND_INPUT>==1 ){
		<MP_OUTPUT> = <MP_FIRST_INPUT>;
		return <MP_OUTPUT>;
	}
	
	if( <MP_SECOND_INPUT>%2 != 0 ){
		// base * mpower(base*base, (power-1)/2)
		
		double mult[<MP_LENGTH>];
		<MP_MAT_MULT_CALL>(<MP_FIRST_INPUT>,<MP_FIRST_INPUT>, mult);
		
		double self[<MP_LENGTH>];
		 <MP_SELF_CALL>(mult, (<MP_SECOND_INPUT>-1)/2, self);
		
		<MP_MAT_MULT_CALL>(<MP_FIRST_INPUT>, self, <MP_OUTPUT>);
	}else{
		// mpower(base*base, power/2)
		
		double mult[<MP_LENGTH>];
		<MP_MAT_MULT_CALL>(<MP_FIRST_INPUT>,<MP_FIRST_INPUT>, mult);
		
		<MP_SELF_CALL>(mult, <MP_SECOND_INPUT>/2, <MP_OUTPUT>);
	}
	
	return <MP_OUTPUT>;
