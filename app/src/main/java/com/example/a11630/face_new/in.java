package com.example.a11630.face_new;
/**安卓人脸识别APP的一个界面后端代码包**/
import android.annotation.TargetApi;
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a11630.R;
import com.example.a11630.domain.add_result_bean;
import com.example.a11630.tools.Base64Util;
import com.example.a11630.tools.GsonUtils;
import com.example.a11630.tools.HttpUtil;
import com.example.a11630.tools.MyHelper;
import com.example.a11630.tools.toolsUnit;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class in extends AppCompatActivity implements View.OnClickListener {
    //定义登录的姓名和ID号
    String names, IDs;

    //定义登录按钮
    Button btn_Login;

    //定义输入的文本域
    EditText et_name, et_ID;

    //定义图片的路径
    private String imagePath = null;

    //定义图片的uri
    private Uri imageUri;

    //定义返回值，判断图片的来源
    private int Photo_ALBUM = 1, CAMERA = 2;

    int FLAG = 0;
    private Bitmap bp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        btn_Login = (Button) findViewById(R.id.btn_login);
        btn_Login.setOnClickListener(this);
        et_name = (EditText) findViewById(R.id.name);
        et_ID = (EditText) findViewById(R.id.ID);
        FLAG = 0;
    }

    //检查登录是否有效
    boolean check(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    //登录按钮被点击
    @Override
    public void onClick(View v) {
        names = et_name.getText().toString().trim();
        IDs = et_ID.getText().toString().trim();

        if (names.equals("") || IDs.equals("")) {
            Toast.makeText(this, "昵称和姓名不能为空", Toast.LENGTH_SHORT).show();
        } else if (check(IDs) == false) {
            Toast.makeText(this, "昵称非法", Toast.LENGTH_SHORT).show();
        } else {
            // 设置对话框标题
            new AlertDialog.Builder(in.this)
                    .setTitle("系统提示")

                    // 设置显示的内容
                    .setMessage("请选择上传方式")


                    //右边按钮
                    .setPositiveButton("返回",
                            new DialogInterface.OnClickListener() {// 添加确定按钮

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {// 确定按钮的响应事件
                                }

                            })
                    //中间按钮
                    .setNeutralButton("从相册上传", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //选择数据
                            Intent in = new Intent(Intent.ACTION_PICK);

                            //选择的数据为图片
                            in.setType("image/*");
                            startActivityForResult(in, Photo_ALBUM);
                        }
                    })
                    //左边按钮
                    .setNegativeButton("拍照",
                            new DialogInterface.OnClickListener() {

                                @TargetApi(Build.VERSION_CODES.M)
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {

                                    //7.0拍照必加
                                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                    StrictMode.setVmPolicy(builder.build());
                                    builder.detectFileUriExposure();

                                    //临时照片存储地
                                    File outputImage = new File(Environment.getExternalStorageDirectory()
                                            + File.separator + "face.jpg");
                                    try {
                                        if (outputImage.exists()) {
                                            outputImage.delete();
                                        }

                                        //创建零食地址
                                        outputImage.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    //获取Uri
                                    imageUri = Uri.fromFile(outputImage);   //将file转成uri对象
                                    imagePath = outputImage.getAbsolutePath();

                                    //，是传递你要保存的图片的路径，系统会根据你提供的地址进行保存图片
                                    Log.i("拍照图片路径", imagePath);

                                    //跳转相机
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                    //相片输出路径
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                                    //返回照片路径
                                    startActivityForResult(intent, CAMERA);
                                }
                            }).show();// 在按键响应事件中显示此对话框
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 相册选择图片
        if (requestCode == Photo_ALBUM) {
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToNext();

                //获得图片的绝对路径
                imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                cursor.close();
                Log.i("图片路径", imagePath);
                bp = toolsUnit.getimage(imagePath);
                runthreaad();
            }
        } else if (requestCode == CAMERA) {
            bp = toolsUnit.getimage(imagePath);
            runthreaad();
        }
    }



    void runthreaad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
                try {
                    byte[] bytes1 = toolsUnit.getBytesByBitmap(bp);
                    //   byte[] bytes1 = FileUtil.readFileByBytes(imagePath);
                    String image1 = Base64Util.encode(bytes1);



                    Map<String, Object> map = new HashMap<>();
                    map.put("image", image1);
                    map.put("group_id", "face");
                    map.put("user_id", IDs);
                    map.put("user_info", "abc");
                    map.put("liveness_control", "NORMAL");
                    map.put("image_type", "BASE64");
                    map.put("quality_control", "LOW");
                    String param = GsonUtils.toJson(map);

                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。

                    String clientId = "g8fLyUEvCkwnmhBdhEKW17pT";
                    String clientSecret = "6LRXGcuZtZGgRxFWMTqAtFLYI5WUOFSu";
                    String accessToken = toolsUnit.getAuth(clientId, clientSecret);

                   //  = "24.470560ecfc8ded10d622b3dd4e258f34.2592000.1563086633.282335-15236904";

                    String result = HttpUtil.post(url, accessToken, "application/json", param);
                    System.out.println(result);
                    Gson gson = new Gson();
                    add_result_bean Result_bean = gson.fromJson(result, add_result_bean.class);

                    int Error_code = Result_bean.getError_code();
                    if (Error_code == 0) {
                        SQLiteDatabase db;
                        MyHelper ggg = new MyHelper(in.this);
                        db = ggg.getWritableDatabase();
                        ggg.Insert(db, "name_id", names, IDs);

                        /**楼下这段话之后的语句跑不了**/
                        Looper.prepare();
                        Toast.makeText(in.this, "上传成功", Toast.LENGTH_LONG).show();
                        Looper.loop();

                    } else {
                        String error_message = "上传失败：" + Result_bean.getError_msg();
                        System.out.println("xixixixixixi" + error_message);
                        Looper.prepare();
                        Toast.makeText(in.this, error_message, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



}

