
ACLOCAL_AMFLAGS = ${ACLOCAL_FLAGS}

EXTRA_DIST = src/*.h bStrLib/*.h src/*/*.h doc/AAP-Bridge.md

bin_PROGRAMS=AAP-bridge
AAP_bridge_SOURCES= \
    bStrLib/bstraux.c \
    bStrLib/bstrlib.c \
    src/servicespawner.c \
    src/keepalive.c \
    src/main.c \
    src/bridge.c \
    src/Dbus/dbuslib.c \
    src/Dbus/listener.c \
    src/Dbus/method.c \
    src/bulkTransfer.c \
    src/accessoryAudio.c \
    src/Message/receivequeue.c \
    src/Message/sendqueue.c 

AAP_bridge_CFLAGS =  -D_BSD_SOURCE -std=c99 -I$(top_srcdir)/bStrLib $(DEPENDENCIES_CFLAGS)
AAP_bridge_LDFLAGS = 
AAP_bridge_LDADD = -lpthread $(DEPENDENCIES_LIBS)

# Pandoc is used for man pages documentation
if HAVE_PANDOC
man_MANS = doc/man/AAP-Bridge.1
doc/man/AAP-Bridge.1: doc/AAP-Bridge.md
	mkdir -p "doc/man"
	pandoc -s -w man "$(top_srcdir)/doc/AAP-Bridge.md" -o "doc/man/AAP-Bridge.1"
endif
