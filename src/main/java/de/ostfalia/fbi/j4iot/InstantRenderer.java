package de.ostfalia.fbi.j4iot;

import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;

/*
 * From https://stackoverflow.com/questions/59817621/vaadin-flow-renderer-for-java-time-date-time-types-beyond-just-localdatetime-l
 *
 * This class is based on source-code directly copied from
 * `LocalDateTimeRenderer.java` of Vaadin 14.1.x
 * as written and published by Vaadin Ltd. from their GitHub page.
 *
 * https://github.com/vaadin/flow/blob/master/flow-data/src/main/java/com/vaadin/flow/data/renderer/LocalDateTimeRenderer.java
 *
 * I re-purposed that class to handle `Instant` objects rather than `LocalDateTime`
 * objects. An `Instant` represents a moment, whereas `LocalDateTime` cannot because
 * of it lacking any concept of time zone or offset-from-UTC. In contrast, `Instant`
 * represents a moment in UTC (an offset-from-UTC of zero hours-minutes-seconds).
 *
 * By default, a `Instant` object renders in Vaadin by way of its `toString` method
 * generating text in standard ISO 8601 format YYYY-MM-DDTHH:MM:SS.SSSSSSSSSZ.
 *
 * If you want other than ISO 8601 format in UTC, use this class. In this class, we
 * apply  a time zone (`ZoneId`) to the `Instant` to adjust from UTC.
 *
 * The `ZoneId` object comes from one of three places:
 *  - Passed implicitly by being set as a property on a `DateTimeFormatter`
 *    object passed as an argument. This is the best case.
 *  - Defaults to calling `ZoneId.systemDefault` if  not found
 *    on the `DateTimeFormatter` object  (where `getZone` returns null).
 *
 * I deleted the constructors taking a formatting pattern string. Parsing such a string
 * and instantiating a `DateTimeFormatter` and handling resulting error conditions
 * should *not* be the job of this class. I believe the Vaadin team made a poor choice
 * in having constructors taking a string formatting pattern rather than just a
 * `DateTimeFormatter` object.
 *
 * Locale is another critical issue. A `Locale` object determines:
 *
 * (a) The human language used for translating items such as name of month and
 * name of day.
 *
 * (b) The cultural norms used in deciding localization issues such as the ordering
 * of elements (ex: day comes before or after month), abbreviation, capitalization,
 * punctuation, and so on.
 *
 * Again, I deleted the constructors taking a `Locale` object. The `DateTimeFormatter`
 * object passed by the calling programmer carries a `Locale`. That calling programmer
 * should have attached their intended locale object to that `DateTimeFormatter` object
 * by calling `DateTimeFormatter::withLocale`. Usually a `DateTimeFormatter` has a default
 * `Locale` assigned. But if found lacking, here we attach the JVM’s current default locale.
 *
 * Following the logic discussed above, I chose to not take a `ZoneId` as an argument.
 * A `ZoneId` can be attached to the `DateTimeFormatter` by calling `withZoneId`.
 * If the passed `DateTimeFormatter` is found lacking, here we attach the JVM’s current
 * default time zone.
 *
 * Typical usage, passing 2 arguments, a method reference and a `DateTimeFormatter` object
 * while omitting 3rd optional argument for null-representation to go with an blank empty string:
 *
 *     myGrid
 *          .addColumn(
 *                  new InstantRenderer <>( TheBusinessObject :: getWhenCreated ,
 *                          DateTimeFormatter
 *                                  .ofLocalizedDateTime( FormatStyle.SHORT , FormatStyle.MEDIUM )
 *                                  .withLocale( Locale.CANADA_FRENCH )
 *                                  .withZone( ZoneId.of( "America/Montreal" ) )
 *                  )
 *         )
 *
 * This code is written for Java 8 or later.
 *
 *  For criticisms and suggestions, contact me via LinkedIn at:  basilbourque
 */

