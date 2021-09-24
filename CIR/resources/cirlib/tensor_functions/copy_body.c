/*void copy_d(t* input, t** output) */

   int i;
   
   /* Prepare the output matrix */
   <CALL_NEW_HELPER>(input-><TENSOR_SHAPE>, input-><TENSOR_DIMS>, output);
	
	/* Verify if they are the same array */
	if(input == *output) {
		return;
	}
	
	/*for(i = 1; i<=input-><TENSOR_LENGTH>; i = i+1) */
	for(i = 0; i<input-><TENSOR_LENGTH>; i = i+1)
	{
		<FULLCALL_SET>;
	}
