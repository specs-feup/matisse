%-----------------------------------------------
% Iterates the grid to get Laplacian solution
% for potential.
function [v_old] = grid_iterate(obstacle, v, iter_max, nx, ny, nz)
   branch_1068055157 = 0;
   branch_1044172029 = 0;
   branch_1219503069 = 0;
   v_0 = 0;
   v_end = 1;
   c = 1 / 6;
   for iter = 1:iter_max
      for i = 2:nx - 1
         for j = 2:ny - 1
            for k = 2:nz - 1
               if (obstacle(i, j, k) == 1)
                  %@if
                  branch_1068055157 = branch_1068055157 + 1;
                  v(i, j, k) = v_0;
               elseif (obstacle(i, j, k) == -1)
                  %@elseif
                  branch_1044172029 = branch_1044172029 + 1;
                  v(i, j, k) = v_end;
               else
                  %@else
                  branch_1219503069 = branch_1219503069 + 1;
                  temp = v(i - 1, j, k) + v(i + 1, j, k) + v(i, j - 1, k) + v(i, j + 1, k) + v(i, j, k - 1) + v(i, j, k + 1);
                  v(i, j, k) = temp * c;
               end
            end
         end
      end
   end
   v_old = v;
   fprintf('Branch with label else was executed %d times.\n', branch_1219503069);
   fprintf('Branch with label elseif was executed %d times.\n', branch_1044172029);
   fprintf('Branch with label if was executed %d times.\n', branch_1068055157);
end

