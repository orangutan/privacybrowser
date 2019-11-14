LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := privacybrowser
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := privacybrowser

pb_root  := $(LOCAL_PATH)
pb_dir   := app
pb_out   := $(PWD)/$(OUT_DIR)/target/common/obj/APPS/$(LOCAL_MODULE)_intermediates
pb_build := $(pb_root)/$(pb_dir)/build
pb_apk   := build/outputs/apk/standard/release/app-standard-release-unsigned.apk

$(pb_root)/$(pb_dir)/$(pb_apk):
	rm -Rf $(pb_build)
	mkdir -p $(pb_out)
	ln -sf $(pb_out) $(pb_build)
	cd $(pb_root)/$(pb_dir) && ../gradlew assembleRelease

LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(pb_dir)/$(pb_apk)
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_OVERRIDES_PACKAGES := Browser Browser2

include $(BUILD_PREBUILT)
