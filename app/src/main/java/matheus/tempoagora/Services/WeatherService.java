package matheus.tempoagora.Services;

import com.google.gson.annotations.SerializedName;


import java.util.ArrayList;
import java.util.List;

import matheus.tempoagora.Models.CurrentWeather;
import matheus.tempoagora.Models.WeatherForecast;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Func1;

public class WeatherService {
    // We are implementing against version 2.5 of the Open Weather Map web service.
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
                                                                   @Query("lat") double latitude);

        @GET("/forecast/daily?units=metric&cnt=7&apikey=" + API_KEY)
        Observable<WeatherForecastListDataEnvelope> fetchWeatherForecasts(
                @Query("lon") double longitude, @Query("lat") double latitude);
    }

    public Observable<CurrentWeather> fetchCurrentWeather(final double longitude,
                                                          final double latitude) {
        return mWebService.fetchCurrentWeather(longitude, latitude)
                .flatMap(new Func1<CurrentWeatherDataEnvelope,
                        Observable<? extends CurrentWeatherDataEnvelope>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends CurrentWeatherDataEnvelope> call(
                            final CurrentWeatherDataEnvelope data) {
                        return data.filterWebServiceErrors();
                    }

                }).map(new Func1<CurrentWeatherDataEnvelope, CurrentWeather>() {

                    // Parse the result and build a CurrentWeather object.
                    @Override
                    public CurrentWeather call(final CurrentWeatherDataEnvelope data) {
                        return new CurrentWeather(data.locationName, data.timestamp,
                                data.weather.get(0).description, data.main.temp,
                                data.main.temp_min, data.main.temp_max);
                    }
                });
    }

    public Observable<List<WeatherForecast>> fetchWeatherForecasts(final double longitude,
                                                                   final double latitude) {
        return mWebService.fetchWeatherForecasts(longitude, latitude)
                .flatMap(new Func1<WeatherForecastListDataEnvelope,
                        Observable<? extends WeatherForecastListDataEnvelope>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends WeatherForecastListDataEnvelope> call(
                            final WeatherForecastListDataEnvelope listData) {
                        return listData.filterWebServiceErrors();
                    }

                }).map(new Func1<WeatherForecastListDataEnvelope, List<WeatherForecast>>() {

                    // Parse the result and build a list of WeatherForecast objects.
                    @Override
                    public List<WeatherForecast> call(final WeatherForecastListDataEnvelope listData) {
                        final ArrayList<WeatherForecast> weatherForecasts =
                                new ArrayList<>();

                        for (WeatherForecastListDataEnvelope.ForecastDataEnvelope data : listData.list) {
                            final WeatherForecast weatherForecast = new WeatherForecast(
                                    listData.city.name, data.timestamp, data.weather.get(0).description,
                                    data.temp.min, data.temp.max);
                            weatherForecasts.add(weatherForecast);
                        }

                        return weatherForecasts;
                    }
                });
    }

    /**
     * Base class for results returned by the weather web service.
     */
    private class WeatherDataEnvelope {
        @SerializedName("cod")
        private int httpCode;

        class Weather {
            String description;
        }

        /**
         * The web service always returns a HTTP header code of 200 and communicates errors
         * through a 'cod' field in the JSON payload of the response body.
         */
        Observable filterWebServiceErrors() {
            if (httpCode == 200) {
                return Observable.just(this);
            } else {
                return Observable.error(
                        new Exception("There was a problem fetching the weather data."));
            }
        }
    }

    /**
     * Data structure for current weather results returned by the web service.
     */
    private class CurrentWeatherDataEnvelope extends WeatherDataEnvelope {
        @SerializedName("name")
        String locationName;
        @SerializedName("dt")
        long timestamp;
         ArrayList<Weather> weather;
         Main main;

        class Main {
             float temp;
             float temp_min;
             float temp_max;
        }
    }

    /**
     * Data structure for weather forecast results returned by the web service.
     */
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
