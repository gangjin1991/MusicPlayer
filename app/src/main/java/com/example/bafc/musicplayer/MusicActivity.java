package com.example.bafc.musicplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicActivity extends AppCompatActivity {

    public static final String KEY_PLAY_MODE="play mode";
    public static final String KEY_PLAY_ACCLER="play accler";
    private int[] modepic = {R.drawable.ic_shuffle_black_24dp,R.drawable.ic_repeat_black_24dp,R.drawable.ic_repeat_one_black_24dp};
    private int clicktime = 0;//accelerometer 切换
    private static int currentposition = -1;//当前播放列表里哪首音乐



    private ArrayList<Map<String, Object>> listems = null;//需要显示在listview里的信息
    public static ArrayList<MusicMedia> musicList = null; //音乐信息列表
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edit;
    private Handler handler;
    private float mLastY=-1;
     ImageView mIvPlayMode;
     ImageView mIvPlay_Accler;
     ImageView mIvShare;
     TextView mTvMusicInfo;
     ListView mLvMusic;
     ImageView mIvPause;
     RelativeLayout mRlTop;
     RelativeLayout mRlBottom;
     SeekBar seekBarMusic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MusicPlayerService", "MusicActivity...onCreate........." + Thread.currentThread().hashCode());
        ButterKnife.bind(this);
        initView();
        init();




    }

    private void initView() {
        mIvPlayMode=findViewById(R.id.iv_play_mode);
        mIvPlay_Accler=findViewById(R.id.iv_play_accelerometer);
        mIvShare=findViewById(R.id.iv_share);
        mTvMusicInfo=findViewById(R.id.musicinfo);
        mLvMusic=findViewById(R.id.musicListView);
        mIvPause=findViewById(R.id.play_pause);
        mRlTop=findViewById(R.id.top);
        mRlBottom=findViewById(R.id.bottom);
        seekBarMusic =findViewById(R.id.seekBar);

    }

    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        edit = sharedPreferences.edit();
        int playmode = sharedPreferences.getInt(KEY_PLAY_MODE, -1);
        if (playmode==-1){//还未设置过
            edit.putInt(KEY_PLAY_MODE,0).commit();
        }else {
            changeMode(playmode);
        }

        //摇一摇
        if(sharedPreferences.getInt(KEY_PLAY_ACCLER,0) == 0){
            //默认摇一摇是打开的
            clicktime = 0;
            mIvPlay_Accler.setBackgroundResource(R.drawable.ic_alarm_on_black_24dp);
        }else{
            clicktime = 1;
            mIvPlay_Accler.setBackgroundResource(R.drawable.ic_alarm_off_black_24dp);
        }

        handler = new Handler();
        mIvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"https://www.baidu.com/");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,"分享到"));
            }
        });

        mLvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentposition=i;
                playMusic(currentposition);
            }
        });

        mLvMusic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mLastY==-1){
                    mLastY = motionEvent.getRawY();
                }
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        //判断上滑还是下滑
                        if (motionEvent.getRawY() > mLastY) {
                            //下滑显示bottom，隐藏top
                            mRlTop.setVisibility(View.GONE);
                            mRlBottom.setVisibility(View.VISIBLE);
                        } else if (motionEvent.getRawY() < mLastY) {
                            //上滑，显示top，隐藏bottom
                            mRlTop.setVisibility(View.VISIBLE);
//                            musicbotom.setVisibility(View.INVISIBLE);
                            mRlBottom.setVisibility(View.GONE);

                        } else {
                            // deltaY = 0.0 时
                            mRlTop.setVisibility(View.VISIBLE);
                            mRlBottom.setVisibility(View.VISIBLE);
                            mLastY = motionEvent.getRawY();
                            return false;//返回false即可响应click事件
                        }
                        mLastY = motionEvent.getRawY();
                        break;
                    default:
                        // reset
                        mLastY = -1;
                        mRlTop.setVisibility(View.VISIBLE);
                        mRlBottom.setVisibility(View.VISIBLE);
                        break;
                }
                return false;}
        });

        musicList  = scanAllAudioFiles();
        //这里其实可以直接在扫描时返回 ArrayList<Map<String, Object>>()
        listems = new ArrayList<Map<String, Object>>();
        for (Iterator iterator = musicList.iterator(); iterator.hasNext();) {
            Map<String, Object> map = new HashMap<String, Object>();
            MusicMedia mp3Info = (MusicMedia) iterator.next();
//            map.put("id",mp3Info.getId());
            map.put("title", mp3Info.getTitle());
            map.put("artist", mp3Info.getArtist());
            map.put("album", mp3Info.getAlbum());
//            map.put("albumid", mp3Info.getAlbumId());
            map.put("duration", mp3Info.getTime());
            map.put("size", mp3Info.getSize());
            map.put("url", mp3Info.getUrl());

            map.put("bitmap", R.drawable.musicfile);

            listems.add(map);

        }

        /*SimpleAdapter的参数说明
         * 第一个参数 表示访问整个android应用程序接口，基本上所有的组件都需要
         * 第二个参数表示生成一个Map(String ,Object)列表选项
         * 第三个参数表示界面布局的id  表示该文件作为列表项的组件
         * 第四个参数表示该Map对象的哪些key对应value来生成列表项
         * 第五个参数表示来填充的组件 Map对象key对应的资源一依次填充组件 顺序有对应关系
         * 注意的是map对象可以key可以找不到 但组件的必须要有资源填充  因为 找不到key也会返回null 其实就相当于给了一个null资源
         * 下面的程序中如果 new String[] { "name", "head", "desc","name" } new int[] {R.id.name,R.id.head,R.id.desc,R.id.head}
         * 这个head的组件会被name资源覆盖
         * */
        SimpleAdapter mSimpleAdapter = new SimpleAdapter(
                this,
                listems,
                R.layout.music_item,
                new String[] {"bitmap","title","artist", "size","duration"},
                new int[] {R.id.video_imageView,R.id.video_title,R.id.video_singer,R.id.video_size,R.id.video_duration}
        );
        //listview里加载数据
        mLvMusic.setAdapter(mSimpleAdapter);









    }

    /*加载媒体库里的音频*/
    public ArrayList<MusicMedia> scanAllAudioFiles(){
        //生成动态数组，并且转载数据
        ArrayList<MusicMedia> mylist = new ArrayList<MusicMedia>();

        /*查询媒体数据库
        参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
        视频：MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        图片;MediaStore.Images.Media.EXTERNAL_CONTENT_URI

         */
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历媒体数据库
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                //歌曲编号
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌曲标题
                String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));


                if (size >1024*800){//大于800K
                    MusicMedia musicMedia = new MusicMedia();
                    musicMedia.setId(id);
                    musicMedia.setArtist(artist);
                    musicMedia.setSize(size);
                    musicMedia.setTitle(tilte);
                    musicMedia.setTime(duration);
                    musicMedia.setUrl(url);
                    musicMedia.setAlbum(album);
                    musicMedia.setAlbumId(albumId);

                    mylist.add(musicMedia);

                }
                cursor.moveToNext();
            }
        }
        return mylist;
    }


    private void playMusic(int currentposition) {






    }

    private void changeMode(int playmode) {
        edit.putInt(KEY_PLAY_MODE,playmode).commit();
        mIvPlayMode.setBackgroundResource(modepic[playmode]);
    }
}
