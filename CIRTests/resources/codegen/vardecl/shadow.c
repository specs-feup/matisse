/**
 */
int test(int s, int m, int e)
{
   int y;

   y = 0;
   if(m != 0){
      int i;
      int sign_1;
      if(m < 0){
         sign_1 = -1;
      }
      
      for(i = 0; i < e; ++i){
         y += sign_1;
      }
      
   }
   
   
   return y;
}
