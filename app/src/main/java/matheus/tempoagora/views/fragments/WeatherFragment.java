package matheus.tempoagora.views.fragments;


import android.support.v4.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import matheus.tempoagora.R;
import matheus.tempoagora.views.adapters.WeatherForecastListAdapter;
import matheus.tempoagora.views.formatters.TemperatureFormatter;
import matheus.tempoagora.models.CurrentWeather;
import matheus.tempoagora.models.WeatherForecast;
import matheus.tempoagora.presenters.LocationService;
import matheus.tempoagora.presenters.WeatherService;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class WeatherFragment extends Fragment {

    private static final String KEY_CURRENT_WEATHER = "key_current_weather";
    private static final String KEY_WEATHER_FORECASTS = "key_weather_forecasts";
    private static final long LOCATION_TIMEOUT_SECONDS = 20;
    private static final String TAG = WeatherFragment.class.getCanonicalName();

    private CompositeSubscription mCompositeSubscription;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mLocationNameTextView;
    private TextView mCurrentTemperatureTextView;
    private ListView mForecastListView;
    private String city = null;

    public static WeatherFragment newInstance(String city) {

        Bundle args = new Bundle();
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(args);
        fragment.city = city;
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        mCompositeSubscription = new CompositeSubscription();
        final View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        mLocationNameTextView = (TextView) rootView.findViewById(R.id.location_name);
        mCurrentTemperatureTextView = (TextView) rootView
                .findViewById(R.id.current_temperature);

        mForecastListView = (ListView) rootView.findViewById(R.id.weather_forecast_list);
        final WeatherForecastListAdapter adapter = new WeatherForecastListAdapter(
                new ArrayList<WeatherForecast>(), getActivity());
        mForecastListView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.swipe_refresh_container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark,
                R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeather();
            }
        });

        updateWeather();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        mCompositeSubscription.unsubscribe();
        super.onDestroyView();
    }

    private void updateWeather() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (city != null) {
            final WeatherService weatherService = new WeatherService();
            mCompositeSubscription.clear();
            mCompositeSubscription.add(Observable.zip(
                    weatherService.fetchCurrentWeather(city),
                    weatherService.fetchWeatherForecasts(city),
                    new Func2<CurrentWeather, List<WeatherForecast>,
                            HashMap<String, WeatherForecast>>() {
                        @Override
                        public HashMap call(final CurrentWeather currentWeather,
                                            final List<WeatherForecast> weatherForecasts) {
                            HashMap<String, Object> weatherData = new HashMap<>();
                            weatherData.put(KEY_CURRENT_WEATHER, currentWeather);
                            weatherData.put(KEY_WEATHER_FORECASTS, weatherForecasts);
                            return weatherData;
                        }
                    }
            )                    .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<HashMap<String, WeatherForecast>>() {
                                @Override
                                public void onNext(final HashMap<String, WeatherForecast> weatherData) {
                                    final CurrentWeather currentWeather = (CurrentWeather) weatherData
                                            .get(KEY_CURRENT_WEATHER);
                                    mLocationNameTextView.setText(currentWeather.getLocationName());
                                    mCurrentTemperatureTextView.setText(
                                            TemperatureFormatter.format(currentWeather.getTemperature()));
                                    final List<WeatherForecast> weatherForecasts = (List<WeatherForecast>)
                                            weatherData.get(KEY_WEATHER_FORECASTS);
                                    final WeatherForecastListAdapter adapter = (WeatherForecastListAdapter)
                                            mForecastListView.getAdapter();
                                    adapter.clear();
                                    adapter.addAll(weatherForecasts);
                                }

                                @Override
                                public void onCompleted() {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }

                                @Override
                                public void onError(final Throwable error) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    Crouton.makeText(getActivity(),
                                            R.string.error_fetch_weather, Style.ALERT).show();
                                    error.printStackTrace();
                                }
                            })
            );
        } else {
            final LocationManager locationManager = (LocationManager) getActivity()
                    .getSystemService(Context.LOCATION_SERVICE);
            final LocationService locationService = new LocationService(locationManager, getActivity());
            final Observable<HashMap<String, WeatherForecast>> fetchDataObservable = locationService.getLocation()
                    .timeout(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .flatMap(new Func1<Location, Observable<HashMap<String, WeatherForecast>>>() {
                        @Override
                        public Observable<HashMap<String, WeatherForecast>> call(final Location location) {
                            final WeatherService weatherService = new WeatherService();
                            final double longitude = location.getLongitude();
                            final double latitude = location.getLatitude();

                            return Observable.zip(
                                    weatherService.fetchCurrentWeather(longitude, latitude),
                                    weatherService.fetchWeatherForecasts(longitude, latitude),
                                    new Func2<CurrentWeather, List<WeatherForecast>,
                                            HashMap<String, WeatherForecast>>() {
                                        @Override
                                        public HashMap call(final CurrentWeather currentWeather,
                                                            final List<WeatherForecast> weatherForecasts) {
                                            HashMap<String, Object> weatherData = new HashMap<>();
                                            weatherData.put(KEY_CURRENT_WEATHER, currentWeather);
                                            weatherData.put(KEY_WEATHER_FORECASTS, weatherForecasts);
                                            return weatherData;
                                        }
                                    }
                            );
                        }
                    });
            mCompositeSubscription.add(fetchDataObservable
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<HashMap<String, WeatherForecast>>() {
                        @Override
                        public void onNext(final HashMap<String, WeatherForecast> weatherData) {
                            final CurrentWeather currentWeather = (CurrentWeather) weatherData
                                    .get(KEY_CURRENT_WEATHER);
                            mLocationNameTextView.setText(currentWeather.getLocationName());
                            mCurrentTemperatureTextView.setText(
                                    TemperatureFormatter.format(currentWeather.getTemperature()));
                            final List<WeatherForecast> weatherForecasts = (List<WeatherForecast>)
                                    weatherData.get(KEY_WEATHER_FORECASTS);
                            final WeatherForecastListAdapter adapter = (WeatherForecastListAdapter)
                                    mForecastListView.getAdapter();
                            adapter.clear();
                            adapter.addAll(weatherForecasts);
                        }

                        @Override
                        public void onCompleted() {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onError(final Throwable error) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Crouton.makeText(getActivity(),
                                    R.string.error_fetch_weather, Style.ALERT).show();
                            error.printStackTrace();
                        }
                    })
            );
        }
    }
}