package nl.drahmann.about;

        import android.app.Activity;
        import android.app.AlertDialog;

        import android.content.pm.ApplicationInfo;
        import android.content.pm.PackageManager;
        import android.content.pm.PackageManager.NameNotFoundException;

        import android.graphics.drawable.Drawable;

        import android.text.Html;

        import android.text.method.LinkMovementMethod;

        import android.view.InflateException;
        import android.view.LayoutInflater;
        import android.view.View;

        import android.widget.TextView;

public class About
{
    public static void show(Activity activity, String aboutText,
                            String okButtonText)
    {
        String versionNumber = "unknown";
        Drawable icon = null;
        String appName = "unknown";
        try
        {
            PackageManager pm = activity.getPackageManager();
            versionNumber = pm.getPackageInfo(activity.getPackageName(), 0)
                    .versionName;
            icon = pm.getApplicationIcon(activity.getPackageName());
            ApplicationInfo ai = pm.getApplicationInfo(activity.getPackageName(),
                    0);
            appName = (String) pm.getApplicationLabel(ai);
            if (appName == null)
                appName = "unknown";
        }
        catch (NameNotFoundException e)
        {
        }
        View about;
        TextView tvAbout;
        try
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            about = inflater.inflate(R.layout.nl_drahmann_about, null);
            tvAbout = (TextView) about.findViewById(R.id.nl_drahmann_aboutText);
        }
        catch(InflateException e)
        {
            about = tvAbout = new TextView(activity);
        }
        tvAbout.setText(Html.fromHtml(aboutText));
        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(activity)
                .setTitle(appName+" "+versionNumber)
                .setIcon(icon)
                .setPositiveButton(okButtonText, null)
                .setView(about)
                .show();
    }
}
