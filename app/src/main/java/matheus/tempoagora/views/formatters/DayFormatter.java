package matheus.tempoagora.views.formatters;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import matheus.tempoagora.R;

public class DayFormatter {
    private final static long MILLISECONDS_IN_SECONDS = 1000;
    private final Context mContext;

    public DayFormatter(Context context) {
        mContext = context;
    }


    public String format(final long unixTimestamp) {
        final long milliseconds = unixTimestamp * MILLISECONDS_IN_SECONDS;
        String day;

//        if (isToday(milliseconds)) {
//            day = mContext.getResources().getString(R.string.today);
//        } else if (isTomorrow(milliseconds)) {
//            day = mContext.getResources().getString(R.string.tomorrow);
//        } else {
            day = getDayOfWeek(milliseconds);
      //  }

        return day;
    }

    private String getDayOfWeek(final long milliseconds) {
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date(milliseconds));
    }

    private boolean isToday(final long milliseconds) {
        final SimpleDateFormat dayInYearFormat = new SimpleDateFormat("yyyyD", Locale.getDefault());
        final String nowHash = dayInYearFormat.format(new Date());
        final String comparisonHash = dayInYearFormat.format(new Date(milliseconds));
        return nowHash.equals(comparisonHash);
    }

    private boolean isTomorrow(final long milliseconds) {
        final SimpleDateFormat dayInYearFormat = new SimpleDateFormat("yyyyD", Locale.getDefault());
        final int tomorrowHash = Integer.parseInt(dayInYearFormat.format(new Date())) + 1;
        final int comparisonHash = Integer.parseInt(dayInYearFormat.format(new Date(milliseconds)));
        return comparisonHash == tomorrowHash;
    }
}
