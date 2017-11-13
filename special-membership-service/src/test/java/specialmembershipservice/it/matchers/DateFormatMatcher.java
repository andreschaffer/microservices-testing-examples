package specialmembershipservice.it.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.google.common.base.Preconditions.checkNotNull;

public class DateFormatMatcher extends TypeSafeMatcher<String> {

    private final String pattern;
    private final DateFormat dateFormat;

    public DateFormatMatcher(String pattern) {
        this.pattern = checkNotNull(pattern);
        this.dateFormat = new SimpleDateFormat(pattern);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Matcher<String> isIsoDateFormat() {
        return new DateFormatMatcher("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    @Override
    protected boolean matchesSafely(String date) {
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("date format (").appendText(pattern).appendText(")");
    }
}
