package matheus.tempoagora.models;

public class CurrentWeather extends WeatherForecast {
    private final float mTemperature;
    private final String mHumidity;
    private final long mSunrise;
    private final long mSunset;
    private final String mPressure;

    public CurrentWeather(final String locationName,
                          final long timestamp,
                          final String iconUrl,
                          final String description,
                          final float temperature,
                          final float minimumTemperature,
                          final float maximumTemperature,
                          final String humidity,
                          final long sunrise,
                          final long sunset,
                          final String pressure) {

        super(locationName, timestamp, iconUrl, description, minimumTemperature, maximumTemperature);
        mTemperature = temperature;
        mHumidity = humidity;
        mSunrise = sunrise;
        mSunset = sunset;
        mPressure = pressure;

    }

    public float getTemperature() {
        return mTemperature;
    }

    public String getmHumidity() {
        return mHumidity;
    }

    public long getmSunrise() {
        return mSunrise;
    }

    public long getmSunset() {
        return mSunset;
    }

    public String getmPressure() {return mPressure; }
}
