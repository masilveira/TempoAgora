package matheus.tempoagora.views.formatters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UnityFormatter {

    public static String toTemperature(float temperature) {
        return String.valueOf(Math.round(temperature)) + "°";
    }
    public static String toHumidity(String humidity) {
        return humidity +" %";
    }
    public static String toPressure (String pressure) {
        return pressure + " hPa";
    }

    public static String toDayOfWeek(final long milliseconds) {
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date(milliseconds)).toUpperCase();
    }

}
