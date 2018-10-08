package example.andy.ijkplayer_demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

import com.tencent.squeezencnn.SqueezeNcnn;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "ZLJ";
    private static long count = 0;
    private static long lastTime = 0;

    private IjkMediaPlayer mMediaPlayer = null;
    private Uri mUri = null;

    private long mPrepareStartTime = 0;
    private long mPrepareEndTime = 0;
    private long mSeekStartTime = 0;
    private long mSeekEndTime = 0;

    private SqueezeNcnn squeezencnn = new SqueezeNcnn();

    private boolean mIsDrawing;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private Handler mRenderHandle;
    private HandlerThread mRenderHandlerThread;

    private Handler mUIHandle;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mSurfaceView = new SurfaceView(this);
        //setContentView(mSurfaceView);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView )findViewById(R.id.surfaceView);
        mTextView = (TextView)findViewById(R.id.result);

        mSurfaceView.getHolder().addCallback(mSurfaceHolderCB);

        mRenderHandlerThread = new HandlerThread("render");
        mRenderHandlerThread.start();
        mRenderHandle = new Handler(mRenderHandlerThread.getLooper(), mRenderHandlerCB);

        try
        {
            initSqueezeNcnn();
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "initSqueezeNcnn error");
        }


        mUIHandle = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String result = (String)msg.obj;
                if (result == null)
                {
                    mTextView.setText("detect failed");
                }
                else
                {
                    mTextView.setText(result);
                }
                return true;
            }
        });

        //mUri = Uri.parse("/storage/emulated/0/Movies/forgotten-object.mp4");
        mUri = Uri.parse("rtsp://192.168.10.101:8554/live.sdp");
        openVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayback();
    }

    private Handler.Callback mRenderHandlerCB = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            long curTime = System.currentTimeMillis();
            count++;
            if (curTime - lastTime >= 1000) {
                Log.w("ZLJ", "fps: " + count);
                lastTime = curTime;
                count = 0;
            }

            if (msg.what == 666) {
                if (mIsDrawing == false) {
                    return false;
                }
                Bitmap bitmap = (Bitmap) msg.obj;
                if (bitmap == null) {
                    return false;
                }

                // resize to 227x227
                Message result = new Message();
                result.what = 777;
                result.obj = squeezencnn.Detect(Bitmap.createScaledBitmap(bitmap, 227, 227, false));
                mUIHandle.sendMessage(result);

                Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas == null) {
                    return false;
                }
                canvas.drawBitmap(bitmap, null, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), null);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                return true;
            }
            return true;
        }
    };

    private SurfaceHolder.Callback mSurfaceHolderCB = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            mIsDrawing = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private IjkMediaPlayer.PreviewCallback mPreviewCallback = new IjkMediaPlayer.PreviewCallback() {
        @Override
        public void onPreviewFrame(int w, int h, byte[] data) {

            Log.d("ZLJ", "" + w + "*" + h + "," + data.length);
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            bitmap.copyPixelsFromBuffer(buffer);

            Message msg = new Message();
            msg.what = 666;
            msg.obj = bitmap;
            mRenderHandle.sendMessage(msg);

//            File desFile = new File("/sdcard/Pictures/xxx" + curTime + ".jpg");
//            FileOutputStream fos = null;
//            BufferedOutputStream bos = null;
//            try {
//                fos = new FileOutputStream(desFile);
//                bos = new BufferedOutputStream(fos);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
//                bos.flush();
//                bos.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    };

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            mPrepareEndTime = System.currentTimeMillis();
            mMediaPlayer.start();
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
        new IMediaPlayer.OnCompletionListener() {
            public void onCompletion(IMediaPlayer mp) {

            }
        };

    private IMediaPlayer.OnInfoListener mInfoListener =
        new IMediaPlayer.OnInfoListener() {
            public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                switch (arg1) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                        Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                        Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                        Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        break;
                }
                return true;
            }
        };

    private IMediaPlayer.OnErrorListener mErrorListener =
        new IMediaPlayer.OnErrorListener() {
            public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                return true;
            }
        };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
        new IMediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            }
        };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            mSeekEndTime = System.currentTimeMillis();
        }
    };

    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {

        }
    };



    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    private void openVideo() {
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_VERBOSE);
        mMediaPlayer.setPreviewCallback(mPreviewCallback);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

        mMediaPlayer.setOnPreparedListener(mPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
        mMediaPlayer.setOnErrorListener(mErrorListener);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
        mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        try {
            mMediaPlayer.setDataSource(this, mUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPrepareStartTime = System.currentTimeMillis();
        mMediaPlayer.prepareAsync();
    }

    private void initSqueezeNcnn() throws IOException
    {
        byte[] param = null;
        byte[] bin = null;
        byte[] words = null;

        {
            InputStream assetsInputStream = getAssets().open("squeezenet_v1.1.param.bin");
            int available = assetsInputStream.available();
            param = new byte[available];
            int byteCode = assetsInputStream.read(param);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open("squeezenet_v1.1.bin");
            int available = assetsInputStream.available();
            bin = new byte[available];
            int byteCode = assetsInputStream.read(bin);
            assetsInputStream.close();
        }
        {
            InputStream assetsInputStream = getAssets().open("synset_words.txt");
            int available = assetsInputStream.available();
            words = new byte[available];
            int byteCode = assetsInputStream.read(words);
            assetsInputStream.close();
        }

        squeezencnn.Init(param, bin, words);
    }
}
