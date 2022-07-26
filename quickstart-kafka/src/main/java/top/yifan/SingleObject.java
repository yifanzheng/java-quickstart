package top.yifan;

/**
 * SingleObject
 *
 * @param <T>
 */
public class SingleObject<T> {
    
    private T value;
    
    public SingleObject() {}
    
    public SingleObject(T value) {
        this.value = value;
    }

    public static <T> SingleObject<T> build(T value) {
        return new SingleObject<>(value);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
    
}
