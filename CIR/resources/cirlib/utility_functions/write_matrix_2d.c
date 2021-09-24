   
   int i, j;
   int zero = 0;
   
   // Write <VARIABLE_NAME>
   FILE *f = fopen(filename, "w");
   if (f == NULL)
   {
       printf("Error opening file '%s'!\n", filename);
       exit(1);
   }

   if (!<MATRIX>) {
       printf("Could not write matrix to '%s' because matrix is NULL\n", filename);
       exit(1);
   }

   if(<MATRIX_DIMS> > 2) {
		printf("Variable <VARIABLE_NAME> will not be saved, it has more than 2 dimensions.\n");
		fclose(f);
		return;
   }

   if (<NUMEL> == 0) {
	   // If the matrix is empty, we won't print it.
	   // We can't just let it reach the loop then we'll have a crash at GET (i, 0).
	   fclose(f);
	   return;
   }

   // Flattens any dimension above 2
   for(i = 0; i < <SIZE_X_1>; i++) {

	   fprintf(f, "%4.17e", <GET_i_0>);
	   for(j = 1; j < <SIZE_X_2>; j++) {
		   fprintf(f, ",%4.17e", <GET_i_j>);
	   }
	   fprintf(f, "\n");
   }


   fclose(f);
