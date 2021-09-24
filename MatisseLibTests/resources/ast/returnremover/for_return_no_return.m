function y = for_return(x)
    return_1 = 0;
    y = 0;
    for i = 1:x
       if i == 1
           y = y + 1;
           return_1 = 1;
       end
       
       if ~return_1
           y = y + 2;
       else
           break
       end
    end
end