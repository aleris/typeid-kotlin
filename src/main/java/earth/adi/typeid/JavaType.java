package earth.adi.typeid;

/**
 * JavaType.
 */
public class JavaType {
  /**
   * Create a new JavaType instance.
   */
  public static JavaType of(Class<?> clazz) {
    return new JavaType(clazz);
  }

  private final Class<?> clazz;

  private JavaType(Class<?> clazz) {
    this.clazz = clazz;
  }

  /**
   * Get the clazz.
   */
  public Class<?> getClazz() {
    return clazz;
  }
}
