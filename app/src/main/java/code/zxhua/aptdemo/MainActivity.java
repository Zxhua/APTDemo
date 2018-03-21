package code.zxhua.aptdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import code.zxhua.app_annation.ActivityMap;


@ActivityMap(value = "主页面")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
