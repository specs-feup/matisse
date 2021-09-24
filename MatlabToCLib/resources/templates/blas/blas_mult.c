	int nRowA;
	int nColA;
	int nRowB;
	int nColB;

	nRowA = <GET_A_0>;
	nColA = <GET_A_1>;
	nRowB = <GET_B_0>;
	nColB = <GET_B_1>;
	
	<NEW_ARRAY_C>;
   	
	int lda = nRowA;
   	int ldb = nRowB;
   	int ldc = nRowA;
   	
	if(<GET_DIMS_A> > 2 || <GET_DIMS_B> > 2) {
		return <MATRIX_MULT>;
	}
	
	<BLAS_CALL>(CblasColMajor, CblasNoTrans, CblasNoTrans, nRowA, nColB, nColA, 1.0, <DATA_A>, lda, <DATA_B>, ldb, 0.0, <DATA_C>, ldc);
   	
	return *C;
