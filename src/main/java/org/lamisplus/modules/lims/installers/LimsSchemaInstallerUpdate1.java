package org.lamisplus.modules.lims.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(2)
@Installer(name = "lims-schema-installer-update-1",
        description = "LIMS installer updates 1",
        version = 2)
public class LimsSchemaInstallerUpdate1 extends AcrossLiquibaseInstaller {
    public LimsSchemaInstallerUpdate1() {
        super("classpath:installers/lims/schema/schema-update-1.xml");
    }
}
