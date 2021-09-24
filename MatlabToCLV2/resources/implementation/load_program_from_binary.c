	cl_int binary_errcode;
	cl_program program = clCreateProgramWithBinary(context, 1, &MATISSE_cl.device, &length, (const unsigned char**) &content, &binary_errcode, &errcode);
	CHECK_CODE(clCreateProgramWithSource, errcode);
	CHECK_CODE(clCreateProgramWithSource, binary_errcode);