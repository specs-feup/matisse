function C = matrix_mul(A, B)

nRow1 = size(A, 1);
nCol1 = size(A, 2);
nRow2 = size(B, 1);
nCol2 = size(B, 2);

%TODO: it should calculate which is the class with highest priority. Solve it using template?
C = zeros(nRow1,nCol2, class(A));

if nCol1 ~= nRow2
    fprintf('inner dimensions must match. Matrix A is %d by %d, Matrix B is %d by %d\n', nRowA, nColA, nRowB, nColB);
	return;
end


for i = 1:nRow1
     for j = 1:nCol2
         for k = 1:nCol1
             C(i,j) = C(i,j) + A(i,k)*B(k,j);
         end
     end
end