package wang.wangby.testcase.model;

import lombok.Data;
import wang.wangby.testcase.model.vo.Usage;

@Data
public class JvmInfo {
    private Usage old;
    private Usage eden;
    private Usage survivor;
    private Usage metaspace;
}
