%!assume_indices_in_range
function tmp3 = different_depths6(s1, s2, s3)
	tmp1 = zeros(s3, s2, s1);
	tmp2 = zeros(s3, s2, s1);
	tmp3 = zeros(s3, s2, s1);
	for i = 1:s1,
		for j = 1:s2,
			for k = 1:s3,
				tmp1(k, j, i) = i + j + k;
				tmp2(k, j, i) = i + j + k;
			end
		end
	end
	for i = 1:numel(tmp3),
		tmp3(i) = tmp1(i) * tmp2(i);
	end
end