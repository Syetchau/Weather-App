package com.example.weatherapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weatherapp.Common.Common;
import com.example.weatherapp.Model.WeatherForecastResult;
import com.example.weatherapp.R;
import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {

    Context context;
    WeatherForecastResult weatherForecastResult;

    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        double temp = (weatherForecastResult.list.get(position).main.getTemp()/10);

        //load icon
        //load image
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.list.get(position).getWeather().get(0).getIcon())
                .append(".png").toString())
                .into(holder.imgWeather);

        holder.txtDateTime.setText(new StringBuilder
                (Common.convertUnixToDate(weatherForecastResult.list.get(position).dt)));
        holder.txtDescription.setText(new StringBuilder
                (weatherForecastResult.list.get(position).weather.get(0).getDescription()));
        holder.txtTemp.setText(new StringBuilder
                (String.format(String.valueOf(Common.roundTwoDecimals(temp)))).append("°C"));
    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtDateTime, txtDescription, txtTemp;
        ImageView imgWeather;

        public MyViewHolder(View itemView) {
            super(itemView);

            imgWeather = (ImageView)itemView.findViewById(R.id.img_weather_forecast);
            txtDateTime = (TextView)itemView.findViewById(R.id.txt_date);
            txtDescription = (TextView)itemView.findViewById(R.id.txt_desc);
            txtTemp = (TextView)itemView.findViewById(R.id.txt_temp);
        }
    }
}
