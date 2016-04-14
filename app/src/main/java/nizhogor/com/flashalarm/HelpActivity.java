package nizhogor.com.flashalarm;

import android.app.Activity;
import android.os.Bundle;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        /*
        Resources resources = getResources();
        String str = resources.getString(R.string.settings_help);

        Spanned formattedStr = Html.fromHtml(str);
        str = formattedStr.toString();
        ((TextView) findViewById(R.id.settings_help)).setText(formattedStr);
*/
    }
}
