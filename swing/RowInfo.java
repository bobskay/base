package wang.wangby.swing;

import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class RowInfo<T> {
    private List<Function<T,String>> functions;
    private List<String> names;


}
