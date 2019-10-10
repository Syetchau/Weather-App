package com.example.weatherapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.Common.Common;
import com.example.weatherapp.Model.WeatherResult;
import com.example.weatherapp.Retrofit.IOpenWeatherMap;
import com.example.weatherapp.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {

    ImageView imgWeather;
    TextView txtCityName, txtHumidity, txtSunrise, txtSunset, txtPressure, txtTemp,
            txtDescription, txtDate, txtWind, txtGeoCoord, txtCloud;
    LinearLayout weatherPanel;
    ProgressBar progressBar;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    static TodayWeatherFragment instance;

    public static TodayWeatherFragment getInstance() {
        // Required empty public constructor
        if (instance == null)
            instance = new TodayWeatherFragment();
        return instance;
    }

    public TodayWeatherFragment(){

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);

        imgWeather = (ImageView)itemView.findViewById(R.id.img_weather);
        txtCityName = (TextView)itemView.findViewById(R.id.txt_city_name);
        txtDate = (TextView)itemView.findViewById(R.id.txt_date_time);
        txtHumidity = (TextView)itemView.findViewById(R.id.txt_humidity);
        txtSunrise = (TextView)itemView.findViewById(R.id.txt_sunrise);
        txtSunset = (TextView)itemView.findViewById(R.id.txt_sunset);
        txtPressure = (TextView)itemView.findViewById(R.id.txt_pressure);
        txtTemp = (TextView)itemView.findViewById(R.id.txt_temperature);
        txtDescription = (TextView)itemView.findViewById(R.id.txt_description);
        txtWind = (TextView)itemView.findViewById(R.id.txt_wind);
        txtGeoCoord = (TextView)itemView.findViewById(R.id.txt_geo_coord);
        txtCloud = (TextView)itemView.findViewById(R.id.txt_cloud);

        weatherPanel = (LinearLayout)itemView.findViewById(R.id.weather_panel);
        progressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);

        getWeatherInfo();

        return itemView;
    }

    private void getWeatherInfo() {

        compositeDisposable.add(mService.getWeatherByLatLng(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        //load image
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                        .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString())
                                .into(imgWeather);

                        //get wind data
                        String speed = new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed()))
                                .append("m/s").toString();
                        String degree = new StringBuilder(String.valueOf(weatherResult.getWind().getDeg()))
                                .append("°").toString();

                        //load data
                        txtCityName.setText(weatherResult.getName());
                        txtDescription.setText(new StringBuilder("Weather in ")
                             .append(weatherResult.getName()).toString());
                        txtTemp.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp()))
                                .append("°C").toString());
                        txtDate.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txtPressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure()))
                                .append(" hpa").toString());
                        txtHumidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity()))
                                .append(" %").toString());
                        txtSunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txtSunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txtGeoCoord.setText(new StringBuilder("")
                                .append(weatherResult.getCoord().toString())
                                .append("").toString());
                        txtCloud.setText(new StringBuilder(String.valueOf(weatherResult.getClouds().getAll()))
                                .append(" %").toString());
                        txtWind.setText(speed + ", " + degree);

                        
                        //display panel
                        weatherPanel.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                },
                    new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }
    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
