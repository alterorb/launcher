package net.alterorb.launcher.alterorb;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(of = {"name", "internalName"})
public class AlterorbGame {

    private String name;
    private String internalName;
    private String gamepackHash;
    private List<String> jvmExtraParams;
}
