#include <stdio.h>
#include <time.h>

#ifdef _WIN32
#include <windows.h>

double get_time_in_seconds() {
	LARGE_INTEGER t, f;
	QueryPerformanceCounter(&t);
	QueryPerformanceFrequency(&f);
	return (double)t.QuadPart/(double)f.QuadPart;
}

#endif