/**
 * A template renderer for presenting {@code Instant} objects.
 *
 * @param <SOURCE> the type of the input item, from which the {@link Instant}
 *                 is extracted
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class InstantRenderer < SOURCE >
        extends BasicRenderer< SOURCE, Instant >
{
    private DateTimeFormatter formatter;
    private String nullRepresentation;

    /**
     * Creates a new InstantRenderer.
     * <p>
     * The renderer is configured to render with the format style
     * {@code FormatStyle.LONG} for the date and {@code FormatStyle.SHORT} for
     * time, with an empty string as its null representation.
     *
     * @param valueProvider the callback to provide a {@link Instant} to the
     *                      renderer, not <code>null</code>
     * @see <a href=
     * "https://docs.oracle.com/javase/8/docs/api/java/time/format/FormatStyle.html#LONG">
     * FormatStyle.LONG</a>
     * @see <a href=
     * "https://docs.oracle.com/javase/8/docs/api/java/time/format/FormatStyle.html#SHORT">
     * FormatStyle.SHORT</a>
     */
    public InstantRenderer (
            ValueProvider < SOURCE, Instant > valueProvider )
    {
        this(
                valueProvider ,
                DateTimeFormatter
                        .ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withZone( ZoneId.systemDefault() )
                        .withLocale( Locale.getDefault() ) ,
                ""
        );
    }

    /**
     * Creates a new InstantRenderer.
     * <p>
     * The renderer is configured to render with the given formatter, with the
     * empty string as its null representation.
     *
     * @param valueProvider the callback to provide a {@link Instant} to the
     *                      renderer, not <code>null</code>
     * @param formatter     the formatter to use, not <code>null</code>
     */
    public InstantRenderer (
            ValueProvider< SOURCE, Instant > valueProvider ,
            DateTimeFormatter formatter
    )
    {
        this(
                valueProvider ,
                formatter ,
                ""
        );
    }

    /**
     * Creates a new InstantRenderer.
     * <p>
     * The renderer is configured to render with the given formatter.
     *
     * @param valueProvider      the callback to provide a {@link Instant} to the
     *                           renderer, not <code>null</code>
     * @param formatter          the formatter to use, not <code>null</code>
     * @param nullRepresentation the textual representation of the <code>null</code> value
     */
    public InstantRenderer (
            final ValueProvider < SOURCE, Instant> valueProvider ,
            final DateTimeFormatter formatter ,
            final String nullRepresentation
    )
    {
        super( valueProvider );

        this.formatter = Objects.requireNonNull( formatter , "formatter may not be null" );
        this.nullRepresentation = Objects.requireNonNull( nullRepresentation , "null-representation may not be null" );

        // If the formatter provided by the calling programmer lacks a time zone, apply the JVM's current default zone.
        // This condition is less than ideal. The calling programmer should have set an appropriate zone.
        // Often the appropriate zone is one specifically chosen or confirmed by the user.
        if ( Objects.isNull( this.formatter.getZone() ) )
        {
            this.formatter = this.formatter.withZone( ZoneId.systemDefault() );
        }

        // If the formatter provided by the calling programmer lacks a locale, apply the JVM's current default locale.
        // This condition is less than ideal. The calling programmer should have set an appropriate locale.
        // Often the appropriate locale is one specifically chosen or confirmed by the user.
        if ( Objects.isNull( this.formatter.getLocale() ) )
        {
            this.formatter = this.formatter.withLocale( Locale.getDefault() );
        }
    }


    @Override
    protected String getFormattedValue ( final Instant instant )
    {
        // If null, return the null representation.
        // If not null, adjust the `Instant` from UTC into the time zone attached to the `DateTimeFormatter` object.
        // This adjustment, made by calling `Instant::atZone`, produces a `ZonedDateTime` object.
        // We then create a `String` with text representing the value of that `ZonedDateTime` object.
        // That text is automatically localized per the `Locale` attached to the `DateTimeFormatter` object.
        String s = Objects.isNull( instant ) ? nullRepresentation : formatter.format( instant.atZone( this.formatter.getZone() ) );
        return s;
    }
}