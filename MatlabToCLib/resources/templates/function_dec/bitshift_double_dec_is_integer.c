
double int_part, frac_part;

// Get the integer and fractional parts of the input
frac_part = modf(input, &int_part);

// See if the fractional part is 0
if(frac_part == 0){
	return 1;
}

return 0;
