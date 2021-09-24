#include "matisse-cl.h"
#include <stdio.h>
#include <stdlib.h>

struct MATISSE_cl_descriptor MATISSE_cl;

const char* get_error_message(cl_int error_code) {
	switch (error_code) {
	#define CASE(X) case X: return #X;
		CASE(CL_BUILD_PROGRAM_FAILURE)
		CASE(CL_COMPILER_NOT_AVAILABLE)
		CASE(CL_DEVICE_NOT_AVAILABLE)
		CASE(CL_DEVICE_NOT_FOUND)
		CASE(CL_IMAGE_FORMAT_NOT_SUPPORTED)
		CASE(CL_INVALID_BINARY)
		CASE(CL_INVALID_BUFFER_SIZE)
		CASE(CL_INVALID_BUILD_OPTIONS)
		CASE(CL_INVALID_COMMAND_QUEUE)
		CASE(CL_INVALID_CONTEXT)
		CASE(CL_INVALID_DEVICE)
		CASE(CL_INVALID_DEVICE_TYPE)
		CASE(CL_INVALID_EVENT_WAIT_LIST)
		CASE(CL_INVALID_GLOBAL_OFFSET)
		CASE(CL_INVALID_HOST_PTR)
		CASE(CL_INVALID_IMAGE_DESCRIPTOR)
		CASE(CL_INVALID_IMAGE_FORMAT_DESCRIPTOR)
		CASE(CL_INVALID_IMAGE_SIZE)
		CASE(CL_INVALID_KERNEL)
		CASE(CL_INVALID_KERNEL_ARGS)
		CASE(CL_INVALID_KERNEL_DEFINITION)
		CASE(CL_INVALID_KERNEL_NAME)
		CASE(CL_INVALID_MEM_OBJECT)
		CASE(CL_INVALID_OPERATION)
		CASE(CL_INVALID_PLATFORM)
		CASE(CL_INVALID_PROGRAM)
		CASE(CL_INVALID_PROGRAM_EXECUTABLE)
		CASE(CL_INVALID_QUEUE_PROPERTIES)
		CASE(CL_INVALID_WORK_DIMENSION)
		CASE(CL_INVALID_WORK_GROUP_SIZE)
		CASE(CL_INVALID_WORK_ITEM_SIZE)
		CASE(CL_INVALID_VALUE)
		CASE(CL_MEM_OBJECT_ALLOCATION_FAILURE)
		CASE(CL_OUT_OF_HOST_MEMORY)
		CASE(CL_OUT_OF_RESOURCES)
		CASE(CL_SUCCESS)

#ifdef CL_VERSION_1_1
		CASE(CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST)
#endif
#ifdef CL_VERSION_1_2
		CASE(CL_INVALID_COMPILER_OPTIONS)
#endif
	#undef CASE
	default: return "<UNKNOWN>";
	}
}

const char* get_device_type(cl_device_type device_type) {
	switch (device_type) {
	case CL_DEVICE_TYPE_CPU: return "CPU";
	case CL_DEVICE_TYPE_GPU: return "GPU";
	case CL_DEVICE_TYPE_ACCELERATOR: return "Accelerator";
	case CL_DEVICE_TYPE_DEFAULT: return "Default";
	default: return "<UNKNOWN>";
	}
}

void validate(const char* filename, int line, const char* functionName, cl_int errCode) {
	if (errCode != CL_SUCCESS) {
		const char* errMessage = get_error_message(errCode);
	
		fprintf(stderr, "Error at %s, line %d:\n%s returned %d (%s)\n", filename, line, functionName, errCode, errMessage);
		abort();
	}
}

static char* alloc_platform_name(cl_platform_id platform) {
	size_t name_size;
	CHECK(clGetPlatformInfo, platform, CL_PLATFORM_NAME, 0, NULL, &name_size);
	
	char* name = malloc(name_size);
	CHECK(clGetPlatformInfo, platform, CL_PLATFORM_NAME, name_size, name, NULL);
	
	return name;
}

static char* alloc_device_name(cl_device_id device) {
	size_t name_size;
	CHECK(clGetDeviceInfo, device, CL_DEVICE_NAME, 0, NULL, &name_size);
	
	char* name = malloc(name_size);
	CHECK(clGetDeviceInfo, device, CL_DEVICE_NAME, name_size, name, NULL);
	
	return name;
}

static void print_platforms(cl_platform_id* platforms, size_t num_platforms) {
	size_t i;
	for (i = 0; i < num_platforms; ++i) {
		char* name = alloc_platform_name(platforms[i]);
		
		printf("[%lu] %s\n", (long unsigned) (i + 1), name);
		free(name);
	}
}

