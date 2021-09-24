/* Variable declaration for time measuring */
#if _WIN32
double timeElapsed;
double start, end;
#endif

#if __linux__
double timeElapsed;
struct timespec start, end;
#endif

#if __MICROBLAZE__
/* MicroBlaze does not need it */
#endif

	
	