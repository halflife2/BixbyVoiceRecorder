package com.example.bixbyvoicerecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity<Save_Path> extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; //오디오 요청에 대한 응답코드 정의
    SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

    public static ArrayList<String> BixbyUtter = new ArrayList<>();
    public static ArrayList<Boolean> Recordcheck = new ArrayList<>();
    public static String fileName; //녹음파일 경로를 저장할 변수 선언
    public static MediaRecorder mediaRecorder = null;
    public static MediaPlayer mediaPlayer = null;
    public static int totalcount = 0;
    public static String Down_File_Name = "readfile";
    public static String Down_File_extend = ".txt";
    public static String Save_folder = Down_File_Name;
    public static String UpLoad_File_extend = ".3gp";
    public static final String BASE_URL = "http://121.162.235.155:3000";
    public static String Down_URL = "files/downloadfile"; //서버 위치
    public static String Save_Path;
    fragment_main.DownloadThread dThread;
    public static final int COMPRESSION_LEVEL = 8;
    public static final int BUFFER_SIZE = 1024 * 2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if(ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
        setContentView(R.layout.activity_main);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


    }
}