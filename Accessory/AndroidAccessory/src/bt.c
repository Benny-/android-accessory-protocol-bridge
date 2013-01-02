#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sco.h>
#include <bluetooth/sdp_lib.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/l2cap.h>
#include "bt.h"

struct BT_SERVICE
{
	struct sockaddr_rc loc_addr;
	sdp_session_t* sdp_session;
	sdp_record_t* sdp_record;
	int fd;
};

static sdp_session_t* register_service(
    const char* service_name,
    const char* svc_dsc,
    const char* service_prov,
    uint32_t svc_uuid_int[],
    uint8_t rfcomm_channel )
{
    // Stolen from http://www.btessentials.com/examples/bluez/sdp-register.c

    char test[100];
    int check = ba2str(BDADDR_ANY,test);
    printf("check: %i\n",check);
    printf("%s\n",test);

    uuid_t root_uuid, l2cap_uuid, rfcomm_uuid, svc_uuid,
           svc_class_uuid;
    sdp_list_t *l2cap_list = 0,
               *rfcomm_list = 0,
               *root_list = 0,
               *proto_list = 0,
               *access_proto_list = 0,
               *svc_class_list = 0,
               *profile_list = 0;
    sdp_data_t *channel = 0;
    sdp_profile_desc_t profile;
    sdp_record_t record = { 0 };
    sdp_session_t *session = 0;

    // set the general service ID
    sdp_uuid128_create( &svc_uuid, svc_uuid_int );
    sdp_set_service_id( &record, svc_uuid );

    char str[256] = "";
    sdp_uuid2strn(&svc_uuid, str, 256);
    printf("Registering UUID %s\n", str);

    // set the service class
    sdp_uuid16_create(&svc_class_uuid, SERIAL_PORT_SVCLASS_ID);
    svc_class_list = sdp_list_append(0, &svc_class_uuid);
    sdp_set_service_classes(&record, svc_class_list);

    // set the Bluetooth profile information
    sdp_uuid16_create(&profile.uuid, SERIAL_PORT_PROFILE_ID);
    profile.version = 0x0100;
    profile_list = sdp_list_append(0, &profile);
    sdp_set_profile_descs(&record, profile_list);

    // make the service record publicly browsable
    sdp_uuid16_create(&root_uuid, PUBLIC_BROWSE_GROUP);
    root_list = sdp_list_append(0, &root_uuid);
    sdp_set_browse_groups( &record, root_list );

    // set l2cap information
    sdp_uuid16_create(&l2cap_uuid, L2CAP_UUID);
    l2cap_list = sdp_list_append( 0, &l2cap_uuid );
    proto_list = sdp_list_append( 0, l2cap_list );

    // register the RFCOMM channel for RFCOMM sockets
    sdp_uuid16_create(&rfcomm_uuid, RFCOMM_UUID);
    channel = sdp_data_alloc(SDP_UINT8, &rfcomm_channel);
    rfcomm_list = sdp_list_append( 0, &rfcomm_uuid );
    sdp_list_append( rfcomm_list, channel );
    sdp_list_append( proto_list, rfcomm_list );

    access_proto_list = sdp_list_append( 0, proto_list );
    sdp_set_access_protos( &record, access_proto_list );

    // set the name, provider, and description
    sdp_set_info_attr(&record, service_name, service_prov, svc_dsc);

    // connect to the local SDP server, register the service record,
    // and disconnect
    session = sdp_connect(BDADDR_ANY, BDADDR_LOCAL, SDP_RETRY_IF_BUSY);
    sdp_record_register(session, &record, 0);

    // cleanup
    sdp_data_free( channel );
    sdp_list_free( l2cap_list, 0 );
    sdp_list_free( rfcomm_list, 0 );
    sdp_list_free( root_list, 0 );
    sdp_list_free( access_proto_list, 0 );
    sdp_list_free( svc_class_list, 0 );
    sdp_list_free( profile_list, 0 );

    return session;
}

BT_SERVICE* bt_listen(
        const char* service_name,
        const char* svc_dsc,
        const char* service_prov,
        uint32_t svc_uuid_int[4] )
{
	BT_SERVICE* bt_service = malloc(sizeof(BT_SERVICE));

	uint8_t port = 3;
    sdp_session_t* session = register_service(service_name, svc_dsc, service_prov, svc_uuid_int, port);

    struct sockaddr_rc rem_addr = { 0 };
    socklen_t opt = sizeof(rem_addr);

    // allocate socket
    bt_service->fd = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
    printf("socket() returned fd %d\n", bt_service->fd);

    // bind socket to of the first available local bluetooth adapter
    bt_service->loc_addr.rc_family = AF_BLUETOOTH;
    bt_service->loc_addr.rc_bdaddr = *BDADDR_ANY;
    bt_service->loc_addr.rc_channel = (uint8_t) port;

    int r;
    r = bind(bt_service->fd, (struct sockaddr *)&bt_service->loc_addr, sizeof(bt_service->loc_addr));
    printf("bind() on channel %d returned %d\n", port, r);

    // put socket into listening mode
    r = listen(bt_service->fd, 1);
    printf("listen() returned %d\n", r);

    return bt_service;
}

int bt_getFD(BT_SERVICE* service)
{
    return service->fd;
}

void bt_close(BT_SERVICE* service)
{
	sdp_close( service->sdp_session );
    // sdp_record_unregister(service->sdp_session, service->sdp_record); // HELP: I dont know if I should call sdp_close() or sdp_record_unregister()
    close(service->fd);
}
