/* Stop measuring */
#if _WIN32
end = get_time_in_seconds();
timeElapsed = end - start;
printf("TIME: %e\n", timeElapsed);
#endif

#if __linux__
clock_gettime(CLOCK_MONOTONIC, &end);
timeElapsed = (end.tv_sec + ((double) end.tv_nsec / 1000000000)) - (start.tv_sec + ((double) start.tv_nsec / 1000000000));
printf("TIME: %e\n", timeElapsed);
#endif

#if __MICROBLAZE__
printf("Y\n");
#endif
