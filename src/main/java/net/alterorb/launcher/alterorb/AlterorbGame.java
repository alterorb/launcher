package net.alterorb.launcher.alterorb;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = {"name", "internalName"})
public class AlterorbGame {

    private String name;
    private String internalName;
    private String gamepackHash;
}
