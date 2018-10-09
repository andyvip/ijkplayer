LOCAL_PATH := $(call my-dir)

MY_APP_JNI_ROOT := $(realpath $(LOCAL_PATH))
MY_APP_PRJ_ROOT := $(realpath $(MY_APP_JNI_ROOT)/..)
MY_APP_ANDROID_ROOT := $(realpath $(MY_APP_PRJ_ROOT)/../../../..)

ifeq ($(TARGET_ARCH_ABI), armeabi-v7a)
NCNN_INSTALL_PATH := $(realpath $(MY_APP_ANDROID_ROOT)/contrib/build/ncnn-armeabi-v7a)
endif

ifeq ($(TARGET_ARCH_ABI), arm64-v8a)
NCNN_INSTALL_PATH := $(realpath $(MY_APP_ANDROID_ROOT)/contrib/build/ncnn-arm64-v8a)
endif

include $(CLEAR_VARS)
LOCAL_MODULE := ncnn
LOCAL_SRC_FILES := $(NCNN_INSTALL_PATH)/install/lib/libncnn.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := squeezencnn
LOCAL_SRC_FILES := squeezencnn_jni.cpp

LOCAL_C_INCLUDES := $(NCNN_INSTALL_PATH)/install/include

LOCAL_STATIC_LIBRARIES := ncnn

#LOCAL_CFLAGS := -O2 -fvisibility=hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math
#LOCAL_CPPFLAGS := -O2 -fvisibility=hidden -fvisibility-inlines-hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math

LOCAL_CFLAGS := -O2 -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math
LOCAL_CPPFLAGS := -O2 -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math

LOCAL_LDFLAGS += -Wl,--gc-sections

LOCAL_CFLAGS += -fopenmp
LOCAL_CPPFLAGS += -fopenmp
LOCAL_LDFLAGS += -fopenmp

LOCAL_LDLIBS := -lz -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
