FF_ARCH=$1
FF_BUILD_ROOT=`pwd`
FF_BUILD_NAME='ncnn'
FF_ANDROID_PLATFORM=android-21

FF_SOURCE=$FF_BUILD_ROOT/$FF_BUILD_NAME
FF_PREFIX=$FF_BUILD_ROOT/build/$FF_BUILD_NAME-$1

mkdir -p $FF_PREFIX
pushd $FF_PREFIX
cmake -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake -DANDROID_ABI="$FF_ARCH" -DANDROID_PLATFORM=$FF_ANDROID_PLATFORM -DANDROID_ARM_NEON=ON $FF_SOURCE
make -j4
make install
popd
