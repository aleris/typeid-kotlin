package earth.adi.typeid;

/**
 * JavaType.
 */
public class JavaType {
  public static JavaType of(Class<?> clazz) {
    return new JavaType(clazz);
  }

  private final Class<?> clazz;

  private JavaType(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Class<?> getClazz() {
    return clazz;
  }
}
