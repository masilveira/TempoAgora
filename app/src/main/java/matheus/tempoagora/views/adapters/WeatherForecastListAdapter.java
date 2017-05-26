package matheus.tempoagora.views.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import matheus.tempoagora.R;
import matheus.tempoagora.views.formatters.UnityFormatter;
import matheus.tempoagora.models.WeatherForecast;

public class WeatherForecastListAdapter extends ArrayAdapter {
    private Typeface weatherFont;

    public WeatherForecastListAdapter(final List<WeatherForecast> weatherForecasts,
                                      final Context context) {
        super(context, 0, weatherForecasts);
        weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weathericons-regular-webfont.ttf");
    }

    @Override
    public boolean isEnabled(final int position) {
        return false;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.weather_forecast_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.day);
            viewHolder.iconView = (TextView) convertView
                    .findViewById(R.id.icon);
            viewHolder.iconView.setTypeface(weatherFont);
            viewHolder.descriptionTextView = (TextView) convertView
                    .findViewById(R.id.description);
            viewHolder.maximumTemperatureTextView = (TextView) convertView
                    .findViewById(R.id.maximum_temperature);
            viewHolder.minimumTemperatureTextView = (TextView) convertView
                    .findViewById(R.id.minimum_temperature);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final WeatherForecast weatherForecast = (WeatherForecast) getItem(position);

        if (weatherForecast != null) {
            viewHolder.iconView.setText(Html.fromHtml(weatherForecast.getmIcon()));
            viewHolder.dayTextView.setText(UnityFormatter.toDayOfWeek(weatherForecast.getTimestamp() * 1000));
            viewHolder.descriptionTextView.setText(weatherForecast.getDescription().toUpperCase());
            viewHolder.maximumTemperatureTextView.setText(
                    UnityFormatter.toTemperature(weatherForecast.getMaximumTemperature()));
            viewHolder.minimumTemperatureTextView.setText(
                    UnityFormatter.toTemperature(weatherForecast.getMinimumTemperature()));
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView dayTextView;
        private TextView iconView;
        private TextView descriptionTextView;
        private TextView maximumTemperatureTextView;
        private TextView minimumTemperatureTextView;
    }
}