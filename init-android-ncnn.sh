IJK_NCNN_UPSTREAM=https://github.com/andyvip/ncnn.git
IJK_NCNN_FORK=https://github.com/andyvip/ncnn.git
IJK_NCNN_COMMIT=20180830
IJK_NCNN_LOCAL_REPO=extra/ncnn

set -e
TOOLS=tools

echo "== pull NCNN base =="
sh $TOOLS/pull-repo-base.sh $IJK_NCNN_UPSTREAM $IJK_NCNN_LOCAL_REPO

echo "== pull NCNN fork =="
sh $TOOLS/pull-repo-ref.sh $IJK_NCNN_FORK android/contrib/ncnn ${IJK_NCNN_LOCAL_REPO}
cd android/contrib/ncnn
git checkout ${IJK_NCNN_COMMIT}
cd -