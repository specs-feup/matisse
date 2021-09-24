function insert_test

end

function empty_function

end

function empty_loop

	for i=1:10 end
end

function empty_if_else

	if true
	elseif false
	else
	end
end

function with_return

	if true
		% Before true return
		return;
		% After if return
	else
		% Before else return
		return;
		% After else return
	end

	% Before function return
	return;
	% After function return
end

function empty_function_for_return

end