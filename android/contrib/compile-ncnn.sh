UNI_BUILD_ROOT=`pwd`
FF_TARGET=$1

FF_ACT_ARCHS_ALL="armeabi-v7a arm64-v8a"

echo_archs() {
    echo "===================="
    echo "[*] check archs"
    echo "===================="
    echo "FF_ALL_ARCHS = $FF_ACT_ARCHS_ALL"
    echo "FF_ACT_ARCHS = $*"
    echo ""
}

echo_nextstep_help() {
    echo ""
    echo "--------------------"
    echo "[*] Finished"
    echo "--------------------"
    echo "# to continue to build ijkplayer, run script below,"
    echo "sh compile-ijk.sh "
}

ANDROID_NDK=/Users/andy/workspace/tool/android-ndk-r15c

case "$FF_TARGET" in
    armeabi-v7a|arm64-v8a)
        echo_archs $FF_TARGET
        sh tools/do-compile-ncnn.sh $FF_TARGET
        echo_nextstep_help
    ;;
    all)
        echo_archs $FF_ACT_ARCHS_ALL
        for ARCH in $FF_ACT_ARCHS_ALL
        do
            sh tools/do-compile-ncnn.sh $ARCH
        done
        echo_nextstep_help
    ;;
    clean)
        echo_archs FF_ACT_ARCHS_ALL
        rm -rf ./build/ncnn-*
    ;;
    check)
        echo_archs FF_ACT_ARCHS_ALL
    ;;
    *)
        exit 1
    ;;
esac
