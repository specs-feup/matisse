function y = while_break(a, b, c)
   y = 0;
   while c,
      if a > b,
         y = a;
         break;
      end
      if a < b,
         y = y + 1;
         continue;
      end
   end
end