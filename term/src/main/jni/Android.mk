
HERE_PATH:= $(call my-dir)

# ============================================================

include $(CLEAR_VARS)
LOCAL_PATH:= $(HERE_PATH)/libtermexec
LOCAL_MODULE:= libde-t2h-tterm-exec2
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SRC_FILES := \
  process.cpp
LOCAL_LDLIBS := -llog -lc
include $(BUILD_SHARED_LIBRARY)

# ============================================================

include $(CLEAR_VARS)
LOCAL_PATH:= $(HERE_PATH)/term
LOCAL_MODULE:= libde-t2h-tterm5
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SRC_FILES := \
  common.cpp \
  termExec.cpp
LOCAL_LDLIBS := -ldl -llog
include $(BUILD_SHARED_LIBRARY)

