package wang.wangby.swing;

import lombok.Data;

@Data
public class Column<T> {
    private Object target;

    public String toString(){
        return target.toString();
    }
}
