	cl_kernel <KERNEL_NAME> = clCreateKernel(program, "<KERNEL_NAME>", &errcode);
	CHECK_CODE(clCreateKernel, errcode);
	MATISSE_cl.<KERNEL_NAME> = <KERNEL_NAME>;
