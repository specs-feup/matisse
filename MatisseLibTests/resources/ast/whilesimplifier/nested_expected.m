function nested(x)
   while 1
      if not(x > 0)
         break;
      end
      w = x;
      while 1
         if not(w > 0)
            break;
         end
         w = 0;
      end
      x = x - 1;
   end
end