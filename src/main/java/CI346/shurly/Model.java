package CI346.shurly;
/**
 * The model for our Data Access Object. Using an interface satisfies SOLID
 * principles, making it easy to change to a different flavour database
 * or to store the data in something that isn't a relational database at all.
 */
public interface Model {
    void putURL(String enc, String url);
    ShurlyURL getURL(String enc);
}
