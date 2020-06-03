package ru.vladimirvlasoff.videogallery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdapterVideoList extends RecyclerView.Adapter<AdapterVideoList.MyViewHolder> {
    // списко
    private ArrayList<ModelVideo> videosList = new ArrayList<>();
    // контекст
    private Context context;
    // конструктор
    AdapterVideoList(Context context, ArrayList<ModelVideo> videosList){
        this.context = context;
        this.videosList = videosList;
    }
    // привязываем компоненты к коду
    @NonNull
    @Override
    public AdapterVideoList.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // получаем вью
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video, parent, false);
        // и инициализируем их
        return new MyViewHolder(itemView);
    }
    // обработка нажатий на видео в списке
    @Override
    public void onBindViewHolder(@NonNull AdapterVideoList.MyViewHolder holder, int position) {
        final ModelVideo item = videosList.get(position);
        holder.tv_title.setText(item.getTitle());
        holder.tv_duration.setText(item.getDuration());
        Glide.with(context).load(item.getData()).into(holder.imgView_thumbnail);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PlayerActivity.class);
            intent.putExtra("videoId", item.getId());
            v.getContext().startActivity(intent);
        });
    }
    // возвращаем количество элементов
    @Override
    public int getItemCount() {
        return videosList.size();
    }
    // в этом классе инициализируем компоненты
    static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgView_thumbnail;
        private TextView tv_title, tv_duration;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_duration = itemView.findViewById(R.id.tv_duration);
            imgView_thumbnail = itemView.findViewById(R.id.imageView_thumbnail);
        }
    }
}
