package matheus.tempoagora.models;

public class CurrentWeather extends WeatherForecast {
    private final float mTemperature;
    private final int mHumidity;
    private final long mSunrise;
    private final long mSunset;

    public CurrentWeather(final String locationName,
                          final long timestamp,
                          final String iconUrl,
                          final String description,
                          final float temperature,
                          final float minimumTemperature,
                          final float maximumTemperature,
                          final int humidity,
                          final long sunrise,
                          final long sunset) {

        super(locationName, timestamp, iconUrl, description, minimumTemperature, maximumTemperature);
        mTemperature = temperature;
        mHumidity = humidity;
        mSunrise = sunrise;
        mSunset = sunset;

    }

    public float getTemperature() {
        return mTemperature;
    }

    public int getmHumidity() {
        return mHumidity;
    }

    public long getmSunrise() {
        return mSunrise;
    }

    public long getmSunset() {
        return mSunset;
    }
}
