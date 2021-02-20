package com.example.afinal;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import static com.example.afinal.Proxy.getProxy;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<ApiResponse> dataSet;
    public List<VideoViewHolder> viewHolderList;
    public List<Boolean> attachedHolders;
    public Boolean isClickAllowed = true;
    private Context context;
    private ViewPager2 viewPager;
    private long firstClick=0;
    private long secondClick;
    private boolean isLike=false;

    public VideoAdapter(Context listener, ViewPager2 viewPager) {
        context = listener;
        viewHolderList = new ArrayList<>();
        attachedHolders = new ArrayList<>();
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.im_list_item, parent, false);
        VideoViewHolder viewHolder = new VideoViewHolder(itemView);
        viewHolder.context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(dataSet.get(position));
        holder.position = position;
        viewHolderList.set(position, holder);
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d("Focus", "Attached " + holder.position);
        attachedHolders.set(holder.position, true);
        if (holder.progress != -1)
            holder.videoView.seekTo(holder.progress);
//            holder.playIcon.setVisibility(View.INVISIBLE);
        holder.videoView.post(new Runnable() {
            @Override
            public void run() {
                holder.videoView.start();
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        attachedHolders.set(holder.position, false);
        holder.progress = holder.videoView.getCurrentPosition();
        Log.d("Focus", "Detached " + holder.position);
    }


    public void setDataSet(List<ApiResponse> data) {
        dataSet = data;
        viewHolderList.clear();
        attachedHolders.clear();
        for (int i = 0; i < data.size(); i++) {
            viewHolderList.add(null);
            attachedHolders.add(false);
        }
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder {
        // Data
        public ApiResponse apiResponse;
        public Context context;
        public int position;

        // Video
        public VideoView videoView;
        private ImageView playIcon;
        private int progress = -1;
        private ImageView likeIcon;
        private ListView comment_list;
        private ImageView comment;
        private TextView hide_down;
        private EditText comment_content;
        private Button comment_send;
        private HttpProxyCacheServer proxy;
        private ArrayList<Comment> data;
        private AdapterComment adapterComment;
        private RelativeLayout comment1;
        private RelativeLayout rl_comment;
        // VideoInfo
        private TextView description;
        private TextView author;

        public LinearLayout close_comment;
        public Boolean isVideoPausedBeforeEnterComment = false;
        public Boolean isInComment = false;

        private BottomNavigationView bnvMenu;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            proxy = getProxy(itemView.getContext());
            getViewsById(itemView);
//            setAllClickHandler(itemView);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer mp) {
                            videoView.start();
                        }
                    });
                }
            });

            // 播放完毕时自动重播
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });

            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(videoView.isPlaying()){
                        videoView.pause();
                    }
                    else {
                        videoView.start();
                    }
                    if(firstClick==0){
                        firstClick= SystemClock.uptimeMillis();
                    }
                    else{
                        secondClick=SystemClock.uptimeMillis();
                        if(secondClick-firstClick<=500){
                            if(isLike){
                                isLike=false;
                                likeIcon.setImageResource(R.drawable.heart_blank);
                            }
                            else {
                                isLike=true;
                                likeIcon.setImageResource(R.drawable.heart_fill);
                            }
                        }
                        else {
                            firstClick=secondClick;
                        }
                    }
                }
            });
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    comment1.setVisibility(View.VISIBLE);
                    comment_list.setVisibility(View.VISIBLE);
                    rl_comment.setVisibility(View.VISIBLE);
//                    InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });

            hide_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide_down.clearAnimation();
                    comment1.setVisibility(View.GONE);
                    comment_list.setVisibility(View.GONE);
                    rl_comment.setVisibility(View.GONE);
                    InputMethodManager im = (InputMethodManager)itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(comment_content.getWindowToken(), 0);
                }
            });

            comment_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendComment();
                }
            });

            videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Log.d("VideoView", "surfaceCreate " + position);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d("VideoView", "surfaceChanged " + position);
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    Log.d("VideoView", "surfaceDestroyed " + position);
                }
            });


            videoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.d("WhereDoesFocusGo?", "videoView " + position + " " + String.valueOf(hasFocus));
                }
            });
        }

        public void getViewsById(@NonNull View itemView) {
            videoView = itemView.findViewById(R.id.videoView);
            description = itemView.findViewById(R.id.des);
            author = itemView.findViewById(R.id.author);
            likeIcon=itemView.findViewById(R.id.beforelike);
            comment_list=itemView.findViewById(R.id.comment_list);
            comment = itemView.findViewById(R.id.comment);
            hide_down =  itemView.findViewById(R.id.hide_down);
            comment_content =  itemView.findViewById(R.id.comment_content);
            comment_send = itemView.findViewById(R.id.comment_send);
            comment1=itemView.findViewById(R.id.comment1);
            rl_comment=itemView.findViewById(R.id.rl_comment);
        }


//        public void setAllClickHandler(@NonNull View itemView) {
////
//
//            close_comment.setOnClickListener(view -> {
//                if (!isClickAllowed)
//                    return;
////                comment.setVisibility(View.GONE);
////                listener.onCommentClick();
//                viewPager.setUserInputEnabled(true);
//                isInComment = false;
//                if (!isVideoPausedBeforeEnterComment)
//                    videoView.start();
//            });
//        }

        public void bind(ApiResponse apiResponse) {
            this.apiResponse = apiResponse;
            if (!apiResponse.url.startsWith("https")) {
                StringBuilder stringBuilder = new StringBuilder(apiResponse.url);
                stringBuilder.insert(4, 's');
                this.apiResponse.url = stringBuilder.toString();
            }
            updateData();
        }

        public void updateData() {
            String proxyUrl = proxy.getProxyUrl(apiResponse.url);
            videoView.setVideoPath(proxyUrl);
            description.setText(apiResponse.description);
            author.setText('@' + apiResponse.nickname);
            if (progress != -1)
                videoView.seekTo(progress);
            //
            // 初始化数据
            data = new ArrayList<>();
            // 初始化适配器
            adapterComment = new AdapterComment(itemView.getContext(), data);
            // 为评论列表设置适配器
            comment_list.setAdapter(adapterComment);
        }

        public void sendComment(){
            if(comment_content.getText().toString().equals("")){
                Toast.makeText(itemView.getContext(), "评论不能为空！", Toast.LENGTH_SHORT).show();
            }else{
                // 生成评论数据
                Comment comment = new Comment();
                comment.setName("评论者"+(data.size()+1)+"：");
                comment.setContent(comment_content.getText().toString());
                adapterComment.addComment(comment);
                // 发送完，清空输入框
                comment_content.setText("");

                Toast.makeText(itemView.getContext(), "评论成功！", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