static void print_device(cl_device_id device) {
	char* name = alloc_device_name(device);
	cl_device_type type;
	CHECK(clGetDeviceInfo, device, CL_DEVICE_TYPE, sizeof(cl_device_type), &type, NULL);
	
	printf("%s (%s)\n", name, get_device_type(type));
	free(name);
}

static void print_devices(cl_device_id* devices, size_t num_devices) {
	size_t i;
	for (i = 0; i < num_devices; ++i) {
		printf("[%lu] ", (long unsigned) (i + 1));
		print_device(devices[i]);
	}
}

static cl_platform_id choose_platform(cl_platform_id* platforms, size_t num_platforms, int argc, char** argv) {
	int chosen_platform = -1;
	
	#ifdef CHOSEN_PLATFORM
	chosen_platform = CHOSEN_PLATFORM;
	if (chosen_platform < 0 || (size_t)chosen_platform >= num_platforms) {
		printf("Compile-time chosen platform %d is out of range.\n", chosen_platform);
		abort();
	}
	#endif
	
	if (argc >= 3) {
		if (sscanf(argv[2], "%d", &chosen_platform) != 1) {
			chosen_platform = -1;
		} else {
			--chosen_platform;
		}
	}
	
	while (chosen_platform < 0 || (size_t)chosen_platform >= num_platforms) {
		printf("Choose the platform to use (or pass as first argument of command-line): \n");
		
		print_platforms(platforms, num_platforms);
		
		if (scanf("%d", &chosen_platform) != 1) {
			chosen_platform = -1;
		} else {
			--chosen_platform;
		}
	}
	
	char* name = alloc_platform_name(platforms[chosen_platform]);
	printf("Using platform: %s\n", name);
	free(name);
	
	return platforms[chosen_platform];
}

static cl_device_id choose_device(cl_device_id* devices, size_t num_devices, int argc, char** argv) {
	int chosen_device = -1;
	
	#ifdef CHOSEN_DEVICE
	chosen_device = CHOSEN_DEVICE;
	if (chosen_device < 0 || (size_t)chosen_device >= num_devices) {
		printf("Compile-time chosen device %d is out of range.\n", chosen_device);
		abort();
	}
	#endif
	
	if (argc >= 4) {
		if (sscanf(argv[3], "%d", &chosen_device) != 1) {
			chosen_device = -1;
		} else {
			--chosen_device;
		}
	}
	
	while (chosen_device < 0 || (size_t)chosen_device >= num_devices) {
		printf("Choose the device to use (or pass as second argument of command-line): \n");
		
		print_devices(devices, num_devices);
		
		if (scanf("%d", &chosen_device) != 1) {
			chosen_device = -1;
		} else {
			--chosen_device;
		}
	}
	
	printf("Using device: \n");
	print_device(devices[chosen_device]);
	
	return devices[chosen_device];
}

static void CL_CALLBACK notify_callback(const char* errinfo, const void* private_info, size_t cb, void* userdata) {
	fprintf(stderr, "CL Error:\n%s\n", errinfo);
}

static cl_command_queue create_command_queue(cl_context context, cl_device_id device, int enable_profiling) {
	cl_int errcode;
	cl_command_queue_properties queue_properties = enable_profiling ? CL_QUEUE_PROFILING_ENABLE : 0;
	
	cl_command_queue queue = clCreateCommandQueue(context, device, queue_properties, &errcode);
	CHECK_CODE(clCreateCommandQueue, errcode);
	
	return queue;
}

static cl_program create_program_from_file(cl_context context, cl_device_id device, const char* filename, const char* options) {
	const char* actual_filename = filename; // TODO
	
	printf("Opening kernel at: %s\n", actual_filename);
	
	FILE* f = fopen(actual_filename, "rb");
	if (!f) {
		fprintf(stderr, "Could not open OpenCL file.\n");
		abort();
	}
	fseek(f, 0, SEEK_END);
	size_t length = (size_t) ftell(f);
	fseek(f, 0, SEEK_SET);
	char* content = malloc(length);
	if (!content) {
		fprintf(stderr, "Could not allocate memory.\n");
		abort();
	}
	if (fread(content, 1, length, f) != length) {
		fprintf(stderr, "Failed to read OpenCL file.\n");
		abort();
	}
	fclose(f);
	
	cl_int errcode;
	if (content[length - 1] == '\0') {
		fprintf(stderr, "?\n");
		abort();
	}
	
<PROGRAM_LOADER_CODE>
	
	printf("Building\n");
	
	errcode = clBuildProgram(program, 1, &device, options, NULL, NULL);
	
	printf("Finished building\n");
	
	size_t log_size;
	CHECK(clGetProgramBuildInfo, program, device, CL_PROGRAM_BUILD_LOG, 0, NULL, &log_size);
	
	if (log_size > 1) {
		char* log = malloc(log_size);
		CHECK(clGetProgramBuildInfo, program, device, CL_PROGRAM_BUILD_LOG, log_size, log, NULL);
		
		fprintf(stderr, "Program log: %s\n", log);
	} else {
		fprintf(stderr, "Program compiled without errors or warnings.\n");
	}
	
	CHECK_CODE(clBuildProgram, errcode);
	
	return program;
}

