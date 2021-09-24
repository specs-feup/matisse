	

	// iteration variable
	int i;

	// range
	double range = <V_END> - <V_START>;

	// vec
	double vec[<N_STEPS>];
	for ( i=0 ; i<<N_STEPS> ; i++ )
	{
		vec[i] = i;
	}

	// overflow test
	double c = range * (<N_STEPS> - 1);
	if ( isinf(c) )
	{
		for ( i=0 ; i<<N_STEPS> ; i++ )
		{
			<V_OUTPUT>[i] = <V_START> + (<V_END> / <N_STEPS>) * vec[i] - (<V_START> / <N_STEPS>) * vec[i];
		}
	}
	else
	{
		for ( i=0 ; i<<N_STEPS> ; i++ )
		{
			<V_OUTPUT>[i] = <V_START> + vec[i] * range / <N_STEPS>;
		}
	}

	// last position
	<V_OUTPUT>[<N_STEPS>] = <V_END>;
	
	return <V_OUTPUT>;
