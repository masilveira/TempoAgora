package matheus.tempoagora.controllers.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import matheus.tempoagora.R;
import matheus.tempoagora.controllers.services.ConnectionStatusService;
import matheus.tempoagora.controllers.services.LocationService;
import matheus.tempoagora.controllers.services.WeatherService;
import matheus.tempoagora.models.CurrentWeather;
import matheus.tempoagora.models.WeatherForecast;
import matheus.tempoagora.views.adapters.WeatherForecastListAdapter;
import matheus.tempoagora.views.formatters.UnityFormatter;
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

    private CompositeSubscription mCompositeSubscription;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mLocationNameTextView;
    private TextView mPressureTextView;
    private TextView mWeatherIconTextView;
    private TextView mDetailsTextView;
    private TextView mHumidityTextView;
    private TextView mCurrentTemperatureTextView;
    private ListView mForecastListView;
    private TextView mSunriseTextView;
    private TextView mSunsetTextView;
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
        Typeface weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf");

        mCompositeSubscription = new CompositeSubscription();
        final View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        mLocationNameTextView = (TextView) rootView.findViewById(R.id.location_name);
        mCurrentTemperatureTextView = (TextView) rootView.findViewById(R.id.current_temperature);
        mPressureTextView = (TextView) rootView.findViewById(R.id.pressure);
        mWeatherIconTextView = (TextView) rootView.findViewById(R.id.weather_icon);
        mWeatherIconTextView.setTypeface(weatherFont);
        mDetailsTextView = (TextView) rootView.findViewById(R.id.details);
        mHumidityTextView = (TextView) rootView.findViewById(R.id.humidity);
        mForecastListView = (ListView) rootView.findViewById(R.id.weather_forecast_list);
        mSunriseTextView = (TextView) rootView.findViewById(R.id.sunrise);
        mSunsetTextView = (TextView) rootView.findViewById(R.id.sunset);
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
        if (ConnectionStatusService.IsOnline(getContext())) {
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
                        ).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<HashMap<String, WeatherForecast>>() {
                                    @Override
                                    public void onNext(final HashMap<String, WeatherForecast> weatherData) {
                                        final CurrentWeather currentWeather = (CurrentWeather) weatherData.get(KEY_CURRENT_WEATHER);
                                        mLocationNameTextView.setText(currentWeather.getLocationName());
                                        mCurrentTemperatureTextView.setText(UnityFormatter.toTemperature(currentWeather.getTemperature()));
                                        mDetailsTextView.setText(currentWeather.getDescription().toUpperCase(Locale.getDefault()));
                                        mHumidityTextView.setText(UnityFormatter.toHumidity(currentWeather.getmHumidity()));
                                        mPressureTextView.setText(UnityFormatter.toPressure(currentWeather.getmPressure()));
                                        mWeatherIconTextView.setText(Html.fromHtml(currentWeather.getmIcon()));
                                        mSunriseTextView.setVisibility(View.GONE);
                                        mSunsetTextView.setVisibility(View.GONE);

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
                                                R.string.error_no_connection, Style.ALERT).show();
                                        error.printStackTrace();
                                    }
                                })
                );
            } else {
                final LocationManager locationManager = (LocationManager) getActivity()
                        .getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
                                    final CurrentWeather currentWeather = (CurrentWeather) weatherData.get(KEY_CURRENT_WEATHER);
                                    mLocationNameTextView.setText(currentWeather.getLocationName());
                                    mCurrentTemperatureTextView.setText(UnityFormatter.toTemperature(currentWeather.getTemperature()));
                                    mDetailsTextView.setText(currentWeather.getDescription().toUpperCase(Locale.getDefault()));
                                    mHumidityTextView.setText(UnityFormatter.toHumidity(currentWeather.getmHumidity()));
                                    mPressureTextView.setText(UnityFormatter.toPressure(currentWeather.getmPressure()));
                                    mWeatherIconTextView.setText(Html.fromHtml(currentWeather.getmIcon()));
                                    mSunriseTextView.setText(android.text.format.DateFormat.getTimeFormat(getContext()).format(new Date(currentWeather.getmSunrise() * 1000)));
                                    mSunsetTextView.setText(android.text.format.DateFormat.getTimeFormat(getContext()).format(new Date(currentWeather.getmSunset() * 1000)));

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
                                    int message;
                                    if (error instanceof SecurityException) {
                                        message = R.string.permission_denied;
                                    } else {
                                        message = R.string.error_no_connection;
                                    }
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    Crouton.makeText(getActivity(), message, Style.ALERT).show();
                                    error.printStackTrace();
                                }
                            })
                    );

                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Crouton.makeText(getActivity(), R.string.no_gps, Style.ALERT).show();
                    displayPromptForEnablingGPS();

                }
            }
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Crouton.makeText(getActivity(), R.string.error_no_connection, Style.ALERT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LocationService.PERMISSION_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateWeather();
                } else {
                    Crouton.makeText(getActivity(), R.string.permission_denied, Style.ALERT).show();
                }
            }
        }
    }

    public void displayPromptForEnablingGPS()
    {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity(),R.style.AppCompatAlertDialogStyle);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        builder
                .setTitle(R.string.no_gps_title)
                .setMessage(R.string.ask_open_gps)
                .setPositiveButton(R.string.goto_gps_settings,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel).toUpperCase(),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeather();
    }
}