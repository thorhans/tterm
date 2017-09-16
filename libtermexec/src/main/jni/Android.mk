
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# This is the target being built.
# T- LOCAL_MODULE:= libjackpal-termexec2
LOCAL_MODULE:= libde-t2h-tterm-exec2

# All of the source files that we will compile.
LOCAL_SRC_FILES:= \
  process.cpp

LOCAL_LDLIBS := -llog -lc

include $(BUILD_SHARED_LIBRARY)
