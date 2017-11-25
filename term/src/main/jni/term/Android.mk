
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# This is the target being built.
#T- LOCAL_MODULE:= libjackpal-androidterm5
LOCAL_MODULE:= libde-t2h-tterm5

# All of the source files that we will compile.
#T!{ ------------------------------------------------------------
#T! LOCAL_SRC_FILES:= \
#T!   common.cpp \
#T!   termExec.cpp \
#T!   fileCompat.cpp
LOCAL_SRC_FILES:= \
  common.cpp \
  termExec.cpp
#T!} ------------------------------------------------------------

LOCAL_LDLIBS := -ldl -llog

include $(BUILD_SHARED_LIBRARY)


