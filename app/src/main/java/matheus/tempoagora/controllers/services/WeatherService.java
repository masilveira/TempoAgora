package matheus.tempoagora.controllers.services;

import com.google.gson.annotations.SerializedName;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import matheus.tempoagora.models.CurrentWeather;
import matheus.tempoagora.models.WeatherForecast;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Func1;

public class WeatherService {
    private static final String DISPLAY_LANGUAGE = Locale.getDefault().getLanguage();
    private static final String WEB_SERVICE_BASE_URL = "http://api.openweathermap.org/data/2.5";
    private static final String API_KEY = "ca3f3f3b4c5fe0541d35f77df61292ad";
    private final OpenWeatherMapWebService mWebService;

    public WeatherService() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WEB_SERVICE_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        mWebService = restAdapter.create(OpenWeatherMapWebService.class);
    }

    private interface OpenWeatherMapWebService {

        @GET("/weather?units=metric&apikey=" + API_KEY)
        Observable<CurrentWeatherDataEnvelope> fetchCurrentWeather(@Query("lon") double longitude,
                                                                   @Query("lat") double latitude,
                                                                   @Query("lang") String language);
        @GET("/weather?units=metric&apikey=" + API_KEY)
        Observable<CurrentWeatherDataEnvelope> fetchCurrentWeather(@Query("q") String city,
                                                                   @Query("lang") String language);

        @GET("/forecast/daily?units=metric&cnt=7&apikey=" + API_KEY)
        Observable<WeatherForecastListDataEnvelope> fetchWeatherForecasts(
                @Query("lon") double longitude, @Query("lat") double latitude,
                @Query("lang") String language);

        @GET("/forecast/daily?units=metric&cnt=7&apikey=" + API_KEY)
        Observable<WeatherForecastListDataEnvelope> fetchWeatherForecasts(
                @Query("q") String city,
                @Query("lang") String language);
    }

    public Observable<CurrentWeather> fetchCurrentWeather(final double longitude,
                                                          final double latitude) {
        return mWebService.fetchCurrentWeather(longitude, latitude, DISPLAY_LANGUAGE)
                .flatMap(new Func1<CurrentWeatherDataEnvelope,
                        Observable<? extends CurrentWeatherDataEnvelope>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends CurrentWeatherDataEnvelope> call(
                            final CurrentWeatherDataEnvelope data) {
                        return data.filterWebServiceErrors();
                    }

                }).map(parseCurrentWeather);
    }

    public Observable<CurrentWeather> fetchCurrentWeather(final String city) {
        return mWebService.fetchCurrentWeather(city, DISPLAY_LANGUAGE)
                .flatMap(new Func1<CurrentWeatherDataEnvelope,
                        Observable<? extends CurrentWeatherDataEnvelope>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends CurrentWeatherDataEnvelope> call(
                            final CurrentWeatherDataEnvelope data) {
                        return data.filterWebServiceErrors();
                    }

                }).map(parseCurrentWeather);
    }

    private  Func1<CurrentWeatherDataEnvelope, CurrentWeather> parseCurrentWeather = new Func1<CurrentWeatherDataEnvelope, CurrentWeather>() {
        @Override
        public CurrentWeather call(final CurrentWeatherDataEnvelope data) {
            return new CurrentWeather(data.locationName, data.timestamp,
                    getWeatherIcon(data.weather.get(0).id), data.weather.get(0).description, data.main.temp,
                    data.main.temp_min, data.main.temp_max, data.main.humidity , data.sys.sunrise, data.sys.sunset, data.main.pressure);
        }
    };

    public Observable<List<WeatherForecast>> fetchWeatherForecasts(final String city) {
        return mWebService.fetchWeatherForecasts(city, DISPLAY_LANGUAGE)
                .flatMap(new Func1<WeatherForecastListDataEnvelope,
                        Observable<? extends WeatherForecastListDataEnvelope>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends WeatherForecastListDataEnvelope> call(
                            final WeatherForecastListDataEnvelope listData) {
                        return listData.filterWebServiceErrors();
                    }

                }).map(parseWeathers);
    }

    public Observable<List<WeatherForecast>> fetchWeatherForecasts(final double longitude,
                                                                   final double latitude) {
        return mWebService.fetchWeatherForecasts(longitude, latitude, DISPLAY_LANGUAGE)
                .flatMap(new Func1<WeatherForecastListDataEnvelope,
                        Observable<? extends WeatherForecastListDataEnvelope>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends WeatherForecastListDataEnvelope> call(
                            final WeatherForecastListDataEnvelope listData) {
                        return listData.filterWebServiceErrors();
                    }

                }).map(parseWeathers);
    }
    private Func1<WeatherForecastListDataEnvelope, List<WeatherForecast>> parseWeathers = new Func1<WeatherForecastListDataEnvelope, List<WeatherForecast>>() {
        @Override
        public List<WeatherForecast> call(final WeatherForecastListDataEnvelope listData) {
            final ArrayList<WeatherForecast> weatherForecasts =
                    new ArrayList<>();

            for (WeatherForecastListDataEnvelope.ForecastDataEnvelope data : listData.list) {
                final WeatherForecast weatherForecast = new WeatherForecast(
                        listData.city.name, data.timestamp, getWeatherIcon(data.weather.get(0).id), data.weather.get(0).description,
                        data.temp.min, data.temp.max);
                weatherForecasts.add(weatherForecast);
            }

            return weatherForecasts;
        }
    };
    private class WeatherDataEnvelope {
        @SerializedName("cod")
        private int httpCode;

        class Weather {
            String icon;
            String description;
            int id;
        }

        Observable filterWebServiceErrors() {
            if (httpCode == 200) {
                return Observable.just(this);
            } else {
                return Observable.error(
                        new Exception("There was a problem fetching the weather data."));
            }
        }
    }

    private class CurrentWeatherDataEnvelope extends WeatherDataEnvelope {
        @SerializedName("name")
        String locationName;
        @SerializedName("dt")
        long timestamp;
        ArrayList<Weather> weather;
        Main main;
        Sys sys;

        class Main {
            float temp;
            float temp_min;
            float temp_max;
            String humidity;
            String pressure;
        }

        class Sys {
            long sunrise;
            long sunset;
        }
    }

    public String getWeatherIcon(int actualId){
        int id = actualId / 100;
        String icon = "";
            switch(id) {
                case 2 : icon = "&#xf01e;";
                    break;
                case 3 : icon = "&#xf01c;";
                    break;
                case 7 : icon = "&#xf014;";
                    break;
                case 8 : icon = "&#xf013;";
                    break;
                case 6 : icon = "&#xf01b;";
                    break;
                case 5 : icon = "&#xf019;";
                    break;
                default:
                    icon = "&#xf02e;";
        }
        return icon;
    }


    private class WeatherForecastListDataEnvelope extends WeatherDataEnvelope {
        Location city;
        ArrayList<ForecastDataEnvelope> list;

        class Location {
            String name;
        }

        class ForecastDataEnvelope {
            @SerializedName("dt")
            long timestamp;
            Temperature temp;
            ArrayList<Weather> weather;
        }

        class Temperature {
            float min;
            float max;
        }
    }
}
