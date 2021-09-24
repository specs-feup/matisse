/* Start measuring */
#if _WIN32
start = get_time_in_seconds();
#endif

#if __linux__
clock_gettime(CLOCK_MONOTONIC, &start);
#endif

#if __MICROBLAZE__
//Calibration
printf("X\n");
printf("Z\n");
//Start measuring cycles
printf("X\n");
#endif
