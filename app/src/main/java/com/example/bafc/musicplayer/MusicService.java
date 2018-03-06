package com.example.bafc.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {

    private static final String TAG = "MusicPlayerService";
    private static final int NOTIFICATION_ID = 1; // 如果id设置为0,会导致不能设置为前台service


    public static final String KEY_MSG = "key_msg";
    public static final String KEY_CURRENT = "key_current";
    public static final String KEY_URL = "key_URL";
    public static final int MSG_START = 1;//暂停后的播放
    public static final int MSG_RESTART = 0;//service启动后的播放
    public static final int MSG_PAUSE = 2;

//    public  MediaPlayer mediaPlayer = null;
    private ArrayList<MusicMedia> musicList;
    //第几首音乐
    public int curPosition=0;
    private MusicMedia musicMedia;
    private String url;
    //默认进度条当前位置
    private int progress =0;
    private MediaPlayer mediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        musicList = MusicActivity.musicList;
        musicMedia=musicList.get(0);
        initmediPlayer(0);
        //        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                playNew();
//            }
//        });

    }

    public void initmediPlayer(int index ) {
        //获取文件路径
        try {
            //此处的两个方法需要捕获IO异常
            //设置音频文件到MediaPlayer对象中
            if (mediaPlayer!=null){
                mediaPlayer.setDataSource(musicList.get(index).getUrl());
                //让MediaPlayer对象准备
                mediaPlayer.prepare();
                Log.i(TAG, "url:"+url + "......." + Thread.currentThread().getName());
            }


        } catch (IOException e) {
            Log.d(TAG, "设置资源，准备阶段出错");
            e.printStackTrace();
        }
//        playMusic();
    }


    /**
     * 播放音乐
     */
    public void playMusic() {
        if (!mediaPlayer.isPlaying()) {
            //如果还没开始播放，就开始
            mediaPlayer.start();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        if (intent != null) {
//            int msg = intent.getIntExtra(KEY_MSG, -1);
//            switch (msg) {
//                case MSG_RESTART:
//                    url = intent.getStringExtra(KEY_URL);
//                    curPosition = intent.getIntExtra(KEY_CURRENT, 0);
//                    musicMedia = musicList.get(curPosition);
//                    Log.i(TAG, "url:"+url + "......." + Thread.currentThread().getName());
//                    play();
//                    break;
//                case MSG_START:
//                    mediaPlayer.start();
//                    break;
//                case MSG_PAUSE:
//                    mediaPlayer.pause();
//                    break;
//                default:
//                    break;
//            }

//            String name = "Current: "+ url.substring(url.lastIndexOf("/") + 1 , url.lastIndexOf("."));
//            Log.i(TAG,name+"歌曲信息");
//            //        //开启前台service
//            Notification notification = null;
//            if (Build.VERSION.SDK_INT < 16) {
//                notification = new Notification.Builder(this)
//                        .setContentTitle("Enter the MusicPlayer").setContentText(name)
//                        .setSmallIcon(R.drawable.ic_a8_default_cover).getNotification();
//            } else {
//                Notification.Builder builder = new Notification.Builder(this);
//                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                        new Intent(this, MusicHomeActivity.class), 0);
//                builder.setContentIntent(contentIntent);
//                builder.setSmallIcon(R.drawable.ic_a8_default_cover);
////        builder.setTicker("Foreground Service Start");
//                builder.setContentTitle("Enter the MusicPlayer");
//                builder.setContentText(name);
//                notification = builder.build();
//            }
//
//            startForeground(NOTIFICATION_ID, notification);


//        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void play() {
        Log.i(TAG, "palyer......");
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    public void playMusic(int i) {
        musicMedia=musicList.get(i);
        if (mediaPlayer!=null){
            mediaPlayer.reset();
            initmediPlayer(i);
            if (!mediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mediaPlayer.start();
            }

        }

    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }


    }

//    public int getCurrent

    private void playNew() {

    }

    public int getProgress() {
        if (mediaPlayer != null) {
            progress = mediaPlayer.getCurrentPosition();
        }
        return progress;
    }


    public  MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    //得到 当前播放第几个音乐
    public int getCurposition(){
        return curPosition;
    }

    //当前播放音乐
    public MusicMedia getMusicMedia() {
        return musicMedia;
    }

    public String toTime(int time){
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

}
