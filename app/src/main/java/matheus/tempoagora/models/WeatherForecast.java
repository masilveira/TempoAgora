package matheus.tempoagora.models;

public class WeatherForecast {
    private final String mLocationName;
    private final long mTimestamp;
    private final String mDescription;
    private final String mIcon;
    private final float mMinimumTemperature;
    private final float mMaximumTemperature;

    public WeatherForecast(final String locationName,
                           final long timestamp,
                           final String iconUrl,
                           final String desccription,
                           final float minimumTemperature,
                           final float maximumTemperature) {

        mLocationName = locationName;
        mTimestamp = timestamp;
        mDescription = desccription;
        mMinimumTemperature = minimumTemperature;
        mMaximumTemperature = maximumTemperature;
        mIcon = iconUrl;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getmIcon() {
        return mIcon;
    }

    public float getMinimumTemperature() {
        return mMinimumTemperature;
    }

    public float getMaximumTemperature() {
        return mMaximumTemperature;
    }

    public String getDescription() {
        return mDescription;
    }



}