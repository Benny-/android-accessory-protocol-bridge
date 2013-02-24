#include <stdio.h>
#include <accessory.h>

// This is a echo server. It will send anything back you send to it.
// You can connect to this accessory using bluetooth or usb.
// There is no associated Android application to test this, you will have to write that yourself.

// Compile this program using:
// gcc echo.c -o echo $(pkg-config --libs --cflags libandroidaccessory-1.0)

int main (int argc, char *argv[])
{
    Accessory* accessory;
    AapConnection* con;
    int connected = 1;
    
    const char* const uuids[] = {
        /* You should generate your own uuid using uuidgen, dont copy paste this */
        "db0d662d-40cf-4995-9849-6dc604563e10",
        NULL
    };
    
    accessory = initAccessory(
        "Echo server company",
        "Echo server",
        "This echo server will send anything back you send it",
        uuids,
        "1.0",
        // I diddent actually test if blueterm can communicate to libAndroidAccessory
        "https://play.google.com/store/apps/details?id=es.pymasde.blueterm",
        "");
    
    printf("Waiting for new connection\n");
    con = getNextAndroidConnection(accessory);
    printf("New connection accepted\n");
    while(connected)
    {
        int read;
        char buffer[1024];
        
        read = readAccessory(con, buffer, sizeof(buffer));
        if(read < 0)
        {
            connected = 0;
            break;
        }
        
        printf("Read %i bytes\n", read);
        
        // Note: I use writeAllAccessory() instead of writeAccessory() here.
        // This function guarantees to write everything or return a error.
        if(writeAllAccessory(con, buffer, read))
        {
            connected = 0;
            break;
        }
    }
    printf("Connection ended.\n");
    
    closeAndroidConnection(con);
    deInitaccessory(accessory);
}

