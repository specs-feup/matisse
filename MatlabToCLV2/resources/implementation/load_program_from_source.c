	cl_program program = clCreateProgramWithSource(context, 1, (const char**) &content, &length, &errcode);
	CHECK_CODE(clCreateProgramWithSource, errcode);