void MATISSE_cl_initialize(int argc, char* argv[]) {
	cl_uint num_platforms;
	CHECK(clGetPlatformIDs, 0, NULL, &num_platforms);
	
	if (num_platforms == 0) {
		fprintf(stderr, "No platforms found.\n");
		exit(2);
	}
	
	cl_platform_id* platforms = calloc(num_platforms, sizeof(cl_platform_id));
	CHECK(clGetPlatformIDs, num_platforms, platforms, NULL);
	
	cl_platform_id platform = choose_platform(platforms, num_platforms, argc, argv);
	
	cl_uint num_devices;
	CHECK(clGetDeviceIDs, platform, CL_DEVICE_TYPE_ALL, 0, NULL, &num_devices);
	
	cl_device_id* devices = calloc(num_devices, sizeof(cl_device_id));
	CHECK(clGetDeviceIDs, platform, CL_DEVICE_TYPE_ALL, num_devices, devices, NULL);
	
	cl_device_id device = choose_device(devices, num_devices, argc, argv);
	
	free(platforms);
	free(devices);
	
	cl_int errcode;
	cl_context context = clCreateContext(NULL, 1, &device, notify_callback, NULL, &errcode);
	CHECK_CODE(clCreateContext, errcode);
	
	cl_program program;
	if (<LOAD_PROGRAM>) {
		program = create_program_from_file(context, device, "<PROGRAM_FILE_NAME>", "<PROGRAM_OPTIONS>");
		MATISSE_cl.program = program;
	}
	
	MATISSE_cl.context = context;
	MATISSE_cl.platform = platform;
	MATISSE_cl.device = device;
	MATISSE_cl.command_queue = create_command_queue(context, device, <ENABLE_PROFILING>);
<LOAD_KERNELS>
}

static void CL_CALLBACK register_event_callback(cl_event evt, cl_int exec_status, void* user_data) {
	if (exec_status != CL_COMPLETE) {
		printf("Unexpected event_command_exec_status %d\n", (int)exec_status);
		abort();
	}

	cl_ulong* total = (cl_ulong*) user_data;
	
	cl_ulong evt_start, evt_end;

	CHECK(clGetEventProfilingInfo, evt, <PROFILE_MODE>, sizeof(cl_ulong), &evt_start, NULL);
	CHECK(clGetEventProfilingInfo, evt, CL_PROFILING_COMMAND_END, sizeof(cl_ulong), &evt_end, NULL);

	// Not thread-safe for now
	// However, since concurrency in MATISSE OpenCL is very limited right now, this is probably fine.
	// After all, this is not a critical component.
	// If we expand concurrency, we'll have to make this an atomic operation or add a mutex.

	*total += evt_end - evt_start;
}

static void register_event(cl_ulong* total, cl_event evt) {
	CHECK(clSetEventCallback, evt, CL_COMPLETE, &register_event_callback, (void*) total);
}

#if <PRINT_KERNEL_TIME>
static cl_ulong kernel_time = 0;

void MATISSE_cl_register_kernel_event(cl_event evt) {
	register_event(&kernel_time, evt);
}
#endif

#if <PRINT_DATA_TRANSFER_TIME>
static cl_ulong host_to_device_data_transfer_time = 0;
static cl_ulong device_to_host_data_transfer_time = 0;

void MATISSE_cl_register_host_to_device_data_transfer_event(cl_event evt) {
	register_event(&host_to_device_data_transfer_time, evt);
}

void MATISSE_cl_register_device_to_host_data_transfer_event(cl_event evt) {
	register_event(&device_to_host_data_transfer_time, evt);
}
#endif

void MATISSE_cl_print_times() {
	CHECK(clFinish, MATISSE_cl.command_queue);

#if <PRINT_KERNEL_TIME>
	printf("Kernel time: %f\n", kernel_time*1.0e-9);
#endif
#if <PRINT_DATA_TRANSFER_TIME>
	printf("Host->Device data transfer time: %f\n", host_to_device_data_transfer_time*1.0e-9);
	printf("Device->Host data transfer time: %f\n", device_to_host_data_transfer_time*1.0e-9);
#endif
}

void MATISSE_cl_reset_times() {
#if <PRINT_KERNEL_TIME>
	kernel_time = 0;
#endif
#if <PRINT_DATA_TRANSFER_TIME>
	host_to_device_data_transfer_time = 0;
	device_to_host_data_transfer_time = 0;
#endif
}
