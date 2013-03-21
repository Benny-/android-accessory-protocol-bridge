#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pulse/pulseaudio.h>

static uint32_t lastIndexChecked = 0;

static void on_module_load(pa_context *c, uint32_t idx, void *userdata)
{
	printf("AccessoryAudio     : on_module_load()\n");
}

/**
 * This function will be called multiple times and the argument is not always a "new" source, contrary
 * to the function name.
 */
static void on_new_source(pa_context *c, const pa_source_info *i, int eol, void *userdata)
{
	printf("AccessoryAudio     : on_new_source()\n");
	if(!eol && i->index > lastIndexChecked)
	{
		printf("AccessoryAudio     : index: %i\n", i->index);
		if(pa_proplist_contains(i->proplist,"device.vendor.id") && pa_proplist_contains(i->proplist,"device.product.id"))
		{
			const char* vendor_id = pa_proplist_gets(i->proplist,"device.vendor.id");
			const char* product_id = pa_proplist_gets(i->proplist,"device.product.id");

			if(!strcmp(vendor_id, "18d1") && (!strcmp(product_id, "2d02") || !strcmp(product_id, "2d03") || !strcmp(product_id, "2d04") || !strcmp(product_id, "2d05")) )
			{
				char argument[20];
				sprintf(argument, "source=%i", i->index);
				pa_operation* operation = pa_context_load_module(
					c,
					"module-loopback",
					argument,
					&on_module_load,
					NULL);
				pa_operation_unref(operation);
			}
		}

		lastIndexChecked = i->index;
	}
}

static void on_server_info(pa_context *c, const pa_server_info *i, void *userdata)
{
	printf("AccessoryAudio     : on_server_info()\n");
}

static void my_subscription_callback(pa_context *c, pa_subscription_event_type_t t,
                              uint32_t idx, void *userdata)
{
    if ((t & PA_SUBSCRIPTION_EVENT_FACILITY_MASK) == PA_SUBSCRIPTION_EVENT_SOURCE) {
        if ((t & PA_SUBSCRIPTION_EVENT_TYPE_MASK) == PA_SUBSCRIPTION_EVENT_NEW) {
            printf("AccessoryAudio     : ... a source was added, let's do stuff! ...\n");
			pa_operation* pa_operation_get_sources = pa_context_get_source_info_list(
				c,
				&on_new_source,
				NULL
			);
			pa_operation_unref(pa_operation_get_sources);
        }
    }
}

static void on_new_context_state(pa_context *c, void *userdata)
{
	printf("AccessoryAudio     : on_new_context_state()\n");
	pa_context_state_t state = pa_context_get_state(c);
	switch (state)
	{
		case PA_CONTEXT_UNCONNECTED:
#ifdef DEBUG
			printf("AccessoryAudio     : PA_CONTEXT_UNCONNECTED\n");
#endif
			break;
		case PA_CONTEXT_CONNECTING:
#ifdef DEBUG
			printf("AccessoryAudio     : PA_CONTEXT_CONNECTING \n");
#endif
			break;
		case PA_CONTEXT_AUTHORIZING:
#ifdef DEBUG
			printf("AccessoryAudio     : PA_CONTEXT_AUTHORIZING\n");
#endif
			break;
		case PA_CONTEXT_SETTING_NAME:
#ifdef DEBUG
			printf("AccessoryAudio     : PA_CONTEXT_SETTING_NAME\n");
#endif
			break;
		case PA_CONTEXT_READY:
			printf("AccessoryAudio     : PA_CONTEXT_READY\n");
			pa_operation* pa_operation_get_serverinfo = pa_context_get_server_info(
				c,
				&on_server_info,
				NULL
			);
			pa_operation_unref(pa_operation_get_serverinfo);

			pa_context_set_subscribe_callback(
				c,
				&my_subscription_callback,
				NULL
			);

			pa_operation* subscribe_operation = pa_context_subscribe 	(
				c,
				PA_SUBSCRIPTION_EVENT_FACILITY_MASK | PA_SUBSCRIPTION_EVENT_TYPE_MASK,
				NULL, // Success callback
				NULL
			);
			pa_operation_unref(subscribe_operation);

			pa_operation* pa_operation_get_sources = pa_context_get_source_info_list(
				c,
				&on_new_source,
				NULL
			);
			pa_operation_unref(pa_operation_get_sources);
			break;
		case PA_CONTEXT_FAILED:
			fprintf(stderr, "AccessoryAudio     : PA_CONTEXT_FAILED\n");
			break;
		case PA_CONTEXT_TERMINATED:
			fprintf(stderr, "AccessoryAudio     : PA_CONTEXT_TERMINATED\n");
			break;
		default:
			fprintf(stderr, "AccessoryAudio     : PA_CONTEXT_UNKNOWN\n");
			break;
	}

}

void accessory_audio_start(void)
{
	pa_context* pa_ctx;
	pa_threaded_mainloop* pa_mainloop;
	pa_operation* subscribe_operation;

	pa_mainloop = pa_threaded_mainloop_new();

	pa_ctx = pa_context_new(
		pa_threaded_mainloop_get_api(pa_mainloop),
		"AAP-Bridge"
	);

	pa_context_set_state_callback(
		pa_ctx,
		&on_new_context_state,
		NULL
	);

	pa_context_connect(
        pa_ctx,
		NULL,	// Connect to a specific server. NULL for default pulseaudio server
		0,		// Flags
		NULL	// pa_spawn_api. May be NULL if we dont use fork()
	);

    // Valgrind may report a memory leak here. The memory is never freed.
    // This is not a problem as it does not continue to leak more and more
    // memory. It is only allocated once.
	pa_threaded_mainloop_start(pa_mainloop);

	//pa_context_disconnect(pa_ctx);
}
