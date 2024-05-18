package earth.adi.typeid;

/**
 * JavaType wrapper for a generic Class.
 */
public class JavaType {
  /**
   * Create a new JavaType instance.
   *
   * @param clazz the clazz
   * @return the JavaType
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
   *
   * @return the clazz
   */
  public Class<?> getClazz() {
    return clazz;
  }
}
