package com.example.a11630.face_new;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11630.R;
import com.example.a11630.domain.Search_result_bean;
import com.example.a11630.tools.Base64Util;
import com.example.a11630.tools.GsonUtils;
import com.example.a11630.tools.HttpUtil;
import com.example.a11630.tools.MyHelper;
import com.example.a11630.tools.SearchThread;
import com.example.a11630.tools.toolsUnit;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class opt extends AppCompatActivity implements View.OnClickListener {

    private String ImagePath = null;
    private Uri imageUri,imageUri_display;
    private int Photo_ALBUM = 1, CAMERA = 2;
    private Bitmap bp = null;

    String result;

    Button btn_pai, btn_xuan;
    ImageView iv_picture;
    TextView tv_sum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opt);
        btn_pai = (Button) findViewById(R.id.take_a_picture);
        btn_pai.setOnClickListener(this);
        iv_picture = (ImageView) findViewById(R.id.picture);
        btn_xuan = (Button) findViewById(R.id.xuan);
        btn_xuan.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {          //点击拍照或者从相册选取，返回值为带地址的intent
        if (v.getId() == R.id.take_a_picture) {   ///拍照
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();            //7.0拍照必加
            File outputImage = new File(Environment.getExternalStorageDirectory() + File.separator + "face.jpg");     //临时照片存储地
            try {                                                                                   //文件分割符
                if (outputImage.exists()) {   //如果临时地址有照片，先清除
                    outputImage.delete();
                }
                outputImage.createNewFile();    ///创建临时地址
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageUri = Uri.fromFile(outputImage);              //获取Uri

         //   imageUri_display= FileProvider.getUriForFile(opt.this,"com.example.a11630.face_new.fileprovider",outputImage);

            ImagePath = outputImage.getAbsolutePath();
            Log.i("拍照图片路径", ImagePath);         //，是传递你要保存的图片的路径，打开相机后，点击拍照按钮，系统就会根据你提供的地址进行保存图片
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //跳转相机
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);                          //相片输出路径
            startActivityForResult(intent, CAMERA);                        //返回照片路径

        } else {
            Intent in = new Intent(Intent.ACTION_PICK);      //选择数据
            in.setType("image/*");                     //选择的数据为图片
            startActivityForResult(in, Photo_ALBUM);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相册选择图片
        if (requestCode == Photo_ALBUM) {
            if (data != null) {       //开启了相册，但是没有选照片
                Uri uri = data.getData();
                //从uri获取内容的cursor
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToNext();
                ImagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));   //获得图片的绝对路径
                cursor.close();
                Log.i("图片路径", ImagePath);
                bp = toolsUnit.getimage(ImagePath);
              //  iv_picture.setImageBitmap(bp);
                runthreaad();      //开启线程，传入图片
            }
        } else if (requestCode == CAMERA) {

            bp = toolsUnit.getimage(ImagePath);
         //  iv_picture.setImageBitmap(bp);
            runthreaad();  //开启线程，传入图片
        }
    }

    void runthreaad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/search";
                try {
                    byte[] bytes1 = toolsUnit.getBytesByBitmap(bp);

                    String image1 = Base64Util.encode(bytes1);

                    Map<String, Object> map = new HashMap<>();
                    map.put("image", image1);
                    map.put("liveness_control", "NORMAL");
                    map.put("group_id_list", "face");
                    map.put("image_type", "BASE64");
                    map.put("quality_control", "LOW");

                    String param = GsonUtils.toJson(map);


                    String clientId = "g8fLyUEvCkwnmhBdhEKW17pT";
                    String clientSecret = "6LRXGcuZtZGgRxFWMTqAtFLYI5WUOFSu";
                    String accessToken = toolsUnit.getAuth(clientId, clientSecret);

                    result = HttpUtil.post(url, accessToken, "application/json", param);
                    System.out.println("hehehe:" + result);


                    Gson gson = new Gson();                      //新建GSON
                    Search_result_bean Result_bean = gson.fromJson(result, Search_result_bean.class); //GSON与我的工具类绑定
                  //  System.out.println("哈哈哈哈哈哈哈哈" + Result_bean.getError_code());
                    int Error_code = Result_bean.getError_code();
                    if (Error_code == 0) {                     //返回值为零，就是打卡识别成功

                        double score = Result_bean.getResult().getUser_list().get(0).getScore();   //一层层进入，获取到score

                        String user = Result_bean.getResult().getUser_list().get(0).getUser_id();   //获取学号

                     //   System.out.println("分数：" + score);
                        if (score >= 70.0) {                                  //分数大于70.0分，判断为同一个人，提示打卡成功

                            SQLiteDatabase db;
                            MyHelper ggg = new MyHelper(opt.this);
                            db = ggg.getWritableDatabase();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                            //  System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
                            ggg.Insert_two(db, "time_id", df.format(new Date()), user);


                            Looper.prepare();
                            Toast.makeText(opt.this, "打卡成功！", Toast.LENGTH_LONG).show();
                            //Looper.loop();
///////////////////////////////////////////////////////////////

                            Intent now = new Intent(opt.this, ConnActivity_student.class);
                            now.putExtra("data","学号:"+user);
                            Intent now_teacher = new Intent(opt.this, SearchThread.class);
                            now_teacher.putExtra("data","学号:"+user);
                            startActivity(now);

                            Looper.loop();

                        } else {
                            Looper.prepare();
                            Toast.makeText(opt.this, "打卡失败！照片不在人脸库", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    } else {
                        String error_message = "打卡失败：" + Result_bean.getError_msg();
                        System.out.println("shibai" + error_message);

                        Looper.prepare();
                        Toast.makeText(opt.this, error_message, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }

                } catch (Exception e) {
                    Log.i("错误", "shibai");
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
