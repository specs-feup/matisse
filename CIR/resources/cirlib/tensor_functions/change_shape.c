   //free(thetas_roll->shape);
   int i;
   
   free(t->shape);
   t->shape = malloc(sizeof(int) * <SHAPE_LENGTH>);
   for(i=0; i<<SHAPE_LENGTH>; i++) {
	   t->shape[i] = <SHAPE_GET>;
   }
