
	int i;
	<FLOAT_TYPE> n;
	<FLOAT_TYPE> q;
	<FLOAT_TYPE> r;
	<FLOAT_TYPE> c;
	int n_i;
	int output_length;
	int k_length;

	// tolerance and sign
	//<FLOAT_TYPE> eps = 2.2204e-016;
	<FLOAT_TYPE> eps = <EPS>;
	<FLOAT_TYPE> tol = <TWO_LITERAL> * eps * <CA_MAX_CALL>(<CA_ABS_START_CALL>(start), <CA_ABS_END_CALL>(end));
	int sig = 0;
	if(step>0){
		sig = 1;
	}else if(step<0){
		sig = -1;
	}

	// consecutive integers
	if(start == floor(start) && step == 1){
		n = floor(end) - start;
	}else{
		// integers with spacing > 1
		if(start == floor(start) && step == floor(step)){
			q = floor(start / step);
			r = start - q * step;
			n = floor( (end-r) / step ) - q;
		}else{
			// general case
			n = round( (end - start) / step);
			if(sig * (start+n*step-end) > tol){
				n = n - 1;
			}
		}
	}

	// c = right hand end point
	c = start + n*step;
	if(sig*(c-end)>-tol){
		c = end;
	}

	// convert n to integer
	n_i = (int) n;

	// output_length = (int)n + 1
	output_length = n_i + 1;

	// if output_length <= 0 return an empty matrix
	if(output_length <= 0){
		<CA_NEW_ARRAY_CALL>(1, 0, output);
		return *output;
	}

	// create a new one and continue otherwise
	<CA_NEW_ARRAY_CALL>(1, output_length, output);

	// output should be symmetric about the mid-point
	k_length = floor(n / 2) + 1;

		
	for(i = 0; i < k_length; i++){
		(**output).<TENSOR_DATA>[i] = start + (i)*step;
		(**output).<TENSOR_DATA>[(n_i) - i] = c - (i)*step;
	}

	if(n_i % 2 == 0){
		(**output).<TENSOR_DATA>[n_i / 2] = (start+c) / 2;
	}
	
	return *output;
