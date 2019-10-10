package com.example.weatherapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {

    private List<String> lstCities;
    private MaterialSearchBar materialSearchBar;

    ImageView imgWeather;
    TextView txtCityName, txtHumidity, txtSunrise, txtSunset, txtPressure, txtTemp,
            txtDescription, txtDate, txtWind, txtGeoCoord, txtCloud;
    LinearLayout weatherPanel;
    ProgressBar progressBar;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    static CityFragment instance;

    public static CityFragment getInstance() {
        // Required empty public constructor
        if (instance == null)
            instance = new CityFragment();
        return instance;
    }

    public CityFragment() {

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_city, container, false);

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
        materialSearchBar = (MaterialSearchBar)itemView.findViewById(R.id.search_bar);
        materialSearchBar.setEnabled(false);

        //AsynTask class to load cities list
        new LoadCities().execute();

        return itemView;
    }

    private class LoadCities extends SimpleAsyncTask<List<String>> {

        @Override
        protected List<String> doInBackgroundSimple() {
            lstCities = new ArrayList<>();
            try {
                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String readed;
                while ((readed = bufferedReader.readLine()) != null)
                    stringBuilder.append(readed);

                lstCities = new Gson().fromJson(stringBuilder.toString(), new TypeToken<List<String>>(){}.getType());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return lstCities;
        }

        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);

            materialSearchBar.setEnabled(true);
            materialSearchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    List<String> suggest = new ArrayList<>();
                    for (String search: listCity)
                    {
                        if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    materialSearchBar.setLastSuggestions(suggest);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {

                    getWeatherInfo(text.toString());
                    materialSearchBar.setLastSuggestions(listCity);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });

            materialSearchBar.setLastSuggestions(listCity);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void getWeatherInfo(String cityName) {

        compositeDisposable.add(mService.getWeatherByCityName(
                cityName,
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
