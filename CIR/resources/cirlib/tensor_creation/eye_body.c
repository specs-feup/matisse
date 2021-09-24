
   int set_index;
   int i;
   int num_rows, num_cols;
   int stop_value;
   int step_size;
   
   <CALL_NEW>(<DIM_NAMES>, 0, t);
   num_rows = (*t)-><TENSOR_SHAPE>[0];
   num_cols = (*t)-><TENSOR_SHAPE>[1];
   
   stop_value = num_rows < num_cols ? num_rows : num_cols;
   step_size = num_rows+1;
   
   /* While 'size' is not implemented, access directly
      Matrix is built row-wise, since that is how the arrays are mapped in memory  in C */

   set_index = 0;
   for(i = 0; i<stop_value; i++)
   {
		(*t)-><TENSOR_DATA>[set_index] = 1;
		set_index = set_index+step_size;
   }

   return *t;
