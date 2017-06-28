package com.hug.elasticview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ElasticView elasticView = (ElasticView) findViewById(R.id.elasticview);
        elasticView.setUnReadCount(30);
        elasticView.setUnreadMsgDotRemovedListener(new ElasticView.UnreadMsgDotRemovedListener() {
            @Override
            public void removed(boolean removed, int count) {
                if (removed){
                    Toast.makeText(MainActivity.this,"清除"+count+"条未读消息！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
