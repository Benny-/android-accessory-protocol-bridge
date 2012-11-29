
ACLOCAL_AMFLAGS = ${ACLOCAL_FLAGS}

EXTRA_DIST = src/*.h bStrLib/*.h src/*/*.h

bin_PROGRAMS=AAP-bridge
AAP_bridge_SOURCES= \
    bStrLib/bstraux.c \
    bStrLib/bstrlib.c \
    src/AAP.c \
    src/server.c \
    src/Dbus/dbuslib.c \
    src/Dbus/listener.c \
    src/Dbus/method.c \
    src/Message/handlemessage.c \
    src/Message/receivequeue.c \
    src/Message/sendqueue.c

AAP_bridge_CFLAGS = -std=c99 -I$(top_srcdir)/bStrLib $(DEPENDENCIES_CFLAGS)
AAP_bridge_LDFLAGS = 
AAP_bridge_LDADD = -lpthread $(DEPENDENCIES_LIBS)

