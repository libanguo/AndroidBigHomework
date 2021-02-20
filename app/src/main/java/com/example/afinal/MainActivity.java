package com.example.afinal;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity  {

    private ViewPager2 viewPager2;
    private VideoAdapter videoAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);;
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        viewPager2 = findViewById(R.id.video_info_viewpager2);
        videoAdapter = new VideoAdapter(this, viewPager2);
        viewPager2.setAdapter(videoAdapter);
        viewPager2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "Test");
            }
        });
        getData();
    }

    private void getData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://beiyou.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getFeed().enqueue(new Callback<List<ApiResponse>>() {
            @Override
            public void onResponse(Call<List<ApiResponse>> call, Response<List<ApiResponse>> response) {
                if (response.body() != null) {
                    List<ApiResponse> apiResponses = response.body();
                    Log.d("retrofit", apiResponses.toString());
                    videoAdapter.setDataSet(response.body());
                    videoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ApiResponse>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT);
                Log.d("retrofit", t.getMessage());
            }
        });
    }
}
