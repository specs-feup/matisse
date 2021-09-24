#include <stdio.h>
#include <stdlib.h>
#include <time.h>

/* Define targets */

#define MULTI_EXEC <MULTI_EXEC>

/* Includes */
#if _WIN32
	#include <windows.h>
	
	double get_time_in_seconds() {
		LARGE_INTEGER t, f;
		QueryPerformanceCounter(&t);
		QueryPerformanceFrequency(&f);
		return (double)t.QuadPart/(double)f.QuadPart;
	}
#endif

#if __linux__
	#include <time.h>
#endif

