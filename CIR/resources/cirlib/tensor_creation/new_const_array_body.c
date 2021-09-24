
   <CALL_NEW>(<DIM_NAMES>, t);
   if((*t)->owns_data) {
	   <CALL_SET_MATRIX>(*t, value);
   } else {
      printf("WARNING (new_const_array): Call to zeros for an array that does not own its data.\n");
   }
   
   return *t;
