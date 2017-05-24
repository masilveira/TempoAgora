package matheus.tempoagora;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import matheus.tempoagora.Helpers.DayFormatter;
import matheus.tempoagora.Helpers.TemperatureFormatter;
import matheus.tempoagora.Models.WeatherForecast;

public class WeatherForecastListAdapter extends ArrayAdapter {

    WeatherForecastListAdapter(final List<WeatherForecast> weatherForecasts,
                               final Context context) {
        super(context, 0, weatherForecasts);
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

        final DayFormatter dayFormatter = new DayFormatter(getContext());
        final String day = dayFormatter.format(weatherForecast != null ? weatherForecast.getTimestamp() : 0);
        viewHolder.dayTextView.setText(day);
        if (weatherForecast != null) {
            viewHolder.descriptionTextView.setText(weatherForecast.getDescription());
        }
        if (weatherForecast != null) {
            viewHolder.maximumTemperatureTextView.setText(
                    TemperatureFormatter.format(weatherForecast.getMaximumTemperature()));
        }
        if (weatherForecast != null) {
            viewHolder.minimumTemperatureTextView.setText(
                    TemperatureFormatter.format(weatherForecast.getMinimumTemperature()));
        }

        return convertView;
    }

    /**
     * Cache to avoid doing expensive findViewById() calls for each getView().
     */
    private class ViewHolder {
        private TextView dayTextView;
        private TextView descriptionTextView;
        private TextView maximumTemperatureTextView;
        private TextView minimumTemperatureTextView;
    }
}