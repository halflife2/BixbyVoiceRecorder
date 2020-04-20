package com.example.bixbyvoicerecorder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class fragment_main extends Fragment {
    public ArrayList<String> BixbyUtter = new ArrayList<>();
    public TextView tv0;
    public TextView textView;
    private ImageButton backbutton;
    private ImageButton nextbutton;
    private ImageButton downloadButton;
    private ImageButton uploadbutton;
    private ImageButton playButton;
    private ImageButton stopButton;
    private ImageButton recordButton;
    private String fileName; //녹음파일 경로를 저장할 변수 선언
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    int totalcount = 0;
    int presentcount = 1;
    String Down_File_Name = "readfile";
    String Down_File_extend = ".txt";
    String Save_folder = Down_File_Name;
    String UpLoad_File_extend = ".wav";
    public static final String BASE_URL = "http://121.162.235.155:3000";
    String Down_URL = "files/downloadfile"; //서버 위치
    String Save_Path;
    DownloadThread dThread;
    final int COMPRESSION_LEVEL = 8;
    final int BUFFER_SIZE = 1024 * 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        tv0 = view.findViewById(R.id.tv0);
        textView = view.findViewById(R.id.textView);
        backbutton = view.findViewById(R.id.backbutton);
        nextbutton = view.findViewById(R.id.nextbutton);
        uploadbutton = view.findViewById(R.id.uploadButton);
        downloadButton = view.findViewById(R.id.downloadButton);
        playButton = view.findViewById(R.id.playButton);
        stopButton = view.findViewById(R.id.stopButton);
        recordButton = view.findViewById(R.id.recordButton);
        stopButton.setVisibility(View.INVISIBLE);
        FileDown();

        downloadButton.setOnClickListener(new View.OnClickListener() { //불러오기 버튼
            int readstate =0;
            public void onClick(View v){
                if(readstate==0){
                    try {
                        ReadFileContent();
                        tv0.setText(BixbyUtter.get(presentcount-1));
                        textView.setText(presentcount+"/"+totalcount);
                        readstate =1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "파일을 모두 불러왔습니다.", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });

        uploadbutton.setOnClickListener(new View.OnClickListener() { //저장하기 버튼
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v){
                try {
                    CompressedFile();
                    FileUpload();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        backbutton.setOnClickListener(v -> {
            if(presentcount>1){
                presentcount--;
                textView.setText(presentcount+"/"+totalcount);
                tv0.setText(BixbyUtter.get(presentcount-1));
            } else{
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "가장 처음입니다.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
//
        nextbutton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(presentcount<totalcount){
                    presentcount++;
                    textView.setText(presentcount+"/"+totalcount);
                    tv0.setText(BixbyUtter.get(presentcount-1));
                }else{
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "가장 마지막입니다.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() { //녹음버튼
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() { //정지버튼
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() { //녹음 플레이버튼
            @Override
            public void onClick(View v) {
                startPlaying();
            }
        });

        return view;
    }

    class DownloadThread extends Thread {
        String ServerUrl;
        String LocalPath;
        DownloadThread(String serverPath, String localPath) {
            ServerUrl = serverPath;
            LocalPath = localPath;
        }
        @Override
        public void run() {
            URL imgurl;
            int Read;
            try {
                imgurl = new URL(ServerUrl);
                HttpURLConnection conn = (HttpURLConnection) imgurl
                        .openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();
                File file = new File(LocalPath);
                FileOutputStream fos = new FileOutputStream(file);
                for (;;) {
                    Read = is.read(tmpByte);
                    if (Read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, Read);
                }
                is.close();
                fos.close();
                conn.disconnect();

            } catch (MalformedURLException e) {
                Log.e("ERROR1", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR2", e.getMessage());
                e.printStackTrace();
            }
            mAfterDown.sendEmptyMessage(0);
        }
    }

    Handler mAfterDown = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //loadingBar.setVisibility(View.GONE);
            // 파일 다운로드 종료 후 다운받은 파일을 실행시킨다.
//            showDownloadFile();
        }

    };

    public static class RetrofitSender {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        public Api getApi() {
            Api api = retrofit.create(Api.class);
            return api;
        }
    }

    public void ReadFileContent() throws IOException {
        String line = null;
        System.out.println("위치1");
        System.out.println("위치"+getActivity().getExternalCacheDir());
        String utterFileName = getActivity().getExternalCacheDir().getAbsolutePath() +"/" + Save_folder +"/" + Down_File_Name  + Down_File_extend;

        try {
            BufferedReader buff = new BufferedReader((new InputStreamReader(new FileInputStream(String.valueOf(utterFileName)), "euc-kr")));
            while ((line = buff.readLine()) != null) {
                BixbyUtter.add(line);
                totalcount +=1;
                System.out.println("totalcountfm"+totalcount);
            }
            if((line = buff.readLine()) != null){


            }
        } catch (FileNotFoundException e ) {

        } catch(IOException e) {
            System.out.println(e);
        }
    }

    public void CompressedFile() throws Exception {
        String sourcePath = getActivity().getExternalCacheDir().getAbsolutePath() + "/" + Save_folder;
        String zipFile = Save_folder+".zip"; //저장되는 파일 이름
        // 압축 대상(sourcePath)이 디렉토리나 파일이 아니면 리턴한다.
        File sourceFile = new File(sourcePath);
        if (!sourceFile.isFile() && !sourceFile.isDirectory()) {
            throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;
        String absoultepath = getActivity().getExternalCacheDir().getAbsolutePath();
        try {
            fos = new FileOutputStream(new File(absoultepath, zipFile)); // FileOutputStream
            bos = new BufferedOutputStream(fos); // BufferedStream
            zos = new ZipOutputStream(bos); // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8
            zipEntry(sourceFile, sourcePath, zos); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
//
    private void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws Exception {
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if (sourceFile.isDirectory()) {
            if (sourceFile.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일 리스트
            for (int i = 0; i < fileArray.length; i++) {
                zipEntry(fileArray[i], sourcePath, zos); // 재귀 호출
            }
        } else { // sourcehFile 이 디렉토리가 아닌 경우
            BufferedInputStream bis = null;
            try {
                String sFilePath = sourceFile.getPath();
                Log.i("aa", sFilePath);
                //String zipEntryName = sFilePath.substring(sourcePath.length() + 1, sFilePath.length());
                StringTokenizer tok = new StringTokenizer(sFilePath, "/");
                int tok_len = tok.countTokens();
                String zipEntryName = tok.toString();
                while (tok_len != 0) {
                    tok_len--;
                    zipEntryName = tok.nextToken();
                }
                bis = new BufferedInputStream(new FileInputStream(sourceFile));

                ZipEntry zentry = new ZipEntry(zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);
                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;
                while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            } finally {
                if (bis != null) {
                    bis.close();
                }
            }
        }
    }
//
    public void FileUpload() {
        File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/" + Save_folder+ ".zip" );
//                if(!file.exists()){
//        File file = new File(pathName);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("uploadfile", file.getPath(), requestFile);
        Api api = new RetrofitSender().getApi();
        Call<MyResponse> call = api.upLoadFile(filePart);

        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                Log.v("TEST", String.valueOf(response.body().result));


                if (String.valueOf(response.body().result) == "true") {
                    Toast.makeText(getActivity().getApplicationContext(), "파일 업로드 완료", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "파일 업로드 실패", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
//                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println("error:"+ t.getMessage());
            }
        });
    }

    public void FileDown()  {
        Save_Path = getActivity().getExternalCacheDir().getAbsolutePath() + "/" + Save_folder;
        File dir = new File(Save_Path);
        if (!dir.exists()) {
            dir.mkdir();
        }else{
            System.out.println("이미 폴더가 있어요");
        }
        if (!new File(Save_Path + "/" + Down_File_Name + Down_File_extend).exists()) {
            dThread = new DownloadThread(BASE_URL + "/" + Down_URL + "/" + Down_File_Name + Down_File_extend,
                    Save_Path + "/" + Down_File_Name + Down_File_extend);
            dThread.start();
            System.out.println("다운로드시작");
        } else {
            System.out.println("이미 다운로드 완료");
        }
    }

    private void startRecording() {
        fileName = getActivity().getExternalCacheDir().getAbsolutePath() + "/" + Save_folder + "/" + Down_File_Name + presentcount + UpLoad_File_extend;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try{
            mediaRecorder.prepare();
        }
        catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "녹음 실패", Toast.LENGTH_LONG).show();
            mediaRecorder = null;
        }
        mediaRecorder.start();
        recordButton.setVisibility(View.INVISIBLE);
        stopButton.setVisibility(View.VISIBLE);
    }
    private void stopRecording(){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder=null;
        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("녹음 완료")        // 제목 설정
//                .setMessage("녹음이 완료되었습니다.")        // 메세지 설정
//                .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
//                .setPositiveButton("녹음된 파일 재생", new DialogInterface.OnClickListener(){
//                    // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
//                    public void onClick(DialogInterface dialog, int whichButton){
//                        //원하는 클릭 이벤트를 넣으시면 됩니다.
//                        startPlaying();
//                    }
//                })
//                .setNegativeButton("닫기", new DialogInterface.OnClickListener(){
//                    // 취소 버튼 클릭시 설정, 왼쪽 버튼입니다.
//                    public void onClick(DialogInterface dialog, int which){
//                        //public void onClick(DialogInterface dialog, int whichButton){
//                        //원하는 클릭 이벤트를 넣으시면 됩니다.
//                    }
//                });
//        AlertDialog dialog = builder.create();    // 알림창 객체 생성
//        dialog.show();    // 알림창 띄우기

        stopButton.setVisibility(View.INVISIBLE);
        recordButton.setVisibility(View.VISIBLE);
    }
    private void startPlaying(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });
        try{
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch(IOException e){
            System.out.println("@22222"+e);
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "재생 실패", Toast.LENGTH_LONG).show();
            mediaPlayer=null;
        }

    }
    private void stopPlaying() {
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
}
