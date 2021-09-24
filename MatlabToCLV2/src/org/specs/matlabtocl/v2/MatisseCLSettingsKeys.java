package org.specs.matlabtocl.v2;

import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;

public interface MatisseCLSettingsKeys {
    public DataKey<Integer[]> GROUP_SIZES = KeyFactory.object("kernel_group_sizes", Integer[].class);
}
