package org.poopeeland.tinytinyfeed.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import org.poopeeland.tinytinyfeed.R;
import org.poopeeland.tinytinyfeed.activities.ArticleReadActivity;
import org.poopeeland.tinytinyfeed.activities.SettingsActivity;
import org.poopeeland.tinytinyfeed.services.WidgetService;

import java.io.File;
import java.util.Locale;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SettingsActivity TinyTinyFeedWidgetConfigureActivity}
 */
public class TinyTinyFeedWidget extends AppWidgetProvider {

    public static final String URL_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_URL";
    public static final String USER_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_USER";
    public static final String PASSWORD_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_PASSWORD";
    public static final String SERVER_TIMEOUT = "org.poopeeland.tinytinyfeed.PREFERENCE_TIMEOUT";
    public static final String HTTP_USER_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_HTTP_USER";
    public static final String HTTP_PASSWORD_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_HTTP_PASSWORD";
    public static final String TEXT_COLOR_KEY = "org.poopeeland.tinytinyfeed.TEXT_COLOR_%d";
    public static final String TEXT_SIZE_KEY = "org.poopeeland.tinytinyfeed.TEXT_SIZE_%d";
    public static final String SOURCE_SIZE_KEY = "org.poopeeland.tinytinyfeed.SOURCE_SIZE_%d";
    public static final String SOURCE_COLOR_KEY = "org.poopeeland.tinytinyfeed.SOURCE_COLOR_%d";
    public static final String TITLE_SIZE_KEY = "org.poopeeland.tinytinyfeed.TITLE_SIZE_%d";
    public static final String TITLE_COLOR_KEY = "org.poopeeland.tinytinyfeed.TITLE_COLOR_%d";
    public static final String EXCERPT_LENGTH_KEY = "org.poopeeland.tinytinyfeed.EXCERPT_LENGTH_KEY_%d";
    public static final String NUM_ARTICLE_KEY = "org.poopeeland.tinytinyfeed.NUM_ARTICLE_KEY_%d";
    public static final String WIDGET_CATEGORIES_KEY = "org.poopeeland.tinytinyfeed.WIDGET_CATEGORIES_%d";
    public static final String FORCE_UPDATE_KEY = "org.poopeeland.tinytinyfeed.FORCE_UPDATE_%d";
    public static final String WIDGET_NAME_KEY = "org.poopeeland.tinytinyfeed.WIDGET_NAME_%d";
    public static final String STATUS_COLOR_KEY = "org.poopeeland.tinytinyfeed.STATUS_COLOR_%d";
    public static final String BG_COLOR_KEY = "org.poopeeland.tinytinyfeed.BACKGROUND_COLOR_%d";
    public static final String ONLY_UNREAD_KEY = "org.poopeeland.tinytinyfeed.ONLY_UNREAD_%d";
    public static final String ALL_SLL_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_SSL_SELF";
    public static final String ALL_HOST_KEY = "org.poopeeland.tinytinyfeed.PREFERENCE_SSL_HOSTNAME";
    public static final String CHECKED = "org.poopeeland.tinytinyfeed.CHECKED";
    public static final String JSON_STORAGE_FILENAME_TEMPLATE = "listArticles_%d.json";
    public static final int DEFAULT_TEXT_COLOR = 0xffffffff;
    public static final int DEFAULT_BG_COLOR = 0x80000000;
    public static final String DEFAULT_TEXT_SIZE = "10";
    public static final String DEFAULT_NUM_ARTICLE = "20";
    public static final String DEFAULT_EXCERPT_SIZE = "200";

    private static final String TAG = "TinyTinyFeedWidget";

    /**
     * Return a Pending Intent asking the refresh of the widget
     *
     * @param context the context
     * @param ids     the ids of the widget to refresh
     * @return a pending intent
     */
    private static PendingIntent actionPendingIntent(final Context context, final int[] ids) {
        Log.d(TAG, "Create pending intent");
        Intent intent = new Intent(context, TinyTinyFeedWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.d(TAG, "Widget update");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(CHECKED, false)) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewWidget);
            PendingIntent refreshIntent = actionPendingIntent(context, appWidgetIds);
            for (final int i : appWidgetIds) {
                final int textColor = preferences.getInt(String.format(Locale.getDefault(), STATUS_COLOR_KEY, i), DEFAULT_TEXT_COLOR);
                final int bgColor = preferences.getInt(String.format(Locale.getDefault(), BG_COLOR_KEY, i), DEFAULT_BG_COLOR);

                Intent intent = new Intent(context, WidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.tiny_tiny_feed_widget);
                rv.setRemoteAdapter(R.id.listViewWidget, intent);
                rv.setEmptyView(R.id.listViewWidget, R.id.widgetEmptyList);
                rv.setOnClickPendingIntent(R.id.widgetEmptyList, refreshIntent);
                rv.setOnClickPendingIntent(R.id.refreshButton, refreshIntent);
                rv.setInt(R.id.refreshButton, "setColorFilter", textColor);
                rv.setInt(R.id.lastUpdateText, "setTextColor", textColor);
                rv.setInt(R.id.widgetEmptyList, "setTextColor", textColor);
                rv.setInt(R.id.widgetLayoutId, "setBackgroundColor", bgColor);
                Intent startActivityIntent = new Intent(context, ArticleReadActivity.class);
                PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setPendingIntentTemplate(R.id.listViewWidget, startActivityPendingIntent);

                appWidgetManager.updateAppWidget(i, rv);
            }

        } else {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.configure_widget_layout);
            Intent intent = new Intent(context, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, SettingsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.no_settings_layout, pendingIntent);
            rv.setInt(R.id.no_settings_layout, "setTextColor", 0xffffff);
            appWidgetManager.updateAppWidget(appWidgetIds, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (int widgetId : appWidgetIds) {
            Log.i(TAG, "Deleting widget " + widgetId);
            editor.remove(String.format(Locale.getDefault(), TEXT_COLOR_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), TEXT_SIZE_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), SOURCE_SIZE_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), SOURCE_COLOR_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), TITLE_SIZE_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), TITLE_COLOR_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), EXCERPT_LENGTH_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), NUM_ARTICLE_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), WIDGET_CATEGORIES_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), FORCE_UPDATE_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), STATUS_COLOR_KEY, widgetId));
            editor.remove(String.format(Locale.getDefault(), BG_COLOR_KEY, widgetId));

            editor.apply();
            File f = new File(context.getApplicationContext().getFilesDir()
                    , String.format(Locale.getDefault(), JSON_STORAGE_FILENAME_TEMPLATE, widgetId));
            if (!f.delete()) {
                Log.e(TAG, f.getAbsolutePath() + " has not been deleted!");
            } else {
                Log.d(TAG, f.getAbsolutePath() + " has been deleted");
            }
        }
    }
}


