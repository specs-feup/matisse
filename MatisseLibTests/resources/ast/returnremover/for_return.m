function y = for_return(x)
    y = 0;
    for i = 1:x
       if i == 1
           y = y + 1;
           return; 
       end
       
       y = y + 2;
    end
end