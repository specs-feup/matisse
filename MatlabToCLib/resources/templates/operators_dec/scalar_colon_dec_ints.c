
	int i;
	int c;
	int k_length;

	// internal variables
	int sig = <CD_SIGN>;
	double tol = <CD_TOL>;
	int n = <CD_N>;

	// if output_length <= 0 return an empty matrix
	if(n+1 <= 0){
		return output;
	}
	
	// c = right hand end point
	c = start + n*step;
	if(sig*(c-end)>-tol){
		c = end;
	}
	
	// output should be symmetric about the mid-point
	k_length = n / 2 + 1;

	for(i = 0; i < k_length; i++){
		output[i] = start + i*step;
		output[n-i] = c - i*step;
	}

	if(n % 2 == 0){
		output[n/2] = (start+c) / 2;
	}

	return output;
